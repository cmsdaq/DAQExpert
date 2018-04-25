package rcms.utilities.daqexpert.jobs;

import com.google.common.collect.EvictingQueue;
import org.apache.log4j.Logger;
import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.processing.DominatingConditionSelector;
import rcms.utilities.daqexpert.processing.context.ContextEntry;
import rcms.utilities.daqexpert.processing.context.ObjectContextEntry;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * Manages the recovery job distribution to the controller given output from Logic Modules (set of conditions with
 * recovery suggestions)
 */
public class RecoveryJobManager {

    private static final Logger logger = Logger.getLogger(RecoveryJobManager.class);

    /** Controller client  */
    private ExpertControllerClient expertControllerClient;


    /** FIFO to keep the most recent conditions - kept to avoid accessing the database when controller rejects the
     *  recovery request. With the rejection the id of current condition being the reason to do recovery is returned.
     *  Expert will see whether this condition should be preempted, continued or postponed depending on the .
     */
    private final Queue<Condition> recentRecoveryConditions = EvictingQueue.create(5);


    /**
     * Ignore all recoveries that are older than the threshold
     */
    private long threshold = 20000;


    public RecoveryJobManager(ExpertControllerClient expertControllerClient) {
        this.expertControllerClient = expertControllerClient;
    }


    /**
     * Run recovery job TODO: accept single request. reuse dominating mechanism
     */
    public Long runRecoveryJob(List<RecoveryRequest> requests) {

        logger.info(requests.size() + " recovery jobs submitted. Now checking the age." );

        Date now = new Date();

        Collection<RecoveryRequest> filtered = requests.stream().filter(r -> r.getCondition().getStart().getTime() > now.getTime()-threshold).collect(Collectors.toList());

        if(requests.size() != filtered.size()){
            logger.info(requests.size() - filtered.size() + " were filtered - older than " + threshold + " ms");
        }


        if(filtered.size() == 0 ){
            logger.info("No recent recovery found. The threshold is " + threshold + "ms old");
            return null;
        }



        logger.info("Now choosing the dominating");

        Condition dominatingCondition = findMostImportant(requests.stream().map(r->r.getCondition()).collect(Collectors.toList()) );

        RecoveryRequest dominatingRequest = requests.stream().filter(r -> r.getProblemId() == dominatingCondition.getId()).findFirst().orElse(null);

        logger.info("Dominating recovery job selected: " + dominatingRequest.getProblemDescription() );


        recentRecoveryConditions.add(dominatingRequest.getCondition());
        RecoveryResponse recoveryResponse = expertControllerClient.sendRecoveryRequest(dominatingRequest);

        String status = recoveryResponse.getStatus();
        if("rejected".equalsIgnoreCase(status)){
            // check if this is the same now

            Long rejectionConditionReasonId = recoveryResponse.getRejectedDueToConditionId();

            logger.info("Controller rejected the recovery request due to condition " + rejectionConditionReasonId );
            Optional<Condition> conditionOptional =  recentRecoveryConditions.stream().filter(c->c.getId().equals(rejectionConditionReasonId)).findFirst();


            if(conditionOptional.isPresent()){

                Condition rejectionConditionReason = conditionOptional.get();
                String decision = handleRejection(dominatingCondition, rejectionConditionReason);

                logger.info("Handling rejection with decision to: " + decision);

                switch (decision){
                    case "continue" :
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


                RecoveryResponse secondRecoveryRespons = expertControllerClient.sendRecoveryRequest(dominatingRequest);

                if(!"accepted".equalsIgnoreCase(secondRecoveryRespons.getStatus())){
                    logger.warn("Second request resulted with rejection." + dominatingRequest);
                } else {
                    logger.info("Recovery accepted by controller with second request");
                }
            } else {
                logger.warn("Could not find condition by id " + rejectionConditionReasonId + " that was the reason to reject");
            }


        } else {
            logger.info("Recovery accepted by controller with first request");
        }
        return dominatingRequest.getProblemId();


    }

    /**
     * Temoporary method to choose dominating condition.
     *
     * TODO: use common dominating mechanism
     */
    private Condition findMostImportant(List<Condition> requests) {

        Condition topRequest = DominatingConditionSelector.findDominating(requests);

        return topRequest;
    }

    private boolean isSameCondition(Condition currentlyDominating, Condition currentlyRejecting){

        if(currentlyDominating.getLogicModule() != currentlyRejecting.getLogicModule()){
            return false;
        }

        if(currentlyDominating.getTitle() != currentlyRejecting.getTitle()){
            return false;
        }

        if(currentlyDominating.getContext() != null) {

            if(currentlyRejecting.getContext() == null){
                return false;
            } else{
                // compare only objects in context

                for(Map.Entry<String, ContextEntry> e: currentlyDominating.getContext().entrySet()){

                    if(e.getValue() instanceof ObjectContextEntry ){

                        if(!currentlyRejecting.getContext().containsKey(e.getKey())){
                            return false;
                        } else{
                            if(!currentlyRejecting.getContext().get(e.getKey()).equals(e.getValue())){
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
     * Handle rejection from controller. 3 possible ways:
     * - continue, if the conditions are the same meaning that the problem is not yet resolved. Maybe other steps are necessary
     * - ignore, if the condition that generate currently executing recovery is more important than ones that appeared afterwards
     * - interrupt, if more important condition appeard after currently executing recovery
     * @param currentlyDominating condition that has appeared recently and dominates all others
     * @param currentlyRejecting condition that is now being recovered
     * @return
     */
    private String handleRejection(Condition currentlyDominating, Condition currentlyRejecting){

        boolean isSame = isSameCondition(currentlyDominating, currentlyRejecting);

        if(isSame){
            return "continue"; //
        } else{

            // use dominating mechanism
            List<Condition> twoConditions = new ArrayList<>();
            twoConditions.add(currentlyDominating);
            twoConditions.add(currentlyRejecting);
            Condition top = findMostImportant(twoConditions);

            if(top.getId() == currentlyRejecting.getId()){
                //TODO: two cases here: "postpone" and "ignore"
                return "postpone"; // currently rejecting is the most important, postpone currently dominating condition and continue with recovery
            } else {
                return "interrupt"; // currenty rejecting is less important, interrupt it and start recovery of currently dominating
            }

        }

    }


    public void notifyConditionFinished(Long id) {
        expertControllerClient.sendConditionFinishedSignal(id);
    }
}
