package rcms.utilities.daqexpert.websocket;

import java.util.*;
import java.util.stream.Collectors;

import javax.xml.bind.DatatypeConverter;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;
import rcms.utilities.daqexpert.reasoning.causality.CausalityManager;
import rcms.utilities.daqexpert.reasoning.causality.CausalityNode;

public class ConditionDashboardTest {

	private static Long id = 0L;

	static Condition c1 = generateCondition("c1", LogicModuleRegistry.VeryHighTcdsInputRate);
	static Condition c2 = generateCondition("c2", LogicModuleRegistry.RateTooHigh);
	static Condition c3 = generateCondition("c3", LogicModuleRegistry.FlowchartCase5);
	static Condition c4 = generateCondition("c4", LogicModuleRegistry.NoRateWhenExpected);

	@BeforeClass
	public static void prepare(){

		List<Condition> conditionList = new ArrayList<>();
		conditionList.add(c1);
		conditionList.add(c2);
		conditionList.add(c3);
		conditionList.add(c4);

		conditionList.stream().forEach(c -> c.getLogicModule().getLogicModule().declareRelations());
		CausalityManager cm = new CausalityManager();
		Set<CausalityNode> a = conditionList.stream().map(c->c.getLogicModule().getLogicModule()).collect(Collectors.toSet());
		cm.transformToCanonical(a);
	}


	@Test
	public void simpleScenarioTest() {

		ConditionDashboard conditionDashboard = new ConditionDashboard(5);

		Set<Condition> conditions = new LinkedHashSet<Condition>();

		/* 1 Change - new condition(s) start/end */
		conditions.add(c4);
		conditionDashboard.update(conditions);

		/* 1 Result */
		Assert.assertEquals("Conditions same priority and date -> first", c4, conditionDashboard.getCurrentCondition());
		Assert.assertEquals(1, conditionDashboard.getCurrentConditions().size());
		Assert.assertEquals(0, conditionDashboard.getConditionsWithoutDominatingCondition().size());

		/* 2 Change - new condition(s) start/end */
		conditions.add(c3);
		conditionDashboard.update(conditions);

		/* 2 Result */
		Assert.assertEquals( c3, conditionDashboard.getCurrentCondition());
		Assert.assertEquals(2, conditionDashboard.getCurrentConditions().size());
		Assert.assertEquals(1, conditionDashboard.getConditionsWithoutDominatingCondition().size());

		/* 3 Change - new condition(s) start/end */
		conditions.add(c1);
		conditions.add(c2);
		conditionDashboard.update(conditions);

		/* 3 Result */
		Assert.assertEquals( c1, conditionDashboard.getCurrentCondition());
		Assert.assertEquals(4, conditionDashboard.getCurrentConditions().size());
		Assert.assertEquals(3, conditionDashboard.getConditionsWithoutDominatingCondition().size());

		/* 4 Change - new condition(s) start/end */
		c1.setEnd(new Date());
		conditions.remove(c1);
		conditionDashboard.update(conditions);

		/* 4 Result */
		Assert.assertEquals( c2, conditionDashboard.getCurrentCondition());
		Assert.assertEquals(4, conditionDashboard.getCurrentConditions().size());
		Assert.assertEquals(3, conditionDashboard.getConditionsWithoutDominatingCondition().size());


		/* 4 Change - new condition(s) start/end */
		c2.setEnd(new Date());
		c4.setEnd(new Date());
		conditions.remove(c2);
		conditions.remove(c4);
		conditionDashboard.update(conditions);

		/* 5 Result */
		Assert.assertEquals(c3, conditionDashboard.getCurrentCondition());
		Assert.assertEquals(4, conditionDashboard.getCurrentConditions().size());
		Assert.assertEquals(3, conditionDashboard.getConditionsWithoutDominatingCondition().size());

		/* 6 no more conditions */
		conditions.clear();
		conditionDashboard.update(conditions);

		/* 6 Only recent conditions */
		Assert.assertEquals(c3, conditionDashboard.getCurrentCondition());
		Assert.assertEquals(4, conditionDashboard.getCurrentConditions().size());
		Assert.assertEquals(3, conditionDashboard.getConditionsWithoutDominatingCondition().size());

		Assert.assertNotNull(conditionDashboard.toString());

	}

	public static Condition generateCondition(String title, LogicModuleRegistry lm) {
		Condition condition = new Condition();
		condition.setId(id++);
		condition.setTitle(title);
		condition.setPriority(ConditionPriority.DEFAULTT);
		condition.setLogicModule(lm);
		condition.setShow(true);
		condition.setStart(new Date());
		return condition;
	}


}
