package rcms.utilities.daqexpert.jobs;

public enum RecoveryJob {


    StopAndStartTheRun("Stop and start the run"),
    RedAndGreenRecycle("Red & green recycle"),
    //TODO: delete next step - same as R&G recycle
    RedRecycle("Red & green recycle"),
    GreenRecycle("Green recycle"),
    TTCHardReset("Issue a TTC hard reset");

    RecoveryJob(String readable){
        this.readable = readable;
    }

    public String getReadable() {
        return readable;
    }

    private String readable;
}
