package rcms.utilities.daqexpert.processing.context;

import java.io.Serializable;
import java.util.stream.Collectors;

public interface ContextEntry <T> extends Serializable {
    String getTextRepresentation();

    T getValue();
}
