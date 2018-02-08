package rcms.utilities.daqexpert.processing.context;

import org.apache.log4j.Logger;
import org.mockito.internal.matchers.Null;
import rcms.utilities.daqexpert.reasoning.base.action.Action;
import rcms.utilities.daqexpert.reasoning.base.action.ConditionalAction;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;

public class ContextHandler {

    private final static Logger logger = Logger.getLogger(ContextHandler.class);

    private Context context;

    /**
     * Set of action keys that are used in case of conditional action.
     */
    private Set<String> actionKey;

    private ContextNotifier contextNotifier;

    public void setHighlightMarkup(boolean highlightMarkup) {
        this.highlightMarkup = highlightMarkup;
    }

    public static boolean highlightMarkup;

    public ContextHandler() {
        this(new ContextNotifier());
    }

    public ContextHandler(ContextNotifier contextNotifier) {
        this.contextNotifier = contextNotifier;
        this.context = new Context();
        this.context.setContextEntryMap(new HashMap<>());
        this.actionKey = new HashSet<>();
        this.highlightMarkup = true;
    }

    public ContextEntry getContextEntry(String key) {
        return context.getContextEntryMap().get(key);
    }


    /**
     * Register object for given key.
     *
     * @param key      key for the object
     * @param object   object, eg. FED, Subsystem etc.
     * @param function function to generate textual representation from the object
     */
    public <T> void registerObject(String key, T object, Function<T, String> function) {
        String s;
        try {
            s = function.apply(object);
        } catch(NullPointerException e){
            logger.warn("Could not get text representation fo object " + object + " given function " + function);
            s = "unavailable";
        }

        if (!context.getContextEntryMap().containsKey(key)) {
            context.getContextEntryMap().put(key, new ObjectContextEntry());
        } else {
            verifyNoContextMismatch(key, ObjectContextEntry.class);
        }

        ObjectContextEntry reusableContextEntry = (ObjectContextEntry) context.getContextEntryMap().get(key);

        reusableContextEntry.update(object, s);
        contextNotifier.registerChange(key);
    }


    public <T> void register(String key, T value) {

        if(value == null){
            logger.warn("Cannot register null value, trying to register the key: " + key);
            return;
        }

        if (!context.getContextEntryMap().containsKey(key)) {
            context.getContextEntryMap().put(key, new ObjectContextEntry());
        } else {
            verifyNoContextMismatch(key, ObjectContextEntry.class);
        }

        ObjectContextEntry simpleContextEntry = (ObjectContextEntry) context.getContextEntryMap().get(key);

        String stringRepresentation = value.toString();
        if(stringRepresentation.length() > 255){
            String oldValue = stringRepresentation;
            stringRepresentation = stringRepresentation.substring(0,252) + "...";
            logger.warn("Trimming context value from: " + oldValue + " to " + stringRepresentation);
        }

        simpleContextEntry.update(value, stringRepresentation);
        contextNotifier.registerChange(key);
    }

    /**
     * Register new statistical value for given key.
     *
     * @param key       key for the value
     * @param value     current value
     * @param unit      unit of the value
     * @param precision desired presentation precision (note that the precision doesn't influence statistics, only final
     *                  textual representation of the contextHandler)
     * @return true if change is significant
     */
    public boolean registerForStatistics(String key, Number value, String unit, int precision) {

        if (!context.getContextEntryMap().containsKey(key)) {
            context.getContextEntryMap().put(key, new StatisticContextEntry(unit, precision));
        } else {
            verifyNoContextMismatch(key, StatisticContextEntry.class);
        }

        StatisticContextEntry statisticContextEntry = (StatisticContextEntry) context.getContextEntryMap().get(key);

        statisticContextEntry.update(value.floatValue());
        contextNotifier.registerChange(key);


        // TODO handle significant changes
        boolean isChangeSignificant = statisticContextEntry.isReport();
        return isChangeSignificant;
    }

    public boolean registerForStatistics(String key, Number value) {
        return registerForStatistics(key, value, "", 1);
    }

    /**
     * Register additional note.
     *
     * @param key   key for the additional note
     * @param value current aditional note
     */
    public void registerConditionalNote(String key, String value) {
        if (!context.getContextEntryMap().containsKey(key)) {
            context.getContextEntryMap().put(key, new OptionalContextEntry());
        } else {
            verifyNoContextMismatch(key, OptionalContextEntry.class);
        }

        OptionalContextEntry optionalContextEntry = (OptionalContextEntry) context.getContextEntryMap().get(key);

        optionalContextEntry.setValue(value);
        contextNotifier.registerChange(key);
    }

    public void unregisterConditionalNote(String key) {
        registerConditionalNote(key, null);
    }


    /**
     * Put collected contextHandler into given text. All variables {{VARIABLE_NAME}} will be replaced with value if
     * exists in contextHandler
     *
     * @param text text where contextHandler will be inserted
     * @return text with contextHandler inserted
     */
    public String putContext(String text) {

        String replacementForRequired = "?";
        String replacementForOptional = "";

        logger.debug("Putting contextHandler into message, current changeset: " + context.getContextEntryMap().keySet());

        for (Map.Entry<String, ContextEntry> contextEntryElement : context.getContextEntryMap().entrySet()) {
            String key = contextEntryElement.getKey();
            ContextEntry contextEntry = contextEntryElement.getValue();
            boolean updated = contextNotifier.isChanged(key);


            String replacement = contextEntry.getTextRepresentation();

            String variableKeyNoRgx = "{{" + key + "}}";
            String variableKeyRegex = "\\{\\{" + key + "\\}\\}";

            if (text.contains(variableKeyNoRgx)) {
                if (updated && highlightMarkup) {
                    replacement = "<strong>" + replacement + "</strong>";
                }
                text = text.replaceAll(variableKeyRegex, replacement);

            }

            variableKeyNoRgx = "[[" + key + "]]";
            variableKeyRegex = "\\[\\[" + key + "\\]\\]";
            if (text.contains(variableKeyNoRgx)) {
                if (updated && highlightMarkup && !"".equals(replacement)) {
                    replacement = "<strong>" + replacement + "</strong>";
                }
                text = text.replaceAll(variableKeyRegex, replacement);

            }
        }

        logger.debug("Replacing remaining keys");
        String optionalVariableKeyRegex = "\\[\\[\\b[A-Z]+\\b\\]\\]";
        text.replaceAll(optionalVariableKeyRegex, replacementForOptional);

        String requiredVariableKeyRegex = "\\{\\{\\b[A-Z]+\\b\\}\\}";
        text.replaceAll(requiredVariableKeyRegex, replacementForRequired);

        return text;
    }

    public List<String> getActionWithContext(Action action) {

        List<String> actionSteps = null;

        if (action instanceof ConditionalAction) {
            ConditionalAction conditionalAction = (ConditionalAction) action;
            actionSteps = conditionalAction.getContextSteps(getActionKey());
        } else if (action instanceof SimpleAction) {
            actionSteps = action.getSteps();
        }
        logger.debug("Putting contextHandler into action: " + actionSteps);
        logger.debug("ContextHandler to be used: " + context.getContextEntryMap());

        if (actionSteps != null) {
            List<String> actionStepsWithContext = new ArrayList<>();

            for (String step : actionSteps) {
                String stepWithContext = putContext(step);
                if(stepWithContext.length() > 255){
                    String oldValue = stepWithContext;
                    stepWithContext = stepWithContext.substring(0,252) + "...";
                    logger.warn("Trimming action step " + oldValue + " to: " + stepWithContext );
                }
                actionStepsWithContext.add(stepWithContext);
            }

            return actionStepsWithContext;
        }
        return null;
    }

    public void clearContext() {
        this.actionKey.clear();
        this.context.getContextEntryMap().clear();
        this.contextNotifier.clearChangeset();
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

    private void verifyNoContextMismatch(String key, Class classs) {
        if (!classs.isInstance(context.getContextEntryMap().get(key))) {
            throw new RuntimeException("Different type of contextHandler (" + context.getContextEntryMap().get(key).getClass().getName()
                    + ") has been already registered for this key: " + key);
        }
    }

    /**
     * Deprecated method. Please use putContext
     *
     * @param text
     * @return
     * @Deprecated use putContext
     */
    @Deprecated
    public String getContentWithContext(String text) {
        return putContext(text);
    }


    public ContextNotifier getContextNotifier() {
        return contextNotifier;
    }

    public void setContextNotifier(ContextNotifier contextNotifier) {
        this.contextNotifier = contextNotifier;
    }


    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
