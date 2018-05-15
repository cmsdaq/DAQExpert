package rcms.utilities.daqexpert.reasoning;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.log4j.Logger;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.mixin.IdGenerators;
import rcms.utilities.daqexpert.reasoning.causality.CausalityNode;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class LogicModuleVisualizer {

    private static Logger logger = Logger.getLogger(LogicModuleVisualizer.class);
    /**
     * Generate requirement and causality graphs
     */
    public void generateGraphs() {


    }

    public void generateGraph(Set<CausalityNode> nodes) {

        Set<CausalityNode> r = getNextLevel(new HashSet<>(), nodes, 0);


        ObjectMapper om = new ObjectMapper();
        om.addMixIn(CausalityNode.class, LogicModuleVisualizer.CausalityNodeMixin.class);
        om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        try {
            om.writeValue(new File("src/main/webapp/static/causality.json"), r);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Set<CausalityNode> getNextLevel(Set<CausalityNode> orderedNodes, Set<CausalityNode> remainingNodes, final int level) {

        if (remainingNodes.size() == 0) {
            return orderedNodes;
        }

        // find nodes in remaining nodes that are affected only by already ordered nodes
        Iterator<CausalityNode> iterator = remainingNodes.iterator();


        Set<CausalityNode> next = new HashSet<>();
        while(iterator.hasNext()){
            CausalityNode node = iterator.next();
            // all causing nodes must be in ordered nodes
            boolean allCausingAlreadyOrdered = true;
            for (CausalityNode causing : node.getCausing()) {
                if (!orderedNodes.contains(causing)) {
                    allCausingAlreadyOrdered = false;
                }
            }

            // confirmed
            if (allCausingAlreadyOrdered) {
                logger.info("Moving node: " + node + " with level " + level);

                next.add(node);
            }

        }

        remainingNodes.removeAll(next);
        orderedNodes.addAll(next);
        next.forEach(c->c.setLevel(level));

        return getNextLevel(orderedNodes, remainingNodes, level + 1);
    }



    @JsonIdentityInfo(generator = IdGenerators.ObjectUniqueIntIdGenerator.class, property = "id")
    @JsonIgnoreProperties({"priority", "logicModuleRegistry", "description", "holdNotifications","maturityThreshold", "contextHandler", "descriptionWithContext", "mature", "action", "actionWithContext", "last"})
    public interface CausalityNodeMixin {

        @JsonProperty("required")
        @JsonIdentityReference(alwaysAsId = true)
        abstract List<CausalityNode> getCausing();

        @JsonIgnore
        abstract List<CausalityNode> getAffected();

        @JsonProperty("name")
        abstract String getNodeName();


    }
}


