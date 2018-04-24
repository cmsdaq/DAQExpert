package rcms.utilities.daqexpert.reasoning.causality;

import rcms.utilities.daqexpert.processing.Requiring;

import java.util.Set;

public interface Causing {

    Set<Causing> getCausing();

    default void declareCausing() {return;}
}
