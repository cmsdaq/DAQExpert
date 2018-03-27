package rcms.utilities.daqexpert.jobs;

public enum Jobs {


    StopAndStartTheRun("Stop and start the run"),
    RedRecycle("Red recycle"),
    GreenRecycle("Green recycle"),
    TTCHardReset("Issue TTC hard reset");

    Jobs(String readable){
        this.readable = readable;
    }

    public String getReadable() {
        return readable;
    }

    private String readable;
}
