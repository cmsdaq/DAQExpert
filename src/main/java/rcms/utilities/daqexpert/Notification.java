package rcms.utilities.daqexpert;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

public class Notification {

	private int type_id;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "CET")
	private Date date;
	private String message;
	private List<String> action;
	private Long id;

	public int getType_id() {
		return type_id;
	}

	public void setType_id(int type_id) {
		this.type_id = type_id;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<String> getAction() {
		return action;
	}

	public void setAction(List<String> action) {
		this.action = action;
	}

	@Override
	public String toString() {
		return "Notification [type_id=" + type_id + ", date=" + date + ", message=" + message + ", action=" + action
				+ ", id=" + id + "]";
	}

}
