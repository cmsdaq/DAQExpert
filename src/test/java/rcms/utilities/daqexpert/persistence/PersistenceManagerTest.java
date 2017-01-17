package rcms.utilities.daqexpert.persistence;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.junit.Assert;
import org.junit.Test;

public class PersistenceManagerTest {

	@Test
	public void simpleTest() {

		PersistenceManager pm = new PersistenceManager("history-test");

		Date entry1Start = DatatypeConverter.parseDateTime("2017-01-17T10:35:00Z").getTime();
		Date entry2Start = DatatypeConverter.parseDateTime("2017-01-17T11:35:00Z").getTime();

		pm.persist(getTestObject(entry1Start, "test1", 1000));
		pm.persist(getTestObject(entry2Start, "test2", 1000));

		Date ts = DatatypeConverter.parseDateTime("2017-01-17T10:30:00Z").getTime();
		Date te = DatatypeConverter.parseDateTime("2017-01-17T11:30:00Z").getTime();
		List<Entry> result = pm.getEntries(ts, te);

		Assert.assertEquals(1, result.size());
		Entry retrievedEntry = result.iterator().next();
		Assert.assertEquals("test1", retrievedEntry.getName());
		Assert.assertEquals(DatatypeConverter.parseDateTime("2017-01-17T10:35:01Z").getTime(), retrievedEntry.getEnd());
	}

	private Entry getTestObject(Date startDate, String name, int duration) {
		Entry entry = new Entry();
		entry.setName(name);
		entry.setContent("Content of entry: " + name);
		entry.setDuration(duration);
		entry.setStart(startDate);

		Calendar cal = Calendar.getInstance();
		cal.setTime(startDate);
		cal.add(Calendar.MILLISECOND, duration);
		Date endDate = cal.getTime();

		entry.setEnd(endDate);

		return entry;
	}

}
