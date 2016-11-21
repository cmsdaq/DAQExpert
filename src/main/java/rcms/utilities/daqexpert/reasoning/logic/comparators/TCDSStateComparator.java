package rcms.utilities.daqexpert.reasoning.logic.comparators;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqexpert.reasoning.base.ComparatorLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.EventPriority;

public class TCDSStateComparator extends ComparatorLogicModule {

	public TCDSStateComparator() {
		this.name = "n/a";
		this.group = EventGroup.TCDS_STATE;
		this.priority = EventPriority.DEFAULTT;
		this.description = "New TCDS state identified";
	}

	public boolean compare(DAQ previous, DAQ current) {
		String tcdsStatus = getTCDSState(current);
		String prevTcdsStatus = getTCDSState(previous);

		if (!tcdsStatus.equals(prevTcdsStatus)) {
			this.name = tcdsStatus;
			return true;
		}
		return false;
	}

	private String getTCDSState(DAQ daq) {
		if (daq.getSubFEDBuilders() != null) {
			for (SubSystem curr : daq.getSubSystems()) {
				/* check tcds subsystem state */
				if (curr.getName().equals("TCDS")) {
					return curr.getStatus();
				}
			}
		}
		return "undefined";
	}

}
