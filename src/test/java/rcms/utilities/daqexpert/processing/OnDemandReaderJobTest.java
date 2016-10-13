package rcms.utilities.daqexpert.processing;

import java.io.File;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import rcms.utilities.daqaggregator.persistence.FileSystemConnector;
import rcms.utilities.daqaggregator.persistence.PersistenceExplorer;

public class OnDemandReaderJobTest extends ReaderJobTestBase {

	@Test
	public void simpleTest() throws Exception {
		PersistenceExplorer explorer = new PersistenceExplorerStub(new FileSystemConnector());
		OnDemandReaderJob onDemandReaderJob = new OnDemandReaderJob(explorer, null);
		onDemandReaderJob.setTimeSpan(1473858460000L, 1473858466000L);
		Pair<Long, List<File>> result = onDemandReaderJob.read();
		Assert.assertEquals(1473858466000L, (long) result.getLeft());
		Assert.assertEquals(6, (long) result.getRight().size());
	}

	@Test
	public void rerunLogicModulesOnDemandTest() {

	}
}
