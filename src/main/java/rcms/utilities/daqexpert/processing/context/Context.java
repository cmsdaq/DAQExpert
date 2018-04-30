package rcms.utilities.daqexpert.processing.context;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * The contextHandler is replicated hence the Serializable
 */
public class Context implements Serializable{



    /**
     * Map of contextHandler elements
     */
    private Map<String, ContextEntry> contextEntryMap;

    public Context(){
        this.contextEntryMap = new HashMap<>();
    }

    public Map<String, ContextEntry> getContextEntryMap() {
        return contextEntryMap;
    }

    public void setContextEntryMap(Map<String, ContextEntry> contextEntryMap) {
        this.contextEntryMap = contextEntryMap;
    }


    public String getTextRepresentation(String key){
        if(contextEntryMap.get(key) != null){
            return contextEntryMap.get(key).getTextRepresentation();
        }
        return null;
    }

    public Object get(String key){
        if(contextEntryMap.get(key) != null){
            return contextEntryMap.get(key).getValue();
        }
        return null;
    }


    public ObjectContextEntry getReusableContextEntry(String key) {
        ContextEntry contextEntry = contextEntryMap.get(key);
        if (contextEntry instanceof ObjectContextEntry) {
            return (ObjectContextEntry) contextEntry;
        }
        return null;
    }


    @Override
    public String toString() {
        return "Context{" +
                 contextEntryMap.keySet() +
                '}';
    }
}
