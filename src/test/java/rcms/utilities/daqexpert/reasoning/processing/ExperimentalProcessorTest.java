package rcms.utilities.daqexpert.reasoning.processing;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import groovy.util.ResourceException;
import groovy.util.ScriptException;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.LogicModule;

public class ExperimentalProcessorTest {

	public final static Logger logger = Logger.getLogger(ExperimentalProcessor.class);

	@Test
	public void runExperimentalLMTest() throws IOException, ResourceException, ScriptException {
		ExperimentalProcessor experimentalProcessor = new ExperimentalProcessor("./experimental/");
		experimentalProcessor.loadExperimentalLogicModules();

		DAQ daq = new DAQ();
		daq.setLhcBeamMode("AA");

		DAQ daqSpy = Mockito.spy(daq);
		HashMap<String, Boolean> checkerResultMap = new HashMap<>();
		experimentalProcessor.runLogicModules(daqSpy, checkerResultMap);
		Mockito.verify(daqSpy, Mockito.times(2)).getLhcBeamMode();
	}

	@Test
	public void resultOfExperimentalLMTest() throws IOException, ResourceException, ScriptException {
		ExperimentalProcessor experimentalProcessor = new ExperimentalProcessor("./experimental/");
		experimentalProcessor.loadExperimentalLogicModules();

		DAQ daq = Mockito.spy(new DAQ());
		daq.setLhcBeamMode("STABLE BEAMS");

		HashMap<String, Boolean> checkerResultMap = new HashMap<>();
		List<Pair<LogicModule, Boolean>> a = experimentalProcessor.runLogicModules(daq, checkerResultMap);

		Assert.assertEquals(2, a.size());

		//TODO: more checks
	}

	@Test
	public void discoveringScriptsTest() throws IOException, ResourceException, ScriptException {
		ExperimentalProcessor experimentalProcessor = new ExperimentalProcessor("./experimental/");

		List<Class<LogicModule>> a = experimentalProcessor.getScripts();
		Assert.assertEquals(2, a.size());

		@SuppressWarnings("rawtypes")
		Class LM2 = a.get(0);
		@SuppressWarnings("rawtypes")
		Class LM1 = a.get(1);

		Assert.assertEquals("StableBeams", LM1.getSimpleName());
		Assert.assertEquals("NonStableBeams", LM2.getSimpleName());
	}

}
