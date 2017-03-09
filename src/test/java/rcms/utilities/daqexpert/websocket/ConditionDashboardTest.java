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

	@Test
	public void simpleTest() {

		ConditionDashboard conditionSelector = new ConditionDashboard(5);

		Date d1 = DatatypeConverter.parseDateTime("2017-03-09T10:00:00Z").getTime();
		Date d2 = DatatypeConverter.parseDateTime("2017-03-09T10:00:01Z").getTime();
		Date d3 = DatatypeConverter.parseDateTime("2017-03-09T10:00:02Z").getTime();
		Date d4 = DatatypeConverter.parseDateTime("2017-03-09T10:00:03Z").getTime();
		Date d5 = DatatypeConverter.parseDateTime("2017-03-09T10:00:04Z").getTime();

		Condition c1 = generateCondition("c1", ConditionPriority.DEFAULTT, d1);
		Condition c2 = generateCondition("c2", ConditionPriority.DEFAULTT, d1);
		Condition c3 = generateCondition("c3", ConditionPriority.DEFAULTT, d1);
		Condition c4 = generateCondition("c4", ConditionPriority.DEFAULTT, d2);
		Condition c5 = generateCondition("c5", ConditionPriority.IMPORTANT, d1);
		Condition c6 = generateCondition("c6", ConditionPriority.CRITICAL, d2);

		Set<Condition> conditions = new LinkedHashSet<Condition>();

		/* 1 Change */
		conditions.add(c1);
		conditions.add(c2);
		conditions.add(c3);
		conditionSelector.update(conditions);

		/* 1 Result */
		Assert.assertEquals("Conditions same priority and date -> first", c1, conditionSelector.getCurrentCondition());
		Assert.assertEquals(3, conditionSelector.getCurrentConditions().size());
		Assert.assertEquals(2, conditionSelector.getFilteredCurrentConditions().size());

		/* 2 Change */
		conditions.add(c4);
		conditionSelector.update(conditions);

		/* 2 Result */
		Assert.assertEquals("Conditions same priority -> last start date", c4, conditionSelector.getCurrentCondition());
		Assert.assertEquals(4, conditionSelector.getCurrentConditions().size());
		Assert.assertEquals(3, conditionSelector.getFilteredCurrentConditions().size());

		/* 3 Change */
		conditions.add(c5);
		conditionSelector.update(conditions);

		/* 3 Result */
		Assert.assertEquals("Priority", c5, conditionSelector.getCurrentCondition());
		Assert.assertEquals(5, conditionSelector.getCurrentConditions().size());
		Assert.assertEquals(4, conditionSelector.getFilteredCurrentConditions().size());

		/* 4 Change */
		c5.setEnd(d3);
		conditions.remove(c5);
		conditionSelector.update(conditions);

		/* 4 Result */
		Assert.assertEquals("Return to last", c4, conditionSelector.getCurrentCondition());
		Assert.assertEquals(5, conditionSelector.getCurrentConditions().size());
		Assert.assertEquals(4, conditionSelector.getFilteredCurrentConditions().size());

		/* 5 Change */
		conditions.add(c6);
		conditionSelector.update(conditions);

		/* 5 Result */
		Assert.assertEquals(c6, conditionSelector.getCurrentCondition());
		Assert.assertEquals(5, conditionSelector.getCurrentConditions().size());
		Assert.assertEquals(4, conditionSelector.getFilteredCurrentConditions().size());

		/* 6 no more conditions */
		c6.setEnd(d4);
		conditions.clear();
		conditionSelector.update(conditions);

		/* 6 Only recent conditions */
		Assert.assertEquals(null, conditionSelector.getCurrentCondition());
		Assert.assertEquals(5, conditionSelector.getCurrentConditions().size());
		Assert.assertEquals(5, conditionSelector.getFilteredCurrentConditions().size());

	}

	public Condition generateCondition(String title, ConditionPriority priority, Date start) {
		Condition condition = new Condition();
		condition.setId(id++);
		condition.setTitle(title);
		condition.setPriority(priority);
		condition.setLogicModule(LogicModuleRegistry.FEDDeadtime);
		condition.setShow(true);
		condition.setStart(start);
		return condition;
	}

	private static Long id = 0L;

}
