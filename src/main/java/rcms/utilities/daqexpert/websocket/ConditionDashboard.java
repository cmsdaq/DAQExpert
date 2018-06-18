package rcms.utilities.daqexpert.websocket;

import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.processing.DominatingConditionSelector;
import rcms.utilities.daqexpert.reasoning.base.ContextLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;
import rcms.utilities.daqexpert.reasoning.causality.DominatingSelector;

import java.util.*;

/**
 * Condition Dashboard represents the current state of Dashboard view of DAQExpert. It holds recent conditions and
 * notifications and marks currently dominating condition in order to facilitate if for operator.
 *
 * <p>
 * Logical Condition dashboard
 * <p>
 *
 * ConditionWebSocketServer.sessionHandler.addCondition(condition); ConditionWebSocketServer.sessionHandler.removeCurrent();
 * ConditionWebSocketServer.sessionHandler.updateCurrent(currentCondition);
 *
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 */
public class ConditionDashboard implements Observer {

    private final static Logger logger = Logger.getLogger(ConditionDashboard.class);

    /** Maximum number of recent conditions displayed in the dashboard. */
    private final int maximumNumberOfConditionsHandled;

    /** Current dominating condition, choosen with the common dominating-selection mechanism */
    private Condition dominatingCondition;

    /** Map of recent conditions. Limited to @ConditionDashboard. */
    private HashMap<Long, Condition> conditions = new LinkedHashMap<>();

    /** Client session handler*/
    private ConditionSessionHandler sessionHander;


    public ConditionDashboard(int max) {
        this.maximumNumberOfConditionsHandled = max;
    }

    private void handleUpdate(Condition condition) {
        if (sessionHander != null) {
            sessionHander.handleConditionUpdate(condition);
        }
    }

    public void update(Set<Condition> conditionsProduced, Long dominatingId) {
        update(conditionsProduced, dominatingId, true);
    }

    public void update(Set<Condition> conditionsProduced, Long dominatingId, boolean requireProblematic) {


        conditionsProduced = conditionsProduced.stream().filter(c->c.isShow()  && !c.isHoldNotifications()).collect(Collectors.toCollection(LinkedHashSet::new));

        Set<Condition> addedThisRound = new HashSet<>();


        /* Handle observation */
        for (Condition condition : conditionsProduced) {
            if(condition.getEnd() == null){
                condition.addObserver(this);
            }else{
                logger.trace("Observers before: " + condition.countObservers());
                condition.deleteObserver(this);
                logger.trace("Observers after: " + condition.countObservers());
            }
        }

        for (Condition condition : conditionsProduced) {
            if (condition.isMature() && ( !requireProblematic || condition.isProblematic())) {

                //compareWithCurrentlyDominating(condition);

                if (!conditions.containsKey(condition.getId())) {
                    if (conditions.size() >= maximumNumberOfConditionsHandled) {
                        Iterator<Condition> it = conditions.values().iterator();
                        Condition oldest = it.next();

                        // dont remove dominating condition from this list, even though it's old, get next one
                        if(oldest.getId() == dominatingId) {
                            oldest = it.next();
                        }
                        conditions.remove(oldest.getId());
                    }
                    conditions.put(condition.getId(), condition);
                    addedThisRound.add(condition);
                }
            }
        }


        Condition lastDominating = dominatingCondition;
        if(dominatingId != null) {
            dominatingCondition = conditions.get(dominatingId);
        }else{
            dominatingCondition = null;
        }




        if (sessionHander != null) {
            if (lastDominating != this.dominatingCondition) {
                // this fires also whe dominating is null - signal to the dashboard that 'all is ok' and no problem at the moment
                sessionHander.handleDominatingConditionChange(this.dominatingCondition);
            }
            if (addedThisRound.size() > 0) {
                sessionHander.handleRecentConditionsChange(addedThisRound);
            }
        }

        //return dominating;
    }

    public Condition getCurrentCondition() {
        return dominatingCondition;
    }

    protected Collection<Condition> getCurrentConditions() {
        return conditions.values();
    }

    /**
     * Get condition list without dominating condition
     *
     * @return
     */
    public Collection<Condition> getConditionsWithoutDominatingCondition() {
        List<Condition> result = new ArrayList<>();
        Iterator<Condition> i = conditions.values().iterator();

        while (i.hasNext()) {
            Condition curr = i.next();
            if (dominatingCondition == null) {
                result.add(curr);
            } else if (dominatingCondition.getId() != curr.getId()) {
                result.add(curr);
            }
        }
        return result;

    }

    public ConditionSessionHandler getSessionHander() {
        return sessionHander;
    }

    public void setSessionHander(ConditionSessionHandler sessionHander) {
        this.sessionHander = sessionHander;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("ConditionDashboard [currentCondition="
                + (dominatingCondition != null ? dominatingCondition.getId() : "<none>") + ", recentConditions=[");
        logger.trace("recent size: " + conditions.size());
        for (Condition condition : conditions.values()) {
            sb.append(condition.getId());
            sb.append(",");
        }
        logger.trace("filtered size: " + getConditionsWithoutDominatingCondition().size());
        sb.append("], filteredConditions=[");
        for (Condition condition : getConditionsWithoutDominatingCondition()) {
            sb.append(condition.getId());
            sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

	@Override
	public void update(Observable o, Object arg) {

		if (o instanceof Condition) {
			Condition condition = (Condition) o;

			handleUpdate(condition);
		}

	}

}
