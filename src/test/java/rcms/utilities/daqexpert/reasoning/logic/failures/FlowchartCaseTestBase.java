package rcms.utilities.daqexpert.reasoning.logic.failures;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.persistence.PersistenceFormat;
import rcms.utilities.daqaggregator.persistence.StructureSerializer;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.SimpleLogicModule;
import rcms.utilities.daqexpert.reasoning.logic.failures.disconnected.FEDDisconnected;
import rcms.utilities.daqexpert.reasoning.logic.failures.disconnected.FMMProblem;
import rcms.utilities.daqexpert.reasoning.logic.failures.disconnected.PiDisconnected;
import rcms.utilities.daqexpert.reasoning.logic.failures.disconnected.ProblemWithPi;

/**
 * @author holzner
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class FlowchartCaseTestBase {

	protected Map<String, Boolean> results = new HashMap<String, Boolean>();

	protected final KnownFailure fc1 = new FlowchartCase1();

	protected final KnownFailure fc2 = new FlowchartCase2();

	protected final KnownFailure fc3 = new FlowchartCase3();

	protected final KnownFailure piDisconnected = new PiDisconnected();
	protected final KnownFailure piProblem = new ProblemWithPi();
	protected final KnownFailure fedDisconnected = new FEDDisconnected();
	protected final KnownFailure fmmProblem = new FMMProblem();

	protected final KnownFailure fc5 = new FlowchartCase5();
	protected final KnownFailure fc6 = new FlowchartCase6();
	protected final UnidentifiedFailure unidentified = new UnidentifiedFailure();

	public FlowchartCaseTestBase() {
		HashSet<String> logicModules = new HashSet<>();

		for (LogicModuleRegistry lm : LogicModuleRegistry.values()) {
			if (lm.getLogicModule() != null && lm.getLogicModule() instanceof KnownFailure) {
				logicModules.add(lm.getLogicModule().getClass().getSimpleName());
			}
		}
		unidentified.setKnownFailureClasses(logicModules);
	}

	/** method to assert that the given logic module has found the expected 
	 *  result. This is used iteratively check the chain of reasoning (where later 
	 *  modules potentially depend on the results of earlier ones) at each step. */ 
	protected void assertEqualsAndUpdateResults(boolean expected, SimpleLogicModule logicModule, DAQ snapshot) {
		boolean result = logicModule.satisfied(snapshot, results);
		Assert.assertEquals("unexpected result for module " + logicModule.getClass().getSimpleName() + ":", expected, result);
		results.put(logicModule.getClass().getSimpleName(), result);
	}

	@Before
	public void cleanResult() {
		results.clear();

		// put results of prerequisite tests by hand
		// (as opposed to get them from a series of snapshots
		// which introduces a dependency on other tests)
		results.put("StableBeams", true);
		results.put("NoRateWhenExpected", true);
	}

	/**
	 * method to load a deserialize a snapshot given a file name
	 */
	protected static DAQ getSnapshot(String fname) throws URISyntaxException {

		StructureSerializer serializer = new StructureSerializer();

		URL url = FlowchartCase1.class.getResource(fname);

		File file = new File(url.toURI());

		return serializer.deserialize(file.getAbsolutePath(), PersistenceFormat.SMILE);
	}

}
