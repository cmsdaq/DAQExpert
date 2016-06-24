package rcms.utilities.daqexpert.servlets.mixin;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.FRLPc;
import rcms.utilities.daqaggregator.data.SubFEDBuilder;

/**
 * Class configuring json serialization
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public interface FRLMixIn {

	@JsonProperty("ref_subFedBuilder")
	abstract SubFEDBuilder getSubFedbuilder();

	@JsonProperty("ref_feds")
	abstract Map<Integer, FED> getFeds();

	@JsonProperty("ref_frlPc")
	abstract FRLPc getFrlPc();

}
