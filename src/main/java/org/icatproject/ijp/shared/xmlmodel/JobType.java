package org.icatproject.ijp.shared.xmlmodel;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.gwt.user.client.rpc.IsSerializable;

@XmlRootElement
public class JobType implements IsSerializable {

	private String name;
	private String executable;
	private boolean multiple;
	private String type;

	@XmlAttribute
	private String family;
	@XmlAttribute
	private boolean sessionId;
	@XmlAttribute
	private boolean icatUrlRequired;
	@XmlAttribute
	private boolean idsUrlRequired;
	@XmlAttribute
	private boolean acceptsDatasets;
	@XmlAttribute
	private boolean acceptsDatafiles;
	@XmlElement
	private List<String> datasetTypes = new ArrayList<String>();
	@XmlElement
	private List<JobOption> jobOptions = new ArrayList<JobOption>();

	public JobType() {

	}

	public String toString() {
		StringBuilder sb = new StringBuilder("name='" + name + "', executable='" + executable
				+ "', multiple='" + multiple + "', type='" + type + "'" + " datasetTypes=");
		for (int i = 0; i < datasetTypes.size(); i++) {
			sb.append("'" + datasetTypes.get(i) + "'");
			if (i != datasetTypes.size() - 1) {
				sb.append(',');
			}
		}
		for (JobOption jobOption : jobOptions) {
			sb.append(" " + jobOption.toString());
		}
		return sb.toString();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getExecutable() {
		return executable;
	}

	public void setExecutable(String executable) {
		this.executable = executable;
	}

	public boolean getMultiple() {
		return multiple;
	}

	public void setMultiple(boolean multiple) {
		this.multiple = multiple;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<String> getDatasetTypes() {
		return datasetTypes;
	}

	public List<JobOption> getJobOptions() {
		return jobOptions;
	}

	public String getFamily() {
		return family;
	}

	public boolean isSessionId() {
		return sessionId;
	}
	
	public boolean isIcatUrlRequired() {
		return icatUrlRequired;
	}

	public boolean isIdsUrlRequired() {
		return idsUrlRequired;
	}
	
	public boolean isAcceptsDatasets(){
		return acceptsDatasets;
	}

	public boolean isAcceptsDatafiles(){
		return acceptsDatafiles;
	}

}
