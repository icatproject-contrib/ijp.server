package org.icatproject.ijp.shared.xmlmodel;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
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
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		JsonGenerator gen = Json.createGenerator(baos).writeStartObject();
		gen.write("name",name);
		gen.write("executable",executable);
		gen.write("multiple",multiple);
		gen.write("type",type);
		gen.writeStartArray("datasetTypes");
		for( String datasetType : datasetTypes ){
			gen.write(datasetType);
		}
		gen.writeEnd(); // of datasetTypes array
		gen.writeStartArray("jobOptions");
		for( JobOption jobOption : jobOptions ){
			// Add each JobOption as a map from its name to its content
			gen
				.writeStartObject()
					.write(jobOption.getName(), jobOption.toString())
				.writeEnd();
		}
		gen.writeEnd(); // of jobOptions array
		gen.writeEnd().close();
		
		return baos.toString();
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
