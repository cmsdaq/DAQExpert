package rcms.utilities.daqexpert.persistence;

import java.sql.Timestamp;
import java.util.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.hibernate.transform.Transformers;
import rcms.utilities.daqexpert.processing.context.OptionalContextEntry;
import rcms.utilities.daqexpert.reasoning.base.ActionLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;
import rcms.utilities.daqexpert.segmentation.DataResolution;
import rcms.utilities.daqexpert.segmentation.RangeResolver;

/**
 * Unit managing persistence of analysis results and multiple resolutions of raw
 * parameters
 * 
 * 
 * Performance test: Time to insert 10000 Entries individually was 55315 ms
 * Performance test: Time to insert 10000 Entries at once was 456 ms
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class PersistenceManager {

	private final EntityManagerFactory entityManagerFactory;
	
	private EntityManager entityManager;

	private static final Logger logger = Logger.getLogger(PersistenceManager.class);

	public PersistenceManager(EntityManagerFactory entityManagerFactory) {
		this.entityManagerFactory = entityManagerFactory;
	}

	private void avoidPersistingOptionalContext(Condition c){
		if(c.getContext() != null){
			Set<String> keysToRemove = new HashSet<>();
			c.getContext().forEach((key, value) -> {
				if(value instanceof OptionalContextEntry){
					logger.info("Avoiding persistence of optional context for condition " + c.getTitle() + " under key; " + key);
					keysToRemove.add(key);
				}
			});
			keysToRemove.forEach(key -> c.getContext().remove(key));
		}
	}

	/**
	 * Persiste multipe entries in one transaction
	 * 
	 * @param entries
	 */
	public void persist(Set<Condition> entries) {
		ensureConditionEntityManagerOpen();
		EntityTransaction tx = entityManager.getTransaction();
		tx.begin();
		Condition latest = null;
		for (Condition e : entries) {
			if(e.getStart() != null){
				if(latest == null || latest.getStart().getTime() < e.getStart().getTime()){
					latest = e;
				}
			}
		}
		if(latest != null) {
			logger.info("Most recent condition to persist: " + latest.getStart().toString());
		}

		for (Condition point : entries) {

			avoidPersistingOptionalContext(point);
			if (point.isShow()) {
				entityManager.persist(point);
			}
		}
		tx.commit();
		//entityManager.close();
	}
	
	private void ensureConditionEntityManagerOpen(){
		if(entityManager == null || ! entityManager.isOpen()){
			entityManager = entityManagerFactory.createEntityManager();
		}
	}

	/**
	 * Persist analysis entry
	 * 
	 * @param entry
	 */
	public void persist(Condition entry) {
		avoidPersistingOptionalContext(entry);
		ensureConditionEntityManagerOpen();
		EntityTransaction tx = entityManager.getTransaction();
		tx.begin();
		entityManager.persist(entry);
		tx.commit();
		//entityManager.close();
	}

	public void persist(Point test) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		EntityTransaction tx = entityManager.getTransaction();
		tx.begin();
		entityManager.persist(test);
		tx.commit();
		entityManager.close();
	}

	public void persist(List<Point> points) {

		EntityManager entityManager = entityManagerFactory.createEntityManager();
		EntityTransaction tx = entityManager.getTransaction();
		tx.begin();
		for (Point point : points) {

			entityManager.persist(point);
		}
		tx.commit();
		entityManager.close();
	}

	public List<Point> getRawData(Date startDate, Date endDate) {

		RangeResolver rangeResolver = new RangeResolver();
		DataResolution resolution = rangeResolver.resolve(startDate, endDate);
		return getRawData(startDate, endDate, resolution);
	}

	public List<Point> getRawData(Date startDate, Date endDate, DataResolution resolution) {

		logger.debug("resolution of data to RAW API " + resolution);

		// extend slightly timespan so that few snapshots more on the left and
		// right are loaded to the chart - avoid cutting the chart lines
		Calendar c = Calendar.getInstance();
		c.setTime(startDate);
		c.add(Calendar.SECOND, -10);
		startDate = c.getTime();
		c.setTime(endDate);
		c.add(Calendar.SECOND, 10);
		endDate = c.getTime();

		EntityManager entityManager = entityManagerFactory.createEntityManager();
		Session session = entityManager.unwrap(Session.class);

		Criteria elementsCriteria = session.createCriteria(Point.class);

		elementsCriteria.add(Restrictions.le("x", endDate));
		elementsCriteria.add(Restrictions.ge("x", startDate));
		elementsCriteria.add(Restrictions.eq("resolution", resolution.ordinal()));
		List<Point> result = elementsCriteria.list();
		logger.debug(
				result.size() + " points of resolution " + resolution.ordinal() + "(" + resolution + ") retrieved");

		entityManager.close();
		return result;
	}

	/**
	 * Get entries with threshold. Inclusion of filtered entries as mask is
	 * parameterized
	 * 
	 * @param startDate
	 * @param endDate
	 * @param durationThreshold
	 * @param includeTinyEntriesMask
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Condition> getEntries(Date startDate, Date endDate, long durationThreshold,
			boolean includeTinyEntriesMask, boolean withDescription) {
		// TODO: close session?
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

		// 2. Event unfinished
		Conjunction eventUninishedRestrictions = Restrictions.conjunction();
		eventUninishedRestrictions.add(Restrictions.le("start", endDate));
		eventUninishedRestrictions.add(Restrictions.isNull("end"));
		disjunction.add(eventUninishedRestrictions);

		elementsCriteria.add(disjunction);

		// Events not hidden
		elementsCriteria.add(Restrictions.ne("group", ConditionGroup.HIDDEN));

		ProjectionList projection = Projections.projectionList()
				.add(Projections.property("id"),"id")
				.add(Projections.property("title"), "title")
				.add(Projections.property("start"),"start")
				.add(Projections.property("end"),"end")
				.add(Projections.property("group"),"group")
				.add(Projections.property("mature"),"mature")
				.add(Projections.property("priority"),"priority")
				.add(Projections.property("duration"),"duration");

		if(withDescription){
				projection.add(Projections.property("description"),"description");
		}

		List<Condition> result = elementsCriteria.setProjection(projection)
				.setResultTransformer(Transformers.aliasToBean(Condition.class)).list();

		entityManager.close();

		return result;
	}

	public List<TinyEntryMapObject> getTinyEntriesMask(Date startDate, Date endDate, long durationThreshold,
			boolean includeTinyEntriesMask, DataResolution resolution) {
		// TODO: close session?
		EntityManager entityManager = entityManagerFactory.createEntityManager();

		List<TinyEntryMapObject> resultTiny = new ArrayList<TinyEntryMapObject>();
		StringBuilder sb = new StringBuilder();
		sb.append("select e.group, count(e.id), min(e.start), max(e.end) from Condition e ");
		sb.append("where duration < :threshold ");
		sb.append("and start_date < :endDate ");
		sb.append("and end_date > :startDate ");
		sb.append("and mature = true ");
		sb.append("group by GROUP_NAME ");

		switch (resolution) {

		case Month:
			sb.append(",YEAR (start_date) ");
			break;
		case Day:
			sb.append(",YEAR (start_date) ");
			sb.append(",EXTRACT(MONTH FROM start_date) ");
			break;
		case Hour:
			sb.append(",YEAR (start_date) ");
			sb.append(",EXTRACT(MONTH FROM start_date) ");
			sb.append(",EXTRACT(DAY FROM start_date) ");
			break;
		case Minute:
			sb.append(",YEAR (start_date) ");
			sb.append(",EXTRACT(MONTH FROM start_date) ");
			sb.append(",EXTRACT(DAY FROM start_date) ");
			sb.append(",EXTRACT(HOUR FROM start_date) ");
			break;
		case Full:
			sb.append(",YEAR (start_date) ");
			sb.append(",EXTRACT(MONTH FROM start_date) ");
			sb.append(",EXTRACT(DAY FROM start_date) ");
			sb.append(",EXTRACT(HOUR FROM start_date) ");
			sb.append(",EXTRACT(MINUTE FROM start_date) ");
		}

		ObjectMapper mapper = new ObjectMapper();
		Query q2 = entityManager.createQuery(sb.toString());

		q2.setParameter("endDate", endDate);
		q2.setParameter("startDate", startDate);
		q2.setParameter("threshold", durationThreshold);
		// Query q2 = entityManager.createQuery("SELECT c FROM Country c");
		List<Object[]> result = q2.getResultList();
		Calendar calendar = Calendar.getInstance();
		for (Object[] row : result) {
			// System.out.println(result);
			TinyEntryMapObject curr = new TinyEntryMapObject();

			ConditionGroup group = (ConditionGroup) row[0];
			curr.setGroup(group);
			curr.setCount((long) row[1]);

			calendar.setTimeInMillis(((Timestamp) row[2]).getTime());
			curr.setStart(calendar.getTime());
			calendar.setTimeInMillis(((Timestamp) row[3]).getTime());
			curr.setEnd(calendar.getTime());
			resultTiny.add(curr);
		}
		entityManager.close();
		return resultTiny;

	}

	/**
	 * Gets entries with a threshold on duration and a pseudo entries
	 * representing small, invisible entries.
	 * 
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public List<Condition> getEntriesWithMask(Date startDate, Date endDate) {
		DataResolution dr = RangeResolver.resolve(startDate, endDate);
		/*
		 * minimum width for filtering the blocks is calculated based on this.
		 * This amount of sequential blocks of the same width is the threshold
		 */
		int elementsInRow = 80;
		/*
		 * Filter entries based on the duration and requested range
		 */
		long rangeInMs = endDate.getTime() - startDate.getTime();
		long durationThreshold = rangeInMs / elementsInRow;
		logger.debug("Duration thresshold: " + durationThreshold);

		List<Condition> thresholdData = getEntriesThreshold(startDate, endDate, durationThreshold);
		logger.debug("Data resolution: " + dr);
		logger.debug("Retrieved " + thresholdData.size() + " thresholded entries");

		List<TinyEntryMapObject> tinyData = getTinyEntriesMask(startDate, endDate, durationThreshold, true, dr);

		logger.debug("Retrieved " + tinyData.size() + " masked entries: " + tinyData);

		long filterId = 0;
		for (TinyEntryMapObject mapObject : tinyData) {
			Condition curr = new Condition();
			curr.setMature(true);
			curr.setStart(mapObject.getStart());
			curr.setEnd(mapObject.getEnd());
			curr.setTitle(Long.toString(mapObject.getCount()));
			curr.setLogicModule(mapObject.getLogicModule());
			curr.setGroup(mapObject.getGroup());
			curr.setClassName(ConditionPriority.FILTERED);
			curr.calculateDuration();
			curr.setId(-filterId++);

			if (curr.getDuration() < durationThreshold) {
				int append = (int) (durationThreshold - curr.getDuration());

				Date alteredStart = new Date(curr.getStart().getTime() - append / 2);
				Date alteredEnd = new Date(curr.getEnd().getTime() + append / 2);
				curr.setStart(alteredStart);
				curr.setEnd(alteredEnd);
				curr.calculateDuration();
			}

			thresholdData.add(curr);
		}

		return thresholdData;
	}

	@SuppressWarnings("unchecked")
	public List<Condition> agetEntries2(Date startDate, Date endDate, long durationThreshold,
			boolean includeTinyEntriesMask) {

		EntityManager entityManager = entityManagerFactory.createEntityManager();
		Session session = entityManager.unwrap(Session.class);

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Condition> cq = cb.createQuery(Condition.class);
		Root<Condition> entry = cq.from(Condition.class);
		cq.groupBy(entry.get("group_name"));

		Criteria elementsCriteria = session.createCriteria(Condition.class);
		// elementsCriteria.addOrder(Order.desc("start"));

		Disjunction disjunction = Restrictions.disjunction();

		// 1. Event finished
		Conjunction eventFinishedRestrictions = Restrictions.conjunction();
		eventFinishedRestrictions.add(Restrictions.le("start", endDate));
		eventFinishedRestrictions.add(Restrictions.ge("end", startDate));
		eventFinishedRestrictions.add(Restrictions.ge("duration", durationThreshold));
		disjunction.add(eventFinishedRestrictions);

		// 2. Event unfinished
		Conjunction eventUninishedRestrictions = Restrictions.conjunction();
		eventUninishedRestrictions.add(Restrictions.le("start", endDate));
		eventUninishedRestrictions.add(Restrictions.isNull("end"));
		disjunction.add(eventUninishedRestrictions);

		elementsCriteria.add(disjunction);

		if (includeTinyEntriesMask) {
			// elementsCriteria.add
		}

		// Events not hidden
		elementsCriteria.add(Restrictions.ne("group", "hidden"));

		List<Condition> result = elementsCriteria.list();
		entityManager.close();
		return result;
	}

	/**
	 * Returns entries without any duration threshold
	 * 
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public List<Condition> getEntriesPlain(Date startDate, Date endDate) {
		return getEntries(startDate, endDate, 0, false, false);
	}

	/**
	 * Get entries with threshold
	 * 
	 * @param startDate
	 * @param endDate
	 * @param threshold
	 * @return
	 */
	public List<Condition> getEntriesThreshold(Date startDate, Date endDate, long threshold) {
		return getEntries(startDate, endDate, threshold, false,false);
	}

	public Condition getEntryById(Long id) {

		EntityManager entityManager = entityManagerFactory.createEntityManager();

		Condition entry = entityManager.find(Condition.class, id);

		/*
		 * Make sure to fetch all list - but dont want to annotate the main
		 * class to be EAGER always
		 */
		Iterator<String> it = entry.getActionSteps().iterator();
		while (it.hasNext()) {
			it.next();
		}
		entityManager.close();
		return entry;
	}

	public List<Condition> agetEntries2(Date start, Date end) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();
		List<Condition> result = entityManager.createQuery("from Conditions", Condition.class).getResultList();
		for (Condition event : result) {
			System.out.println("Event (" + event.getStart() + ") : " + event.getTitle());
		}
		entityManager.getTransaction().commit();
		entityManager.close();
		return result;
	}

	public Date getLastFinish() {
		Date endDate = null;

		EntityManager entityManager = entityManagerFactory.createEntityManager();
		Session session = entityManager.unwrap(Session.class);

		Criteria elementsCriteria = session.createCriteria(Condition.class);

		elementsCriteria.add(Restrictions.eq("group", ConditionGroup.EXPERT_VERSION));
		elementsCriteria.addOrder(Order.desc("end"));
		elementsCriteria.setMaxResults(1);

		List<Condition> result = elementsCriteria.list();

		entityManager.close();

		if (result.size() >= 1) {
			Condition lastVersion = result.iterator().next();
			logger.info("Last version: " + lastVersion.getTitle() + " finished on " + lastVersion.getEnd());
			endDate = lastVersion.getEnd();
		}

		return endDate;
	}

	public List<Condition> getLastActionConditions() {

		EntityManager entityManager = entityManagerFactory.createEntityManager();
		Session session = entityManager.unwrap(Session.class);

		Criteria elementsCriteria = session.createCriteria(Condition.class);

		elementsCriteria.add(Restrictions.eq("group", ConditionGroup.FLOWCHART));
		elementsCriteria.addOrder(Order.desc("end"));
		elementsCriteria.setMaxResults(50);

		List<Condition> result = elementsCriteria.list();
		for (Condition c : result) {
			c.getActionSteps().size();
		}

		entityManager.close();

		return result;
	}

}
