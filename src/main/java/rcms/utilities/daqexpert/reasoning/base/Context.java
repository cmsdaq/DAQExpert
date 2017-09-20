package rcms.utilities.daqexpert.reasoning.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import rcms.utilities.daqexpert.reasoning.base.action.Action;
import rcms.utilities.daqexpert.reasoning.base.action.ConditionalAction;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;

import java.io.Serializable;
import java.util.*;

@SuppressWarnings("serial")
public class Context extends Observable implements Serializable {

    private static final Logger logger = Logger.getLogger(Context.class);

    private Map<String, Set<Object>> context;
    private Map<String, CalculationContext> contextForCalculations;

    private Set<String> changeset;
    private Set<String> actionKey;

    public Context() {
        this.context = new HashMap<>();
        this.contextForCalculations = new HashMap<>();
        this.actionKey = new HashSet<>();
        this.changeset = new HashSet<>();
    }

    public void register(String key, Object object) {
        if (!context.containsKey(key)) {
            context.put(key, new HashSet<Object>());
        }

        if (!context.get(key).contains(object)) {
            context.get(key).add(object);
            changeset.add(key);
        }

        setChanged();

    }

    public boolean registerForStatistics(String key, Number value) {
        if (!contextForCalculations.containsKey(key)) {
            contextForCalculations.put(key, new CalculationContext());
        }

        CalculationContext cc = contextForCalculations.get(key);
        cc.update(value.floatValue());
        boolean isChangeSignificant = cc.isReport();

        changeset.add(key);
        setChanged();
        if (isChangeSignificant) {
            System.out.println(
                    "Report this change: (min=" + cc.getMin() + ", max=" + cc.getMax() + ", avg=" + cc.getAvg() + ")");
            //conditionProducer.triggerUpdate();
        }
        return isChangeSignificant;
    }

    public void triggerReady() {
        if (hasChanged()) {
            logger.debug("I changed " + this.context + ". Notifying observers: " + this.countObservers());
            notifyObservers();
            clearChanged();
        }
    }

    public void clearChangeset() {
        changeset = new HashSet<>();
    }

    /**
     * Called when Condition ends
     */
    public void clearContext() {
        this.context = new HashMap<>();
        this.actionKey = new HashSet<>();
    }

    public Map<String, Set<Object>> getContext() {
        return this.context;
    }

    @Override
    public String toString() {
        return "ContextCollector [context=" + context + "]";
    }

    public List<String> getActionWithContext(Action actionn) {

        List<String> actionSteps = null;

        if (actionn instanceof ConditionalAction) {
            ConditionalAction action = (ConditionalAction) actionn;
            actionSteps = action.getContextSteps(getActionKey());
        } else if (actionn instanceof SimpleAction) {
            actionSteps = actionn.getSteps();
        }
        logger.debug("Putting context into action: " + actionSteps);
        logger.debug("Context to be used: " + context);

        if (actionSteps != null) {
            List<String> actionStepsWithContext = new ArrayList<>();

            for (String step : actionSteps) {
                actionStepsWithContext.add(putContext(step));
            }

            return actionStepsWithContext;
        }
        return null;
    }

    /**
     * Put collected context into given text. All variables {{VARIABLE_NAME}}
     * will be replaced with value if exists in context or ? sign
     *
     * @param input text where context will be inserted
     * @return copy of the text with context inserted
     */
    private String putContext(String input) {
        ObjectMapper mapper = new ObjectMapper();
        String output = new String(input);

        logger.debug("Putting context into message, current changeset: " + changeset);

        for (java.util.Map.Entry<String, Set<Object>> entry : this.getContext().entrySet()) {
            boolean updated = false;
            if (changeset.contains(entry.getKey())) {
                updated = true;
            }

            String variableKeyNoRgx = "{{" + entry.getKey() + "}}";
            String variableKeyRegex = "\\{\\{" + entry.getKey() + "\\}\\}";

            if (output.contains(variableKeyNoRgx)) {

                String replacement = "";
                try {
                    if (entry.getValue().size() == 1) {
                        if (entry.getValue().iterator().next() != null)
                            replacement = entry.getValue().iterator().next().toString();
                        else replacement = "[empty]";
                    } else {
                        if (entry.getValue().size() > 3) {
                            replacement = "[" + entry.getValue().iterator().next().toString() + " and "
                                    + (entry.getValue().size() - 1) + " more]";
                        } else {
                            replacement = mapper.writeValueAsString(entry.getValue());
                        }
                    }
                    if (updated) {
                        replacement = "<<" + replacement + ">>";
                    }
                    output = output.replaceAll(variableKeyRegex, replacement);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

            } else {
                logger.debug("No key " + variableKeyNoRgx + " in " + output);
            }
        }

        for (Map.Entry<String, CalculationContext> entry : this.contextForCalculations.entrySet()) {
            boolean updated = false;
            if (changeset.contains(entry.getKey())) {
                updated = true;
            }

            String variableKeyNoRgx = "{{" + entry.getKey() + "}}";
            String variableKeyRegex = "\\{\\{" + entry.getKey() + "\\}\\}";

            if (output.contains(variableKeyNoRgx)) {

                String replacement = "";

                replacement = entry.getValue().toString();
                if (updated) {
                    replacement = "<<" + replacement + ">>";
                }
                output = output.replaceAll(variableKeyRegex, replacement);


            } else {
                logger.debug("No key " + variableKeyNoRgx + " in " + output);
            }
        }


        return output;
    }

    public String getContentWithContext(String message) {

        logger.debug("Putting context into message: " + message);
        logger.debug("Context to be used: " + context);

        String newMessage = putContext(message);

        logger.debug("Message with context: " + newMessage);

        return newMessage;
    }

    public String getActionKey() {
        if (actionKey.size() == 1)
            return actionKey.iterator().next();
        else
            return null;
    }

    public void setActionKey(String actionKey) {
        this.actionKey.add(actionKey);
    }

}
