package rcms.utilities.daqexpert.reasoning.base;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;

/**
 * Type of Logic Module which identifies state transitions.
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public abstract class ComparatorLogicModule extends LogicModule {

	public ComparatorLogicModule(){
		this.problematic = false;
	}

	private DAQ last;

	private static Logger logger = Logger.getLogger(ComparatorLogicModule.class);

	public boolean compare(DAQ daq) {
		boolean result = false;
		
		if(last == null && daq == null){
			return result;
		}

		if (last == null) {
			last = new DAQ();
		}
		try {
			result = compare(last, daq);
		} catch (RuntimeException e) {
			//logger.error("Error comparing snapshots", e);
			result = true;
			name = "undefined";
		}

		last = daq;
		return result;
	}

	public abstract boolean compare(DAQ previous, DAQ current);

	public DAQ getLast() {
		return last;
	}

	public void setLast(DAQ last) {
		this.last = last;
	}

}
