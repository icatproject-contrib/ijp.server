package org.icatproject.ijp.client;

import java.util.List;

/**
 * Interface for row-data objects for SelectionListPanel. We anticipate two
 * implementations, one using DatasetOverview, and one for Datafiles.
 */
public interface SelectionListContent {

	/**
	 * The list of available columns, giving their titles
	 * 
	 * @return column titles as List&lt;String&gt;
	 */
	public List<String> availableColumns();

	/**
	 * Get the String value for the given column name
	 * 
	 * @param columnName
	 *            the name of the column
	 * 
	 * @return the string value
	 */
	public String getColumn(String columnName);

	/**
	 * Get the ID for this object. Implementation-dependent: at present, will be
	 * a dataset ID or a datafile ID.
	 * 
	 * @return the id
	 */
	public Long getId();
}
