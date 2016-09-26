package rcms.utilities.daqexpert.servlets.mixin;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import rcms.utilities.daqaggregator.data.FMM;
import rcms.utilities.daqaggregator.data.FRL;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqaggregator.data.mixin.IdGenerators;

/**
 * Class configuring json serialization
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
@JsonIdentityInfo(generator = IdGenerators.ObjectUniqueIntIdGenerator.class, property = "@id")
public interface FEDMixIn {

	@JsonProperty("ref_frl")
	@JsonIdentityReference(alwaysAsId = true)
	public FRL getFrl();

	@JsonProperty("ref_fmm")
	@JsonIdentityReference(alwaysAsId = true)
	public FMM getFmm();

	@JsonProperty("ref_ttcp")
	@JsonIdentityReference(alwaysAsId = true)
	public TTCPartition getTtcp();

}
