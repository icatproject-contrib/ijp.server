package org.icatproject.ijp.shared.xmlmodel;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.gwt.user.client.rpc.IsSerializable;

@XmlRootElement
public class JobDatasetType implements IsSerializable {

	@XmlElement
	private List<String> datasetTypes = new ArrayList<String>();
	@XmlElement(name="jobDatasetParameter")
	private List<JobDatasetParameter> jobDatasetParameterList = new ArrayList<JobDatasetParameter>();
	
	public JobDatasetType() {
		
	}
	
	public String toString() {
		String lineSep = "\n";
		String objectAsString = "datasetTypes=";
		for (int i=0; i<datasetTypes.size(); i++) {
			objectAsString += "'" + datasetTypes.get(i) + "'";
			if ( i != datasetTypes.size()-1 ) {
				objectAsString += ",";
			}
		}
		objectAsString += lineSep;
		for (JobDatasetParameter jobDatasetParameter : jobDatasetParameterList) {
			objectAsString += jobDatasetParameter.toString() + lineSep;
		}
		return objectAsString;
	}

	public List<String> getDatasetTypes() {
		return datasetTypes;
	}

	public List<JobDatasetParameter> getJobDatasetParameterList() {
		return jobDatasetParameterList;
	}

}
