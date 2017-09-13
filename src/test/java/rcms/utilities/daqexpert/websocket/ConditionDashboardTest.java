package rcms.utilities.daqexpert.websocket;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.bind.DatatypeConverter;

import org.junit.Assert;
import org.junit.Test;

import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

public class ConditionDashboardTest {

	Date d1 = DatatypeConverter.parseDateTime("2017-03-09T10:00:00Z").getTime();
	Date d2 = DatatypeConverter.parseDateTime("2017-03-09T10:00:01Z").getTime();
	Date d3 = DatatypeConverter.parseDateTime("2017-03-09T10:00:02Z").getTime();
	Date d4 = DatatypeConverter.parseDateTime("2017-03-09T10:00:03Z").getTime();
	Date d5 = DatatypeConverter.parseDateTime("2017-03-09T10:00:04Z").getTime();

	Condition c1 = generateCondition("c1", ConditionPriority.DEFAULTT, LogicModuleRegistry.FEDDeadtime, d1);
	Condition c2 = generateCondition("c2", ConditionPriority.DEFAULTT, LogicModuleRegistry.FEDDeadtime, d1);
	Condition c3 = generateCondition("c3", ConditionPriority.DEFAULTT, LogicModuleRegistry.FEDDeadtime, d1);
	Condition c4 = generateCondition("c4", ConditionPriority.DEFAULTT, LogicModuleRegistry.OutOfSequenceData, d1);
	Condition c5 = generateCondition("c5", ConditionPriority.IMPORTANT, LogicModuleRegistry.FEDDeadtime, d1);
	Condition c6 = generateCondition("c6", ConditionPriority.CRITICAL, LogicModuleRegistry.FEDDeadtime, d2);
	Condition c7 = generateCondition("c6", ConditionPriority.CRITICAL, LogicModuleRegistry.FEDDeadtime, d3);

	@Test
	public void dominantBasedOnPriorityTest() {

		ConditionDashboard conditionDashboard = new ConditionDashboard(5);
		Set<Condition> conditions = new LinkedHashSet<Condition>();

		conditions.add(c1);
		conditions.add(c5);
		conditionDashboard.update(conditions);

		Assert.assertEquals(c5, conditionDashboard.getCurrentCondition());
		Assert.assertEquals(1, conditionDashboard.getConditionsWithoutDominatingCondition().size());

		conditions.clear();
		conditions.add(c5);
		conditions.add(c1);
		conditionDashboard.update(conditions);

		Assert.assertEquals(c5, conditionDashboard.getCurrentCondition());
		Assert.assertEquals(1, conditionDashboard.getConditionsWithoutDominatingCondition().size());

	}

	@Test
	public void dominantBasedOnUsefulnessTest() {

		ConditionDashboard conditionDashboard = new ConditionDashboard(5);
		Set<Condition> conditions = new LinkedHashSet<Condition>();

		conditions.add(c6);
		conditions.add(c7);
		conditionDashboard.update(conditions);

		Assert.assertEquals(c7, conditionDashboard.getCurrentCondition());
		Assert.assertEquals(1, conditionDashboard.getConditionsWithoutDominatingCondition().size());

		conditions.clear();
		conditions.add(c7);
		conditions.add(c6);
		conditionDashboard.update(conditions);

		Assert.assertEquals(c7, conditionDashboard.getCurrentCondition());
		Assert.assertEquals(1, conditionDashboard.getConditionsWithoutDominatingCondition().size());

	}

	@Test
	public void dominantBasedOnStartDateTest() {

		ConditionDashboard conditionDashboard = new ConditionDashboard(5);
		Set<Condition> conditions = new LinkedHashSet<Condition>();

		conditions.add(c3);
		conditions.add(c4);
		conditionDashboard.update(conditions);

		Assert.assertEquals(c4, conditionDashboard.getCurrentCondition());
		Assert.assertEquals(1, conditionDashboard.getConditionsWithoutDominatingCondition().size());

		conditions.clear();
		conditions.add(c4);
		conditions.add(c3);
		conditionDashboard.update(conditions);

		Assert.assertEquals(c4, conditionDashboard.getCurrentCondition());
		Assert.assertEquals(1, conditionDashboard.getConditionsWithoutDominatingCondition().size());

	}

	@Test
	public void simpleScenarioTest() {

		ConditionDashboard conditionDashboard = new ConditionDashboard(5);

		Set<Condition> conditions = new LinkedHashSet<Condition>();

		/* 1 Change */
		conditions.add(c1);
		conditions.add(c2);
		conditions.add(c3);
		conditionDashboard.update(conditions);

		/* 1 Result */
		Assert.assertEquals("Conditions same priority and date -> first", c1, conditionDashboard.getCurrentCondition());
		Assert.assertEquals(3, conditionDashboard.getCurrentConditions().size());
		Assert.assertEquals(2, conditionDashboard.getConditionsWithoutDominatingCondition().size());

		/* 2 Change */
		conditions.add(c4);
		conditionDashboard.update(conditions);

		/* 2 Result */
		Assert.assertEquals("Conditions same priority -> higher usefulness date", c4,
				conditionDashboard.getCurrentCondition());
		Assert.assertEquals(4, conditionDashboard.getCurrentConditions().size());
		Assert.assertEquals(3, conditionDashboard.getConditionsWithoutDominatingCondition().size());

		/* 3 Change */
		conditions.add(c5);
		conditionDashboard.update(conditions);

		/* 3 Result */
		Assert.assertEquals("Priority", c5, conditionDashboard.getCurrentCondition());
		Assert.assertEquals(5, conditionDashboard.getCurrentConditions().size());
		Assert.assertEquals(4, conditionDashboard.getConditionsWithoutDominatingCondition().size());

		/* 4 Change */
		c5.setEnd(d3);
		conditions.remove(c5);
		conditionDashboard.update(conditions);

		/* 4 Result */
		Assert.assertEquals("Return to last", c4, conditionDashboard.getCurrentCondition());
		Assert.assertEquals(5, conditionDashboard.getCurrentConditions().size());
		Assert.assertEquals(4, conditionDashboard.getConditionsWithoutDominatingCondition().size());

		/* 5 Change */
		conditions.add(c6);
		conditionDashboard.update(conditions);

		/* 5 Result */
		Assert.assertEquals(c6, conditionDashboard.getCurrentCondition());
		Assert.assertEquals(5, conditionDashboard.getCurrentConditions().size());
		Assert.assertEquals(4, conditionDashboard.getConditionsWithoutDominatingCondition().size());

		/* 6 no more conditions */
		c6.setEnd(d4);
		conditions.clear();
		conditionDashboard.update(conditions);

		/* 6 Only recent conditions */
		Assert.assertEquals(null, conditionDashboard.getCurrentCondition());
		Assert.assertEquals(5, conditionDashboard.getCurrentConditions().size());
		Assert.assertEquals(5, conditionDashboard.getConditionsWithoutDominatingCondition().size());

		Assert.assertNotNull(conditionDashboard.toString());

	}

	public Condition generateCondition(String title, ConditionPriority priority, LogicModuleRegistry lm, Date start) {
		Condition condition = new Condition();
		condition.setId(id++);
		condition.setTitle(title);
		condition.setPriority(priority);
		condition.setLogicModule(lm);
		condition.setShow(true);
		condition.setStart(start);
		return condition;
	}

	private static Long id = 0L;

}
