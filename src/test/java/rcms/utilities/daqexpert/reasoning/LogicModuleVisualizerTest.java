package rcms.utilities.daqexpert.reasoning;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import rcms.utilities.daqexpert.reasoning.causality.CausalityNode;
import rcms.utilities.daqexpert.reasoning.causality.CausalityNodeMock;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class LogicModuleVisualizerTest {

    @Test
    public void sortTest() throws JsonProcessingException {
        LogicModuleVisualizer lmv = new LogicModuleVisualizer();


        Set<CausalityNode> a = new HashSet<>();

        CausalityNode n1 = new CausalityNodeMock("n1");
        CausalityNode n2 = new CausalityNodeMock("n2");
        CausalityNode n3 = new CausalityNodeMock("n3");
        CausalityNode n4 = new CausalityNodeMock("n4");

        a.add(n1);
        a.add(n2);
        a.add(n3);
        a.add(n4);

        n3.declareCausing(n4);
        n2.declareCausing(n3);
        n1.declareCausing(n2);

        Set<CausalityNode> b = new HashSet<>();


        Set<CausalityNode> r = lmv.getNextLevel(b,a,0);


        r.stream().forEach(System.out::println);


        lmv.generateGraph(b);


    }

}