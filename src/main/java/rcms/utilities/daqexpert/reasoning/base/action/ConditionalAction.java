package rcms.utilities.daqexpert.reasoning.base.action;

import org.apache.log4j.Logger;

import java.util.*;

public class ConditionalAction implements Action {

	private static final Logger logger = Logger.getLogger(ConditionalAction.class);

	private static final String DEFAULT_KEY = "default";

	private Map<String, List<String>> action = new HashMap<>();


	private Map<String, Boolean> isAutomationEnabled = new HashMap<>();

	public ConditionalAction(String... steps) {
		this.isAutomationEnabled.put(DEFAULT_KEY, false);
		this.action.put(DEFAULT_KEY, Arrays.asList(steps));
	}

	public ConditionalAction(Boolean isAutomationEnabled, String... steps) {
		this.isAutomationEnabled.put(DEFAULT_KEY, isAutomationEnabled);
		this.action.put(DEFAULT_KEY, Arrays.asList(steps));
	}

	public void addContextSteps(String key, String... steps) {
		this.isAutomationEnabled.put(key, false);
		this.action.put(key.toLowerCase(), Arrays.asList(steps));
	}

	public void addContextSteps(String key, Boolean isAutomationEnabled, String... steps) {
		this.isAutomationEnabled.put(key, isAutomationEnabled);
		this.action.put(key.toLowerCase(), Arrays.asList(steps));
	}

	@Override
	public List<String> getSteps() {
		return this.action.get(DEFAULT_KEY);
	}

	@Override
	public boolean isAutomationEnabled() {

		return this.isAutomationEnabled.get(DEFAULT_KEY);
	}

    public boolean isAutomationEnabled(String key) {

        if (this.isAutomationEnabled.containsKey(key)) {
            return this.isAutomationEnabled.get(key);
        } else {
            return false;
        }
    }

	public List<String> getContextSteps(String actionKeyy) {
		if (actionKeyy != null) {
			String actionKey = actionKeyy.toLowerCase();
			if (this.action.containsKey(actionKey)) {
				return this.action.get(actionKey);
			} else {
				logger.debug("No action for key: " + actionKey + ", using default action");
				return this.action.get(DEFAULT_KEY);
			}
		} else {
			logger.warn("No action key for contextHandler action, using default action");
			return getSteps();
		}

	}

	public Set<String> getActionKeys(){
		return action.keySet();
	}

}
