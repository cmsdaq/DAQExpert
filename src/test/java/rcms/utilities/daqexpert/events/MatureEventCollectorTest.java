package rcms.utilities.daqexpert.events;

import org.junit.Assert;
import org.junit.Test;

import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

public class MatureEventCollectorTest {

	@Test
	public void comparatorAreMatureImmediatelyTest() {

		MatureEventCollector c = new MatureEventCollector();

		Condition c1 = generate(LogicModuleRegistry.LHCBeamModeComparator);
		c.registerBegin(c1);

		Assert.assertEquals(1, c.getEvents().size());
	}

	@Test
	public void simpleEventsMustBeMatureTest() {

		MatureEventCollector c = new MatureEventCollector();

		Condition c1 = generate(LogicModuleRegistry.NoRate);
		c.registerBegin(c1);

		Assert.assertEquals(0, c.getEvents().size());

		c1.setMature(true);

		Assert.assertEquals(1, c.getEvents().size());

	}

	private Condition generate(LogicModuleRegistry logicModule) {
		Condition c = new Condition();
		c.setLogicModule(logicModule);
		c.setTitle("test condition");
		c.setShow(true);
		c.setPriority(ConditionPriority.IMPORTANT);
		return c;
	}

}
