package org.icatproject.ijp.shared.xmlmodel;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.xml.bind.annotation.XmlElement;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.rpc.IsSerializable;

public class JobOption implements IsSerializable {

	private String name;
	private String groupName;
	private String type;
	private String programParameter;
	@XmlElement
	private List<String> values = new ArrayList<String>();
	private String defaultValue;
	private String minValue;
	private String maxValue;
	private String condition;
	private String tip;
	
	public JobOption() {
		
	}

	/**
	 * Return a JSON string representing this JobOption.
	 */
	public String toString() {
		
		return this.toJson().toString();
	}
	
	/**
	 * Return a JSONObject representing this JobOption
	 * @return JSONObject
	 */
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("name",new JSONString(name));
		json.put("groupName",new JSONString(groupName));
		json.put("type",new JSONString(type));
		json.put("programParameter",new JSONString(programParameter));
		JSONArray valuesArray = new JSONArray();
		int i = 0;
		for( String value : values ){
			valuesArray.set(i++, new JSONString(value));
		}
		json.put("values",  valuesArray);
		json.put("defaultValue",new JSONString(defaultValue));
		json.put("minValue",new JSONString(minValue));
		json.put("maxValue",new JSONString(maxValue));
		json.put("condition",new JSONString(condition));
		json.put("tip",new JSONString(tip));
		return json;
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

	public String getMinValue() {
		return minValue;
	}

	public void setMinValue(String minValue) {
		this.minValue = minValue;
	}

	public String getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(String maxValue) {
		this.maxValue = maxValue;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public String getTip() {
		return tip;
	}

	public void setTip(String tip) {
		this.tip = tip;
	}

}
