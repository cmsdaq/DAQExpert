package rcms.utilities.daqexpert.servlets.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;

import rcms.utilities.daqaggregator.data.FEDBuilder;

/**
 * Class configuring json serialization
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public interface RUMixIn {

	@JsonProperty("ref_fedBuilder")
	abstract FEDBuilder getFedBuilder();

}
