package rcms.utilities.daqexpert.jobs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import rcms.utilities.daqexpert.persistence.Condition;

import javax.persistence.Transient;
import java.util.List;
import java.util.Set;

/**
 * Recovery request. Sent to the controller to request automated recovery procedure.
 */
public class RecoveryRequest {

    @JsonIgnore
    Long id;


    @JsonIgnore
    Condition condition;
    /**
     * Id of the condition in DAQExpert. Used to match automatic recovery with current problem
     */
    private Long problemId;


    /**
     * Status of the recovery request
     */
    private String status;


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

    @Transient
    private List<RecoveryStep> recoverySteps;

    /**
     * Description of the problem that will be recovered
     */
    String problemDescription;

    public String getProblemDescription() {
        return problemDescription;
    }

    public void setProblemDescription(String problemDescription) {
        this.problemDescription = problemDescription;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProblemId() {
        return problemId;
    }

    public void setProblemId(Long problemId) {
        this.problemId = problemId;
    }

    public boolean isWithInterrupt() {
        return withInterrupt;
    }

    public void setWithInterrupt(boolean withInterrupt) {
        this.withInterrupt = withInterrupt;
    }


    public List<RecoveryStep> getRecoverySteps() {
        return recoverySteps;
    }

    public void setRecoverySteps(List<RecoveryStep> recoverySteps) {
        this.recoverySteps = recoverySteps;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }


    public boolean isSameProblem() {
        return isSameProblem;
    }

    public void setSameProblem(boolean sameProblem) {
        isSameProblem = sameProblem;
    }

    public boolean isWithPostponement() {
        return withPostponement;
    }

    public void setWithPostponement(boolean withPostponement) {
        this.withPostponement = withPostponement;
    }

    @Override
    public String toString() {
        return "RecoveryRequest{" +
                "condition=" + condition.getStart() +
                ", problemId=" + problemId +
                '}';
    }
}
