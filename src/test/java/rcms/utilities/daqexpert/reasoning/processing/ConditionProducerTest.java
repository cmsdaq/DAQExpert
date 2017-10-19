package rcms.utilities.daqexpert.reasoning.processing;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqexpert.events.ConditionEvent;
import rcms.utilities.daqexpert.events.collectors.EventRegister;
import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.ComparatorLogicModule;
import rcms.utilities.daqexpert.reasoning.logic.comparators.DAQStateComparator;
import rcms.utilities.daqexpert.reasoning.logic.comparators.TCDSStateComparator;

public class ConditionProducerTest {

	private static ConditionProducer conditionProducer;

	private static ComparatorLogicModule checker;

	private static Calendar calendar;

	@Test
	public void testDaqStateComparator() {
		conditionProducer = new ConditionProducer();
		conditionProducer.setEventRegister(new EventRegisterStub());
		checker = new DAQStateComparator();
		calendar = Calendar.getInstance();
		testCase();
	}

	@Test
	public void testTcdsStateComparator() {
		conditionProducer = new ConditionProducer();
		conditionProducer.setEventRegister(new EventRegisterStub());
		checker = new TCDSStateComparator();
		calendar = Calendar.getInstance();
		testCase();
	}

	public void testCase() {
		calendar.setTime(DatatypeConverter.parseDateTime("2017-03-12T00:00:00Z").getTime());

		/* Transition 1 */
		Pair<Boolean, Condition> r1 = testStep("s1");
		Assert.assertTrue(r1.getLeft());
		Condition c1 = r1.getRight();

		testProducedCondition(c1, "s1");
		Assert.assertFalse(testStep("s1").getLeft());
		Assert.assertFalse(testStep("s1").getLeft());

		/* Transition 2 */
		Pair<Boolean, Condition> r2 = testStep("s2");
		Assert.assertTrue(r2.getLeft());
		Condition c2 = r2.getRight();

		testProducedCondition(c2, "s2");
		Assert.assertNotNull(c1.getEnd());

		/* Problem */
		Pair<Boolean, Condition> r3 = testStep("s2", true);
		Assert.assertTrue(r3.getLeft());
		Condition c3 = r3.getRight();

		testProducedCondition(c3, "undefined");
		Assert.assertNotNull(c2.getEnd());

	}

	private void testProducedCondition(Condition c, String expectedTitle) {
		Assert.assertNotNull(c);
		Assert.assertNotNull(c.getStart());
		Assert.assertNull(c.getEnd());
		Assert.assertTrue(c.isShow());
		Assert.assertEquals(expectedTitle, c.getTitle());
	}

	private Pair<Boolean, Condition> testStep(String state) {
		return testStep(state, false);
	}

	private Pair<Boolean, Condition> testStep(String state, boolean fakeProblem) {

		DAQ daq;
		Date date = calendar.getTime();
		calendar.add(Calendar.SECOND, 1);
		if (fakeProblem) {
			daq = null;
		} else {
			daq = new DAQ();
			daq.setDaqState(state);

			SubSystem tcds = new SubSystem();
			tcds.setStatus(state);
			List<SubSystem> subSystems = new ArrayList<>();
			subSystems.add(tcds);
			tcds.setName("TCDS");
			daq.setSubSystems(subSystems);
			daq.setLastUpdate(date.getTime());
		}

		boolean value = checker.compare(daq);

		Pair<Boolean, Condition> produced = conditionProducer.produce(checker, value, date);

		return produced;

	}

}

class EventRegisterStub implements EventRegister {

	@Override
	public void registerBegin(Condition condition) {

	}

	@Override
	public void registerEnd(Condition condition) {

	}

	@Override
	public void registerUpdate(Condition condition) {

	}

	@Override
	public List<ConditionEvent> getEvents() {
		return null;
	}

}
