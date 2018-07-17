package rcms.utilities.daqexpert.processing;

import java.util.HashSet;
import java.util.Set;

public class RequiredMock implements Requiring {

    public RequiredMock(String id){
        this.id= id;
    }

    public String getId() {
        return id;
    }

    private final String id;

    public Set<Requiring> getRequired() {
        return required;
    }

    @Override
    public String toString() {
        return "RequiredMock{" +
                "id='" + id + '\'' +
                '}';
    }

    public void setRequired(Set<Requiring> required) {
        this.required = required;
    }

    private Set<Requiring> required = new HashSet<>();


    @Override
    public int getLevel() {
        return 0;
    }

    @Override
    public void setLevel(int level) {

    }

    @Override
    public String getNodeName() {
        return null;
    }
}
