package rcms.utilities.daqexpert.reasoning.causality;

import org.apache.log4j.Logger;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jgrapht.alg.cycle.TarjanSimpleCycles;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import rcms.utilities.daqexpert.ExpertException;
import rcms.utilities.daqexpert.ExpertExceptionCode;

public class CausalityManager {

    private static final Logger logger = Logger.getLogger(CausalityManager.class);

    /**
     * Verify that there is no cycle in the LM causality declarations
     */
    public void verifyNoCycle(Set<CausalityNode> causalityNodes) {

        DefaultDirectedGraph<CausalityNode, DefaultEdge> directedGraph =
                new DefaultDirectedGraph<CausalityNode, DefaultEdge>(DefaultEdge.class);


        for(CausalityNode node : causalityNodes){
            directedGraph.addVertex(node);
        }

        for(CausalityNode source : causalityNodes){
            for(CausalityNode target: source.getAffected())
            directedGraph.addEdge(source, target);
        }

        TarjanSimpleCycles tsc = new  TarjanSimpleCycles();


        tsc.setGraph(directedGraph);
        List<List<CausalityNode>> cycles = tsc.findSimpleCycles();
        System.out.println("Result: " + cycles);

        if(cycles.size() > 0){
            throw new ExpertException(ExpertExceptionCode.LogicModuleMisconfiguration, "Detected cycle in following Logic Modules: " + cycles.get(0).stream().map(c->c.getNodeName()).collect(Collectors.toList()));
        }

    }

    /**
     * Fill affected based on causing and vice versa. Node A that declares node B as affected is equal to node B
     * declaring node A as causing.  It is not required to fill both sites of the relationship. After transformation
     * node A will point to node B with 'affected' relation and node B to node A with 'causing' relation.
     */
    public void transformToCanonical(Set<CausalityNode> causalityNodes) {

        for(CausalityNode causing: causalityNodes){
            Set<CausalityNode> affectedNodes = causing.getAffected();
            for(CausalityNode affectedNode : affectedNodes){
                if(!affectedNode.getCausing().contains(causing)){
                    logger.debug("Filling causing set with " + causing+ " of affected node: " + affectedNode);
                    affectedNode.getCausing().add(causing);
                }
            }

        }

        for(CausalityNode affected: causalityNodes){
            Set<CausalityNode> causingNodes = affected.getCausing();
            for(CausalityNode causingNode: causingNodes){
                if(!causingNode.getAffected().contains(affected)){
                    logger.debug("Filling affected set with " + affected+ " of causing node: " + causingNode);
                    causingNode.getAffected().add(affected);
                }
            }
        }


    }

    /**
     *
     */
    public void printCausalityGraph(Set<CausalityNode> causalityNodes) {

        for (CausalityNode node : causalityNodes) {

        }
    }
}
