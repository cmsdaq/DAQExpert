package rcms.utilities.daqexpert.persistence;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import rcms.utilities.daqexpert.reasoning.base.LogicModule;

public class LogicModuleRegistryTest {

	/*
	 * If there is any order misconfiguration, more specifically the run order
	 * number is duplicated this test will allow to detect the problem on unit
	 * testing - not during the deployment.
	 */
	@Test
	public void orderConfiguredProperlyTest() {
		List<LogicModule> a = LogicModuleRegistry.getModulesInRunOrder();
		Assert.assertNotNull(a);
		Assert.assertTrue(a.size() > 0);

		Integer orderOfRunOngoing=-1, orderOfExpectedRate=-1, orderOfFedDeadtime=-1, orderOfFedDeadtimeDueToDaq=-1, orderOfBpFromHlt=-1, orderOfHltCpuLoad=-1;

		for(LogicModule lm: a){
			LogicModuleRegistry lmr = lm.getLogicModuleRegistry();
			switch(lmr){
				case RunOngoing:
					orderOfRunOngoing = a.indexOf(lm);
					break;
				case ExpectedRate:
					orderOfExpectedRate = a.indexOf(lm);
					break;
				case FEDDeadtime:
					orderOfFedDeadtime = a.indexOf(lm);
					break;
				case FedDeadtimeDueToDaq:
					orderOfFedDeadtimeDueToDaq = a.indexOf(lm);
					break;
				case BackpressureFromHlt:
					orderOfBpFromHlt = a.indexOf(lm);
					break;
				case HltCpuLoad:
					orderOfHltCpuLoad = a.indexOf(lm);
					break;
			}
		}

		System.out.println("Ordered: ");
		a.stream().map(f -> f.getName() + " - " + f.getClass().getSimpleName()).forEach(System.out::println);
		//orderedModules.values().stream().filter(f->f.getLogicModule() !=null).map(f->f.getLogicModule().getName() + ": "+f.getLogicModule().getClass().getSimpleName()).forEach(System.out::println);


		Assert.assertTrue(orderOfRunOngoing >=0);
		Assert.assertTrue(orderOfRunOngoing < orderOfExpectedRate);
		Assert.assertTrue(orderOfExpectedRate < orderOfFedDeadtime);
		Assert.assertTrue(orderOfFedDeadtime < orderOfFedDeadtimeDueToDaq);
		Assert.assertTrue(orderOfFedDeadtimeDueToDaq < orderOfBpFromHlt);
		Assert.assertTrue(orderOfBpFromHlt < orderOfHltCpuLoad);

	}

}
