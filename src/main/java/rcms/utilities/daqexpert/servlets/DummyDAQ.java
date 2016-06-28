package rcms.utilities.daqexpert.servlets;

import rcms.utilities.daqaggregator.data.DAQ;

public class DummyDAQ {
	private long lastUpdate;
	private long rate;
	private long events;

	public DummyDAQ() {

	}

	public DummyDAQ(DAQ daq) {
		this.setRate((long) daq.getFedBuilderSummary().getRate());
		this.setEvents(daq.getFedBuilderSummary().getSumEventsInRU());
		this.setEvents(daq.getBuSummary().getNumEvents());
		this.lastUpdate = daq.getLastUpdate();
	}

	public long getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(long lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public long getEvents() {
		return events;
	}

	public void setEvents(long events) {
		this.events = events;
	}

	public long getRate() {
		return rate;
	}

	public void setRate(long rate) {
		this.rate = rate;
	}

}
