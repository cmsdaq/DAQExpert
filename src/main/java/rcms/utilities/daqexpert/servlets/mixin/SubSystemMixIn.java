package rcms.utilities.daqexpert.servlets.mixin;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqaggregator.data.mixin.IdGenerators;

/**
 * Class configuring json serialization
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */

@JsonIdentityInfo(generator = IdGenerators.ObjectUniqueIntIdGenerator.class, property = "@id")
public interface SubSystemMixIn {

	@JsonProperty("ref_ttcPartitions")
	@JsonIdentityReference(alwaysAsId = true)
	Set<TTCPartition> getTtcPartitions();

}
