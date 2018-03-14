package rcms.utilities.daqexpert.processing;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class OrderManagerTest {

    @Test
    public void trivialTest() {
        OrderManager orderManager = new OrderManager();

        RequiredMock o1 = new RequiredMock("1");
        RequiredMock o2 = new RequiredMock("2");
        RequiredMock o3 = new RequiredMock("3");

        Set<Requiring> setToOrder = new LinkedHashSet<>(Arrays.asList(o1, o2, o3));
        List<Requiring> result = orderManager.order(setToOrder);
        System.out.println("Result: " + result);
        Assert.assertEquals(Arrays.asList(o1, o2, o3), result);

    }

    @Test
    public void simpleTest() {
        OrderManager orderManager = new OrderManager();

        RequiredMock o1 = new RequiredMock("1");
        RequiredMock o2 = new RequiredMock("2");
        RequiredMock o3 = new RequiredMock("3");

        o1.getRequired().add(o3);

        Set<Requiring> setToOrder = new LinkedHashSet<>(Arrays.asList(o1, o2, o3));
        List<Requiring> result = orderManager.order(setToOrder);
        System.out.println("Result: " + result);
        Assert.assertEquals(Arrays.asList(o2, o3, o1), result);

    }

    @Test
    public void crossDependencyTest() {
        OrderManager orderManager = new OrderManager();

        RequiredMock o1 = new RequiredMock("1");
        RequiredMock o2 = new RequiredMock("2");
        RequiredMock o3 = new RequiredMock("3");

        o2.getRequired().add(o1);
        o2.getRequired().add(o3);
        o3.getRequired().add(o1);

        Set<Requiring> setToOrder = new LinkedHashSet<>(Arrays.asList(o1, o2, o3));
        List<Requiring> result = orderManager.order(setToOrder);
        System.out.println("Result: " + result);
        Assert.assertEquals(Arrays.asList(o1, o3, o2), result);

    }

    @Test
    public void test() {
        OrderManager orderManager = new OrderManager();

        RequiredMock i1 = new RequiredMock("I1");
        RequiredMock i2 = new RequiredMock("I2");
        RequiredMock i3 = new RequiredMock("I3");

        RequiredMock d1 = new RequiredMock("D1");
        RequiredMock d2 = new RequiredMock("D2");

        RequiredMock vd1 = new RequiredMock("VD1");
        RequiredMock vd2 = new RequiredMock("VD2");

        RequiredMock c1 = new RequiredMock("C1");
        RequiredMock c2 = new RequiredMock("C2");
        RequiredMock c3 = new RequiredMock("C3");
        RequiredMock c4 = new RequiredMock("C4");
        RequiredMock c5 = new RequiredMock("C5");

        d1.getRequired().add(i1);
        d2.getRequired().add(i2);

        vd1.getRequired().add(d1);
        vd2.getRequired().add(d2);

        c1.getRequired().add(i1);
        c2.getRequired().add(c1);
        c3.getRequired().add(c2);
        c4.getRequired().add(c3);
        c5.getRequired().add(c4);

        Set<Requiring> setToOrder = new LinkedHashSet<>(Arrays.asList(c5, c4, c3, vd2, d2, vd1, i1, d1, c2, c1, i3, i2));
        List<Requiring> result = orderManager.order(setToOrder);
        System.out.println("Result: " + result);
        Assert.assertEquals(Arrays.asList(i1, i3, i2, d2, d1, c1, vd2, vd1, c2, c3, c4, c5), result);

    }


    @Test(expected=RuntimeException.class)
    public void detectCycleIsolatedTest(){


        OrderManager orderManager = new OrderManager();

        RequiredMock o1 = new RequiredMock("c1");
        RequiredMock o2 = new RequiredMock("c2");
        RequiredMock o3 = new RequiredMock("c3");

        o1.getRequired().add(o2);
        o2.getRequired().add(o3);
        o3.getRequired().add(o1);

        Set<Requiring> setToOrder = new LinkedHashSet<>(Arrays.asList(o1, o2, o3));
        List<Requiring> result = orderManager.order(setToOrder);

    }

    @Test(expected=RuntimeException.class)
    public void detectCycleWithOthersTest(){


        OrderManager orderManager = new OrderManager();

        RequiredMock o1 = new RequiredMock("c1");
        RequiredMock o2 = new RequiredMock("c2");
        RequiredMock o3 = new RequiredMock("c3");

        RequiredMock o4 = new RequiredMock("1");
        RequiredMock o5 = new RequiredMock("2");
        RequiredMock o6 = new RequiredMock("3");

        o1.getRequired().add(o2);
        o2.getRequired().add(o3);
        o3.getRequired().add(o1);

        o4.getRequired().add(o5);
        o5.getRequired().add(o6);

        Set<Requiring> setToOrder = new LinkedHashSet<>(Arrays.asList(o4, o1, o5,  o2, o6, o3));
        List<Requiring> result = orderManager.order(setToOrder);

    }
}