package rcms.utilities.daqexpert.reasoning.logic.failures;

import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqexpert.reasoning.base.ActionLogicModule;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;
import rcms.utilities.daqexpert.reasoning.base.enums.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.EventPriority;
import rcms.utilities.daqexpert.reasoning.base.enums.TTSState;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRateWhenExpected;

/**
 * Logic module identifying 6 flowchart case.
 * 
 * @see flowchart at https://twiki.cern.ch/twiki/pub/CMS/ShiftNews/DAQStuck3.pdf
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class FlowchartCase6 extends ActionLogicModule {

	public FlowchartCase6() {
		this.name = "FC6";
		this.group = EventGroup.FLOWCHART;
		this.priority = EventPriority.CRITICAL;

		this.description = "TTCP {{TTCP}} of subsystem {{SUBSYSTEM}} in {{TTCPSTATE}} TTS state, and FED {{FED}} is backpressured. "
				+ "Backpressure is going through that FED, it's in {{FEDSTATE}} but there is NOTHING wrong with it. "
				+ "A FED stopped sending data.";

		this.action = new SimpleAction("Try to recover: Stop the run",
				"Red & green recycle the subsystem {{SUBSYSTEM}} (whose FED stopped sending data)",
				"Start new Run (Try 1 time)",
				"Problem fixed: Make an e-log entry. Call the DOC of the subsystem {{SUBSYSTEM}} (whose FED stopped sending data) to inform",
				"Problem not fixed: Call the DOC for the subsystem {{SUBSYSTEM}} (whose FED stopped sending data)");
	}

	private static final Logger logger = Logger.getLogger(FlowchartCase6.class);

	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

		boolean result = false;
		if (!results.get(NoRateWhenExpected.class.getSimpleName()))
			return false;

		for (SubSystem subSystem : daq.getSubSystems()) {

			for (TTCPartition ttcp : subSystem.getTtcPartitions()) {

				TTSState currentState = TTSState.getByCode(ttcp.getTtsState());
				if (currentState == TTSState.BUSY || currentState == TTSState.WARNING) {

					for (FED fed : ttcp.getFeds()) {
						TTSState currentFedState = TTSState.getByCode(fed.getTtsState());
						if ((currentFedState == TTSState.BUSY || currentFedState == TTSState.WARNING)
								&& fed.getPercentBackpressure() > 0F) {

							context.register("TTCP", ttcp.getName());
							context.register("TTCPSTATE", currentState.name());
							context.register("SUBSYSTEM", subSystem.getName());
							context.register("FED", fed.getSrcIdExpected());
							context.register("FEDSTATE", currentFedState.name());

							logger.debug("M6: " + name + " with fed " + fed.getId() + " in backpressure at "
									+ new Date(daq.getLastUpdate()));
							result = true;
						}
					}
				}
			}
		}

		return result;
	}

}
