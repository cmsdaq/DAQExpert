package rcms.utilities.daqexpert.processing.context;

public class OptionalContextEntry implements ContextEntry<String> {

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

