package rcms.utilities.daqexpert.reasoning.logic.failures;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.persistence.PersistenceFormat;
import rcms.utilities.daqaggregator.persistence.StructureSerializer;
import rcms.utilities.daqexpert.reasoning.logic.failures.backpressure.BugInFilterfarm;
import rcms.utilities.daqexpert.reasoning.logic.failures.backpressure.CorruptedData;
import rcms.utilities.daqexpert.reasoning.logic.failures.backpressure.HLTProblem;
import rcms.utilities.daqexpert.reasoning.logic.failures.backpressure.LinkProblem;
import rcms.utilities.daqexpert.reasoning.logic.failures.backpressure.OnlyFedStoppedSendingData;
import rcms.utilities.daqexpert.reasoning.logic.failures.backpressure.OutOfSequenceData;
import rcms.utilities.daqexpert.reasoning.logic.failures.backpressure.RuStuck;
import rcms.utilities.daqexpert.reasoning.logic.failures.backpressure.RuStuckWaiting;
import rcms.utilities.daqexpert.reasoning.logic.failures.backpressure.RuStuckWaitingOther;

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

		URL url = KnownFailure.class.getResource(fname);

		File file = new File(url.toURI());

		return serializer.deserialize(file.getAbsolutePath(), PersistenceFormat.SMILE);
	}

	protected final KnownFailure fc1 = new OutOfSequenceData();
	protected final KnownFailure lfc1 = new LegacyFlowchartCase1();

	protected final KnownFailure fc2 = new CorruptedData();

	protected final KnownFailure fc3 = new FlowchartCase3();

	protected final KnownFailure fc4 = new FlowchartCase4();

	protected final KnownFailure fc5 = new FlowchartCase5();

	protected final KnownFailure ruStuckWaiting = new RuStuckWaiting();
	protected final KnownFailure ruStuckWaitingOther = new RuStuckWaitingOther();
	

	protected final KnownFailure b1 = new BugInFilterfarm();
	protected final KnownFailure b2 = new HLTProblem();
	protected final KnownFailure b3 = new LinkProblem();
	protected final KnownFailure b4 = new OnlyFedStoppedSendingData();
	protected final KnownFailure ruStuck = new RuStuck();

	
}
