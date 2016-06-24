package rcms.utilities.daqexpert.servlets.mixin;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import rcms.utilities.daqaggregator.data.FED;

/**
 * Class configuring json serialization
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public interface TTCPartitionMixIn {

	@JsonProperty("ref_feds")
	abstract List<FED> getFeds();

}
