package rcms.utilities.daqexpert.persistence;

import java.util.Date;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.junit.Assert;
import org.junit.Test;

public class PersistenceManagerTest {

	@Test
	public void simpleTest() {

		PersistenceManager pm = new PersistenceManager("history-test");

		Date t1 = DatatypeConverter.parseDateTime("2017-01-17T10:35:00Z").getTime();
		Date t2 = DatatypeConverter.parseDateTime("2017-01-17T11:35:00Z").getTime();

		pm.persist(getTestObject(t1, "test1"));
		pm.persist(getTestObject(t2, "test2"));

		Date ts = DatatypeConverter.parseDateTime("2017-01-17T10:30:00Z").getTime();
		Date te = DatatypeConverter.parseDateTime("2017-01-17T11:30:00Z").getTime();
		List<Entry> result = pm.getEntries(ts, te);

		Assert.assertEquals(1, result.size());
	}

	private Entry getTestObject(Date date, String content) {
		Entry entry = new Entry();
		entry.setContent(content);
		entry.setStart(date);
		return entry;
	}

}
