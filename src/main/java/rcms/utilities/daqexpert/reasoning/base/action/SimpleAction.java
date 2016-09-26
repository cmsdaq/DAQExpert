package rcms.utilities.daqexpert.reasoning.base.action;

import java.util.Arrays;
import java.util.List;

public class SimpleAction implements Action {

	public SimpleAction(String... steps) {
		this.action = Arrays.asList(steps);
	}

	private List<String> action;

	@Override
	public List<String> getSteps() {
		return action;
	}

}
