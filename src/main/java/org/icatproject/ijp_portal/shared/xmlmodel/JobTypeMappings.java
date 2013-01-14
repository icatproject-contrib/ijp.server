package org.icatproject.ijp_portal.shared.xmlmodel;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

public class JobTypeMappings implements IsSerializable {

	private Map<String,JobType> jobTypesMap;
	
	public JobTypeMappings() {
		jobTypesMap = new HashMap<String,JobType>();
	}

	public void addJobTypeToMap(JobType jobType) {
		jobTypesMap.put(jobType.getName(), jobType);
	}
	
	public Map<String,JobType> getJobTypesMap() {
		return jobTypesMap;
	}

	public String toString() {
		String lineSep = "\n";
		String objectAsString = "";
		for (String jobName : jobTypesMap.keySet()) {
			objectAsString += jobName + ":" + lineSep;
			JobType jobType = jobTypesMap.get(jobName);
			objectAsString += jobType.toString() + lineSep;
		}
		return objectAsString;
	}
	
}
