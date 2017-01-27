package rcms.utilities.daqexpert.persistence;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;

import com.fasterxml.jackson.databind.ObjectMapper;

import rcms.utilities.daqexpert.reasoning.base.enums.EventPriority;
import rcms.utilities.daqexpert.segmentation.DataResolution;
import rcms.utilities.daqexpert.segmentation.RangeResolver;

public class PersistenceManager {

	private final EntityManagerFactory entityManagerFactory;
	private final EntityManager entityManager;

	private static final Logger logger = Logger.getLogger(PersistenceManager.class);

	public PersistenceManager(String persistenceUnitName) {
		entityManagerFactory = Persistence.createEntityManagerFactory(persistenceUnitName);
		entityManager = entityManagerFactory.createEntityManager();
	}

	public void persist(Set<Entry> points) {

		Session session = entityManager.unwrap(Session.class);
		Transaction tx = session.beginTransaction();
		for (Entry point : points) {

			if (point.isShow())
				session.save(point);
		}
		tx.commit();
		// session.close();
	}

	/**
	 * Persist analysis entry
	 * 
	 * @param entry
	 */
	public void persist(Entry entry) {
		entityManager.getTransaction().begin();
		entityManager.persist(entry);
		entityManager.getTransaction().commit();
	}

	public void persist(Point test) {
		entityManager.getTransaction().begin();
		entityManager.persist(test);
		entityManager.getTransaction().commit();
	}

	public void persist2(List<Point> points) {

		entityManager.getTransaction().begin();
		for (Point point : points) {

			entityManager.persist(point);
		}
		entityManager.getTransaction().commit();
	}

	public void persist(List<Point> points) {

		Session session = entityManager.unwrap(Session.class);
		Transaction tx = session.beginTransaction();
		for (Point point : points) {

			session.save(point);
		}
		tx.commit();
		// session.close();
	}

	public List<Point> getRawData(Date startDate, Date endDate) {

		RangeResolver rangeResolver = new RangeResolver();
		DataResolution resolution = rangeResolver.resolve(startDate, endDate);

		logger.info("resolution of data to RAW API " + resolution);

		// extend slightly timespan so that few snapshots more on the left and
		// right are loaded to the chart - avoid cutting the chart lines
		Calendar c = Calendar.getInstance();
		c.setTime(startDate);
		c.add(Calendar.SECOND, -10);
		startDate = c.getTime();
		c.setTime(endDate);
		c.add(Calendar.SECOND, 10);
		endDate = c.getTime();

		// TODO: close session?
		Session session = entityManager.unwrap(Session.class);

		Criteria elementsCriteria = session.createCriteria(Point.class);

		elementsCriteria.add(Restrictions.le("x", endDate));
		elementsCriteria.add(Restrictions.ge("x", startDate));
		elementsCriteria.add(Restrictions.eq("resolution", resolution.ordinal()));
		List<Point> result = elementsCriteria.list();
		logger.debug(result.size() + " points of resolution " + resolution.ordinal() + "(" + resolution + ") retrieved");

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
	public List<Entry> getEntries(Date startDate, Date endDate, long durationThreshold,
			boolean includeTinyEntriesMask) {
		// TODO: close session?
		Session session = entityManager.unwrap(Session.class);

		Criteria elementsCriteria = session.createCriteria(Entry.class);
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

		return elementsCriteria.list();
	}

	public List<TinyEntryMapObject> getTinyEntriesMask(Date startDate, Date endDate, long durationThreshold,
			boolean includeTinyEntriesMask, DataResolution resolution) {
		// TODO: close session?

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
	public List<Entry> getEntriesWithMask(Date startDate, Date endDate) {
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

		List<Entry> thresholdData = getEntriesThreshold(startDate, endDate, durationThreshold);
		logger.debug("Data resolution: " + dr);
		logger.debug("Retrieved " + thresholdData.size() + " thresholded entries");

		List<TinyEntryMapObject> tinyData = getTinyEntriesMask(startDate, endDate, durationThreshold, true, dr);

		logger.debug("Retrieved " + tinyData.size() + " masked entries: " + tinyData);

		for (TinyEntryMapObject mapObject : tinyData) {
			Entry curr = new Entry();
			curr.setStart(mapObject.getStart());
			curr.setEnd(mapObject.getEnd());
			curr.setContent(Long.toString(mapObject.getCount()));
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
	public List<Entry> getEntries2(Date startDate, Date endDate, long durationThreshold,
			boolean includeTinyEntriesMask) {
		// TODO: close session?
		Session session = entityManager.unwrap(Session.class);

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Entry> cq = cb.createQuery(Entry.class);
		Root<Entry> entry = cq.from(Entry.class);
		cq.groupBy(entry.get("group_name"));

		Criteria elementsCriteria = session.createCriteria(Entry.class);
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

		return elementsCriteria.list();
	}

	/**
	 * Returns entries without any duration threshold
	 * 
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public List<Entry> getEntriesPlain(Date startDate, Date endDate) {
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
	public List<Entry> getEntriesThreshold(Date startDate, Date endDate, long threshold) {
		return getEntries(startDate, endDate, threshold, false);
	}

	public Entry getEntryById(Long id) {

		Entry entry = entityManager.find(Entry.class, id);
		return entry;
	}

	public List<Entry> getEntries2(Date start, Date end) {
		entityManager.getTransaction().begin();
		List<Entry> result = entityManager.createQuery("from Entry", Entry.class).getResultList();
		for (Entry event : result) {
			System.out.println("Event (" + event.getStart() + ") : " + event.getContent());
		}
		entityManager.getTransaction().commit();
		entityManager.close();
		return result;
	}

}
