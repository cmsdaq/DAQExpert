package rcms.utilities.daqexpert.persistence;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class LogicModuleRegistryTest {

	/*
	 * If there is any order misconfiguration, more specifically the run order
	 * number is duplicated this test will allow to detect the problem on unit
	 * testing - not during the deployment.
	 */
	@Test
	public void orderConfiguredProperlyTest() {
		List<LogicModuleRegistry> a = LogicModuleRegistry.getModulesInRunOrder();
		Assert.assertNotNull(a);
		Assert.assertTrue(a.size() > 0);
	}

}
