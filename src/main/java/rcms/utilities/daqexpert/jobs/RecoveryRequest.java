package rcms.utilities.daqexpert.jobs;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Set;

public class RecoveryRequest {


    @JsonIgnore
    Long id;

    /**
     * Description of the problem that will be recovered
     */
    String problemDescription;

    /**
     * Steps that cannot be automatized and must be done manually
     */
    Set<String> manualSteps;


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

    public Set<String> getGreenRecycle() {
        return greenRecycle;
    }

    public void setGreenRecycle(Set<String> greenRecycle) {
        this.greenRecycle = greenRecycle;
    }

    public Set<String> getFault() {
        return fault;
    }

    public void setFault(Set<String> fault) {
        this.fault = fault;
    }

    public Set<String> getReset() {
        return reset;
    }

    public void setReset(Set<String> reset) {
        this.reset = reset;
    }


    public Set<String> getRedRecycle() {
        return redRecycle;
    }

    public void setRedRecycle(Set<String> redRecycle) {
        this.redRecycle = redRecycle;
    }

    public String getProblemDescription() {
        return problemDescription;
    }

    public void setProblemDescription(String problemDescription) {
        this.problemDescription = problemDescription;
    }

    public Set<String> getManualSteps() {
        return manualSteps;
    }

    public void setManualSteps(Set<String> manualSteps) {
        this.manualSteps = manualSteps;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "RecoveryRequest{" +
                "redRecycle=" + redRecycle +
                ", greenRecycle=" + greenRecycle +
                ", fault=" + fault +
                ", reset=" + reset +
                '}';
    }
}
