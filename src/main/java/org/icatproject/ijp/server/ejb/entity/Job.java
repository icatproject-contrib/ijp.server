package org.icatproject.ijp.server.ejb.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;

@SuppressWarnings("serial")
@Entity
@NamedQueries({
		@NamedQuery(name = "Job.FIND_BY_USERNAME",
					query = "SELECT j FROM Job j WHERE j.username = :username ORDER BY j.submitDate DESC"),
		@NamedQuery(name = "Job.FIND_BY_ID",
					query = "SELECT j FROM Job j WHERE j.id = :id")
})

public class Job implements Serializable {



	public enum Status {
		COMPLETED, CANCELLED, OTHER
	}

	public final static String FIND_BY_USERNAME = "Job.FIND_BY_USERNAME";

	private String batch;

	@Enumerated(EnumType.STRING)
	private Status status;

	@Id
	@GeneratedValue
	private long id;

	private String username;

	private String jobId;

	@Temporal(value = TemporalType.TIMESTAMP)
	private Date submitDate;

	private String jobType;

	private long provenanceId;

	public Job() {
	}

	public String getBatch() {
		return batch;
	}

	public long getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public String getJobId() {
		return jobId;
	}

	public Date getSubmitDate() {
		return submitDate;
	}

	public void setSubmitDate(Date date) {
		this.submitDate = date;
	}

	public void setBatch(String batch) {
		this.batch = batch;
	}

	public void setJobType(String jobType) {
		this.jobType = jobType;
	}

	public String getJobType() {
		return jobType;
	}

	public long getProvenanceId() {
		return provenanceId;
	}

	public void setProvenanceId(long provenanceId) {
		this.provenanceId = provenanceId;
	}

}
