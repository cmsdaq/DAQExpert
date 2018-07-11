package rcms.utilities.daqexpert.jobs;

public enum RecoveryJob {


    StopAndStartTheRun("Stop and start the run"),
    RedAndGreenRecycle("Red & green recycle"),
    RedRecycle("Red recycle"),
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
