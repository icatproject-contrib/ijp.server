package org.icatproject.ijp.shared.xmlmodel;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
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
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		JsonGenerator gen = Json.createGenerator(baos).writeStartObject();
		gen.write("name",name);
		gen.write("groupName", groupName);
		gen.write("type",type);
		gen.write("programParameter",programParameter);
		gen.writeStartArray("values");
		for( String value : values ){
			gen.write(value);
		}
		gen.writeEnd(); // of values array
		gen.write("defaultValue",defaultValue);
		gen.write("minValue",minValue);
		gen.write("maxValue",maxValue);
		gen.write("condition",condition);
		gen.write("tip",tip);
		gen.writeEnd().close();
		
		return baos.toString();
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
