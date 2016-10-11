package rcms.utilities.daqexpert.reasoning.base;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

import rcms.utilities.daqexpert.reasoning.base.enums.EntryState;

/**
 * 
 * Base object of analysis result.
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class Entry implements Comparable<Entry> {

	@JsonIgnore
	private long duration;

	@JsonIgnore
	private static long globalId = 1;

	@JsonIgnore
	private boolean show;

	@JsonIgnore
	private EntryState state;

	@JsonIgnore
	private LogicModule eventFinder;

	@JsonIgnore
	private Context finishedContext;

	private long id;

	/**
	 * Short description of event. Displayed in main expert view
	 */
	private String content;

	private Date start;
	private Date end;

	/**
	 * Group in which will be displayed in main expert view
	 */
	private String group;

	/**
	 * Class name of the event, indicates if event is important and should be
	 * highlighted or not TODO: enum this
	 */
	private String className;

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public boolean isShow() {
		return show;
	}

	public void setShow(boolean show) {
		this.show = show;
	}

	public Entry() {
		this.id = globalId;
		globalId++;
		show = false;
		this.state = EntryState.NEW;
	}

	public Entry(Entry entry) {
		this.id = globalId;
		this.start = entry.start;
		this.end = entry.end;
		this.group = entry.group;
		this.duration = entry.duration;
		this.state = entry.state;
		globalId++;
	}

	public long getDuration() {
		return duration;
	}

	public void calculateDuration() {
		this.duration = this.getEnd().getTime() - this.getStart().getTime();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	@Override
	public int compareTo(Entry arg0) {
		return (int) (this.duration - arg0.duration);
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public EntryState getState() {
		return state;
	}

	public void setState(EntryState state) {
		this.state = state;
	}

	public boolean hasChanged() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String toString() {
		return "Entry [show=" + show + ", state=" + state + ", id=" + id + ", content=" + content + ", start=" + start
				+ ", end=" + end + ", group=" + group + ", className=" + className + "]";
	}

	public LogicModule getEventFinder() {
		return eventFinder;
	}

	public void setEventFinder(LogicModule eventFinder) {
		this.eventFinder = eventFinder;
	}

	public Context getFinishedContext() {
		return finishedContext;
	}

	public void setFinishedContext(Context finishedContext) {
		this.finishedContext = finishedContext;
	}
}
