package rcms.utilities.daqexpert.jobs;


public class RecoveryResponse {

    /**
     * Result of the recovery request, may be accepted or rejected
     */
    private String status;

    /**
     * Id of the recovery that was accepted or rejected
     */
    private Long recoveryId;

    /**
     * Id of the condition that generated recovery
     */
    private Long conditionId;

    /**
     * Id of the condition that was a reason of rejection
     */
    private Long rejectedDueToConditionId;

    /**
     * Id of the same condition it continues
     */
    private Long continuesTheConditionId;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getRecoveryId() {
        return recoveryId;
    }

    public void setRecoveryId(Long recoveryId) {
        this.recoveryId = recoveryId;
    }

    public Long getConditionId() {
        return conditionId;
    }

    public void setConditionId(Long conditionId) {
        this.conditionId = conditionId;
    }

    public Long getRejectedDueToConditionId() {
        return rejectedDueToConditionId;
    }

    public void setRejectedDueToConditionId(Long rejectedDueToConditionId) {
        this.rejectedDueToConditionId = rejectedDueToConditionId;
    }

    public Long getContinuesTheConditionId() {
        return continuesTheConditionId;
    }

    public void setContinuesTheConditionId(Long continuesTheConditionId) {
        this.continuesTheConditionId = continuesTheConditionId;
    }
}
