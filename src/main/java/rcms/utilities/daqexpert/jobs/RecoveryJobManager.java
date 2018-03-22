package rcms.utilities.daqexpert.jobs;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.persistence.InternalConditionProducer;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionGroup;

import java.util.Date;
import java.util.List;

public class RecoveryJobManager {

    private static final Logger logger = Logger.getLogger(RecoveryJobManager.class);
    private final InternalConditionProducer producer;

    private Pair<RecoveryRequest, Condition> currentlyRunningJob;
    private Pair<RecoveryRequest, Condition> preemptedJob;

    private RecoveryJobPerformer recoveryJobPerformer;
    private Condition recoveryCondition;



    private long threshold = 2000; // dont send jobs older than 2 seconds;

    public RecoveryJobManager(InternalConditionProducer producer) {
        this.recoveryJobPerformer = new RecoveryJobPerformer();
        this.producer = producer;
    }


    /**
     * Run recovery job
     */
    public void runRecoveryJob(List<Pair<RecoveryRequest, Condition>> requests) {

        logger.info("Recovery jobs submitted: " + requests);

        Pair<RecoveryRequest, Condition> topRequest = findMostImportant(requests);

        logger.info("Top recovery job selected: " + topRequest);

        if (currentlyRunningJob == null) {
            logger.info("No other job running - selecting this one");

            Date jobStart = topRequest.getRight().getStart();
            if (jobStart.getTime() + threshold > (new Date()).getTime()) {
                fireRecovery(topRequest);
            } else {
                logger.info("Job from the past will be ignored: " + topRequest);
            }
        } else {
            checkRecoveryStatus();

            if (interupt(topRequest)) {
                logger.info("Found job " + topRequest + " that will interrupt currently running one " + currentlyRunningJob);

                topRequest.getLeft().setWithInterrupt(true);
                preemptedJob = currentlyRunningJob;
                fireRecovery(topRequest);
                checkRecoveryStatus();

            } else {
                logger.info("Current job will NOT be interrupted");
            }
        }

    }

    private void fireRecovery(Pair<RecoveryRequest, Condition> request) {
        currentlyRunningJob = request;

        if(preemptedJob != null){
            recoveryCondition.setDescription("Job preempted by condition " + request.getLeft().getProblemDescription());
            recoveryCondition.setEnd(new Date());
            producer.updateCondition(recoveryCondition);
        }

        recoveryCondition = producer.persistCondition("Recovery", new Date(), null, ConditionGroup.LHC_BEAM);
        Long id = recoveryJobPerformer.sendRequest(request.getLeft());
        currentlyRunningJob.getLeft().setId(id);
    }

    public void checkRecoveryStatus() {

        if (currentlyRunningJob == null) {
            return;
        }

        String status = recoveryJobPerformer.checkStatus(currentlyRunningJob.getLeft().getId());

        if ("finished".equalsIgnoreCase(status)) {
            currentlyRunningJob = null;
            recoveryCondition.setDescription("Successfully finished");
            recoveryCondition.setEnd(new Date());
            producer.updateCondition(recoveryCondition);
            logger.info("Job finished");
        } else if ("accepted".equalsIgnoreCase(status)) {
            recoveryCondition.setDescription("Accepted by operator");
            producer.updateCondition(recoveryCondition);
            logger.info("Job accepted");
        } else if ("rejected".equalsIgnoreCase(status)) {
            currentlyRunningJob = null;
            recoveryCondition.setDescription("Rejected by operator");
            recoveryCondition.setEnd(new Date());
            producer.updateCondition(recoveryCondition);
            logger.info("Job rejected");
        } else if("timeout".equalsIgnoreCase(status)){
            // check timeout
            currentlyRunningJob = null;
            recoveryCondition.setDescription("Timeout. No decision from operator in time");
            recoveryCondition.setEnd(new Date());
            producer.updateCondition(recoveryCondition);
            logger.info("Job timeout");
        }



    }

    /**
     * Check weather the request should interrupt currently running one
     */
    private boolean interupt(Pair<RecoveryRequest, Condition> request) {
        Condition currentlyRunningCondition = currentlyRunningJob.getRight();

        if (request.getRight().getLogicModule().ordinal() > currentlyRunningCondition.getLogicModule().ordinal()) {
            return true;
        } else {
            return false;
        }

    }

    private Pair<RecoveryRequest, Condition> findMostImportant(List<Pair<RecoveryRequest, Condition>> requests) {

        Pair<RecoveryRequest, Condition> interupting = requests.stream().max((c1, c2) -> Integer.compare(c1.getRight().getLogicModule().ordinal(), c2.getRight().getLogicModule().ordinal())).orElse(null);


        return interupting;
    }
}
