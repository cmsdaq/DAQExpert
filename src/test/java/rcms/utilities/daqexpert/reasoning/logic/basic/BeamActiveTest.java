package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.ResultSupplier;
import rcms.utilities.daqexpert.reasoning.base.enums.LHCBeamMode;

/**
 *
 * @author holzner
 */
public class BeamActiveTest
{
	/**
	 * Test of satisfied method, of class BeamActive.
	 */
	@Test
	public void test01()
	{
		DAQ snapshot = new DAQ();

		// modes where the BeamActive module should be satisfied
		Set<LHCBeamMode> activeModeSet = new HashSet<LHCBeamMode>(Arrays.asList(
		 new LHCBeamMode[] {
			LHCBeamMode.INJECTION_PROBE_BEAM,
			LHCBeamMode.INJECTION_SETUP_BEAM,
			LHCBeamMode.INJECTION_PHYSICS_BEAM,
			LHCBeamMode.PREPARE_RAMP,
			LHCBeamMode.RAMP,
			LHCBeamMode.FLAT_TOP,
			LHCBeamMode.SQUEEZE,
			LHCBeamMode.ADJUST,
			LHCBeamMode.STABLE_BEAMS
		}));

		//ResultSupplier resultSupplier = new ResultSupplier();

		BeamActive instance = new BeamActive();

		// check all modes
		for (LHCBeamMode mode : LHCBeamMode.values()) {

			String modeName = mode.getCode();
			snapshot.setLhcBeamMode(modeName);

			boolean expectedResult = activeModeSet.contains(mode);

			//instance.setResultSupplier(new ResultSupplier());


			boolean result = instance.satisfied(snapshot);

			assertEquals("unexpected result for beam mode " + modeName, expectedResult, result);

		} // loop over LHC modes
		
	}

}
