package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Iterator;
import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqexpert.reasoning.base.ContextLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.EventPriority;

/**
 * This logic module identifies partition deadtime
 */
public class PartitionDeadtime extends ContextLogicModule {

	private final float threshold;

	public PartitionDeadtime(final float threshold) {
		this.name = "Partition deadtime";
		this.group = EventGroup.PARTITION_DEADTIME;
		this.priority = EventPriority.DEFAULTT;
		this.description = "Deadtime of partition(s) {{TTCP}} in subsystem(s) {{SUBSYSTEM}} is greater than 5%";
		this.setNotificationPlay(true);
		this.threshold = threshold;
	}

	/**
	 * Dead time when greater than 5%
	 */
	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

		boolean transition = false;
		boolean expectedRate = false;
		expectedRate = results.get(ExpectedRate.class.getSimpleName());
		if (!expectedRate)
			return false;
		transition = results.get(LongTransition.class.getSimpleName());
		if (transition)
			return false;

		boolean result = false;

		Iterator<TTCPartition> i = daq.getTtcPartitions().iterator();

		while (i.hasNext()) {
			TTCPartition partition = i.next();
			if (!partition.isMasked()) {
				float deadPercentage = 0;
				deadPercentage += partition.getPercentBusy();
				deadPercentage += partition.getPercentWarning();

				if (deadPercentage > threshold) {
					result = true;
					context.register("TTCP", partition.getName());
					context.register("SUBSYSTEM", partition.getSubsystem().getName());
				}
			}
		}

		return result;
	}

}
