package rcms.utilities.daqexpert.processing.context;

import org.hibernate.annotations.CollectionOfElements;

import javax.persistence.*;
import java.lang.reflect.ParameterizedType;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Register objects and ignore duplicates by text representation
 */
@Entity
@Table(name="condition_context_object")
public class ObjectContextEntry<T> extends ContextEntry<Set<T>>{


    private String objectType;

    /**
     * ContextHandler objects - e.g Subsystem object, FED object etc
     */
    @Transient
    private transient Set<T> objectSet;

    //@Convert(converter = StringListConverter.class)
    @ElementCollection
    @CollectionTable(name = "condition_context_object_value", joinColumns=@JoinColumn(name="context_object_id"))
    @Column(name="value")
    private Set<String> textRepresentationSet;


    public ObjectContextEntry(){
        this.objectSet = new LinkedHashSet<>();
        this.textRepresentationSet = new LinkedHashSet<>();
        this.type = "O";
    }


    public void update(T object, String textRepresentation){
        if(objectType == null){
            objectType = object.getClass().getSimpleName();
        }
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
        return this.objectSet;
    }


}