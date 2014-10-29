package org.icatproject.ijp.shared;

import java.io.Serializable;
import java.util.Date;

/**
 * Data Transfer Object for the Job entity bean
 * 
 */
@SuppressWarnings("serial")
public class JobDTO implements Serializable {

	public enum Status {
		Completed, Executing, Held, Queued, Cancelled, Unknown
	}

	private long id;

	private Status status;

	private Date submitDate;

	private String jobType;

	public JobDTO() {
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Date getSubmitDate() {
		return submitDate;
	}

	public void setSubmitDate(Date submitDate) {
		this.submitDate = submitDate;
	}

	public String getJobType() {
		return jobType;
	}

	public void setJobType(String jobType) {
		this.jobType = jobType;
	}

}
