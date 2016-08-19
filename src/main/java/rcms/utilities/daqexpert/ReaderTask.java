package rcms.utilities.daqexpert;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.persistence.SnapshotFormat;
import rcms.utilities.daqaggregator.persistence.StructureSerializer;
import rcms.utilities.daqexpert.reasoning.base.EventProducer;
import rcms.utilities.daqexpert.reasoning.base.SnapshotProcessor;
import rcms.utilities.daqexpert.servlets.DummyDAQ;

/**
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class ReaderTask extends TimerTask {

	private static final Logger logger = Logger.getLogger(ReaderTask.class);
	long last = 0;

	private DataResolutionManager dataSegmentator;
	private SnapshotProcessor snapshotProcessor = new SnapshotProcessor();

	public ReaderTask(DataResolutionManager dataSegmentator) {
		this.dataSegmentator = dataSegmentator;
	}

	@Override
	public void run() {

		try {
			StructureSerializer structurePersistor = new StructureSerializer();

			Entry<Long, List<List<File>>> entry = ExpertPersistorManager.get().explore(last);

			last = entry.getKey();

			for (List<File> chunk : entry.getValue()) {
				DAQ daq = null;
				for (File file : chunk) {

					daq = structurePersistor.deserialize(file.getAbsolutePath().toString(),SnapshotFormat.SMILE);

					if (daq != null) {
						TaskManager.get().rawData.add(new DummyDAQ(daq));
						snapshotProcessor.process(daq);
					} else {
						logger.error("Snapshot not deserialized " + file.getAbsolutePath());
					}

				}

				logger.debug("files processed in this round " + entry.getValue().size());
				dataSegmentator.prepareMultipleResolutionData();

				if (daq != null)
					EventProducer.get().finish(new Date(daq.getLastUpdate()));

			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
