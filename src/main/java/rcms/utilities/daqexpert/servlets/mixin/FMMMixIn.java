package rcms.utilities.daqexpert.servlets.mixin;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.FMMApplication;
import rcms.utilities.daqaggregator.data.TTCPartition;

/**
 * Class configuring json serialization
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public interface FMMMixIn {

	@JsonProperty("ref_ttcPartition")
	abstract TTCPartition getTtcPartition();

	@JsonProperty("ref_fmmApplication")
	abstract FMMApplication getFmmApplication();

	@JsonProperty("ref_feds")
	abstract List<FED> getFeds();

}
