package rcms.utilities.daqexpert.reasoning.logic.failures;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.Output;

import java.util.Map;

public interface HavingSpecialInstructions {

    String selectSpecialInstructionKey(DAQ daq, Map<String, Output> results);
}
