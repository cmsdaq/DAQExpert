package rcms.utilities.daqexpert.jobs;

import lombok.*;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RecoveryRequestStep {

    /**
     * Step ordinal number in procedure
     */
    Integer stepIndex;

    String humanReadable;

    Boolean issueTTCHardReset;

    /**
     * Subsystems to red recycle
     */
    Set<String> redRecycle;

    /**
     * Subsystems to green recycle
     */
    Set<String> greenRecycle;

    /**
     * Subsystems to blame
     */
    Set<String> fault;

    /**
     * Subsystems to reset. Some schedules could have been planned by shifter. This will reset that actions.
     */
    Set<String> reset;

}
