package rcms.utilities.daqexpert.reasoning.logic.failures.fixingSoftErrors;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.SubSystem;

/**
 * A generator of (mock) snapshots with subsystems and Level0 and selected 
 * subsystems going into certain states after a given time.
 */
public class SubsystemStateSequenceGenerator {
	
	/** name for the level zero 'subsystem' */
	public static final String LEVELZERO = "levelZero";

	/** represents a state change in one subsystem */
	private static class UpdateRecord {
		private final String subsystem, newState;

		public UpdateRecord(String subsystem, String newState) {
			this.subsystem = subsystem;
			this.newState = newState;
		}
	}

	/** sequence of state changes to be played back. Note that there
	 *  can be more than one subsystem changing its state at the same time.
	 *  The map is ordered by keys, the values are lists.
	 */
	private final ListMultimap<Long, UpdateRecord> timeline = MultimapBuilder.treeKeys().arrayListValues().build();

	/** list of all encountered subsystems */
	private Set<String> allSubsystems = new HashSet<String>();

	/** add a new state transition of the given subsystem at the given time */
	public void addNewState(long timestamp, String subsystem, String newState) {
		timeline.put(timestamp, new UpdateRecord(subsystem, newState));

		allSubsystems.add(subsystem);
	}

	/** adds a point in time at which there is no change. This can e.g.
	 *  be used at the beginning to get all subsystems in 'Running' state.
	 */
	public void addPointInTime(long timestamp) {

		// Guava's multimap does not accept null values so we 
		// create a 'null like record'
		timeline.put(timestamp, new UpdateRecord(null, null));
	} 

	/** @return a list with DAQ objects which follow
	 *  the specified state transitions. At the beginning, all seen subsystems
	 *  are considered to be in 'Running' state (in the future we could
	 *  also return an Iterator<DAQ>).
	 */
	public List<DAQ> makeSnapshots() {

		List<DAQ> result = new ArrayList<DAQ>();

		// start with all subsystems being in running
		Map<String, String> states = new HashMap<String, String>();

		final String initialState = "Running";

		for (String subsystem : allSubsystems) {
			states.put(subsystem, initialState);
		}

		// process the timeline
		for (Map.Entry<Long, Collection<UpdateRecord>> entry : timeline.asMap().entrySet()) {

			long timestamp = entry.getKey();

			DAQ daq = new DAQ();
			daq.setLastUpdate(timestamp);

			// update the current state per subsystem
			for (UpdateRecord updateRecord : entry.getValue()) {

				// exclude null subsystem, these are just used to have
				// a point in time with no state changes
				if (updateRecord.subsystem != null) {
					states.put(updateRecord.subsystem, updateRecord.newState);
				}
			}

			// now create all subsystems in the current snapshot and set their state
			List<SubSystem> subsysList = new ArrayList<SubSystem>();

			for (Map.Entry<String, String> entry2 : states.entrySet()) {
				String subsysName = entry2.getKey();
				String state = entry2.getValue();

				if (LEVELZERO.equals(subsysName)) {
					daq.setLevelZeroState(state);
				} else {
					// 'ordinary' subsystem
					SubSystem subsys = new SubSystem();
					subsys.setName(subsysName);
					subsys.setStatus(state);

					subsysList.add(subsys);
				}
			}

			daq.setSubSystems(subsysList);

			result.add(daq);

		} // loop over points in time

		return result;
	}

}
