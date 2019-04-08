package rcms.utilities.daqexpert.processing.context;

import com.fasterxml.jackson.annotation.JsonIgnore;
import rcms.utilities.daqexpert.processing.context.functions.ObjectListOptimizer;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Register objects and ignore duplicates by text representation
 */
@Entity
@Table(name="condition_context_object")
public class ObjectContextEntry<T> extends ContextEntry<Set<T>>{


    private String objectType;

    @Transient
    @JsonIgnore
    private ObjectListOptimizer<String> objectListOptimizer;

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
        this.objectListOptimizer = new ObjectListOptimizer<>();

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

    @Override
    public String getTextRepresentation() {

       return objectListOptimizer.getShortestListRepresentation(textRepresentationSet, o->o);
    }

    @Override
    public Set<T> getValue() {
        return this.objectSet;
    }


    public Set<String> getTextRepresentationSet() {
        return textRepresentationSet;
    }

    public void setTextRepresentationSet(Set<String> textRepresentationSet){
        this.textRepresentationSet = textRepresentationSet;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObjectContextEntry<?> that = (ObjectContextEntry<?>) o;
        return Objects.equals(objectType, that.objectType) &&
                Objects.equals(objectSet, that.objectSet);
    }

    @Override
    public int hashCode() {

        return Objects.hash(objectType, objectSet);
    }
}
