package rcms.utilities.daqaggregator.reasoning.base;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Entry implements Comparable<Entry> {

	@JsonIgnore
	private long duration;
	@JsonIgnore
	private static int globalId = 1;

	@JsonIgnore
	private boolean show;

	@JsonIgnore
	private final EventRaport eventRaport = new EventRaport();

	private int id;
	private String content;

	private Date start;
	private Date end;
	private String group;
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
	}
	
	public Entry(Entry entry){
		this.id = globalId;
		this.start = entry.start;
		this.end = entry.end;
		this.group = entry.group;
		this.duration = entry.duration;
		globalId++;
	}

	@Override
	public String toString() {
		return "Entry [content=" + content + ", start=" + start + ", end=" + end + "]";
	}

	public long getDuration() {
		return duration;
	}

	public void calculateDuration() {
		this.duration = this.getEnd().getTime() - this.getStart().getTime();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
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
		// TODO Auto-generated method stub
		return (int) (this.duration - arg0.duration);
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public EventRaport getEventRaport() {
		return eventRaport;
	}
}
