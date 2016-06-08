package rcms.utilities.daqaggregator.reasoning.base;

import rcms.utilities.daqaggregator.data.DAQ;

public interface Condition extends Classificable {

	public Boolean satisfied(DAQ daq);
	
	public void gatherInfo(DAQ daq, Entry entry);
	
	
}
