package org.icatproject.ijp_portal.shared.xmlmodel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

public class JobDatasetMappings implements IsSerializable {

	private Map<String,List<JobDatasetParameter>> jobDatasetParametersMap;
	
	public JobDatasetMappings() {
		jobDatasetParametersMap = new HashMap<String,List<JobDatasetParameter>>();
	}

	public Map<String,List<JobDatasetParameter>> getJobDatasetParametersMap() {
		return jobDatasetParametersMap;
	}
	
	public List<JobDatasetParameter> getJobDatasetParametersForType(String datasetType) {
		return jobDatasetParametersMap.get(datasetType);
	}

	public void addJobDatasetTypeToMap(JobDatasetType jobDatasetType) {
		List<String> datasetTypes = jobDatasetType.getDatasetTypes();
		for (String datasetType : datasetTypes) {
			List<JobDatasetParameter> jobDatasetParametersList = getJobDatasetParametersForType(datasetType);
			if (jobDatasetParametersList == null) {
				// create a new entry for this dataset type
				jobDatasetParametersMap.put(datasetType, jobDatasetType.getJobDatasetParameterList());
			} else {
				// add to the existing entry for this dataset type
				jobDatasetParametersList.addAll(jobDatasetType.getJobDatasetParameterList());
			}
		}
	}
	
	public String toString() {
		String lineSep = "\n";
		String objectAsString = "";
		for (String datasetType : jobDatasetParametersMap.keySet()) {
			objectAsString += datasetType + ":" + lineSep;
			List<JobDatasetParameter> jobDatasetParametersList = getJobDatasetParametersForType(datasetType);
			for (JobDatasetParameter jobDatasetParameter : jobDatasetParametersList) {
				objectAsString += jobDatasetParameter.toString() + lineSep;
			}
		}
		return objectAsString;
	}

}
