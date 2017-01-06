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
  private static DAQ snapshot;
  
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

    StructureSerializer serializer = new StructureSerializer();
    
    java.net.URL url = FlowchartCase1.class.getResource("1480809927880.smile");
		File file = new File(url.toURI());
		
    snapshot = serializer.deserialize(file.getAbsolutePath(), 
            PersistenceFormat.SMILE);
    
  }
  
  /**
   * Test of satisfied method, of class FlowchartCase1.
   */
  @Test
  public void testSatisfied()
  {
    Map<String, Boolean> results = new HashMap();
    
    boolean res = false;
    
    // required by ExpectedRate
    RunOngoing runOngoing = new RunOngoing();
    res = runOngoing.satisfied(snapshot, results);
    results.put(RunOngoing.class.getSimpleName(), res);
    
    // required by NoRateWhenExpected
    StableBeams stableBeams = new StableBeams();
    res = stableBeams.satisfied(snapshot, results);
    results.put(StableBeams.class.getSimpleName(), res);

    ExpectedRate expectedRate = new ExpectedRate();
    res = expectedRate.satisfied(snapshot, results);
    results.put(ExpectedRate.class.getSimpleName(), res);
    
    NoRate noRate = new NoRate();
    res = noRate.satisfied(snapshot, results);
    results.put(NoRate.class.getSimpleName(), res);
    
    Transition transition = new Transition();
    res = transition.satisfied(snapshot, results);
    results.put(Transition.class.getSimpleName(), res);
    
    // required by FlowchartCase1
    NoRateWhenExpected nrwe = new NoRateWhenExpected();
    res = nrwe.satisfied(snapshot, results);
    results.put(NoRateWhenExpected.class.getSimpleName(), res);
    
    FlowchartCase1 instance = new FlowchartCase1();
    instance.getContext().clearContext();
    boolean expResult = true;
    boolean result = instance.satisfied(snapshot, results);
    assertEquals(expResult, result);
  }
  
}
