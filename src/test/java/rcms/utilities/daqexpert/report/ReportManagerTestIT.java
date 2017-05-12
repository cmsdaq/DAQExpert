package rcms.utilities.daqexpert.report;

import java.util.Calendar;
import java.util.Date;

import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import rcms.utilities.daqexpert.Application;
import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.persistence.PersistenceManager;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

public class ReportManagerTestIT {

	private static Date t1 = DatatypeConverter.parseDateTime("2017-01-17T10:00:00Z").getTime();
	private static Date t2 = DatatypeConverter.parseDateTime("2017-01-17T10:01:00Z").getTime();
	private static Date t3 = DatatypeConverter.parseDateTime("2017-01-17T10:02:00Z").getTime();
	private static Date t4 = DatatypeConverter.parseDateTime("2017-01-17T10:03:00Z").getTime();
	private static Date t5 = DatatypeConverter.parseDateTime("2017-01-17T10:04:00Z").getTime();
	private static Date t6 = DatatypeConverter.parseDateTime("2017-01-17T10:05:00Z").getTime();
	private static PersistenceManager pm;
	private static final Logger logger = Logger.getLogger(ReportManagerTestIT.class);

	@BeforeClass
	public static void prepare() {
		Logger.getRootLogger().setLevel(Level.INFO);

		Application.initialize("src/test/resources/integration.properties");
		Logger.getRootLogger().setLevel(Level.INFO);

		Logger.getLogger(ReportManager.class).setLevel(Level.ALL);
		Logger.getLogger(Report.class).setLevel(Level.ALL);
		pm = Application.get().getPersistenceManager();
	}

	@Test
	public void test() {

		// create 2 fills
		pm.persist(getFinishedEntry(t1, "fill 1", 50, LogicModuleRegistry.StableBeams));
		pm.persist(getFinishedEntry(t4, "fill 2", 50, LogicModuleRegistry.StableBeams));
		pm.persist(getFinishedEntry(t1, "no rate", 25, LogicModuleRegistry.NoRate));
		pm.persist(getFinishedEntry(t1, "no rate when expected", 20, LogicModuleRegistry.NoRateWhenExpected));

		ReportManager r = new ReportManager(pm.getEntityManagerFactory());

		Report report = r.prepareReport(DatatypeConverter.parseDateTime("2017-01-01T00:00:00Z").getTime(),
				DatatypeConverter.parseDateTime("2017-02-01T00:00:00Z").getTime());
		logger.info("Report: " + report);
		logger.info("Report: " + report.getReport());

	}

	private static Condition getFinishedEntry(Date startDate, String name, int duration,
			LogicModuleRegistry logicModule) {
		Condition entry = new Condition();
		entry.setClassName(ConditionPriority.DEFAULTT);
		entry.setTitle(name);
		entry.setStart(startDate);
		entry.setGroup(ConditionGroup.OTHER);
		entry.setLogicModule(logicModule);

		Calendar cal = Calendar.getInstance();
		cal.setTime(startDate);
		cal.add(Calendar.SECOND, duration);
		Date endDate = cal.getTime();
		entry.setEnd(endDate);
		entry.calculateDuration();

		return entry;
	}
}
