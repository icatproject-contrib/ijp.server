package org.icatproject.ijp.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.icatproject.ijp.client.SelectionListContent;

public class DatafileOverview implements SelectionListContent, Serializable {

	private static final long serialVersionUID = 1L;

	private static final String COL_TITLE_NAME = "Name";
	private static final String COL_TITLE_DESC = "Description";
	private static final String COL_TITLE_SIZE = "Size";
	private static final String COL_TITLE_CREATE_TIME = "Created";
	private static final String COL_TITLE_MOD_TIME = "Modified";
	
	private Long id;
	private String name;
	private String description;
	private String size;
	private String createTime;
	private String modTime;
	private Long datasetId;
	
	private static final List<String> availableColumnsList 
		= new ArrayList<String>(Arrays.asList(COL_TITLE_NAME,COL_TITLE_DESC,COL_TITLE_SIZE,COL_TITLE_CREATE_TIME,COL_TITLE_MOD_TIME));
	
	public DatafileOverview() {
		
	}
	
	@Override
	public List<String> availableColumns() {
		return DatafileOverview.availableColumnsList;
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
	
	public void setId( Long id ){
		this.id = id;
	}
	
	public void setName( String name ){
		this.name = name;
	}
	
	public void setSize( String size ){
		this.size = size;
	}
	
	public void setCreateTime( String createTime ){
		this.createTime = createTime;
	}
	
	public void setModTime( String modTime ){
		this.modTime = modTime;
	}
	
	public Long getDatasetId(){
		return datasetId;
	}
	
	public void setDatasetId( Long datasetId ){
		this.datasetId = datasetId;
	}

	// Override equals to use the IDs
	
	@Override
	public boolean equals(Object obj){
		if( !(obj instanceof DatafileOverview) ){
			return false;
		}
		if( obj == this ){
			return true;
		}
		DatafileOverview dlc = (DatafileOverview) obj;
		return getId().equals(dlc.getId());
	}
	
	// HashCode should be based on the ID.
	@Override
	public int hashCode(){
		return Long.valueOf(getId()).hashCode();
	}
}
