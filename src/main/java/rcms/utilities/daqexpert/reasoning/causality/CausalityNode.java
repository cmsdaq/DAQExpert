package rcms.utilities.daqexpert.reasoning.causality;

import rcms.utilities.daqexpert.processing.Requiring;

import java.util.Set;

public interface CausalityNode {

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


    int getLevel();

    void setLevel(int level);

    String getNodeName();

    default void declareCausing(CausalityNode causing) {getCausing().add(causing);}

    default void declareAffected(CausalityNode affected){getAffected().add(affected);}


}
