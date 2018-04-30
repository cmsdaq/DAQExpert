package rcms.utilities.daqexpert.processing.context;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
public class OptionalContextEntry extends ContextEntry<String> {


    @Transient
    String value;

    public void setValue(String value){
        this.value = value;
    }

    @Override
    public String getTextRepresentation() {

        if(value == null){
            return "";
        }
        return value;
    }

    @Override
    public String getValue() {
        return value;
    }
}

