package rcms.utilities.daqexpert.jobs;


import lombok.Data;

@Data
public class RecoveryResponse {

    /**
     * Result of the recovery request, eg. accepted, rejected
     */
    private String acceptanceDecision;

    /**
     * Id of the recovery procedure that is executed in controller
     */
    private Long recoveryProcedureId;

    /**
     * Id of the condition that was a reason of rejection
     */
    private Long rejectedDueToConditionId;

    /**
     * Id of the same condition it continues
     */
    private Long continuesTheConditionId;



    @Override
    public String toString() {
        return "RecoveryResponse{" +
                "acceptanceDecision='" + acceptanceDecision +
                ", recoveryProcedureId=" + recoveryProcedureId +
                ", rejectedDueToConditionId=" + rejectedDueToConditionId +
                ", continuesTheConditionId=" + continuesTheConditionId +
                '}';
    }
}
