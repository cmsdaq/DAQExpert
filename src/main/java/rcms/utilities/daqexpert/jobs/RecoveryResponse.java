package rcms.utilities.daqexpert.jobs;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    private List<Long> continuesTheConditionIds;




    @Override
    public String toString() {
        return "RecoveryResponse{" +
                "acceptanceDecision='" + acceptanceDecision +
                ", recoveryProcedureId=" + recoveryProcedureId +
                ", rejectedDueToConditionId=" + rejectedDueToConditionId +
                ", continuesTheConditionIds=" + continuesTheConditionIds +
                '}';
    }
}
