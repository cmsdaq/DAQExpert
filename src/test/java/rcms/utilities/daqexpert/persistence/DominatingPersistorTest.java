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

    Deque<Condition> persisted = new ArrayDeque<>();

    @Test
    public void simpleTest(){

        DominatingPersistor dp = new DominatingPersistor(new PersistenceManagerMock());
        Condition c2 = generate("c2", t1, null);

        dp.persistDominating(null, c2);

        Assert.assertEquals(1, persisted.size());
        Assert.assertEquals("c2", persisted.getLast().getTitle());

    }

    @Test
    public void preemptionTest(){

        DominatingPersistor dp = new DominatingPersistor(new PersistenceManagerMock());
        Condition c1 = generate("c1", t1, null);
        Condition c2 = generate("c2", t2, null);

        dp.persistDominating(null, c1);
        dp.persistDominating(c1, c2);

        Assert.assertEquals(2, persisted.size());
        Condition first = persisted.pop();
        Condition second = persisted.pop();
        Assert.assertEquals("c2", second.getTitle());
        Assert.assertEquals(t2, first.getEnd());

    }


    @Test
    public void preemptAndComeBackTest(){

        DominatingPersistor dp = new DominatingPersistor(new PersistenceManagerMock());
        Condition c1 = generate("c1", t1, null);
        Condition c2 = generate("c2", t2, null);

        dp.persistDominating(null, c1);
        dp.persistDominating(c1, c2);
        c2.setEnd(t3);
        dp.persistDominating(c2, c1);

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