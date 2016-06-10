package rcms.utilities.daqexpert.reasoning.base;

import java.util.HashMap;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class Aware {

	protected HashMap<String, Boolean> results;
	

	public HashMap<String, Boolean> getResults() {
		return results;
	}

	public void setResults(HashMap<String, Boolean> results) {
		this.results = results;
	}

}
