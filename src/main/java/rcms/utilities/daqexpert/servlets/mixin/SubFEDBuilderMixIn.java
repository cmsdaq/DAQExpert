package rcms.utilities.daqexpert.servlets.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;

import rcms.utilities.daqaggregator.data.FEDBuilder;
import rcms.utilities.daqaggregator.data.FRLPc;
import rcms.utilities.daqaggregator.data.TTCPartition;

/**
 * Class configuring json serialization
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public interface SubFEDBuilderMixIn {

	@JsonProperty("ref_fedBuilder")
	abstract FEDBuilder getFedBuilder();

	@JsonProperty("ref_ttcPartition")
	abstract TTCPartition getTtcPartition();

	@JsonProperty("ref_frlPc")
	abstract FRLPc getFrlPc();

}
