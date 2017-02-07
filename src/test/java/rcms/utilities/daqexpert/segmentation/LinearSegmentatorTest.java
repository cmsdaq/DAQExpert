package rcms.utilities.daqexpert.segmentation;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import rcms.utilities.daqexpert.persistence.Point;

public class LinearSegmentatorTest {

	private static Logger logger = Logger.getLogger(LinearSegmentatorTest.class);

	@BeforeClass
	public static void prepare() throws URISyntaxException, JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();

		java.net.URL url = LinearSegmentator.class.getResource("1h.json");
		File file = new File(url.toURI());

		logger.debug("Deserialize file: " + file.getAbsolutePath());

		Class<List<Pair<Long, Float>>> clazz = (Class) List.class;

		List<Pair<Long, Float>> result = mapper.readValue(file, clazz);

		logger.debug("There is " + result.size());

		realDataExample = new ArrayList<>();
		Iterator<?> iterator = result.iterator();
		while (iterator.hasNext()) {

			Object entry = iterator.next();

			// logger.info(entry.toString());
			// logger.info(entry.getClass());
			LinkedHashMap lentry = (LinkedHashMap) entry;

			Entry llentry = (Entry) lentry.entrySet().iterator().next();

			Long time = Long.valueOf((String) llentry.getKey());
			float value = ((Double) llentry.getValue()).floatValue();

			logger.debug(llentry.getKey());
			logger.debug(llentry.getValue());
			realDataExample.add(new PointMock(time, value));
		}

		Assert.assertEquals(1327, realDataExample.size());
	}

	private static List<Point> realDataExample;

	@Test
	public void realDataMinuteResolutionSegmentationTest() {
		LinearSegmentator ls = new LinearSegmentator(SegmentationSettings.Minute);
		List<Point> outputStream = ls.segmentate(realDataExample);
		printDataToVisualize(realDataExample);
		printDataToVisualize(outputStream);
		Assert.assertEquals(11, outputStream.size());
	}

	@Test
	public void realDataHourResolutionSegmentationTest() {
		LinearSegmentator ls = new LinearSegmentator(SegmentationSettings.Hour);
		List<Point> outputStream = ls.segmentate(realDataExample);
		printDataToVisualize(realDataExample);
		printDataToVisualize(outputStream);
		Assert.assertEquals(2, outputStream.size());
	}

	@Test
	public void realDataDayResolutionSegmentationTest() {
		LinearSegmentator ls = new LinearSegmentator(SegmentationSettings.Day);
		List<Point> outputStream = ls.segmentate(realDataExample);
		printDataToVisualize(realDataExample);
		printDataToVisualize(outputStream);
		Assert.assertEquals(2, outputStream.size());
	}

	@Test
	public void realDataMonthResolutionSegmentationTest() {
		LinearSegmentator ls = new LinearSegmentator(SegmentationSettings.Month);
		List<Point> outputStream = ls.segmentate(realDataExample);
		printDataToVisualize(realDataExample);
		printDataToVisualize(outputStream);
		Assert.assertEquals(2, outputStream.size());
	}

	@Test
	public void sequeceResolutionSegmentationTest() {
		LinearSegmentator ls1 = new LinearSegmentator(SegmentationSettings.Minute);
		LinearSegmentator ls2 = new LinearSegmentator(SegmentationSettings.Hour);
		LinearSegmentator ls3 = new LinearSegmentator(SegmentationSettings.Day);
		LinearSegmentator ls4 = new LinearSegmentator(SegmentationSettings.Month);
		printDataToVisualize(realDataExample);

		List<Point> output1 = ls1.segmentate(realDataExample);
		List<Point> output2 = ls2.segmentate(output1);
		List<Point> output3 = ls3.segmentate(output2);
		List<Point> output4 = ls4.segmentate(output3);

		printDataToVisualize(output1);
		printDataToVisualize(output2);
		printDataToVisualize(output3);
		printDataToVisualize(output4);

	}

	private void printDataToVisualize(List<Point> stream) {
		ObjectMapper om = new ObjectMapper();
		try {
			String result = om.writeValueAsString(stream);
			logger.debug(result);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void constantValueTest() {

		List<Point> inputStream = new ArrayList<Point>();

		inputStream.add(new PointMock(1, 0));
		inputStream.add(new PointMock(2, 0));
		inputStream.add(new PointMock(3, 0));

		LinearSegmentator ls = new LinearSegmentator(SegmentationSettings.Day);

		List<Point> outputStream = ls.segmentate(inputStream);

		logger.debug(inputStream);
		logger.debug(outputStream);
	}

	@Test
	public void lineTest() {

		List<Point> inputStream = new ArrayList<Point>();

		inputStream.add(new PointMock(1, 3));
		inputStream.add(new PointMock(2, 4));
		inputStream.add(new PointMock(3, 5));

		LinearSegmentator ls = new LinearSegmentator(SegmentationSettings.Day);

		List<Point> outputStream = ls.segmentate(inputStream);

		logger.debug(inputStream);
		logger.debug(outputStream);
	}

	@Test
	public void twoLinesTest() {

		List<Point> inputStream = new ArrayList<Point>();

		inputStream.add(new PointMock(1, 3));
		inputStream.add(new PointMock(2, 4));
		inputStream.add(new PointMock(3, 5));
		inputStream.add(new PointMock(4, 3));
		inputStream.add(new PointMock(5, 1));
		inputStream.add(new PointMock(6, -1));

		LinearSegmentator ls = new LinearSegmentator(SegmentationSettings.Day);

		List<Point> outputStream = ls.segmentate(inputStream);

		logger.debug(inputStream);
		logger.debug(outputStream);
	}

}

class PointMock extends Point {
	public PointMock(long timestamp, float value) {
		super();
		this.setY(value);
		this.setX(new Date(timestamp));
	}
}
