package rcms.utilities.daqexpert.reasoning.causality;

import org.jgrapht.alg.cycle.TarjanSimpleCycles;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.Assert;
import org.junit.Test;
import rcms.utilities.daqexpert.ExpertException;

import javax.persistence.OneToMany;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.*;

public class CausalityManagerTest {

    @Test
    public void verifyNoCycle() throws Exception {

        CausalityManager cm = new CausalityManager();
        Set<CausalityNode> set = new HashSet<>();

        CausalityNode c1 = new CausalityNodeMock("t1");
        CausalityNode c2 = new CausalityNodeMock("t2");
        CausalityNode c3 = new CausalityNodeMock("t3");

        /*
         * Cycle T1->T2->T3->T1
         */
        c1.declareAffected(c2);
        c2.declareAffected(c3);
        c3.declareAffected(c1);

        set.add(c1);
        set.add(c2);
        set.add(c3);

        try{
            cm.verifyNoCycle(set);
            fail("Exception was not thrown");

        } catch (ExpertException e){
            Assert.assertThat( e.getMessage(), startsWith("Detected cycle in following Logic Modules: "));
        }
    }


    @Test
    public void externalAlgorithmTest(){
        DefaultDirectedGraph<String, DefaultEdge> directedGraph =
                new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
        directedGraph.addVertex("a");
        directedGraph.addVertex("b");
        directedGraph.addVertex("c");
        directedGraph.addVertex("d");
        directedGraph.addEdge("a", "b");
        directedGraph.addEdge("b", "c");
        directedGraph.addEdge("c", "d");
        directedGraph.addEdge("d", "a");

        directedGraph.addEdge("b", "c");
        directedGraph.addEdge("c", "b");

        TarjanSimpleCycles tsc = new  TarjanSimpleCycles();

        tsc.setGraph(directedGraph);
        List a = tsc.findSimpleCycles();

        Assert.assertEquals(2, a.size());

        System.out.println("Result: " + a);
    }
    @Test
    public void transformTest(){
        CausalityManager cm = new CausalityManager();
        Set<CausalityNode> set = new HashSet<>();

        CausalityNode c1 = new CausalityNodeMock("t1");
        CausalityNode c2 = new CausalityNodeMock("t2");
        CausalityNode c3 = new CausalityNodeMock("t3");

        /*
         * Cycle T1->T2->T3->T1
         */
        c1.declareAffected(c2);
        c3.declareCausing(c2);
        c3.declareAffected(c1);

        set.add(c1);
        set.add(c2);
        set.add(c3);

        System.out.println("Before: "+set);
        cm.transformToCanonical(set);
        System.out.println("After:  "+set);
    }

}
