package rcms.utilities.daqexpert.jobs;

import com.google.common.collect.EvictingQueue;
import org.apache.log4j.Logger;
import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.processing.context.ContextEntry;
import rcms.utilities.daqexpert.processing.context.ObjectContextEntry;
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
    private final Queue<Condition> recentIssuedRecoveryConditions = EvictingQueue.create(5);


    /**
     * Ignore all recoveries that are older than the threshold
     */
    private long threshold = 20000;

    private DominatingSelector dominatingSelector;


    public RecoveryJobManager(ExpertControllerClient expertControllerClient) {
        this.expertControllerClient = expertControllerClient;
        this.dominatingSelector = new DominatingSelector();
        ;
    }


    /**
     * Run recovery job TODO: accept single request. reuse dominating mechanism
     */
    public Long runRecoveryJob(RecoveryRequest dominatingRequest) {

        logger.info(dominatingRequest.getProblemTitle() + " recovery job submitted. Now checking the age.");

        Date now = new Date();

        if (!(dominatingRequest.getCondition().getStart().getTime() > now.getTime() - threshold)) {
            logger.info("No recent recovery found. The problem is " + (now.getTime() - threshold) + "ms old. The threshold is " + threshold + "ms");
            return null;
        }

        if (dominatingRequest.getRecoveryRequestSteps().size() == 0) {
            logger.info("No executable recovery steps. The recovery request cannot be automated.");
            return null;
        }


        logger.info("Dominating recovery job selected: " + dominatingRequest.getProblemDescription());

        recentIssuedRecoveryConditions.add(dominatingRequest.getCondition());
        RecoveryResponse recoveryResponse = expertControllerClient.sendRecoveryRequest(dominatingRequest);

        String status = recoveryResponse.getAcceptanceDecision();
        if ("rejected".equalsIgnoreCase(status)) {
            // check if this is the same now

            Long rejectionConditionReasonId = recoveryResponse.getRejectedDueToConditionId();

            logger.info("Controller rejected the recovery request due to condition " + rejectionConditionReasonId);
            Optional<Condition> conditionOptional = recentIssuedRecoveryConditions.stream().filter(c -> c.getId().equals(rejectionConditionReasonId)).findFirst();


            if (conditionOptional.isPresent()) {

                Condition rejectionConditionReason = conditionOptional.get();
                String decision = handleRejection(dominatingRequest.getCondition(), rejectionConditionReason);

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
                        return dominatingRequest.getProblemId();

                }

            } else {
                logger.warn("Could not find condition by id " + rejectionConditionReasonId + " that was the reason to reject. Most likely the DAQExpert was redeployed.");
                dominatingRequest.setWithInterrupt(true);
            }

            RecoveryResponse secondRecoveryRespons = expertControllerClient.sendRecoveryRequest(dominatingRequest);

            if (!"accepted".equalsIgnoreCase(secondRecoveryRespons.getAcceptanceDecision())) {
                logger.warn("Second request resulted with rejection." + dominatingRequest);
            } else {
                logger.info("Recovery accepted by controller with second request");
            }


        } else {
            logger.info("Recovery accepted by controller with first request");
        }
        return dominatingRequest.getProblemId();


    }


    private boolean isSameCondition(Condition currentlyDominating, Condition currentlyRejecting) {

        if (currentlyDominating.getLogicModule() != currentlyRejecting.getLogicModule()) {
            return false;
        }

        if (currentlyDominating.getTitle() != currentlyRejecting.getTitle()) {
            return false;
        }

        if (currentlyDominating.getContext() != null) {

            if (currentlyRejecting.getContext() == null) {
                return false;
            } else {
                // compare only objects in context

                for (Map.Entry<String, ContextEntry> e : currentlyDominating.getContext().entrySet()) {

                    if (e.getValue() instanceof ObjectContextEntry) {

                        if (!currentlyRejecting.getContext().containsKey(e.getKey())) {
                            return false;
                        } else {
                            if (!currentlyRejecting.getContext().get(e.getKey()).equals(e.getValue())) {
                                return false;
                            }
                        }

                    } else {
                        // ignore statistic entries - because they change with each snapshots
                        // and note entries
                    }

                }

            }

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
