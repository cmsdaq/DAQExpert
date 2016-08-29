package rcms.utilities.daqexpert;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.persistence.PersistenceFormat;
import rcms.utilities.daqaggregator.persistence.StructureSerializer;
import rcms.utilities.daqexpert.servlets.DummyDAQ;

public class PastReader extends ReaderTask {

	private static final Logger logger = Logger.getLogger(PastReader.class);

	public PastReader(DataResolutionManager dataSegmentator, String sourceDirectory, long startTime, long readTo) {
		super(dataSegmentator, sourceDirectory);
		this.readFrom = startTime;
		this.readTo = readTo;
	}

	/**
	 * Indicator of last snapshot processed timestamp
	 */
	protected long readFrom;

	protected long readTo;

	@Override
	public void run() {
		logger.debug("PAST task iteration");

		try {
			StructureSerializer structurePersistor = new StructureSerializer();

			if (readFrom > readTo) {
				logger.info("Finished reading for this reader");
				dataSegmentator.prepareMultipleResolutionDataForPast(readTo);
				future.cancel(true);
				dataSegmentator = null;
				snapshotProcessor = null;
				eventProducer = null;
				return;
			}

			Date chunkStart = new Date(readFrom);
			Date chunkEnd = new Date(getForwardHourChunk(readFrom));
			logger.debug("Getting chunk " + chunkStart + " - " + chunkEnd);

			Entry<Long, List<File>> entry;
			if (getForwardHourChunk(readFrom) > readTo) {

				entry = ExpertPersistorManager.get().explore(readFrom, readTo, sourceDirectory);
			} else {
				// get chunk of data
				entry = ExpertPersistorManager.get().explore(readFrom, getForwardHourChunk(readFrom), sourceDirectory);
			}

			// remember last explored snapshot timestamp
			readFrom = entry.getKey();

			if (entry.getValue().size() == 0) {
				logger.debug("No data in this period, try again");
				readFrom = getForwardHourChunk(readFrom);
				run();
				return;
			}

			DAQ daq = null;

			for (File file : entry.getValue()) {
				daq = structurePersistor.deserialize(file.getAbsolutePath().toString(), PersistenceFormat.SMILE);

				if (daq != null) {

					List<DummyDAQ> list = DataManager.get().rawData;
					synchronized (list) {
						list.add(new DummyDAQ(daq));
					}

					snapshotProcessor.process(daq, false);
				} else {
					logger.error("Snapshot not deserialized " + file.getAbsolutePath());
				}

			}

			logger.info("Files processed in this round " + entry.getValue().size() + ", chunk " + chunkStart + " - "
					+ chunkEnd);
			// dataSegmentator.prepareMultipleResolutionData();

			if (daq != null) {
				eventProducer.finish(new Date(daq.getLastUpdate()));
				logger.debug("Finishing the round.");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public long getBackwardHourChunk(long time) {
		Date date = new Date(time);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.HOUR, -1);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime().getTime();
	}

	public long getForwardHourChunk(long time) {
		Date date = new Date(time);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.HOUR, 1);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 999);
		return calendar.getTime().getTime();
	}

}
