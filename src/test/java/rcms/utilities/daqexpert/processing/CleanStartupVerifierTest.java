package rcms.utilities.daqexpert.processing;

import org.junit.Test;
import org.mockito.Mockito;
import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.persistence.PersistenceManager;
import rcms.utilities.daqexpert.persistence.Point;
import rcms.utilities.daqexpert.segmentation.DataResolution;

import javax.persistence.EntityManagerFactory;
import javax.xml.bind.DatatypeConverter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class CleanStartupVerifierTest {

    private Date t1 = DatatypeConverter.parseDateTime("2018-02-02T00:00:00Z").getTime();
    private Date t2 = DatatypeConverter.parseDateTime("2018-02-02T01:00:00Z").getTime();

    /* Verify that no interaction with db was made */
    @Test
    public void emptyDatabaseTest(){

        PersistenceManagerStub persistenceManagerStub = new PersistenceManagerStub(null, null, 0,0, false);
        persistenceManagerStub = Mockito.spy(persistenceManagerStub);
        CleanStartupVerifier sut = new CleanStartupVerifier(persistenceManagerStub);
        sut.ensureSafeStartupProcedure();

        /* Verify that no DB cleanup is performed */
        Mockito.verify(persistenceManagerStub, Mockito.times(0)).update(Mockito.isA(Condition.class));
    }

    /* Verify that no interaction with db was made */
    @Test
    public void gracefullyFinishedDAQExpertProcessInThePastTest(){
        Condition versionEntry = new Condition();
        versionEntry.setStart(t1);
        versionEntry.setEnd(t2);
        PersistenceManagerStub persistenceManagerStub = new PersistenceManagerStub(null, versionEntry, 0, 10, false);
        persistenceManagerStub = Mockito.spy(persistenceManagerStub);
        CleanStartupVerifier sut = new CleanStartupVerifier(persistenceManagerStub);
        sut.ensureSafeStartupProcedure();

        /* Verify that no DB cleanup is performed */
        Mockito.verify(persistenceManagerStub, Mockito.times(0)).update(Mockito.isA(Condition.class));
    }


    @Test
    public void crashedDAQExpertProcessInThePastTest(){
        Condition versionEntry = new Condition();
        versionEntry.setStart(t1);
        versionEntry.setEnd(null);
        PersistenceManagerStub persistenceManagerStub = Mockito.spy(new PersistenceManagerStub(null, versionEntry, 2, 10,false));
        CleanStartupVerifier sut = new CleanStartupVerifier(persistenceManagerStub);
        sut.observationInterval = 100;
        sut.ensureSafeStartupProcedure();

        /* Verify that version entry is finished + 2 unfinished entries */
        Mockito.verify(persistenceManagerStub, Mockito.times(3)).update(Mockito.isA(Condition.class));
    }

    /* Verify that no second instance of DAQExpert will be run */
    @Test(expected = RuntimeException.class)
    public void otherDAQExpertRunningTest(){

        Condition versionEntry = new Condition();
        versionEntry.setStart(t1);
        versionEntry.setEnd(null);
        PersistenceManagerStub persistenceManagerStub = Mockito.spy(new PersistenceManagerStub(null, versionEntry, 2, 10,true));
        CleanStartupVerifier sut = new CleanStartupVerifier(persistenceManagerStub);
        sut.ensureSafeStartupProcedure();

        fail("The startup procedure should interrupted at this point.");
    }

    class PersistenceManagerStub extends PersistenceManager{

        Condition lastVersionEntry;

        List<Condition> fakeEntries;

        int fakeRawEntriesCount;

        boolean fakeDataIncrement;

        public PersistenceManagerStub(EntityManagerFactory entityManagerFactory, Condition lastVersionEntry, int numberOfUnfinishedEntries, int numberOfFinishedEntries, boolean fakeDataIncrement) {
            super(entityManagerFactory);
            this.lastVersionEntry = lastVersionEntry;
            this.fakeEntries = new ArrayList<>();
            this.fakeDataIncrement = fakeDataIncrement;
            long id = 1;
            for (int i = 0; i<numberOfUnfinishedEntries; i++){
                Condition c = new Condition();
                c.setId(id);
                c.setTitle("unifinished");
                c.setStart(t1);
                c.setEnd(null);
                fakeEntries.add(c);
                id++;
            }
            for (int i = 0; i<numberOfFinishedEntries; i++){
                Condition c = new Condition();
                c.setId(id);
                c.setTitle("finished");
                c.setStart(t1);
                c.setEnd(t2);
                c.calculateDuration();
                fakeEntries.add(c);
                id++;
            }
            System.out.println("Fake entries in DB: " + fakeEntries);
        }

        @Override
        public void update(Condition entry) {
            System.out.println("Persisting single condition: " + entry);
        }

        @Override
        public List<Point> getRawData(Date startDate, Date endDate, DataResolution resolution) {
            if(fakeDataIncrement){
                fakeRawEntriesCount++;
                List<Point> fakeResult = new ArrayList<>();
                for(int i = 0; i < fakeRawEntriesCount; i++){
                    fakeResult.add(new Point());
                }
                return fakeResult;

            } else{
                return new ArrayList<>();
            }
        }

        /**
         * Returns generated fake conditions in db
         */
        @Override
        public List<Condition> getEntriesPlain(Date startDate, Date endDate) {
            return fakeEntries;
        }

        @Override
        public Condition getLastVersionEntry() {
            return this.lastVersionEntry;
        }

    }

}
