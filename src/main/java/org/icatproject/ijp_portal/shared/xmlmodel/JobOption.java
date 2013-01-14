package org.icatproject.ijp_portal.shared.xmlmodel;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import com.google.gwt.user.client.rpc.IsSerializable;

public class JobOption implements IsSerializable {

	private String name;
	private String groupName;
	private String type;
	private String programParameter;
	@XmlElement
	private List<String> values = new ArrayList<String>();
	private String defaultValue;
	private String condition;
	
	public JobOption() {
		
	}

	public String toString() {
		String valuesAsString = "null";
		if (values != null) {
			valuesAsString = "";
			for (int i=0; i<values.size(); i++) {
				valuesAsString += "'" + values.get(i) + "'";
				if ( i != values.size()-1 ) {
					valuesAsString += ",";
				}
			}
		}
		return "name='" + name + "', " + 
			   "groupName='" + groupName + "', " + 
			   "type='" + type + "', " + 
			   "programParameter='" + programParameter + "', " +
			   "values='" + valuesAsString + "', " +
			   "defaultValue='" + defaultValue + "', " +
			   "condition='" + condition + "'";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getProgramParameter() {
		return programParameter;
	}

	public void setProgramParameter(String programParameter) {
		this.programParameter = programParameter;
	}

	public List<String> getValues() {
		return values;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

}
