package rcms.utilities.daqexpert.reasoning.base.action;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

public class SimpleAction implements Action {

	@Getter
	private boolean isAutomationEnabled;

	public SimpleAction(String... steps) {
		this.action = Arrays.asList(steps);
	}

	public SimpleAction(Boolean isAutomationEnabled, String... steps) {
		this.isAutomationEnabled = isAutomationEnabled;
		this.action = Arrays.asList(steps);
	}

	private List<String> action;

	@Override
	public List<String> getSteps() {
		return action;
	}

}
