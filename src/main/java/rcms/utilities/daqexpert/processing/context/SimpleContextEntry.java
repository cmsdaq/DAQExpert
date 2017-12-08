package rcms.utilities.daqexpert.processing.context;

import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class SimpleContextEntry <T> implements ContextEntry <Set<T>>{

    private static final Logger logger = Logger.getLogger(SimpleContextEntry.class);


    private Set<T> objectSet;


    public SimpleContextEntry(){
        objectSet = new HashSet<>();
    }

    public void update(T object){
        if(object == null){
            logger.warn("Null value registered");
            return;
        }
        if(!objectSet.contains(object)){
            objectSet.add(object);
        } else{
            // ignore duplicate
        }
    }

    public Set<T> getObjectSet(){
        return this.objectSet;
    }

    @Override
    public String getTextRepresentation() {
        int limit = 4;

        String representation = objectSet.stream().limit(limit).map(f->f.toString()).collect(Collectors.joining(", "));

        if(objectSet.size() > limit){
            representation += " and " + (objectSet.size() - limit ) + " more";
        }
        if(objectSet.size() > 1){
            representation = "[" + representation + "]";
        }
        return representation  ;
    }

    @Override
    public Set<T> getValue() {
        return objectSet;
    }


    @Override
    public String toString(){
        return getTextRepresentation();
    }

}

