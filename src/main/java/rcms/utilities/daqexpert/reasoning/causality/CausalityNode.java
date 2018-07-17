package rcms.utilities.daqexpert.reasoning.causality;

import rcms.utilities.daqexpert.processing.HierarchicalNode;

import java.util.Set;

public interface CausalityNode extends HierarchicalNode {

    /**
     * Get list of Nodes that causes this one. The node is Logic module.
     * @return
     */
    Set<CausalityNode> getCausing();

    /**
     * Get list of Nodes that are caused by this one. The node is Logic module.
     * @return
     */
    Set<CausalityNode> getAffected();


    default void declareCausing(CausalityNode causing) {getCausing().add(causing);}

    default void declareAffected(CausalityNode affected){getAffected().add(affected);}


}
