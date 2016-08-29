package rcms.utilities.daqexpert;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.persistence.PersistenceFormat;
import rcms.utilities.daqaggregator.persistence.StructureSerializer;
import rcms.utilities.daqexpert.servlets.DummyDAQ;

public class ForwardReaderTask extends ReaderTask {

	private static final Logger logger = Logger.getLogger(ForwardReaderTask.class);
	private Long last;

	public ForwardReaderTask(DataResolutionManager dataSegmentator, String sourceDirectory, long startTime) {
		super(dataSegmentator, sourceDirectory);
		this.last = startTime;
	}

	@Override
	public void run() {

		logger.debug("RT task iteration");

		try {
			StructureSerializer structurePersistor = new StructureSerializer();

			// get chunk of data
			Entry<Long, List<File>> entry = ExpertPersistorManager.get().explore(last, sourceDirectory);

			// remember last explored snapshot timestamp
			last = entry.getKey();

			DAQ daq = null;
			for (File file : entry.getValue()) {

				daq = structurePersistor.deserialize(file.getAbsolutePath().toString(), PersistenceFormat.SMILE);

				if (daq != null) {
					List<DummyDAQ> list = DataManager.get().rawData;
					synchronized (list) {
						list.add(new DummyDAQ(daq));
					}
					snapshotProcessor.process(daq, true);
				} else {
					logger.error("Snapshot not deserialized " + file.getAbsolutePath());
				}

			}

			logger.debug("files processed in this round " + entry.getValue().size());
			dataSegmentator.prepareMultipleResolutionData();

			if (daq != null) {
				eventProducer.finish(new Date(daq.getLastUpdate()));
				logger.debug("Finishing the round.");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
