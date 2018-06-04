package rcms.utilities.daqexpert.processing;

import java.util.Set;

public interface Requiring {

    Set<Requiring> getRequired();
}

