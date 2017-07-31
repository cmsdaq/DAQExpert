package rcms.utilities.daqexpert.report;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.persistence.PersistenceManager;

public class ReportManager {

	private final EntityManagerFactory entityManagerFactory;

	private static final Logger logger = Logger.getLogger(PersistenceManager.class);

	private ObjectMapper objectMapper = new ObjectMapper();

	public ReportManager(EntityManagerFactory entityManagerFactory) {
		this.entityManagerFactory = entityManagerFactory;
	}

	public List<Long> getHistogram(LogicModuleRegistry logicModule, Date start, Date end, Long minThreshold,
			Long maxThreshold) {

		List<Long> result = new ArrayList<>();

		List<Condition> insideStableBeams = getConditions(LogicModuleRegistry.StableBeams, start, end, minThreshold);

		List<Condition> a = getConditions(logicModule, start, end, minThreshold);
		//List<Condition> a = getConditionsInside(logicModule,insideStableBeams, minThreshold);

		for (Condition condition : a) {
			if (condition.getDuration() < maxThreshold) {
				result.add(condition.getDuration() / 1000);
			} else {
				logger.info("Ignoring entry of duration " + condition.getDuration() / 1000 / 60 / 24 + "h");
			}
		}

		logger.info("Returning " + result.size() + " set to prepare the histogram");

		return result;

	}

	public ArrayNode getProblemSubSystems(Date start, Date end, Long minThreshold) {
		ArrayNode arrayNode = objectMapper.createArrayNode();

		List<String> subsystems = Arrays.asList("ECAL", "HCAL", "CSC", "CTPPS", "TRACKER", "SCAL", "RPC", "CTPPS_TOT",
				"ES", "TCDS", "DT", "HF", "PIXEL", "TRG");

		Map<String, Long> cumulatedTimes = new HashMap<>();
		Long totalTime = 0L;

		List<Condition> insideStableBeams = getConditions(LogicModuleRegistry.StableBeams, start, end, minThreshold);

		for (LogicModuleRegistry lmr : LogicModuleRegistry.getIdentifiedProblems()) {

			logger.trace("Getting statistics for LM " + lmr.name());
			List<Condition> a = getConditionsInside(lmr, insideStableBeams, minThreshold);
			// List<Condition> a = getConditions(lmr, start, end, minThreshold);
			for (Condition c : a) {
				for (String subsystem : subsystems) {
					if (c.getDescription().contains(subsystem)) {
						if (!cumulatedTimes.containsKey(subsystem)) {
							cumulatedTimes.put(subsystem, 0L);
						}
						Long current = cumulatedTimes.get(subsystem);
						cumulatedTimes.put(subsystem, current + c.getDuration());
					}
				}
			}

		}

		for (Entry<String, Long> x : cumulatedTimes.entrySet()) {
			totalTime += x.getValue();
		}

		for (Entry<String, Long> subsystemTimes : cumulatedTimes.entrySet()) {

			float percentage = (subsystemTimes.getValue() * 100) / totalTime;

			ObjectNode objectNode1 = objectMapper.createObjectNode();
			objectNode1.put("name", subsystemTimes.getKey());
			objectNode1.put("y", percentage);

			logger.info("part of response json built: " + objectNode1.toString());
			arrayNode.add(objectNode1);
		}

		return arrayNode;
	}

	public ArrayNode getProblemPieChart(Date start, Date end, Long minThreshold) {
		ArrayNode arrayNode = objectMapper.createArrayNode();

		Map<String, Long> cumulatedTimes = new HashMap<>();
		Long totalTime = 0L;

		List<Condition> insideStableBeams = getConditions(LogicModuleRegistry.StableBeams, start, end, minThreshold);
		
		for (LogicModuleRegistry lmr : LogicModuleRegistry.getIdentifiedProblems()) {

			logger.trace("Getting statistics for LM " + lmr.name());
			//List<Condition> a = getConditions(lmr, start, end, minThreshold);
			List<Condition> a = getConditionsInside(lmr, insideStableBeams, minThreshold);
			Long cumulatedTime = 0L;
			for (Condition c : a) {
				cumulatedTime += c.getDuration();
			}
			if (!a.isEmpty()) {
				cumulatedTimes.put(a.iterator().next().getLogicModule().getLogicModule().getName(), cumulatedTime);
				totalTime += cumulatedTime;

			}
			logger.trace("Total time of " + lmr.name() + " is " + cumulatedTime + " ms");
		}

		for (Entry<String, Long> timePerLm : cumulatedTimes.entrySet()) {

			float percentage = (timePerLm.getValue() * 100) / totalTime;

			ObjectNode objectNode1 = objectMapper.createObjectNode();
			objectNode1.put("name", timePerLm.getKey());
			objectNode1.put("y", percentage);

			logger.info("part of response json built: " + objectNode1.toString());
			arrayNode.add(objectNode1);
		}

		return arrayNode;
	}

	public Pair<ArrayNode, ArrayNode> getDowntimeStatistics(Date start, Date end) {

		KeyValueReport kvr = getKeyValueStatistics(start, end);
		ArrayNode efficiency = getEfficiency(kvr);
		ArrayNode nature = getDowntimeNature(kvr);

		return Pair.of(efficiency, nature);

	}

	public ArrayNode getEfficiency(KeyValueReport kvr) {
		ArrayNode arrayNode = objectMapper.createArrayNode();

		float totalValue = 0;
		float uptimeValue = 0;
		float downtimeValue = 0;

		uptimeValue = kvr.getValues().get("totalUptime");
		totalValue = kvr.getValues().get("totalStableBeamTime");

		if (totalValue > 0) {
			downtimeValue = totalValue - uptimeValue;

			float uptimePercentage = (100 * uptimeValue) / totalValue;
			float downtimePercentage = (100 * downtimeValue) / totalValue;

			ObjectNode uptime = objectMapper.createObjectNode();
			uptime.put("name", "uptime");
			uptime.put("y", uptimePercentage);

			ObjectNode downtime = objectMapper.createObjectNode();
			downtime.put("name", "downtime");
			downtime.put("y", downtimePercentage);

			arrayNode.add(uptime);
			arrayNode.add(downtime);
		}

		return arrayNode;
	}

	public ArrayNode getDowntimeNature(KeyValueReport kvr) {
		ArrayNode arrayNode = objectMapper.createArrayNode();

		float totalValue = 0;
		float decisionTime = 0;
		float recoveryTime = 0;

		decisionTime = kvr.getValues().get("totalDecisionTime");
		recoveryTime = kvr.getValues().get("totalRecoveryTime");

		if (decisionTime > 0 || recoveryTime > 0) {
			totalValue = decisionTime + recoveryTime;

			float decisionPercentage = (100 * decisionTime) / totalValue;
			float recoveryPercentage = (100 * recoveryTime) / totalValue;

			ObjectNode uptime = objectMapper.createObjectNode();
			uptime.put("name", "reaction time");
			uptime.put("y", decisionPercentage);

			ObjectNode downtime = objectMapper.createObjectNode();
			downtime.put("name", "recovery time");
			downtime.put("y", recoveryPercentage);

			arrayNode.add(uptime);
			arrayNode.add(downtime);
		}

		return arrayNode;
	}

	public KeyValueReport getKeyValueStatistics(Date start, Date end) {
		KeyValueReport report = new KeyValueReport();

		Long threshold = 1000L;

		// 1. get the list of all run ongoing events
		List<Condition> a = getConditions(LogicModuleRegistry.StableBeams, start, end, threshold);

		logger.info("There are " + a.size() + " fills");
		for (Condition condition : a) {
			logger.info(" + " + condition.getTitle() + ", duration " + condition.getDuration() + "ms, starting: "
					+ condition.getStart());
		}
		long totalStableBeamTime = 0;

		for (Condition condition : a) {
			totalStableBeamTime += condition.getDuration();
		}
		report.getValues().put("totalStableBeamTime", totalStableBeamTime);

		// 2. see how much rate non-zero
		// 3. see how much rate zero
		List<Condition> b = getConditionsInside(LogicModuleRegistry.NoRate, a, threshold);
		Long totalNoRateDuration = 0L;
		for (Condition condition : b) {
			totalNoRateDuration += condition.getDuration();
		}
		logger.info("There are " + b.size() + " no rate events with total duration of " + totalNoRateDuration + "ms");
		report.getValues().put("totalNoRateDuration", totalNoRateDuration);

		report.getValues().put("totalUptime", (totalStableBeamTime - totalNoRateDuration));

		// 4. see how much of this rate zero were recovery actions (reseting
		// etc)
		List<Condition> c = getConditionsInside(LogicModuleRegistry.NoRateWhenExpected, b, threshold);
		Long totalNoRateWhenExpectedDuration = 0L;
		for (Condition condition : c) {
			totalNoRateWhenExpectedDuration += condition.getDuration();
		}
		logger.info("There are " + b.size() + " no-rate-when-expected events with total duration of "
				+ totalNoRateWhenExpectedDuration + "ms");
		report.getValues().put("totalDecisionTime", totalNoRateWhenExpectedDuration);
		report.getValues().put("totalRecoveryTime", (totalNoRateDuration - totalNoRateWhenExpectedDuration));

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
