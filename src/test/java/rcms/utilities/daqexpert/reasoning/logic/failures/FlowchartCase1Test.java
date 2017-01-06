package rcms.utilities.daqexpert.reasoning.logic.failures;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.persistence.PersistenceFormat;
import rcms.utilities.daqaggregator.persistence.StructureSerializer;
import rcms.utilities.daqexpert.reasoning.logic.basic.ExpectedRate;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRate;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRateWhenExpected;
import rcms.utilities.daqexpert.reasoning.logic.basic.RunOngoing;
import rcms.utilities.daqexpert.reasoning.logic.basic.StableBeams;
import rcms.utilities.daqexpert.reasoning.logic.basic.Transition;

/**
 *
 * @author holzner
 */
public class FlowchartCase1Test
{
  private static List<DAQ> snapshots = new ArrayList();
  
	Transition transition     = new Transition();
	RunOngoing runOngoing     = new RunOngoing();
  StableBeams stableBeams   = new StableBeams();
  ExpectedRate expectedRate = new ExpectedRate();
  NoRate noRate             = new NoRate();
  NoRateWhenExpected nrwe   = new NoRateWhenExpected();
  FlowchartCase1 fc1        = new FlowchartCase1();
	
  public FlowchartCase1Test()
  {
  }
  
	private static DAQ getSnapshot(String fname) throws URISyntaxException {
		
		StructureSerializer serializer = new StructureSerializer();
		
		URL url = FlowchartCase1.class.getResource(fname);
		
		File file = new File(url.toURI());
		
		return serializer.deserialize(file.getAbsolutePath(), 
           PersistenceFormat.SMILE);
	}
	
  @BeforeClass
  public static void prepare() throws URISyntaxException, IOException {

		snapshots.add(getSnapshot("1480809938304.smile"));
		snapshots.add(getSnapshot("1480809943439.smile"));
    snapshots.add(getSnapshot("1480809948643.smile"));

	}
	
	/** runs tests on a single snapshot */
	private boolean testSingle(DAQ snapshot)
	{
		Map<String, Boolean> results = new HashMap();
    
    boolean res = false;
    
    // required by ExpectedRate
    res = runOngoing.satisfied(snapshot, results);
    results.put(RunOngoing.class.getSimpleName(), res);
    
    // required by NoRateWhenExpected
    res = stableBeams.satisfied(snapshot, results);
    results.put(StableBeams.class.getSimpleName(), res);

    res = expectedRate.satisfied(snapshot, results);
    results.put(ExpectedRate.class.getSimpleName(), res);
    
    res = noRate.satisfied(snapshot, results);
    results.put(NoRate.class.getSimpleName(), res);

    res = transition.satisfied(snapshot, results);
    results.put(Transition.class.getSimpleName(), res);
    
    // required by FlowchartCase1
    res = nrwe.satisfied(snapshot, results);
    results.put(NoRateWhenExpected.class.getSimpleName(), res);
    
    fc1.getContext().clearContext();
    boolean result = fc1.satisfied(snapshot, results);
		return result;
	}
	
  /**
   * Test of satisfied method, of class FlowchartCase1.
   */
  @Test
  public void testSatisfied()
  {
    Boolean result = null;
		
		for (DAQ snapshot : snapshots)
		{
			// note that we run over three snapshots in time such that 
			// the Transition test returns the expected result. We
			// only look at the result of the FlowchartCase1 test
			// of the last snapshot
			result = testSingle(snapshot);
		}

		boolean expResult = true;
    assertEquals(expResult, result);

	}
  
}
