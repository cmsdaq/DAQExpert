package rcms.utilities.daqexpert.reasoning.logic.experimental;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.SimpleLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.EventPriority;

/**
 * This is an sketch of Logic Module (LM)
 * @author full-name (email-address)
 */
public class SketchLogicModule extends SimpleLogicModule {

	public SketchLogicModule() {
		
		/* TODO: 1. set following fields: name, description */
		this.name = "experimental";
		this.description = "quick start LM";
		this.group = EventGroup.EXPERIMENTAL;
		this.priority = EventPriority.EXPERIMENTAL;
	}

	/**
	 * Returns true when the condition is satisfied, 
	 * otherwise returns false.
	 */
	@Override
	public boolean satisfied(DAQ snapshot, Map<String, Boolean> results) {

		/* TODO: 2. implement your condition here */
		return false;
	}
}
