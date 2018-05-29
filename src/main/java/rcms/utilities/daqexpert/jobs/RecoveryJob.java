package rcms.utilities.daqexpert.jobs;

public enum RecoveryJob {


    StopAndStartTheRun("Stop and start the run"),
    RedRecycle("Red recycle"), //TODO: RedAndGreenRecycle
    GreenRecycle("Green recycle"),
    TTCHardReset("Issue TTC hard reset");

    RecoveryJob(String readable){
        this.readable = readable;
    }

    public String getReadable() {
        return readable;
    }

    private String readable;
}
