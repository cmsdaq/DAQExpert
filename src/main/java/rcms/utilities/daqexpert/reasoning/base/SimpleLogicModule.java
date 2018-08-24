package rcms.utilities.daqexpert.reasoning.base;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.processing.Requiring;

/**
 * Simple type of Logic Module. It checks if the condition is satisfied.
 * 
 * This class implements the maturity threshold to avoid fluctuations of short
 * condition satisfied. It also supplies with some common helper methods.
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public abstract class SimpleLogicModule extends LogicModule {

	/**
	 * Is condition of Logic Module satisfied. *
	 * 
	 * @param daq
	 *            Snapshot to examine by this Logic Module
	 * @return condition satisfied, <code>true</code> when satisfied,
	 *         <code>false</code> otherwise.
	 */
	public abstract boolean satisfied(DAQ daq);

	/**
	 * Is condition stable i.e. is condition lasting for some time or it's only
	 * caused by temporary monitoring data fluctuation
	 */
	private boolean isMature;

	/**
	 * Current value of maturity
	 */
	private int maturity;

	/**
	 * Threshold for becoming mature
	 */
	public int maturityThreshold;

	/**
	 * increase maturity and check if it exceeded the threshold to become mature
	 */
	public void increaseMaturity() {
		maturity++;
		if (maturity > maturityThreshold) {
			isMature = true;
		}
	}

	public void resetMaturity() {
		maturity = 0;
		isMature = false;
	}

	public boolean isMature() {
		return isMature;
	}

	public SimpleLogicModule() {
		this.isMature = false;
		this.maturity = 0;
		this.maturityThreshold = 1;
	}

}
