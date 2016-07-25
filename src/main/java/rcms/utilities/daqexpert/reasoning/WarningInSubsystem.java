package rcms.utilities.daqexpert.reasoning;

import java.util.Arrays;
import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqexpert.reasoning.base.Entry;
import rcms.utilities.daqexpert.reasoning.base.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.EventPriority;
import rcms.utilities.daqexpert.reasoning.base.EventRaport;
import rcms.utilities.daqexpert.reasoning.base.ExtendedCondition;

public class WarningInSubsystem extends ExtendedCondition {

	public WarningInSubsystem() {
		this.name = "Exists TTCP with warning > 0%";
		this.description = "One or more TTCP (attached below) has warning above 0%, it may affect rate.";
		this.action = Arrays.asList("No action");
		this.group = EventGroup.Warning;
		this.priority = EventPriority.defaultt;
	}

	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

		boolean result = false;

		for (SubSystem subSystem : daq.getSubSystems()) {

			for (TTCPartition ttcp : subSystem.getTtcPartitions()) {

				if (ttcp.getPercentWarning() != 0F) {
					result = true;
				}
			}
		}

		return result;
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

}
