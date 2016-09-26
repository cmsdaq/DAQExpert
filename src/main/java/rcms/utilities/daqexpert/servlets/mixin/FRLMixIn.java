package rcms.utilities.daqexpert.servlets.mixin;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.FRLPc;
import rcms.utilities.daqaggregator.data.SubFEDBuilder;
import rcms.utilities.daqaggregator.data.mixin.IdGenerators;

/**
 * Class configuring json serialization
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
@JsonIdentityInfo(generator = IdGenerators.ObjectUniqueIntIdGenerator.class, property = "@id")
public interface FRLMixIn {

	@JsonProperty("ref_subFedBuilder")
	@JsonIdentityReference(alwaysAsId = true)
	abstract SubFEDBuilder getSubFedbuilder();

	@JsonProperty("ref_feds")
	@JsonIdentityReference(alwaysAsId = true)
	abstract Map<Integer, FED> getFeds();

	@JsonProperty("ref_frlPc")
	@JsonIdentityReference(alwaysAsId = true)
	abstract FRLPc getFrlPc();

}
