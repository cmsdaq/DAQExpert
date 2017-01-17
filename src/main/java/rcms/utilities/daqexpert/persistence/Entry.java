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

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	private Long id;

	@Column(columnDefinition = "VARCHAR2(4000)")
	private String content;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "START_DATE")
	private Date start;

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

}
