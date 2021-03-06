package rcms.utilities.daqexpert.servlets.mixin;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;

import rcms.utilities.daqaggregator.data.mixin.IdGenerators;

/**
 * Class configuring json serialization
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
@JsonIdentityInfo(generator = IdGenerators.ObjectUniqueIntIdGenerator.class, property = "@id")
public interface DAQMixIn {

	// nothing to alter here
}
