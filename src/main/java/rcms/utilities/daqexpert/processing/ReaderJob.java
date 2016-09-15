package rcms.utilities.daqexpert.processing;

import java.io.File;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Interface for reader jobs
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public interface ReaderJob {

	public Pair<Long, List<File>> read();
	
	public boolean finished();

}
