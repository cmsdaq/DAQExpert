package rcms.utilities.daqaggregator.reasoning;

import java.text.NumberFormat;
import java.util.HashSet;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqaggregator.reasoning.base.Condition;
import rcms.utilities.daqaggregator.reasoning.base.Entry;
import rcms.utilities.daqaggregator.reasoning.base.EventClass;
import rcms.utilities.daqaggregator.reasoning.base.EventRaport;
import rcms.utilities.daqaggregator.reasoning.base.Level;

public class WarningInSubsystem implements Condition {
	private final static Logger logger = Logger.getLogger(WarningInSubsystem.class);

	private static final String name = "Exists TTCP with warning > 0%";
	private static final String description = "One or more TTCP (attached below) has warning above 0%, it may affect rate.";
	private static final String action = "No action";

	private String text = "";

	@Override
	public Boolean satisfied(DAQ daq) {

		boolean result = false;

		for (SubSystem subSystem : daq.getSubSystems()) {

			for (TTCPartition ttcp : subSystem.getTtcPartitions()) {

				if (ttcp.getPercentWarning() != 0F) {
					result = true;
					text = "TTCP in warning";
				}
			}
		}

		return result;
	}

	@Override
	public Level getLevel() {
		return Level.Warning;
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public void gatherInfo(DAQ daq, Entry entry) {

		EventRaport eventRaport = entry.getEventRaport();
		if (!eventRaport.isInitialized()) {
			eventRaport.initialize(name, description, action);
		}

		for (SubSystem subSystem : daq.getSubSystems()) {

			for (TTCPartition ttcp : subSystem.getTtcPartitions()) {

				if (ttcp.getPercentWarning() != 0F) {

					String ttcpString = "TTCP: " + ttcp.getName() + ", subsystem: " + subSystem.getName();
					String value = String.format("%.3f", ttcp.getPercentWarning()) + "%";
					eventRaport.getSetByCode("ttcpInWarning").add(ttcpString);
					eventRaport.getSetByCode("warningValues").add(value);
				}
			}
		}
	}

	@Override
	public EventClass getClassName() {
		return EventClass.defaultt;
	}

}
