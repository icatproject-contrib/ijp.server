package org.icatproject.ijp.shared;

import java.io.Serializable;
import java.util.Date;

public class GenericSearchSelections implements Serializable {

	private static final long serialVersionUID = 1L;

	private String searchParamName;
	private String searchOperator;
	private String searchValueString;
	private Double searchValueNumeric;
	private Double fromValueNumeric;
	private Double toValueNumeric;
	private Date fromDate;
	private Date toDate;
	
	public String toString() {
		return "Param: " + searchParamName + " " +
			   "Oper: " + searchOperator + " " +
			   "SearchStr: " + searchValueString + " " +
			   "SearchNum: " + searchValueNumeric + " " +
			   "FromNum: " + fromValueNumeric + " " +
			   "ToNum: " + toValueNumeric + " " +
			   "FromDate: " + fromDate + " " +
			   "ToDate: " + toDate;
	}
	
	public String getSearchParamName() {
		return searchParamName;
	}
	
	public void setSearchParamName(String searchParamName) {
		this.searchParamName = searchParamName;
	}

	public String getSearchOperator() {
		return searchOperator;
	}

	public void setSearchOperator(String searchOperator) {
		this.searchOperator = searchOperator;
	}
	
	public String getSearchValueString() {
		return searchValueString;
	}
	
	public void setSearchValueString(String searchValueString) {
		this.searchValueString = searchValueString;
	}
	
	public Date getFromDate() {
		return fromDate;
	}
	
	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}
	
	public Date getToDate() {
		return toDate;
	}
	
	public void setToDate(Date toDate) {
		this.toDate = toDate;
	}

	public Double getFromValueNumeric() {
		return fromValueNumeric;
	}

	public void setFromValueNumeric(Double fromValueNumeric) {
		this.fromValueNumeric = fromValueNumeric;
	}

	public Double getToValueNumeric() {
		return toValueNumeric;
	}

	public void setToValueNumeric(Double toValueNumeric) {
		this.toValueNumeric = toValueNumeric;
	}

	public Double getSearchValueNumeric() {
		return searchValueNumeric;
	}

	public void setSearchValueNumeric(Double searchValueNumeric) {
		this.searchValueNumeric = searchValueNumeric;
	}
	
	
}
