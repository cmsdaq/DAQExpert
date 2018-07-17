package rcms.utilities.daqexpert.processing;

/**
 * Node of the hierarchical grapsh. Can be identified by name and has a level in the hierarchy.
 */
public interface HierarchicalNode {


    int getLevel();

    void setLevel(int level);

    String getNodeName();

}
