package rcms.utilities.daqexpert.jobs;

import com.google.common.collect.EvictingQueue;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.log4j.Logger;
import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.processing.context.Context;
import rcms.utilities.daqexpert.processing.context.ContextEntry;
import rcms.utilities.daqexpert.processing.context.ObjectContextEntry;
import rcms.utilities.daqexpert.reasoning.base.ContextLogicModule;
import rcms.utilities.daqexpert.reasoning.causality.DominatingSelector;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages the recovery job distribution to the controller given output from Logic Modules (set of conditions with
 * recovery suggestions)
 */
public class RecoveryJobManager {

    private static final Logger logger = Logger.getLogger(RecoveryJobManager.class);

    /**
     * Controller client
     */
    private ExpertControllerClient expertControllerClient;


    /**
     * FIFO to keep the most recent conditions - kept to avoid accessing the database when controller rejects the
     * recovery request. With the rejection the id of current condition being the reason to do recovery is returned.
     * Expert will see whether this condition should be preempted, continued or postponed depending on the.
     * <p>
     * Note that to this queue we add only those conditions that generated recovery requests that were passed to
     * controller.
     */
    private final Queue<Condition> recentIssuedRecoveryConditions = EvictingQueue.create(15);


    /**
     * Ignore all recoveries that are older than the threshold
     */
    private long threshold = 20000;

    protected DominatingSelector dominatingSelector;


    public RecoveryJobManager(ExpertControllerClient expertControllerClient, DominatingSelector dominatingSelector) {
        this.expertControllerClient = expertControllerClient;
        this.dominatingSelector = dominatingSelector;
        ;
    }

    /**
     * Run recovery job TODO: accept single request. reuse dominating mechanism
     *
     *
     * Too many things happening
     * - handling rejection decision
     * - preventing from sending requests for old conditions && no step recoveries
     * - saving recent conditions to the tmp queue
     * - sending recovery requests
     * - modification of request flags for 2nd request
     *
     */
    public Triple<String, String, String> runRecoveryJob(RecoveryRequest dominatingRequest) {

        logger.info(dominatingRequest.getProblemTitle() + " recovery job submitted. Now checking the age.");

        Date now = new Date();

        if (!(dominatingRequest.getCondition().getStart().getTime() > now.getTime() - threshold)) {
            logger.info("No recent recovery found. The problem is " + (now.getTime() - threshold) + "ms old. The threshold is " + threshold + "ms");
            return Triple.of("abandoned, too old",null,null);
        }

        if (dominatingRequest.getRecoveryRequestSteps().size() == 0) {
            logger.info("No executable recovery steps. The recovery request cannot be automated.");
            return Triple.of("abandoned, no steps",null,null);
        }


        logger.info("Dominating recovery job selected: " + dominatingRequest.getProblemDescription());

        recentIssuedRecoveryConditions.add(dominatingRequest.getCondition());
        RecoveryResponse recoveryResponse = expertControllerClient.sendRecoveryRequest(dominatingRequest);

        String status = recoveryResponse.getAcceptanceDecision();

        if("rejectedDueToManualRecovery".equalsIgnoreCase(status)){
            logger.info("Recovery has been rejected due to manual recovery. Abandoning this recovery.");
            return Triple.of(status, null, null);
        }
        else if ("rejected".equalsIgnoreCase(status)) {
            // check if this is the same now

            Long rejectionConditionReasonId = recoveryResponse.getRejectedDueToConditionId();

            logger.info("Controller rejected the recovery request due to condition " + rejectionConditionReasonId);
            Optional<Condition> conditionOptional = recentIssuedRecoveryConditions.stream().filter(c -> c.getId().equals(rejectionConditionReasonId)).findFirst();


            String decision;
            if (conditionOptional.isPresent()) {

                Condition rejectionConditionReason = conditionOptional.get();
                decision = handleRejection(dominatingRequest.getCondition(), rejectionConditionReason);

                logger.info("Handling rejection with decision to: " + decision);

                switch (decision) {
                    case "continue":
                        dominatingRequest.setSameProblem(true);
                        break;
                    case "interrupt":
                        dominatingRequest.setWithInterrupt(true);
                        break;
                    case "postpone":
                        dominatingRequest.setWithPostponement(true);
                        break;
                    case "ignore":
                    default:
                        logger.info("Will not try second request");
                        return Triple.of(status, decision, null);

                }

            } else {
                logger.warn("Could not find condition by id " + rejectionConditionReasonId + " that was the reason to reject. Most likely the DAQExpert was redeployed.");
                dominatingRequest.setWithInterrupt(true);
                decision = "interrupt due to unknown condition";
            }

            RecoveryResponse secondRecoveryRespons = expertControllerClient.sendRecoveryRequest(dominatingRequest);

            if (!"accepted".equalsIgnoreCase(secondRecoveryRespons.getAcceptanceDecision())) {
                logger.warn("Second request resulted with rejection." + dominatingRequest);
            } else {
                logger.info("Recovery accepted by controller with second request");
            }
            return Triple.of(status,decision, secondRecoveryRespons.getAcceptanceDecision());


        } else {
            logger.info("Recovery accepted by controller with first request");
            return Triple.of(status, null, null);
        }


    }


    protected boolean isSameCondition(Condition currentlyDominating, Condition currentlyRejecting) {

        logger.info("Checking if the same recovery: ");
        logger.info(" - currently dominating: " + currentlyDominating.getDescription());
        logger.info(" - currently rejecting : " + currentlyRejecting.getDescription());

        if (currentlyDominating.getLogicModule() != currentlyRejecting.getLogicModule()) {
            return false;
        }

        if (currentlyDominating.getTitle() != currentlyRejecting.getTitle()) {
            return false;
        }


        Map<String, ContextEntry> currentlyDominatingContextEntryMap = null;
        if (currentlyDominating.getContext() == null) {

            if (currentlyDominating.getProducer() != null && currentlyDominating.getProducer() instanceof ContextLogicModule) {

                ContextLogicModule contextLogicModule = ((ContextLogicModule) currentlyDominating.getProducer());
                if (contextLogicModule.getContextHandler() != null && contextLogicModule.getContextHandler().getContext() != null)
                    currentlyDominatingContextEntryMap =
                            contextLogicModule.getContextHandler().getContext().getContextEntryMap();

            }
        } else {
            currentlyDominatingContextEntryMap = currentlyDominating.getContext();
        }

        /* both have some context */
        if (currentlyDominatingContextEntryMap != null && currentlyRejecting.getContext() != null) {

                // compare only objects in context
                logger.info("Comparing objects in the context");

                for (Map.Entry<String, ContextEntry> e : currentlyDominatingContextEntryMap.entrySet()) {

                    if (e.getValue() instanceof ObjectContextEntry) {

                        if (!currentlyRejecting.getContext().containsKey(e.getKey())) {
                            return false;
                        } else {
                            if (!currentlyRejecting.getContext().get(e.getKey()).getTextRepresentation().equals(e.getValue().getTextRepresentation())) {
                                logger.info(String.format(
                                        "Context value for key %s is different: %s vs %s, (dominating vs rejecting)",
                                        e.getKey(),
                                        e.getValue().getTextRepresentation(),
                                        currentlyRejecting.getContext().get(e.getKey()).getTextRepresentation()));
                                return false;
                            }
                        }

                    } else {
                        // ignore statistic entries - because they change with each snapshots
                        // and note entries
                    }

                }



        }

        /* If both have context null */
        else if(currentlyDominatingContextEntryMap == null && currentlyRejecting.getContext() == null){
            logger.info("Both have context null");
            return true;
        }

        /* one have context other doesn't*/
        else {
            logger.info("One have context, other doesn't: ");
            logger.info("Dominating: " + currentlyDominatingContextEntryMap);
            logger.info("Rejecting : " + currentlyRejecting.getContext());
            return false;
        }

        return true;

    }

    /**
     * Handle rejection from controller. 3 possible ways: - continue, if the conditions are the same meaning that the
     * problem is not yet resolved. Maybe other steps are necessary - ignore, if the condition that generate currently
     * executing recovery is more important than ones that appeared afterwards - interrupt, if more important condition
     * appeared after currently executing recovery
     *
     * @param currentlyDominating condition that has appeared recently and dominates all others
     * @param currentlyRejecting  condition that is now being recovered
     * @return
     */
    private String handleRejection(Condition currentlyDominating, Condition currentlyRejecting) {

        boolean isSame = isSameCondition(currentlyDominating, currentlyRejecting);

        if (isSame) {
            return "continue"; //
        } else {

            // use dominating mechanism
            List<Condition> twoConditions = new ArrayList<>();
            twoConditions.add(currentlyDominating);
            twoConditions.add(currentlyRejecting);

            logger.info("Comparing: " + twoConditions.stream().map(f -> f.getId() + "(" + f.getTitle() + ")").collect(Collectors.toList()));

            Condition top = dominatingSelector.selectDominating(twoConditions, true);

            logger.info("Dominating selector chooses: " + top.getId() + "(" + top.getTitle() + ") as dominating");

            if (top.getId() == currentlyRejecting.getId()) {
                //TODO: two cases here: "postpone" and "ignore"
                return "postpone"; // currently rejecting is the most important, postpone currently dominating condition and continue with recovery
            } else {
                return "interrupt"; // currently rejecting is less important, interrupt it and start recovery of currently dominating
            }

        }

    }


    public void notifyConditionFinished(Long id) {
        expertControllerClient.sendConditionFinishedSignal(id);
    }
}
