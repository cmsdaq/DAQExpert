package rcms.utilities.daqexpert.processing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import rcms.utilities.daqaggregator.persistence.FileSystemConnector;
import rcms.utilities.daqaggregator.persistence.PersistenceExplorer;

public class ReaderJobTestBase {

	public ReaderJobTestBase() {
		testSet.addAll(Arrays.asList(1473858460000L, 1473858461000L, 1473858462000L, 1473858463000L, 1473858464000L,
				1473858465000L, 1473858466000L, 1473858467000L, 1473858468000L, 1473858469000L));
	}

	protected List<Long> testSet = new ArrayList<>();

	public class PersistenceExplorerStub extends PersistenceExplorer {

		public PersistenceExplorerStub(FileSystemConnector fileSystemConnector) {
			super(fileSystemConnector);
		}

		@Override
		public Pair<Long, List<File>> explore(Long startTimestamp, Long endTimestamp, String dir, int chunkSize)
				throws IOException {

			System.out.println("Explore stub");
			List<File> files = new ArrayList<File>();
			Long last = null;

			for (Long timestamp : testSet) {
				if (startTimestamp < timestamp && timestamp <= endTimestamp) {
					files.add(new File(timestamp + ".json"));
					last = timestamp;
				}
			}
			return Pair.of(last, files);
		}
	}
}
