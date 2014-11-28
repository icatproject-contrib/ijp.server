package org.icatproject.ijp.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DatafileListContent implements SelectionListContent {

	private static final String COL_TITLE_NAME = "Name";
	private static final String COL_TITLE_DESC = "Description";
	private static final String COL_TITLE_SIZE = "Size";
	private static final String COL_TITLE_CREATE_TIME = "Created";
	private static final String COL_TITLE_MOD_TIME = "Modified";
	
	// For now, use local fields. TODO define DatafileOverview ?
	private Long id;
	private String name;
	private String description;
	private String size;
	private String createTime;
	private String modTime;
	
	private static final List<String> availableColumnsList 
		= new ArrayList<String>(Arrays.asList(COL_TITLE_NAME,COL_TITLE_DESC,COL_TITLE_SIZE,COL_TITLE_CREATE_TIME,COL_TITLE_MOD_TIME));
	
	public DatafileListContent( Long id, String name, String description, String size, String createTime, String modTime ){
		this.id = id;
		this.name = name;
		this.description = description;
		this.size = size;
		this.createTime = createTime;
		this.modTime = modTime;
	}
	
	@Override
	public List<String> availableColumns() {
		return DatafileListContent.availableColumnsList;
	}

	@Override
	public String getColumn(String columnName) {
		if( columnName.equals(COL_TITLE_NAME) ){
			return this.name;
		} else if( columnName.equals(COL_TITLE_DESC) ){
			return this.description;
		} else if( columnName.equals(COL_TITLE_SIZE) ){
			return this.size;
		} else if( columnName.equals(COL_TITLE_CREATE_TIME) ){
			return this.createTime;
		} else if( columnName.equals(COL_TITLE_MOD_TIME) ){
			return this.modTime;
		}
		return null;
	}

	@Override
	public Long getId() {
		return id;
	}

	// Override equals to use the IDs
	
	@Override
	public boolean equals(Object obj){
		if( !(obj instanceof DatafileListContent) ){
			return false;
		}
		if( obj == this ){
			return true;
		}
		DatafileListContent dlc = (DatafileListContent) obj;
		return getId().equals(dlc.getId());
	}
	
	// HashCode should be based on the ID.
	@Override
	public int hashCode(){
		return Long.valueOf(getId()).hashCode();
	}
}
