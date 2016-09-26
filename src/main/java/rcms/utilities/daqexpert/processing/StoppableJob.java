package rcms.utilities.daqexpert.processing;

import java.util.concurrent.ScheduledFuture;

public abstract class StoppableJob implements Runnable {

	private ScheduledFuture<?> future;

	public ScheduledFuture<?> getFuture() {
		return future;
	}

	public void setFuture(ScheduledFuture<?> future) {
		this.future = future;
	}

}
