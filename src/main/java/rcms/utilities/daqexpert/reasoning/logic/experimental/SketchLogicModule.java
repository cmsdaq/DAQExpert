package rcms.utilities.daqexpert.reasoning.logic.experimental;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.SimpleLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

/**
 * This is an sketch of Logic Module (LM)
 * @author full-name (email-address)
 */
public class SketchLogicModule extends SimpleLogicModule {

	public SketchLogicModule() {
		
		/* TODO: 1. set following fields: name, description */
		this.name = "experimental";
		this.description = "quick start LM";
		this.priority = ConditionPriority.EXPERIMENTAL;
	}

	/**
	 * Returns true when the condition is satisfied, 
	 * otherwise returns false.
	 */
	@Override
	public boolean satisfied(DAQ snapshot) {

		/* TODO: 2. implement your condition here */
		return false;
	}
}
