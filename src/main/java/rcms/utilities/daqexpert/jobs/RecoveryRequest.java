package rcms.utilities.daqexpert.jobs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.ToString;
import rcms.utilities.daqexpert.persistence.Condition;

import java.util.List;

/**
 * Recovery request. Sent to the controller to request automated recovery procedure.
 */
@Data
@ToString
public class RecoveryRequest {

    /**
     * Id of the condition in DAQExpert. Used to match automatic recovery with current problem
     */
    private Long problemId;

    /**
     * Short title of the problem
     */
    private String problemTitle;

    /**
     * Description of the problem that will be recovered
     */
    private String problemDescription;

    /**
     * Indicates that this request should preempt current recovery
     */
    private boolean withInterrupt;


    /**
     * Indicates that this request should continue current recovery
     */
    private boolean isSameProblem;

    /**
     * Indicates that this request is less important that current one and should be postponed
     */
    private boolean withPostponement;

    private List<RecoveryRequestStep> recoveryRequestSteps;

    @JsonIgnore
    private Condition condition;


}
