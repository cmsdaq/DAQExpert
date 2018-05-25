package rcms.utilities.daqexpert.websocket;

import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;
import org.apache.log4j.Logger;

import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.processing.DominatingConditionSelector;
import rcms.utilities.daqexpert.reasoning.base.ContextLogicModule;

/**
 *
 * Logical Condition dashboard
 *
 * ConditionWebSocketServer.sessionHandler.addCondition(condition);
 * ConditionWebSocketServer.sessionHandler.removeCurrent();
 * ConditionWebSocketServer.sessionHandler.updateCurrent(currentCondition);
 *
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class ConditionDashboard implements Observer{

	private final static Logger logger = Logger.getLogger(ConditionDashboard.class);

	/**
	 * Current condition
	 */
	private Condition dominatingCondition;

	private HashMap<Long, Condition> conditions = new LinkedHashMap<>();

	private ConditionSessionHandler sessionHander;

	private final int maximumNumberOfConditionsHandled;

	public ConditionDashboard(int max) {
		this.maximumNumberOfConditionsHandled = max;
	}

	private void handleUpdate(Condition condition) {
		if (sessionHander != null) {
			sessionHander.handleConditionUpdate(condition);
		}
	}

	public void compareWithCurrentlyDominating(Condition condition){
		this.dominatingCondition =DominatingConditionSelector.findDominating(dominatingCondition,condition);
	}

	public void update(Collection<Condition> conditionsProduced) {

		conditionsProduced = conditionsProduced.stream().filter(c->c.isShow() && !c.isHoldNotifications()).collect(Collectors.toCollection(LinkedHashSet::new));

		Set<Condition> addedThisRound = new HashSet<>();
		Condition lastDominating = dominatingCondition;

		if (dominatingCondition != null) {
			if(dominatingCondition.getEnd() != null){
				dominatingCondition = null;
			}
		}

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
			if (condition.isMature() && condition.getLogicModule().getLogicModule() instanceof ContextLogicModule) {


				compareWithCurrentlyDominating(condition);

				if (!conditions.containsKey(condition.getId())) {
					if (conditions.size() >= maximumNumberOfConditionsHandled) {

						logger.debug("Need to remove recent conditions: " + conditions.size() + ", " + conditions.keySet());
						Condition oldest = conditions.values().iterator().next();
						conditions.remove(oldest.getId());
						logger.debug("Removing condition " + oldest.getId());
					}
					conditions.put(condition.getId(), condition);
					addedThisRound.add(condition);

				}
			}
		}

		for(Condition condition: conditions.values()){
			if(condition.getEnd() == null) {
				compareWithCurrentlyDominating(condition);
			}
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


			if (arg != null && "becomeMature".equals((String) arg) &&  !conditions.containsKey(condition.getId())) {
				update(Sets.newHashSet(condition));
			}

			handleUpdate(condition);
		}

	}

}
