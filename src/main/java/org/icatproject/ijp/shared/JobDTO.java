package org.icatproject.ijp.shared;

import java.io.Serializable;
import java.util.Date;

/**
 * Data Transfer Object for the Job entity bean
 *
 */
@SuppressWarnings("serial")
public class JobDTO implements Serializable {

	private String id;
	
	private String status;

	private String username;
	
	private Date submitDate;
	
	private long icatJobId;
	
	private String batchFilename;
	
	private String workerNode;

	private String comment;
	
	public String getComment() {
		return comment;
	}

	public JobDTO() {
		
	}
	
	public String toString() {
		return 	"id='" + id + "' " + 
				"status='" + status + "' " +  
				"username='" + username + "' " +  
				"submitDate='" + submitDate.toString() + "' " + 
				"icatJobId='" + icatJobId + "' " +  
				"batchFilename='" + batchFilename + "' " +  
				"workerNode='" + workerNode + "'" +
				"comment='" + comment + "'";
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getSubmitDate() {
		return submitDate;
	}

	public void setSubmitDate(Date submitDate) {
		this.submitDate = submitDate;
	}

	public long getIcatJobId() {
		return icatJobId;
	}

	public void setIcatJobId(long icatJobId) {
		this.icatJobId = icatJobId;
	}

	public String getBatchFilename() {
		return batchFilename;
	}

	public void setBatchFilename(String batchFilename) {
		this.batchFilename = batchFilename;
	}

	public String getWorkerNode() {
		return workerNode;
	}

	public void setWorkerNode(String workerNode) {
		this.workerNode = workerNode;
	}

	public void setComment(String comment) {
		this.comment = comment;
		
	}

	
}
