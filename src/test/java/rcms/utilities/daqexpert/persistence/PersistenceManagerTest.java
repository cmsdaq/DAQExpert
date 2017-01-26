package rcms.utilities.daqexpert.persistence;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonAnyFormatVisitor;

import rcms.utilities.daqexpert.reasoning.base.Entry;
import rcms.utilities.daqexpert.reasoning.base.enums.EventGroup;
import rcms.utilities.daqexpert.segmentation.DataResolution;

/**
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class PersistenceManagerTest {
	private static PersistenceManager pm;

	private static Date entry1Start = DatatypeConverter.parseDateTime("2017-01-17T10:35:00Z").getTime();
	private static Date entry2Start = DatatypeConverter.parseDateTime("2017-01-17T11:35:00Z").getTime();

	private static Date entry3Start = DatatypeConverter.parseDateTime("2017-01-17T10:35:03Z").getTime();
	private static Date entry4Start = DatatypeConverter.parseDateTime("2017-01-17T10:35:04Z").getTime();
	private static Date entry5Start = DatatypeConverter.parseDateTime("2017-01-17T11:35:05Z").getTime();
	private static Date entry6Start = DatatypeConverter.parseDateTime("2017-01-17T11:35:06Z").getTime();

	/**
	 * <pre>
	 * 2017-01-17
	 * - 10:30 left request limit
	 * - 10:35 start entry 1
	 * - 11:30 right request limit
	 * - 11:35 start entry 2
	 * - 11:35 start entry 3
	 * </pre>
	 */
	@BeforeClass
	public static void initializeObjects() {
		pm = new PersistenceManager("history-test");

		pm.persist(getFinishedEntry(entry1Start, "test1", 10000));
		pm.persist(getFinishedEntry(entry2Start, "test2", 10000));

		pm.persist(getFinishedEntry(entry3Start, "test3", 1000));
		pm.persist(getFinishedEntry(entry4Start, "test4", 1000));
		pm.persist(getFinishedEntry(entry5Start, "test5", 1000));
		pm.persist(getFinishedEntry(entry6Start, "test6", 1000));
	}

	/**
	 * 
	 * Tests if elements within the requested limit are returned
	 * 
	 * 
	 */
	@Test
	public void elementFilterTest() {

		Date ts = DatatypeConverter.parseDateTime("2017-01-17T10:30:00Z").getTime();
		Date te = DatatypeConverter.parseDateTime("2017-01-17T11:30:00Z").getTime();
		List<Entry> result = pm.getEntriesPlain(ts, te);

		Assert.assertEquals(3, result.size());
		Entry retrievedEntry = result.iterator().next();
		Assert.assertEquals("test1", retrievedEntry.getClassName());
		Assert.assertEquals(DatatypeConverter.parseDateTime("2017-01-17T10:35:10Z").getTime(), retrievedEntry.getEnd());
	}

	/**
	 * Tests if too short elements are filtered in the result
	 */
	@Test
	public void durationThresholdTest() {

		Date ts = DatatypeConverter.parseDateTime("2017-01-17T10:30:00Z").getTime();
		Date te = DatatypeConverter.parseDateTime("2017-01-17T13:30:00Z").getTime();

		List<Entry> resultWithoutLimit = pm.getEntriesPlain(ts, te);
		Assert.assertEquals(6, resultWithoutLimit.size());

		List<Entry> resultWithLimit = pm.getEntriesThreshold(ts, te, 5000);
		Assert.assertEquals(2, resultWithLimit.size());

	}

	/**
	 * Tests if tiny entries are included in the result
	 * 
	 * @throws JsonProcessingException
	 */
	@Test
	public void detailEntriesTest() throws JsonProcessingException {
		Date ts = DatatypeConverter.parseDateTime("2017-01-17T10:30:00Z").getTime();
		Date te = DatatypeConverter.parseDateTime("2017-01-17T13:30:00Z").getTime();

		List<TinyEntryMapObject> resultWithLimit = pm.getTinyEntriesMask(ts, te, 5000, true, DataResolution.Minute);

		ObjectMapper mapper = new ObjectMapper();

		for (TinyEntryMapObject result : resultWithLimit) {
			// System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
			//System.out.println(result);
		}

		Assert.assertEquals(2, resultWithLimit.size());
		TinyEntryMapObject retrievedEntry = resultWithLimit.iterator().next();
		Assert.assertEquals(2, retrievedEntry.getCount());
		Assert.assertEquals(entry3Start, retrievedEntry.getStart());

		Calendar cal = Calendar.getInstance();
		cal.setTime(entry4Start);
		cal.add(Calendar.MILLISECOND, 1000);
		Date endDate = cal.getTime();
		Assert.assertEquals(endDate, retrievedEntry.getEnd());

	}
	
	@Test
	public void entriesAndMaskTest(){
		Date ts = DatatypeConverter.parseDateTime("2017-01-17T10:30:00Z").getTime();
		Date te = DatatypeConverter.parseDateTime("2017-01-17T13:30:00Z").getTime();

		List<TinyEntryMapObject> resultWithLimit = pm.getTinyEntriesMask(ts, te, 5000, true, DataResolution.Hour);
		
	}
	
	

	private static Entry getFinishedEntry(Date startDate, String name, int duration) {
		Entry entry = new Entry();
		entry.setClassName(name);
		entry.setContent("Content of entry: " + name);
		entry.setStart(startDate);
		entry.setGroup(EventGroup.LHC_BEAM.getCode());

		Calendar cal = Calendar.getInstance();
		cal.setTime(startDate);
		cal.add(Calendar.MILLISECOND, duration);
		Date endDate = cal.getTime();
		entry.setEnd(endDate);
		entry.calculateDuration();

		return entry;
	}

}