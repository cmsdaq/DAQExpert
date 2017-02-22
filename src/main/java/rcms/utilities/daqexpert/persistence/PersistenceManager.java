package rcms.utilities.daqexpert.persistence;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

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
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;

import com.fasterxml.jackson.databind.ObjectMapper;

import rcms.utilities.daqexpert.reasoning.base.enums.EventPriority;
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

	private static final Logger logger = Logger.getLogger(PersistenceManager.class);

	private final EntityManager entryEntityManager;

	public PersistenceManager(String persistenceUnitName, Properties props) {

		entityManagerFactory = Persistence.createEntityManagerFactory(persistenceUnitName, props);
		entryEntityManager = entityManagerFactory.createEntityManager();
	}

	/**
	 * Persiste multipe entries in one transaction
	 * 
	 * @param entries
	 */
	public void persist(Set<Condition> entries) {

		EntityTransaction tx = entryEntityManager.getTransaction();
		tx.begin();
		for (Condition point : entries) {

			if (point.isShow())
				entryEntityManager.persist(point);
		}
		tx.commit();
	}

	/**
	 * Persist analysis entry
	 * 
	 * @param entry
	 */
	public void persist(Condition entry) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		EntityTransaction tx = entityManager.getTransaction();
		tx.begin();
		entityManager.persist(entry);
		tx.commit();
		entityManager.close();
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
			boolean includeTinyEntriesMask) {
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
		elementsCriteria.add(Restrictions.ne("group", "hidden"));

		List<Condition> result = elementsCriteria.list();
		entityManager.close();

		return result;
	}

	public List<TinyEntryMapObject> getTinyEntriesMask(Date startDate, Date endDate, long durationThreshold,
			boolean includeTinyEntriesMask, DataResolution resolution) {
		// TODO: close session?
		EntityManager entityManager = entityManagerFactory.createEntityManager();

		List<TinyEntryMapObject> resultTiny = new ArrayList<TinyEntryMapObject>();
		StringBuilder sb = new StringBuilder();
		sb.append("select e.group, count(e.id), min(e.start), max(e.end) from Entry e ");
		sb.append("where duration < :threshold ");
		sb.append("and start_date < :endDate ");
		sb.append("and end_date > :startDate ");
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
			curr.setGroup((String) row[0]);
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
		int elementsInRow = 100;
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

		for (TinyEntryMapObject mapObject : tinyData) {
			Condition curr = new Condition();
			curr.setStart(mapObject.getStart());
			curr.setEnd(mapObject.getEnd());
			curr.setTitle(Long.toString(mapObject.getCount()));
			curr.setGroup(mapObject.getGroup());
			curr.setClassName(EventPriority.FILTERED.getCode());
			curr.calculateDuration();

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
	public List<Condition> getEntries2(Date startDate, Date endDate, long durationThreshold,
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
		return getEntries(startDate, endDate, 0, false);
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
		return getEntries(startDate, endDate, threshold, false);
	}

	public Condition getEntryById(Long id) {

		EntityManager entityManager = entityManagerFactory.createEntityManager();

		Condition entry = entityManager.find(Condition.class, id);
		
		/* Make sure to fetch all list - but dont want to annotate the main class to be EAGER always */
		Iterator<String> it = entry.getActionSteps().iterator();
		while(it.hasNext()){
			it.next();
		}
		entityManager.close();
		return entry;
	}

	public List<Condition> getEntries2(Date start, Date end) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();
		List<Condition> result = entityManager.createQuery("from Entry", Condition.class).getResultList();
		for (Condition event : result) {
			System.out.println("Event (" + event.getStart() + ") : " + event.getTitle());
		}
		entityManager.getTransaction().commit();
		entityManager.close();
		return result;
	}

}
