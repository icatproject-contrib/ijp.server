package org.icatproject.ijp.shared.xmlmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.gwt.user.client.rpc.IsSerializable;

@XmlRootElement
public class SearchItems implements IsSerializable {

	@XmlElement(name = "searchItem")
	private List<SearchItem> searchItemList = new ArrayList<SearchItem>();

	public SearchItems() {

	}

	public String toString() {
		if (searchItemList == null) {
			return "<null>";
		} else if (searchItemList.size() == 0) {
			return "<empty list>";
		}
		String lineSep = "\n";
		String returnString = "";
		for (int i = 0; i < searchItemList.size(); i++) {
			returnString += "searchItemList[" + i + "]:" + lineSep
					+ searchItemList.get(i).toString() + lineSep;
		}
		return returnString;
	}

	public List<SearchItem> getSearchItemList() {
		return searchItemList;
	}

	/**
	 * Return the search items in a Map keyed on the parameter name
	 */
	public Map<String, SearchItem> toMap() {
		Map<String, SearchItem> map = new HashMap<String, SearchItem>();
		for (SearchItem searchItem : searchItemList) {
			map.put(searchItem.getParamName(), searchItem);
		}
		return map;
	}
}
