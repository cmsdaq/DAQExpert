package rcms.utilities.daqexpert.jobs;

import org.apache.log4j.Logger;
import org.springframework.web.client.RestTemplate;
import rcms.utilities.daqexpert.ExpertException;
import rcms.utilities.daqexpert.ExpertExceptionCode;

/**
 * Controller client. Used to pass the recovery requests and receive decisions whether they are accepted or not.
 */
public class ExpertControllerClient {

    /** Address of the controller */
    private final String mainUri;

    /** Address of the API that accepts recovery requests */
    private final String postJobUri;

    /** Address of the API that accepts condition-finished signals.  */
    private final String finishedConditionUri;

    private static final Logger logger = Logger.getLogger(ExpertControllerClient.class);

    public ExpertControllerClient(String mainUri) {
        this.mainUri = mainUri;
        this.postJobUri = mainUri + "/recover";
        this.finishedConditionUri = mainUri + "/finished";
    }

    /**
     * Sends recovery request to the controller. The request might be accepted or not, it will be included in response.
     */
    public RecoveryResponse sendRecoveryRequest(RecoveryRequest recoveryRequest) {

        RestTemplate restTemplate = new RestTemplate();

        try {
            logger.info("Sending recovery request: " + recoveryRequest.getProblemId() + "("+recoveryRequest.getProblemTitle()+")");
            logger.debug("Details: "  + recoveryRequest);
            RecoveryResponse recoveryResponse = restTemplate.postForObject(postJobUri, recoveryRequest, RecoveryResponse.class);

            logger.info("Recovery request id: " + recoveryResponse);
            return recoveryResponse;
        }catch(Exception e){
            logger.error("Requests to " + postJobUri + " failed");
            logger.error(e);
            throw new ExpertException(ExpertExceptionCode.AutomaticRecoveryProblem,"Request to controller failed with message :" + e.getMessage());
        }

    }

    /**
     * Sends finished signal to the controller. This indicates that condition with given id is no longer satisfied.
     * @param id condition id
     */
    public void sendConditionFinishedSignal(Long id) {
        RestTemplate restTemplate = new RestTemplate();

        try {
            logger.info("Notifying that condition " + id + " finished");
            restTemplate.postForObject(finishedConditionUri, id, Void.class);

        }catch(Exception e){
            logger.error(e);
            throw new ExpertException(ExpertExceptionCode.AutomaticRecoveryProblem,"Request to controller failed with message :" + e.getMessage());
        }
    }
}
