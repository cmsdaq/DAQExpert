package rcms.utilities.daqexpert.persistence;

import org.apache.log4j.Logger;
import rcms.utilities.daqaggregator.persistence.PersistorManager;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

import java.util.Date;

public class InternalConditionProducer {

    private Logger logger = Logger.getLogger(InternalConditionProducer.class);

    private PersistenceManager persistenceManager;

    public InternalConditionProducer(PersistenceManager persistenceManager){
        this.persistenceManager = persistenceManager;
    }

    public Condition persistCondition(String title, Date start, Date end, ConditionGroup group ){
        Condition condition = new Condition();
        condition.setMature(true);
        condition.setStart(start);
        condition.setEnd(end);
        if (end != null) {
            condition.calculateDuration();
        }
        // TODO: class name vs priority - decide on one convention
        condition.setClassName(ConditionPriority.DEFAULTT);

        condition.setGroup(group);

        condition.setTitle(title);
        this.persistenceManager.persist(condition);
        return condition;
    }

    public void updateCondition(Condition condition){
        condition.calculateDuration();
        this.persistenceManager.persist(condition);
    }
}
