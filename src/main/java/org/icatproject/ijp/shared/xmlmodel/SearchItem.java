package org.icatproject.ijp.shared.xmlmodel;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SearchItem implements IsSerializable {

	private String paramName;
	private boolean multipleSelect = false;
	private int visibleItemCount = 1;
	private String query;
	@XmlElement
	private List<ListOption> listOptions = new ArrayList<ListOption>();

	public SearchItem() {
		
	}
	
	public String toString() {
		String lineSep = "\n";
		String returnString = "";
		returnString += "paramName='" + paramName + "'" + lineSep;
		returnString += "multipleSelect='" + multipleSelect + "'" + lineSep;
		returnString += "visibleItemCount='" + visibleItemCount + "'" + lineSep;
		returnString += "query='" + query + "'" + lineSep;
		for ( int i=0; i<listOptions.size(); i++ ) {
			returnString += "listOptions["+i+"]=[" + listOptions.get(i).toString() + "]" + lineSep;
		}
		return returnString;
	}
	
	public String getParamName() {
		return paramName;
	}
	
	public void setParamName(String paramName) {
		this.paramName = paramName;
	}
	
	public boolean isMultipleSelect() {
		return multipleSelect;
	}
	
	public void setMultipleSelect(boolean multipleSelect) {
		this.multipleSelect = multipleSelect;
	}
	
	public int getVisibleItemCount() {
		return visibleItemCount;
	}
	
	public void setVisibleItemCount(int visibleItemCount) {
		this.visibleItemCount = visibleItemCount;
	}
	
	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public List<ListOption> getListOptions() {
		return listOptions;
	}

}
