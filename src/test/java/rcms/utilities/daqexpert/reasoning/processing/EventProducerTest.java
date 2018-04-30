package rcms.utilities.daqexpert.reasoning.processing;

import java.util.Date;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.processing.context.ContextHandler;
import rcms.utilities.daqexpert.reasoning.base.ComparatorLogicModule;
import rcms.utilities.daqexpert.reasoning.base.LogicModule;
import rcms.utilities.daqexpert.reasoning.base.SimpleLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRate;
import rcms.utilities.daqexpert.reasoning.logic.comparators.LHCBeamModeComparator;

@Ignore
// TODO: update test after refactor
public class EventProducerTest {

	Logger logger = Logger.getLogger(EventProducerTest.class);

	Date t1 = DatatypeConverter.parseDateTime("2016-10-13T15:30:00Z").getTime();
	Date t2 = DatatypeConverter.parseDateTime("2016-10-13T15:31:00Z").getTime();
	Date t3 = DatatypeConverter.parseDateTime("2016-10-13T15:32:00Z").getTime();
	Date t4 = DatatypeConverter.parseDateTime("2016-10-13T15:33:00Z").getTime();

	@Test
	public void eventProducingTest() {

		ConditionProducer eventProducer = Mockito.spy(new ConditionProducer());

		SimpleLogicModule checker = new NoRate();
		// checker.setLogicModuleRegistry(LogicModuleRegistry.NoRate);
		boolean value = false;
		Pair<Boolean, Condition> a = eventProducer.produce(checker, value, t1);
		Assert.assertTrue(a.getLeft());
		Assert.assertNotNull(a);
		Assert.assertEquals(t1, a.getRight().getStart());
		Assert.assertEquals(null, a.getRight().getEnd());
		Assert.assertEquals(0, a.getRight().getDuration());

		Pair<Boolean, Condition> b = eventProducer.produce(checker, !value, t2);
		Assert.assertTrue(b.getLeft());
		Assert.assertTrue(a.getRight() != b.getRight());

		Assert.assertEquals(t1, a.getRight().getStart());
		Assert.assertEquals(t2, a.getRight().getEnd());
		Assert.assertEquals(60000, a.getRight().getDuration());

		Assert.assertEquals(t2, b.getRight().getStart());
		Assert.assertEquals(null, b.getRight().getEnd());
		Assert.assertEquals(0, b.getRight().getDuration());

		Mockito.verify(eventProducer, Mockito.times(2)).produce(Mockito.any(SimpleLogicModule.class),
				Mockito.anyBoolean(), Mockito.any(Date.class));

		Mockito.verify(eventProducer, Mockito.times(2)).finishOldAddNew(Mockito.notNull(LogicModule.class),
				Mockito.anyString(), Mockito.anyBoolean(), Mockito.notNull(Date.class),
				Mockito.notNull(ConditionPriority.class), Mockito.isNull(ContextHandler.class));
	}

	@Test
	public void eventProducintTestNoChange() {

		ConditionProducer eventProducer = Mockito.spy(new ConditionProducer());

		SimpleLogicModule checker = new NoRate();
		boolean value = false;
		Date date = DatatypeConverter.parseDateTime("2016-10-13T15:30:00Z").getTime();
		Pair<Boolean, Condition> a = eventProducer.produce(checker, value, date);
		Assert.assertTrue(a.getLeft());
		Assert.assertNotNull(a);
		Assert.assertEquals(date, a.getRight().getStart());
		Assert.assertEquals(null, a.getRight().getEnd());
		Assert.assertEquals(0, a.getRight().getDuration());

		Date date2 = DatatypeConverter.parseDateTime("2016-10-13T15:40:00Z").getTime();
		Pair<Boolean, Condition> b = eventProducer.produce(checker, value, date2);
		Assert.assertFalse(b.getLeft());
		Assert.assertTrue(a.getRight() == b.getRight());

		Assert.assertEquals(date, a.getRight().getStart());
		Assert.assertEquals(null, a.getRight().getEnd());
		Assert.assertEquals(0, a.getRight().getDuration());

		Assert.assertEquals(date, b.getRight().getStart());
		Assert.assertEquals(null, b.getRight().getEnd());
		Assert.assertEquals(0, b.getRight().getDuration());
	}

	@Test
	public void comparatorLMTest() {

		ConditionProducer eventProducer = Mockito.spy(new ConditionProducer());

		ComparatorLogicModule checker = new LHCBeamModeComparator();
		Pair<Boolean, Condition> a = eventProducer.produce(checker, true, t2);

		Assert.assertTrue(a.getLeft());
		Assert.assertNotNull(a);
		Assert.assertEquals(t2, a.getRight().getStart());
		Assert.assertEquals(null, a.getRight().getEnd());
		Assert.assertEquals(0, a.getRight().getDuration());

		Assert.assertEquals(0, eventProducer.getFinishedThisRound().size());
		Assert.assertEquals(1, eventProducer.getUnfinished().size());

		Mockito.verify(eventProducer, Mockito.times(2)).finishOldAddNew(Mockito.notNull(LogicModule.class),
				Mockito.anyString(), Mockito.anyBoolean(), Mockito.notNull(Date.class),
				Mockito.notNull(ConditionPriority.class), Mockito.any(ContextHandler.class));

		// this should not change anything
		Pair<Boolean, Condition> b = eventProducer.produce(checker, false, t3);
		Assert.assertFalse(b.getLeft());
		Assert.assertNull(b.getRight());
		Assert.assertTrue(a.getLeft());
		Assert.assertNotNull(a);
		Assert.assertEquals(t2, a.getRight().getStart());
		Assert.assertEquals(null, a.getRight().getEnd());
		Assert.assertEquals(0, a.getRight().getDuration());

		Assert.assertEquals(0, eventProducer.getFinishedThisRound().size());
		Assert.assertEquals(1, eventProducer.getUnfinished().size());
		Mockito.verify(eventProducer, Mockito.times(2)).finishOldAddNew(Mockito.notNull(LogicModule.class),
				Mockito.anyString(), Mockito.anyBoolean(), Mockito.notNull(Date.class),
				Mockito.notNull(ConditionPriority.class), Mockito.any(ContextHandler.class));

		// this should change
		Pair<Boolean, Condition> c = eventProducer.produce(checker, true, t4);
		Assert.assertEquals(t2, a.getRight().getStart());
		Assert.assertEquals(t4, a.getRight().getEnd()); // FIXME why t2-t4
		Assert.assertEquals(120000, a.getRight().getDuration());

		Assert.assertEquals(t4, c.getRight().getStart());
		Assert.assertEquals(null, c.getRight().getEnd());
		Assert.assertEquals(0, c.getRight().getDuration());

		logger.info(eventProducer.getFinishedThisRound());
		Assert.assertEquals(1, eventProducer.getFinishedThisRound().size());
		Assert.assertEquals(1, eventProducer.getUnfinished().size());

		Mockito.verify(eventProducer, Mockito.times(4)).finishOldAddNew(Mockito.notNull(LogicModule.class),
				Mockito.anyString(), Mockito.anyBoolean(), Mockito.notNull(Date.class),
				Mockito.notNull(ConditionPriority.class), Mockito.any(ContextHandler.class));
	}

	@Test
	public void finishEventTest() {

		ConditionProducer eventProducer = Mockito.spy(new ConditionProducer());

		ComparatorLogicModule checker = new LHCBeamModeComparator();
		Pair<Boolean, Condition> a = eventProducer.produce(checker, true, t2);

		Assert.assertEquals(0, eventProducer.getFinishedThisRound().size());
		Assert.assertEquals(1, eventProducer.getUnfinished().size());
		Assert.assertEquals(null, a.getRight().getEnd());

		// eventProducer.finish(t3);

		Assert.assertEquals(0, eventProducer.getFinishedThisRound().size());
		Assert.assertEquals(1, eventProducer.getUnfinished().size());
		Assert.assertEquals(t3, a.getRight().getEnd());
	}

	@Test
	public void unchangedCompareLMTest() {

		ConditionProducer eventProducer = Mockito.spy(new ConditionProducer());

		ComparatorLogicModule checker = new LHCBeamModeComparator();
		Pair<Boolean, Condition> a = eventProducer.produce(checker, false, t2);

		Assert.assertEquals(0, eventProducer.getFinishedThisRound().size());

		// TODO: check this assertions
		// Assert.assertEquals(1, eventProducer.getUnfinished().size());
		// Assert.assertNull(a.getRight());

		// eventProducer.finish(t3);

		Assert.assertEquals(0, eventProducer.getFinishedThisRound().size());
		Assert.assertEquals(0, eventProducer.getUnfinished().size());
		// Assert.assertEquals(t3, a.getRight().getEnd());
	}

}
