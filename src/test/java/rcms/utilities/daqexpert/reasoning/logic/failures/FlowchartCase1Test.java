package rcms.utilities.daqexpert.reasoning.logic.failures;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.persistence.PersistenceFormat;
import rcms.utilities.daqaggregator.persistence.StructureSerializer;

/**
 *
 * @author holzner
 */
public class FlowchartCase1Test
{
  private static DAQ snapshot;
  
	/** method to load a deserialize a snapshot given a file name */
	private static DAQ getSnapshot(String fname) throws URISyntaxException {
		
		StructureSerializer serializer = new StructureSerializer();
		
		URL url = FlowchartCase1.class.getResource(fname);
		
		File file = new File(url.toURI());
		
		return serializer.deserialize(file.getAbsolutePath(), 
						PersistenceFormat.SMILE);
	}
	
  @BeforeClass
  public static void prepare() throws URISyntaxException, IOException {

		snapshot = getSnapshot("1480809948643.smile");

	}
	
  /**
   * Test of satisfied method, of class FlowchartCase1.
   */
  @Test
  public void testSatisfied()
  {
		Map<String, Boolean> results = new HashMap();

		// put results of prerequisite tests by hand
		// (as opposed to get them from a series of snapshots
		// which introduces a dependency on other tests)

		results.put("StableBeams",        false);
		results.put("NoRateWhenExpected", true);

		FlowchartCase1 fc1 = new FlowchartCase1();
    
		boolean result = fc1.satisfied(snapshot, results);

		boolean expResult = true;
		assertEquals(expResult, result);

	}
  
}
