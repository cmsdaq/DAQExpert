package rcms.utilities.daqexpert.events;

import java.util.Date;

import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.ComparatorLogicModule;

public class ConditionEvent {

	private Condition condition;

	private EventType type;

	private Date date;

	private String title;

	private LogicModuleRegistry logicModule;

	public Condition getCondition() {
		return condition;
	}

	public void setCondition(Condition condition) {
		this.condition = condition;
	}

	public EventType getType() {
		return type;
	}

	public void setType(EventType type) {
		this.type = type;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@Override
	public String toString() {
		return "Event [condition=" + condition + ", type=" + type + ", date=" + date + "]";
	}

	public ConditionEventResource generateEventToSend() {
		ConditionEventResource eventToSend = new ConditionEventResource();

		eventToSend.setMessage(condition.getDescription());
		eventToSend.setTitle(title);
		eventToSend.setConditionId(condition.getId());
		eventToSend.setEventType(type);
		eventToSend.setSender(this.getClass().getPackage().getImplementationVersion());
		eventToSend.setEventSenderType(EventSenderType.Expert);
		eventToSend.setDate(date);
		eventToSend.setLogicModule(logicModule);

		return eventToSend;

	}

	public LogicModuleRegistry getLogicModule() {
		return logicModule;
	}

	public void setLogicModule(LogicModuleRegistry logicModule) {
		this.logicModule = logicModule;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}
