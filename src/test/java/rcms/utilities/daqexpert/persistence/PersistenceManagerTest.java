package rcms.utilities.daqexpert.persistence;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.Test;

public class PersistenceManagerTest {

	@Test
	public void test() {

		Entry entry = new Entry();
		entry.setContent("test entry");
		entry.setStart(new Date());

		EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("history");
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();

		entityManager.persist(entry);

		entityManager.getTransaction().commit();
		entityManager.close();

	}

	@Test
	public void test2() {
		EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("history");
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();
		List<Entry> result = entityManager.createQuery("from Event", Entry.class).getResultList();
		for (Entry event : result) {
			System.out.println("Event (" + event.getStart() + ") : " + event.getContent());
		}
		entityManager.getTransaction().commit();
		entityManager.close();
	}

}
