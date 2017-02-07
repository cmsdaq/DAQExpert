package rcms.utilities.daqexpert.persistence;

import java.util.Date;

public class TinyEntryMapObject {

	private String group;

	private long count;

	private Date start;

	private Date end;

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
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
	public String toString() {
		return "TinyEntryMapObject [group=" + group + ", count=" + count + ", start=" + start + ", end=" + end + "]";
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

}
