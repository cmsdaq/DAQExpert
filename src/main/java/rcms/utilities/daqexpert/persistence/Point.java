package rcms.utilities.daqexpert.persistence;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

/**
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 *         TODO: index on dates
 */
@Entity
public class Point {
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	private Long id;

	@Column(name = "stream_id")
	private int group;

	@Column(name = "resolution_id")
    @Index(name = "idx_resolution")
	private int resolution;

	@Temporal(TemporalType.TIMESTAMP)
    @Index(name = "idx_x")
	private Date x;

	private float y;


	public String toString() {
		return "(" + x + "," + y + ")";
	}

	public int getGroup() {
		return group;
	}

	public int getResolution() {
		return resolution;
	}

	public Date getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setGroup(int group) {
		this.group = group;
	}

	public void setResolution(int resolution) {
		this.resolution = resolution;
	}

	public void setX(Date x) {
		this.x = x;
	}

	public void setY(float y) {
		this.y = y;
	}
}