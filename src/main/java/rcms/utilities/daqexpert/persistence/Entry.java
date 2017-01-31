package rcms.utilities.daqexpert.persistence;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

import com.fasterxml.jackson.annotation.JsonIgnore;

import rcms.utilities.daqexpert.reasoning.base.Context;
import rcms.utilities.daqexpert.reasoning.base.LogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.EntryState;

/**
 * 
 * Base object of analysis result.
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 * 
 * TODO: index on dates
 *
 */
@Entity
public class Entry implements Comparable<Entry> {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	private Long id;

	@JsonIgnore
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
    @Index(name = "IDX_STARTDATEINDEX")
	private Date start;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "END_DATE")
    @Index(name = "IDX_ENDDATEINDEX")
	private Date end;

	/**
	 * Group in which will be displayed in main expert view
	 */
	/** TODO: replace by enum/id */
	@Column(columnDefinition = "VARCHAR2(20)", name = "GROUP_NAME")
	private String group;

	/**
	 * Class name of the event, indicates if event is important and should be
	 * highlighted or not TODO: enum this
	 */
	/** TODO: replace by enum/id */
	@Column(columnDefinition = "VARCHAR2(20)", name = "CLASS_NAME")
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
		show = true;
		this.state = EntryState.NEW;
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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
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
		result = prime * result + ((className == null) ? 0 : className.hashCode());
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + (int) (duration ^ (duration >>> 32));
		result = prime * result + ((end == null) ? 0 : end.hashCode());
		result = prime * result + ((group == null) ? 0 : group.hashCode());
		result = prime * result + (show ? 1231 : 1237);
		result = prime * result + ((start == null) ? 0 : start.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
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
		if (className == null) {
			if (other.className != null)
				return false;
		} else if (!className.equals(other.className))
			return false;
		if (content == null) {
			if (other.content != null)
				return false;
		} else if (!content.equals(other.content))
			return false;
		if (duration != other.duration)
			return false;
		if (end == null) {
			if (other.end != null)
				return false;
		} else if (!end.equals(other.end))
			return false;
		if (group == null) {
			if (other.group != null)
				return false;
		} else if (!group.equals(other.group))
			return false;
		if (show != other.show)
			return false;
		if (start == null) {
			if (other.start != null)
				return false;
		} else if (!start.equals(other.start))
			return false;
		if (state != other.state)
			return false;
		return true;
	}
}
