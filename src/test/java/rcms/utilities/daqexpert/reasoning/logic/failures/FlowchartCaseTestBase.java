package rcms.utilities.daqexpert.reasoning.logic.failures;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.persistence.PersistenceFormat;
import rcms.utilities.daqaggregator.persistence.StructureSerializer;

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

	protected final KnownFailure fc4 = new FlowchartCase4();

	protected final KnownFailure fc5 = new FlowchartCase5();

	protected final KnownFailure fc6 = new FlowchartCase6();
}
