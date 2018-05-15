package rcms.utilities.daqexpert.reasoning.causality;


import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class CausalityNodeMock implements CausalityNode{

    private Set<CausalityNode> causing;
    private Set<CausalityNode> affected;
    private String name;
    private int level;

    @Override
    public String getNodeName() {
        return name;
    }

    public CausalityNodeMock(String testName){
        causing = new HashSet<>();
        affected = new HashSet<>();
        name= testName;
    }

    @Override
    public Set<CausalityNode> getCausing() {
        return causing;
    }

    @Override
    public Set<CausalityNode> getAffected() {
        return affected;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public String toString() {
        return "Node{" +
                "name= " + name +
                ", causing=" + causing.stream().map(c-> c.getNodeName()).collect(Collectors.toSet()) +
                ", affected=" + affected.stream().map(c-> c.getNodeName()).collect(Collectors.toSet()) +
                ", level= " + level +
                '}';
    }
}
