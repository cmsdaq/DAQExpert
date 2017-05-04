package rcms.utilities.daqexpert.reasoning.logic.failures;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.persistence.PersistenceFormat;
import rcms.utilities.daqaggregator.persistence.StructureSerializer;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
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
	/**
	 * method to load a deserialize a snapshot given a file name
	 */
	protected static DAQ getSnapshot(String fname) throws URISyntaxException {

		StructureSerializer serializer = new StructureSerializer();

		URL url = FlowchartCase1.class.getResource(fname);

		File file = new File(url.toURI());

		return serializer.deserialize(file.getAbsolutePath(), PersistenceFormat.SMILE);
	}

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
	
	public FlowchartCaseTestBase(){
		HashSet<String> logicModules = new HashSet<>();
		logicModules.add(fc1.getClass().getSimpleName());
		logicModules.add(fc2.getClass().getSimpleName());
		logicModules.add(fc3.getClass().getSimpleName());

		logicModules.add(piDisconnected.getClass().getSimpleName());
		logicModules.add(piProblem.getClass().getSimpleName());
		logicModules.add(fedDisconnected.getClass().getSimpleName());
		logicModules.add(fmmProblem.getClass().getSimpleName());

		logicModules.add(fc5.getClass().getSimpleName());
		logicModules.add(fc6.getClass().getSimpleName());
		unidentified.setKnownFailureClasses(logicModules);
	}
}
