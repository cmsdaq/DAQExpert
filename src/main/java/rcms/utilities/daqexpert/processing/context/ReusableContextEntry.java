package rcms.utilities.daqexpert.processing.context;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Register objects and ignore duplicates by text representation
 */
public class ReusableContextEntry<T> implements ContextEntry<Set<T>>{

    /**
     * ContextHandler objects - e.g Subsystem object, FED object etc
     */
    private Set<T> objectSet;

    private Set<String> textRepresentationSet;


    public ReusableContextEntry(){
        this.objectSet = new LinkedHashSet<>();
        this.textRepresentationSet = new LinkedHashSet<>();
    }


    public void update(T object, String textRepresentation){
        if(!textRepresentationSet.contains(textRepresentation)){
            objectSet.add(object);
            textRepresentationSet.add(textRepresentation);
        } else{
            // ignore duplicate
        }
    }

    public Set<T> getObjectSet(){
        return this.objectSet;
    }

    /*
     * TODO: print only few first objects
     */
    @Override
    public String getTextRepresentation() {
        int limit = 4;
        String representation = textRepresentationSet.stream().limit(limit).collect(Collectors.joining(", "));

        if(textRepresentationSet.size() > limit){
            representation += " and " + (textRepresentationSet.size() - limit ) + " more";
        }

        if(textRepresentationSet.size() > 1){
            representation = "[" + representation + "]";
        }
        return representation  ;
    }

    @Override
    public Set<T> getValue() {
        return null;
    }


}
