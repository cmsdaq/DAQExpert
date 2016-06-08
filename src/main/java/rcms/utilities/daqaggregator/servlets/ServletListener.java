package rcms.utilities.daqaggregator.servlets;

import java.io.IOException;
import java.util.Timer;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.DataResolutionManager;
import rcms.utilities.daqaggregator.ExpertPersistorManager;
import rcms.utilities.daqaggregator.ReaderTask;
import rcms.utilities.daqaggregator.reasoning.base.Entry;
import rcms.utilities.daqaggregator.reasoning.base.EventProducer;

public class ServletListener implements ServletContextListener {

	private static final Logger logger = Logger.getLogger(ServletListener.class);

	ExpertPersistorManager persistorManager = new ExpertPersistorManager("/tmp/mgladki/persistence-tiny");
	DataResolutionManager dataSegmentator = new DataResolutionManager();
	

	Timer t = new Timer();

	public void contextInitialized(ServletContextEvent e) {
		long last = walkAllFilesProcessAndStoreInMemory();
		
		t.scheduleAtFixedRate(new ReaderTask(dataSegmentator,last), 10000, 10000);
	}

	public void contextDestroyed(ServletContextEvent e) {
		t.cancel();
	}

	/**
	 * Will walk all files, analyze all snapshots and results will be in memory
	 */
	private long walkAllFilesProcessAndStoreInMemory() {
		try {
			logger.info("Walking through all data..");
			persistorManager.walkAll();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		dataSegmentator.prepareMultipleResolutionData();
		
		long last = 0;
		
		for(Entry e: EventProducer.get().getResult()){
			if(e.getStart().getTime() > last){
				last = e.getStart().getTime();
			}
		}
		return last;
		
	}



}