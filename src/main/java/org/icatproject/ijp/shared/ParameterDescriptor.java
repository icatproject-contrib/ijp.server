package org.icatproject.ijp.shared;

import org.icatproject.ijp.shared.PortalUtils.ParameterLevelType;
import org.icatproject.ijp.shared.PortalUtils.ParameterValueType;

public class ParameterDescriptor {

	private String icatParameterName;
	private ParameterValueType parameterValueType;
	private ParameterLevelType parameterLevelType;

	public ParameterDescriptor( String icatParameterName, ParameterValueType parameterValueType, ParameterLevelType parameterLevelType ) {
		this.icatParameterName = icatParameterName;
		this.parameterValueType = parameterValueType;
		this.parameterLevelType = parameterLevelType;
	}
	
	public String getIcatParameterName() {
		return icatParameterName;
	}

	public void setIcatParameterName(String icatParameterName) {
		this.icatParameterName = icatParameterName;
	}
	
	public ParameterValueType getParameterValueType() {
		return parameterValueType;
	}

	public void setParameterValueType(ParameterValueType parameterValueType) {
		this.parameterValueType = parameterValueType;
	}

	public ParameterLevelType getParameterLevelType() {
		return parameterLevelType;
	}

	public void setParameterLevelType(ParameterLevelType parameterLevelType) {
		this.parameterLevelType = parameterLevelType;
	}

}
