package rcms.utilities.daqexpert.persistence;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import rcms.utilities.daqexpert.reasoning.base.Entry;

public class PersistenceManager {

	private final EntityManagerFactory entityManagerFactory;
	private final EntityManager entityManager;

	public PersistenceManager(String persistenceUnitName) {
		entityManagerFactory = Persistence.createEntityManagerFactory(persistenceUnitName);
		entityManager = entityManagerFactory.createEntityManager();
	}
	
	public void persist(Entry entry) {
		entityManager.getTransaction().begin();
		entityManager.persist(entry);
		entityManager.getTransaction().commit();
	}

	@SuppressWarnings("unchecked")
	public List<Entry> getEntries(Date startDate, Date endDate) {

		// TODO: close session?
		Session session = entityManager.unwrap(Session.class);

		Criteria elementsCriteria = session.createCriteria(Entry.class);
		elementsCriteria.addOrder(Order.desc("start"));
		elementsCriteria.add(Restrictions.between("start", startDate, endDate));

		return elementsCriteria.list();
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
