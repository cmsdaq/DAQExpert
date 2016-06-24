package rcms.utilities.daqexpert.servlets.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;

import rcms.utilities.daqaggregator.data.DAQ;

/**
 * Class configuring json serialization
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public interface BUSummaryMixIn {

	@JsonProperty("ref_daq")
	abstract DAQ getDaq();

}
