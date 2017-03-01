package rcms.utilities.daqexpert.events;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class EventToSend {

	private String message;

	private String title;

	private String sender;

	private String textToSpeech;

	private Long conditionId;

	private EventSenderType eventSenderType;

	private EventType eventType;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "CET")
	private Date date;

	/** Flag indicating if this notification should be displayed */
	private boolean display;

	/** Flag indicating if this notification should be played */
	private boolean play;

	private int soundId;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getTextToSpeech() {
		return textToSpeech;
	}

	public void setTextToSpeech(String textToSpeech) {
		this.textToSpeech = textToSpeech;
	}

	public Long getConditionId() {
		return conditionId;
	}

	public void setConditionId(Long conditionId) {
		this.conditionId = conditionId;
	}

	@Override
	public String toString() {
		return "EventToSend [message=" + message + ", title=" + title + ", sender=" + sender + ", textToSpeech="
				+ textToSpeech + ", conditionId=" + conditionId + ", eventSenderType=" + eventSenderType
				+ ", eventType=" + eventType + "]";
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public boolean isDisplay() {
		return display;
	}

	public void setDisplay(boolean display) {
		this.display = display;
	}

	public boolean isPlay() {
		return play;
	}

	public void setPlay(boolean play) {
		this.play = play;
	}

	public int getSoundId() {
		return soundId;
	}

	public void setSoundId(int soundId) {
		this.soundId = soundId;
	}

	public void setEventSenderType(EventSenderType eventSenderType) {
		this.eventSenderType = eventSenderType;
	}

	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}

	public EventSenderType getEventSenderType() {
		return eventSenderType;
	}

	public EventType getEventType() {
		return eventType;
	}

}
