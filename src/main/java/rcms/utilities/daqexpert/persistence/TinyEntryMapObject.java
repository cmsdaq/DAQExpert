package rcms.utilities.daqexpert.persistence;

import java.util.Date;

import rcms.utilities.daqexpert.reasoning.base.LogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionGroup;

public class TinyEntryMapObject {

	private LogicModuleRegistry logicModule;
	private ConditionGroup group;

	private long count;

	private Date start;

	private Date end;

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
		return "TinyEntryMapObject [logicModule=" + logicModule + ", count=" + count + ", start=" + start + ", end="
				+ end + "]";
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public LogicModuleRegistry getLogicModule() {
		return logicModule;
	}

	public void setLogicModule(LogicModuleRegistry logicModule) {
		this.logicModule = logicModule;
	}

	public ConditionGroup getGroup() {
		return group;
	}

	public void setGroup(ConditionGroup group) {
		this.group = group;
	}

}
