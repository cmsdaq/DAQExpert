package rcms.utilities.daqexpert.report;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;

import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.persistence.PersistenceManager;

public class ReportManager {

	private final EntityManagerFactory entityManagerFactory;

	private static final Logger logger = Logger.getLogger(PersistenceManager.class);

	public ReportManager(EntityManagerFactory entityManagerFactory) {
		this.entityManagerFactory = entityManagerFactory;
	}

	public Report prepareReport() {
		Report report = new Report();

		Long threshold = 1000L;

		// 1. get the list of all run ongoing events
		List<Condition> a = getConditions(LogicModuleRegistry.StableBeams,
				DatatypeConverter.parseDateTime("2017-01-01T00:00:00Z").getTime(),
				DatatypeConverter.parseDateTime("2017-02-01T00:00:00Z").getTime(), threshold);

		logger.info("There are " + a.size() + " fills");
		for (Condition condition : a) {
			logger.info(" + " + condition.getTitle() + ", duration " + condition.getDuration() + "ms, starting: "
					+ condition.getStart());
		}
		long totalStableBeamTime = 0;

		for(Condition condition: a){
			totalStableBeamTime += condition.getDuration();
		}
		report.getValues().put("totalStableBeamTime", totalStableBeamTime);

		// 2. see how much rate non-zero
		// 3. see how much rate zero
		List<Condition> b = getConditionsInside(LogicModuleRegistry.NoRate, a, threshold);
		Long totalNoRateDuration = 0L;
		for(Condition condition: b){
			totalNoRateDuration += condition.getDuration();
		}
		logger.info("There are " + b.size() + " no rate events with total duration of " + totalNoRateDuration + "ms");
		report.getValues().put("totalNoRateDuration", totalNoRateDuration);
		
		report.getValues().put("totalUptime", (totalStableBeamTime - totalNoRateDuration));

		// 4. see how much of this rate zero were recovery actions (reseting
		// etc)
		List<Condition> c = getConditionsInside(LogicModuleRegistry.NoRateWhenExpected, b, threshold);
		Long totalNoRateWhenExpectedDuration = 0L;
		for(Condition condition: c){
			totalNoRateWhenExpectedDuration += condition.getDuration();
		}
		logger.info("There are " + b.size() + " no-rate-when-expected events with total duration of " + totalNoRateWhenExpectedDuration + "ms");
		report.getValues().put("totalDecisionTime", totalNoRateWhenExpectedDuration);
		report.getValues().put("totalRecoveryTime", (totalNoRateDuration -totalNoRateWhenExpectedDuration));
		
		report.getValues().put("totalUptime", (totalStableBeamTime - totalNoRateDuration));

		// 5. calculate the total down-time

		// 6. calculate avoidable down-time

		return report;
	}

	private List<Condition> getConditions(LogicModuleRegistry logicModule, Date startDate, Date endDate,
			Long durationThreshold) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		Session session = entityManager.unwrap(Session.class);

		Criteria elementsCriteria = session.createCriteria(Condition.class);
		// elementsCriteria.addOrder(Order.desc("start"));

		Disjunction disjunction = Restrictions.disjunction();

		// 1. Event finished
		Conjunction eventFinishedRestrictions = Restrictions.conjunction();
		eventFinishedRestrictions.add(Restrictions.le("start", endDate));
		eventFinishedRestrictions.add(Restrictions.ge("end", startDate));
		eventFinishedRestrictions.add(Restrictions.ge("duration", durationThreshold));
		disjunction.add(eventFinishedRestrictions);

		elementsCriteria.add(disjunction);

		// source LM
		elementsCriteria.add(Restrictions.eq("logicModule", logicModule));

		List<Condition> result = elementsCriteria.list();
		entityManager.close();

		return result;
	}

	private List<Condition> getConditionsInside(LogicModuleRegistry logicModule, List<Condition> conditions,
			Long durationThreshold) {
		List<Condition> result = new ArrayList<>();

		for (Condition condition : conditions) {
			List<Condition> subresult = getConditions(logicModule, condition.getStart(), condition.getEnd(),
					durationThreshold);
			logger.debug("Found " + subresult.size() + " conditions inside");
			result.addAll(subresult);
		}
		return result;
	}

}
