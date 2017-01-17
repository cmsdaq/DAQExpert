package rcms.utilities.daqexpert.persistence;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class Entry {

	/** Unique identifier */
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	private Long id;

	/** Name of the entry */
	@Column(columnDefinition = "VARCHAR2(80)")
	private String name;

	/** Content of the entry */
	@Column(columnDefinition = "VARCHAR2(4000)")
	private String content;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "START_DATE")
	private Date start;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "END_DATE")
	private Date end;

	/**
	 * Duration of the entry. Intentionally denormalized in database for quicker
	 * querying. There is functional dependency with the columns start and end.
	 */
	private int duration;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

}
