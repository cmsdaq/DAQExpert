package rcms.utilities.daqexpert.websocket;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.apache.log4j.Logger;

import rcms.utilities.daqexpert.persistence.Condition;
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
		// exists some unfinished
		// TODO: add some threshold
		if (condition.getEnd() == null) {

			// no condition at the moment
			if (dominatingCondition == null) {
				this.dominatingCondition = condition;
			}

			// exists other condition at the moemnt
			else {

				// current is more important than old
				if (condition.getPriority().ordinal() > dominatingCondition.getPriority().ordinal()) {
					this.dominatingCondition = condition;
				}

				// current is less important than old
				else if (condition.getPriority().ordinal() < dominatingCondition.getPriority().ordinal()) {
					// nothing to do
				}

				// both are equally important
				else {

					// current is more useful than old
					if (condition.getLogicModule().getUsefulness() > dominatingCondition.getLogicModule()
							.getUsefulness()) {
						this.dominatingCondition = condition;

					}
					// current is less useful than old
					else if (condition.getLogicModule().getUsefulness() < dominatingCondition.getLogicModule()
							.getUsefulness()) {
						// nothing to do
					}
					// both are equally useful
					else {
						// newest will be displayed
						if (condition.getStart().after(dominatingCondition.getStart())) {
							this.dominatingCondition = condition;
						}
					}

				}
			}
		}

	}

	public void update(Set<Condition> conditionsProduced) {

		Set<Condition> addedThisRound = new HashSet<>();
		Condition lastDominating = dominatingCondition;

		if (dominatingCondition != null) {
			if(dominatingCondition.getEnd() != null){
				dominatingCondition = null;
			}
		}

		for (Condition condition : conditionsProduced) {
			if (condition.isShow() && !condition.isHoldNotifications()/*
									 * && condition.getPriority() ==
									 * ConditionPriority.CRITICAL
									 */
					&& condition.getLogicModule().getLogicModule() instanceof ContextLogicModule) {


				compareWithCurrentlyDominating(condition);

				if (!conditions.containsKey(condition.getId())) {
					if (conditions.size() >= maximumNumberOfConditionsHandled) {
						Condition oldest = conditions.values().iterator().next();
						logger.trace("Observers before: " + oldest.countObservers());
						oldest.deleteObserver(this);
						logger.trace("Observers after: " + oldest.countObservers());
						conditions.remove(oldest.getId());
					}
					conditions.put(condition.getId(), condition);
					condition.addObserver(this);

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
			handleUpdate(condition);
		}

	}

}
