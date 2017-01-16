package rcms.utilities.daqexpert.reasoning.base;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

import rcms.utilities.daqexpert.reasoning.base.enums.EntryState;

/**
 * 
 * Base object of analysis result.
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */

@Entity
public class Entry implements Comparable<Entry> {

	@JsonIgnore
	@Transient
	private static long globalId = 1;

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	private Long id;

	@JsonIgnore
	@Transient
	private long duration;

	@JsonIgnore
	@Transient
	private boolean show;

	@JsonIgnore
	@Transient
	private EntryState state;

	@JsonIgnore
	@Transient
	private LogicModule eventFinder;

	@JsonIgnore
	@Transient
	private Context finishedContext;

	/**
	 * Short description of event. Displayed in main expert view
	 */
	@Column(columnDefinition = "VARCHAR2(4000)")
	private String content;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "START_DATE")
	private Date start;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "END_DATE")
	private Date end;

	/**
	 * Group in which will be displayed in main expert view
	 */
	@Transient
	private String group;

	/**
	 * Class name of the event, indicates if event is important and should be
	 * highlighted or not TODO: enum this
	 */
	@Transient
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
		id = globalId++;
		show = true;
		this.state = EntryState.NEW;
	}

	/**
	 * TODO: What exactly is the reason of this constructor
	 * 
	 * @param entry
	 */
	public Entry(Entry entry) {
		this.id = -entry.id;
		this.start = entry.start;
		this.end = entry.end;
		this.group = entry.group;
		this.duration = entry.duration;
		this.state = entry.state;
	}

	/**
	 * Get duration in ms
	 * 
	 * @return duration in ms
	 */
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Entry other = (Entry) obj;
		if (id != other.id)
			return false;
		return true;
	}
}
