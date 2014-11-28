package org.icatproject.ijp.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.icatproject.ijp.shared.DatasetOverview;

public class DatasetListContent implements SelectionListContent {

	private DatasetOverview datasetOverview;
	
	private static final String COL_TITLE_NAME = "Name";
	private static final String COL_TITLE_SAMPLE = "Sample Description";
	private static final String COL_TITLE_USERS = "Users";
	
	private static final List<String> availableColumnsList = new ArrayList<String>(Arrays.asList(COL_TITLE_NAME,COL_TITLE_SAMPLE,COL_TITLE_USERS));
	
	public DatasetListContent( DatasetOverview datasetOverview ){
		this.datasetOverview = datasetOverview;
	}
	
	@Override
	public List<String> availableColumns() {
		return DatasetListContent.availableColumnsList;
	}

	@Override
	public String getColumn(String columnName) {
		if (columnName.equals(COL_TITLE_NAME)) {
			return datasetOverview.getName();
		} else if (columnName.equals(COL_TITLE_SAMPLE)) {
			return datasetOverview.getSampleDescription();
		} else if (columnName.equals(COL_TITLE_USERS)) {
			return datasetOverview.getUsers();
		}
		return null;
	}

	@Override
	public Long getId() {
		return datasetOverview.getDatasetId();
	}

	public DatasetOverview getDatasetOverview() {
		return this.datasetOverview;
	}
	
	// Override equals to use the IDs
	// (Using DatasetOverview.equals() does not work - uses object identity)
	
	@Override
	public boolean equals(Object obj){
		if( !(obj instanceof DatasetListContent) ){
			return false;
		}
		if( obj == this ){
			return true;
		}
		DatasetListContent dlc = (DatasetListContent) obj;
		return getId().equals(dlc.getId());
	}
	
	// HashCode should be based on the ID.
	@Override
	public int hashCode(){
		return Long.valueOf(getId()).hashCode();
	}

}
