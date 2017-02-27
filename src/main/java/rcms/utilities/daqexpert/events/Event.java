package rcms.utilities.daqexpert.events;

import java.util.Date;

import rcms.utilities.daqexpert.persistence.Condition;

public class Event {

	private Condition condition;

	private EventType type;

	private Date date;

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

	public EventToSend generateEventToSend() {
		EventToSend eventToSend = new EventToSend();

		eventToSend.setMessage(condition.getDescription());
		eventToSend.setTitle(type.getName() + " " + condition.getLogicModule().getLogicModule().getName());
		eventToSend.setConditionId(condition.getId());
		eventToSend.setEventType(type);
		eventToSend.setSender(this.getClass().getPackage().getImplementationVersion());
		eventToSend.setEventSenderType(EventSenderType.Expert);
		eventToSend.setDate(date);

		return eventToSend;

	}
}
