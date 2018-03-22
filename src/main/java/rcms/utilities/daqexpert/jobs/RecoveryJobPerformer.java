package rcms.utilities.daqexpert.jobs;

import org.apache.log4j.Logger;
import org.springframework.web.client.RestTemplate;

public class RecoveryJobPerformer {


    private static final Logger logger = Logger.getLogger(RecoveryJobPerformer.class);

    final String mainUri =  "http://localhost:8082";
    final String postJobUri = mainUri + "/recover";
    final String getRequestStatusUri = mainUri + "/status/{id}/";

    public Long sendRequest(RecoveryRequest recoveryRequest) {

        RestTemplate restTemplate = new RestTemplate();

        Long requestId = restTemplate.postForObject(postJobUri, recoveryRequest, Long.class);

        logger.info("Recovery request id: " + requestId);
        return requestId;

    }

    /**
     * Check job status
     * TODO: include timestamp when it has changed
     */
    public String checkStatus(Long id){

        RestTemplate restTemplate = new RestTemplate();
        String status  = restTemplate.getForObject(getRequestStatusUri, String.class, id);
        logger.info("Retrieved status of job " + id + ", it's " + status);
        return status;
    }

}
