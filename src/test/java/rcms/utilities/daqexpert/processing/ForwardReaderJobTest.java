package rcms.utilities.daqexpert.processing;

import java.io.File;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import rcms.utilities.daqaggregator.persistence.FileSystemConnector;
import rcms.utilities.daqaggregator.persistence.PersistenceExplorer;

public class ForwardReaderJobTest extends ReaderJobTestBase {

	@Test
	public void simpleTest() throws Exception {
		PersistenceExplorer explorer = new PersistenceExplorerStub(new FileSystemConnector());
		ForwardReaderJob forwardReaderJob = new ForwardReaderJob(explorer, 1473858463000L,null, null);
		Pair<Long, List<File>> result = forwardReaderJob.read();
		Assert.assertEquals(1473858469000L, (long) result.getLeft());
		Assert.assertEquals(6, (long) result.getRight().size());

		result = forwardReaderJob.read();
		Assert.assertEquals(1473858469000L, (long) result.getLeft());
		Assert.assertEquals(0, (long) result.getRight().size());

		testSet.add(1473858470000L);

		result = forwardReaderJob.read();
		Assert.assertEquals(1473858470000L, (long) result.getLeft());
		Assert.assertEquals(1, (long) result.getRight().size());
	}

}
