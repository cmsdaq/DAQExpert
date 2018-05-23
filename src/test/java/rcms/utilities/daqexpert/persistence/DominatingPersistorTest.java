package rcms.utilities.daqexpert.persistence;

import org.junit.Assert;
import org.junit.Test;

import javax.persistence.EntityManagerFactory;
import javax.xml.bind.DatatypeConverter;

import java.util.*;

import static org.junit.Assert.*;

public class DominatingPersistorTest {



    static Date t1 = DatatypeConverter.parseDateTime("2017-03-09T10:00:00Z").getTime();
    static Date t2 = DatatypeConverter.parseDateTime("2017-03-09T10:00:01Z").getTime();
    static Date t3 = DatatypeConverter.parseDateTime("2017-03-09T10:00:02Z").getTime();
    static Date t4 = DatatypeConverter.parseDateTime("2017-03-09T10:00:03Z").getTime();
    static Date t5 = DatatypeConverter.parseDateTime("2017-03-09T10:00:04Z").getTime();
    static Date t6 = DatatypeConverter.parseDateTime("2017-03-09T10:00:05Z").getTime();

    Deque<Condition> persisted = new ArrayDeque<>();

    @Test
    public void simpleTest(){

        DominatingPersistor dp = new DominatingPersistor(new PersistenceManagerMock());
        Condition c2 = generate("c2", t1, null);

        dp.persistDominating(null, c2,c2.getStart());

        Assert.assertEquals(1, persisted.size());
        Assert.assertEquals("c2", persisted.getLast().getTitle());

    }

    @Test
    public void oneDominatingEndsTest(){

        DominatingPersistor dp = new DominatingPersistor(new PersistenceManagerMock());
        Condition c2 = generate("c2", t1, null);

        dp.persistDominating(null, c2, c2.getStart());
        c2.setEnd(t2);
        dp.persistDominating(c2, null, c2.getEnd());

        Assert.assertEquals(1, persisted.size());
        Assert.assertEquals("c2", persisted.getLast().getTitle());
        Assert.assertEquals(t1, persisted.getLast().getStart());
        Assert.assertEquals(t2, persisted.getLast().getEnd());

    }

    @Test
    public void preemptionTest(){

        DominatingPersistor dp = new DominatingPersistor(new PersistenceManagerMock());
        Condition c1 = generate("c1", t1, null);
        Condition c2 = generate("c2", t2, null);

        dp.persistDominating(null, c1, c1.getStart());
        dp.persistDominating(c1, c2, c2.getStart());

        Assert.assertEquals(2, persisted.size());
        Condition first = persisted.pop();
        Condition second = persisted.pop();
        Assert.assertEquals("c2", second.getTitle());
        Assert.assertEquals(t2, first.getEnd());

    }

    @Test
    public void multiplePreemptionTest(){

        DominatingPersistor dp = new DominatingPersistor(new PersistenceManagerMock());
        Condition c1 = generate("c1", t1, null);
        Condition c2 = generate("c2", t2, null);
        Condition c3 = generate("c3", t3, null);

        dp.persistDominating(null, c1, c1.getStart());
        dp.persistDominating(c1, c2, c2.getStart());
        dp.persistDominating(c2, c3, c3.getStart());
        c3.setEnd(t4);
        dp.persistDominating(c3, c2, c3.getEnd());
        c2.setEnd(t5);
        dp.persistDominating(c2, c1, c2.getEnd());
        c1.setEnd(t6);
        dp.persistDominating(c1, null, c1.getEnd());

        Assert.assertEquals(5, persisted.size());

        Condition r1 = persisted.pop();
        Condition r2 = persisted.pop();
        Condition r3 = persisted.pop();
        Condition r4 = persisted.pop();
        Condition r5 = persisted.pop();

        Assert.assertEquals("c1", r1.getTitle());
        Assert.assertEquals("c2", r2.getTitle());
        Assert.assertEquals("c3", r3.getTitle());
        Assert.assertEquals("c2", r4.getTitle());
        Assert.assertEquals("c1", r5.getTitle());

        Assert.assertEquals(t2, r1.getEnd());
        Assert.assertEquals(t3, r2.getEnd());
        Assert.assertEquals(t4, r3.getEnd());
        Assert.assertEquals(t5, r4.getEnd());
        Assert.assertEquals(t6, r5.getEnd());

    }

    @Test
    public void preemptAndComeBackTest(){

        DominatingPersistor dp = new DominatingPersistor(new PersistenceManagerMock());
        Condition c1 = generate("c1", t1, null);
        Condition c2 = generate("c2", t2, null);

        dp.persistDominating(null, c1, c1.getStart());
        dp.persistDominating(c1, c2, c2.getStart());
        c2.setEnd(t3);
        dp.persistDominating(c2, c1, c2.getEnd());

        Assert.assertEquals(3, persisted.size());

        Condition r1 = persisted.pop();
        Condition r2 = persisted.pop();
        Condition r3 = persisted.pop();

        Assert.assertEquals(t2, r1.getEnd());
        Assert.assertEquals("c1", r1.getTitle());

        Assert.assertEquals(t3, r2.getEnd());
        Assert.assertEquals("c2", r2.getTitle());

        Assert.assertEquals("c1", r3.getTitle());
        Assert.assertEquals(null, r3.getEnd());


    }


    class PersistenceManagerMock extends PersistenceManager{

        public PersistenceManagerMock() {
            super(null);
        }


        @Override
        public void persist(Condition entry) {
            System.out.println("Persisting: " + entry.getTitle() + " with start: " + entry.getStart() + " and end: " + entry.getEnd());
            persisted.addLast(entry);
        }

        @Override
        public void update(Condition condition) {
            System.out.println("Updating: " + condition.getTitle());
            //nothing to do, updated through reference
        }
    }

    private Condition generate(String title, Date start, Date end){

        Condition c = new Condition();

        c.setTitle(title);
        c.setStart(start);
        c.setEnd(end);

        return c;
    }
}