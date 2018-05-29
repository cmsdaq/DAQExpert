package rcms.utilities.daqexpert.processing.context.functions;

import org.junit.Assert;
import org.junit.Test;
import rcms.utilities.daqaggregator.data.FED;

import java.util.Arrays;
import java.util.stream.Collectors;


public class ObjectListOptimizerTest {

    @Test
    public void simpleTest() {

        Assert.assertEquals("1-2", getObjectsTextRepresentation(1, 2));
        Assert.assertEquals("[1, 3-5]", getObjectsTextRepresentation(1, 3, 4, 5));
        Assert.assertEquals("1", getObjectsTextRepresentation(1));
        Assert.assertEquals("", getObjectsTextRepresentation());

        Assert.assertEquals("[1, 3-5, 8-20, 22-23, 100-102, 200]", getObjectsTextRepresentation(1, 3, 4, 5, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 22, 23, 100, 101, 102, 200));

    }


    @Test
    public void testRangeBuilder() {

        ObjectListOptimizer<Integer> optimizer = new ObjectListOptimizer<>();

        Assert.assertEquals(Arrays.asList("1-3"), optimizer.convertSortedToRanges(Arrays.asList(1, 2, 3), o -> o));
        Assert.assertEquals(Arrays.asList("4-10"), optimizer.convertSortedToRanges(Arrays.asList(4, 5, 6, 7, 8, 9, 10), o -> o));

        Assert.assertEquals(Arrays.asList("1"), optimizer.convertSortedToRanges(Arrays.asList(1), o -> o));

        Assert.assertEquals(Arrays.asList("1-2"), optimizer.convertSortedToRanges(Arrays.asList(1, 2), o -> o));

        Assert.assertEquals(Arrays.asList("1-2", "4-5"), optimizer.convertSortedToRanges(Arrays.asList(1, 2, 4, 5), o -> o));


        Assert.assertEquals(Arrays.asList("1", "3", "5"), optimizer.convertSortedToRanges(Arrays.asList(1, 3, 5), o -> o));

        Assert.assertEquals(Arrays.asList("1", "3-5", "7"), optimizer.convertSortedToRanges(Arrays.asList(1, 3, 4, 5, 7), o -> o));
        Assert.assertEquals(Arrays.asList(), optimizer.convertSortedToRanges(Arrays.asList(), o -> o));


    }

    private String getObjectsTextRepresentation(Integer... t) {
        ObjectListOptimizer<FEDMock> optimizer = new ObjectListOptimizer<>();

        String stringRepresentation = optimizer.getShortestListRepresentation(Arrays.stream(t).map(e -> new FEDMock(e)).collect(Collectors.toSet()), f -> f.getSrcIdExpected());
        return stringRepresentation;

    }

    class FEDMock extends FED {

        public FEDMock(int id) {
            setSrcIdExpected(id);
        }
    }

}