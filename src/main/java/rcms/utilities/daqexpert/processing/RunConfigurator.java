package rcms.utilities.daqexpert.processing;

import java.util.Date;

import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.Logger;

import rcms.utilities.daqexpert.Application;
import rcms.utilities.daqexpert.ExpertException;
import rcms.utilities.daqexpert.ExpertExceptionCode;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.persistence.PersistenceManager;

public class RunConfigurator {

	private final PersistenceManager persistenceManager;

	public RunConfigurator(PersistenceManager persistenceManager) {
		this.persistenceManager = persistenceManager;
	}

	private final Logger logger = Logger.getLogger(RunConfigurator.class);

	public Date getStartDate() {

		Date startDate = null;
		String startDateString = Application.get().getProp(Setting.PROCESSING_START_DATETIME);

		if (startDateString.equalsIgnoreCase("auto")) {
			logger.info("Expert run try to find the date of last expert end");
			startDate = persistenceManager.getLastFinish();

		} else {
			try {
				startDate = DatatypeConverter.parseDateTime(startDateString).getTime();
			} catch (IllegalArgumentException e) {
				throw new ExpertException(ExpertExceptionCode.CannotParseProcessingEndDate,
						"Cannot parse start date " + startDateString + ", (special key possible 'auto')");
			}
		}

		return startDate;

	}

	public Date getEndDate() {
		Date endDate = null;
		String endDateString = Application.get().getProp(Setting.PROCESSING_END_DATETIME);
		if (endDateString.equalsIgnoreCase("unlimited")) {
			logger.info("Expert run unlimited, will process as long as there are new snapshots");
		} else {
			try {
				endDate = DatatypeConverter.parseDateTime(endDateString).getTime();
			} catch (IllegalArgumentException e) {
				throw new ExpertException(ExpertExceptionCode.CannotParseProcessingEndDate,
						"Cannot parse end date " + endDateString + ", (special key possible 'unlimited')");
			}
		}
		return endDate;
	}

}
