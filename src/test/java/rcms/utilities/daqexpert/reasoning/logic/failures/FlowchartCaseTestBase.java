package rcms.utilities.daqexpert.reasoning.logic.failures;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.RU;
import rcms.utilities.daqaggregator.persistence.StructureSerializer;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.ContextLogicModule;
import rcms.utilities.daqexpert.reasoning.base.LogicModule;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.SimpleLogicModule;
import rcms.utilities.daqexpert.reasoning.logic.basic.*;
import rcms.utilities.daqexpert.reasoning.logic.failures.backpressure.*;
import rcms.utilities.daqexpert.reasoning.logic.failures.deadtime.BackpressureFromEventBuilding;
import rcms.utilities.daqexpert.reasoning.logic.failures.deadtime.BackpressureFromFerol;
import rcms.utilities.daqexpert.reasoning.logic.failures.deadtime.BackpressureFromHlt;
import rcms.utilities.daqexpert.reasoning.logic.failures.deadtime.FedDeadtimeDueToDaq;
import rcms.utilities.daqexpert.reasoning.logic.failures.disconnected.FEDDisconnected;
import rcms.utilities.daqexpert.reasoning.logic.failures.disconnected.FMMProblem;
import rcms.utilities.daqexpert.reasoning.logic.failures.disconnected.PiDisconnected;
import rcms.utilities.daqexpert.reasoning.logic.failures.disconnected.ProblemWithPi;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author holzner
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class FlowchartCaseTestBase {

	protected Map<String, Output> results = new HashMap<>();

	protected final KnownFailure fc1 = new OutOfSequenceData();
	protected final KnownFailure legacyFc1 = new LegacyFlowchartCase1();

	protected final KnownFailure ruFailed = new RuFailed();

	protected final KnownFailure fc2 = new CorruptedData();
	protected final KnownFailure legacyFc2 = new LegacyFlowchartCase2();

	protected final KnownFailure fc3 = new FlowchartCase3();

	protected final KnownFailure piDisconnected = new PiDisconnected();
	protected final KnownFailure piProblem = new ProblemWithPi();
	protected final KnownFailure fedDisconnected = new FEDDisconnected();
	protected final KnownFailure fmmProblem = new FMMProblem();

	protected final KnownFailure fc5 = new FlowchartCase5();

	protected final FEROLFifoStuck ferolFifoStuck = new FEROLFifoStuck();

	// protected final KnownFailure fc6 = new FlowchartCase6();

	protected final KnownFailure ruStuckWaiting = new RuStuckWaiting();
	protected final KnownFailure ruStuckWaitingOther = new RuStuckWaitingOther();

	protected final KnownFailure b1 = new BugInFilterfarm();
	protected final KnownFailure b2 = new HLTProblem();
	protected final KnownFailure b3 = new LinkProblem();
	protected final KnownFailure b4 = new OnlyFedStoppedSendingData();
	protected final KnownFailure ruStuck = new RuStuck();

	protected final KnownFailure backpressureFromFerol = new BackpressureFromFerol();
	protected final KnownFailure backpressureFromEventBuilding = new BackpressureFromEventBuilding();
	protected final KnownFailure backpressureFromHlt = new BackpressureFromHlt();

	protected final UnidentifiedFailure unidentified = new UnidentifiedFailure();

	protected final List<SimpleLogicModule> allLMsUnderTest = new ArrayList<>();

	private static final Logger logger = Logger.getLogger(FlowchartCaseTestBase.class);

	public FlowchartCaseTestBase() {


		Properties properties = new Properties();
		properties.setProperty(Setting.EXPERT_LOGIC_DEADTIME_BACKPRESSURE_FED.getKey(),"2");
		properties.setProperty(Setting.EXPERT_LOGIC_DEADTIME_THESHOLD_FED.getKey(),"2");
		properties.setProperty(Setting.EXPERT_LOGIC_BACKPRESSUREFROMHLT_THRESHOLD_BUS.getKey(),".3");
		properties.setProperty(Setting.EXPERT_LOGIC_EVM_FEW_EVENTS.getKey(),"100");

		allLMsUnderTest.add(fc3);

		allLMsUnderTest.add(piDisconnected);
		allLMsUnderTest.add(piProblem);
		allLMsUnderTest.add(fedDisconnected);
		allLMsUnderTest.add(fmmProblem);

		allLMsUnderTest.add(fc5);

		allLMsUnderTest.add(b1);
		allLMsUnderTest.add(b2);
		allLMsUnderTest.add(b3);
		allLMsUnderTest.add(b4);
		allLMsUnderTest.add(ruStuck);
		allLMsUnderTest.add(ruStuckWaiting);
		allLMsUnderTest.add(ruStuckWaitingOther);
		allLMsUnderTest.add(fc1);
		allLMsUnderTest.add(fc2);

		allLMsUnderTest.add(legacyFc1);
		allLMsUnderTest.add(legacyFc2);

		allLMsUnderTest.add(ferolFifoStuck);
		allLMsUnderTest.add(ruFailed);


		allLMsUnderTest.add(backpressureFromFerol);
		allLMsUnderTest.add(backpressureFromEventBuilding);
		allLMsUnderTest.add(backpressureFromHlt);


		allLMsUnderTest.add(unidentified);

		HashSet<LogicModule> logicModules = new HashSet<>();

		for (LogicModuleRegistry lm : LogicModuleRegistry.values()) {
			if (lm.getLogicModule() != null && lm.getLogicModule() instanceof KnownFailure) {
				//logicModules.add(lm.getLogicModule().getClass().getSimpleName());
			}

		}

		for(LogicModule lm: allLMsUnderTest){

			if(lm!= null &&!( lm instanceof UnidentifiedFailure)){
				logicModules.add(lm);
			}

			if (lm != null && lm instanceof Parameterizable) {
				((Parameterizable) lm).parametrize(properties);
			}
		}
		unidentified.setKnownFailureClasses(logicModules);
	}

	protected void assertSatisfiedLogicModules(DAQ snapshot, SimpleLogicModule... expected) {



		for(RU ru: snapshot.getRus()){
			if(ru.isEVM() && ru.getRate() > 0){
				results.put(NoRateWhenExpected.class.getSimpleName(),new Output(false));
			}
		}

		List<SimpleLogicModule> expectedList = Arrays.asList(expected);

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		TimeZone tz = TimeZone.getTimeZone("Europe/Zurich");
		df.setTimeZone(tz);
		logger.info("Running LMs on snapshot " + df.format(new Date(snapshot.getLastUpdate())));
		for (SimpleLogicModule lm : allLMsUnderTest) {

			if (expectedList.contains(lm)) {

				logger.info("Asserting target LM " + lm.getClass().getSimpleName() + " is satisfied");
				assertEqualsAndUpdateResults(true, lm, snapshot);
			} else {
				logger.debug("Asserting other LM " + lm.getClass().getSimpleName() + " is not satisfied");
				assertEqualsAndUpdateResults(false, lm, snapshot);
			}
		}
		logger.info("---");

	}

	protected void assertOnlyOneIsSatisified(SimpleLogicModule satisfied, DAQ snapshot) {
		assertSatisfiedLogicModules(snapshot, satisfied);

	}

	/**
	 * method to assert that the given logic module has found the expected result. This is used iteratively check the
	 * chain of reasoning (where later modules potentially depend on the results of earlier ones) at each step.
	 */
	protected void assertEqualsAndUpdateResults(boolean expected, SimpleLogicModule logicModule, DAQ snapshot) {
		boolean result = logicModule.satisfied(snapshot, results);
		if (result != expected) {
			String output = "";
			if (logicModule instanceof ContextLogicModule) {
				ContextLogicModule clm = (ContextLogicModule) logicModule;
				output = clm.getDescriptionWithContext();
			} else {
				output = logicModule.getDescription();
			}

			logger.info("'" + result + "' is unexpected result of LM '" + logicModule.getName() + "': " + output);
		}
		Assert.assertEquals("unexpected result for module " + logicModule.getClass().getSimpleName() + ":", expected,
				result);
		if (result) {
			String output = null;
			if (logicModule instanceof KnownFailure) {
				KnownFailure kf = (KnownFailure) logicModule;
				output = kf.getDescriptionWithContext();
			} else {
				output = logicModule.getDescription();
			}
			TimeZone tz = TimeZone.getTimeZone("UTC");
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			df.setTimeZone(tz);
			String date = df.format(new Date(snapshot.getLastUpdate()));
			logger.info("Output of LM '" + logicModule.getName() + "' for " + date + ":\n" + output);
		}
		Assert.assertEquals(logicModule.getClass().getSimpleName() + " is expected to return " + expected, expected,
				result);
		results.put(logicModule.getClass().getSimpleName(), new Output(result));
	}

	@Before
	public void cleanResult() {
		results.clear();

		// put results of prerequisite tests by hand
		// (as opposed to get them from a series of snapshots
		// which introduces a dependency on other tests)
		results.put(StableBeams.class.getSimpleName(), new Output(true));
		results.put(NoRateWhenExpected.class.getSimpleName(), new Output(true));
		results.put(ExpectedRate.class.getSimpleName(), new Output(true));
		results.put(FedDeadtimeDueToDaq.class.getSimpleName(), new Output(true));
		results.put(TmpUpgradedFedProblem.class.getSimpleName(), new Output(true));

		}

	/**
	 * method to load a deserialize a snapshot given a file name
	 */
	public static DAQ getSnapshot(String fname) throws URISyntaxException {

		StructureSerializer serializer = new StructureSerializer();

		URL url = KnownFailure.class.getResource(fname);

		File file = new File(url.toURI());

		return serializer.deserialize(file.getAbsolutePath());
	}

	public void print(KnownFailure lm){
		System.out.println(">"+lm.getName() + ": \n>" + lm.getDescriptionWithContext());
	}

}
