package rcms.utilities.daqexpert.jobs;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Transient;
import java.util.Date;
import java.util.Set;

public class RecoveryStep {

    /**
     * Step ordinal number in procedure
     */
    int stepIndex;

    String humanReadable;


    /**
     * Subsystems to red recycle
     */
    @Transient
    Set<String> redRecycle;

    /**
     * Subsystems to green recycle
     */
    @Transient
    Set<String> greenRecycle;

    /**
     * Subsystems to blame
     */
    @Transient
    Set<String> fault;

    /**
     * Subsystems to reset. Some schedules could have been planned by shifter. This will reset that actions.
     */
    @Transient
    Set<String> reset;


    private Boolean issueTTCHardReset;

    @JsonIgnore
    String status;

    Date started;

    Date finished;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getFinished() {
        return finished;
    }

    public void setFinished(Date finished) {
        this.finished = finished;
    }

    public Date getStarted() {
        return started;
    }

    public void setStarted(Date started) {
        this.started = started;
    }

    public int getStepIndex() {
        return stepIndex;
    }

    public void setStepIndex(int stepIndex) {
        this.stepIndex = stepIndex;
    }

    public String getHumanReadable() {
        return humanReadable;
    }

    public void setHumanReadable(String humanReadable) {
        this.humanReadable = humanReadable;
    }

    public Boolean getIssueTTCHardReset() {
        return issueTTCHardReset;
    }

    public void setIssueTTCHardReset(Boolean issueTTCHardReset) {
        this.issueTTCHardReset = issueTTCHardReset;
    }

    @Override
    public String toString() {
        return "RecoveryRequest{" +
                "redRecycle=" + redRecycle +
                ", greenRecycle=" + greenRecycle +
                ", fault=" + fault +
                ", reset=" + reset +
                ", issueTTCHardReset=" + issueTTCHardReset +
                '}';
    }
}
