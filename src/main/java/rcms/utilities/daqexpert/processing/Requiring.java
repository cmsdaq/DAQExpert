package rcms.utilities.daqexpert.processing;

import java.util.Set;

public interface Requiring extends HierarchicalNode {

    Set<Requiring> getRequired();
}

