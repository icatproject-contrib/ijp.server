package org.icatproject.ijp_portal.server.manager;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.datatype.XMLGregorianCalendar;

import org.icatproject.Dataset;
import org.icatproject.DatasetParameter;
import org.icatproject.EntityField;
import org.icatproject.EntityInfo;
import org.icatproject.ICAT;
import org.icatproject.IcatExceptionType;
import org.icatproject.IcatException_Exception;
import org.icatproject.Login.Credentials;
import org.icatproject.Login.Credentials.Entry;
import org.icatproject.ParameterType;
import org.icatproject.ijp_portal.server.Icat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.icatproject.ijp_portal.shared.Constants;
import org.icatproject.ijp_portal.shared.DatasetOverview;
import org.icatproject.ijp_portal.shared.GenericSearchSelections;
import org.icatproject.ijp_portal.shared.PortalUtils;
import org.icatproject.ijp_portal.shared.PortalUtils.ParameterLevelType;
import org.icatproject.ijp_portal.shared.PortalUtils.ParameterValueType;
import org.icatproject.ijp_portal.shared.ServerException;
import org.icatproject.ijp_portal.shared.SessionException;
import org.icatproject.ijp_portal.shared.xmlmodel.JobDatasetParameter;
import org.icatproject.ijp_portal.shared.xmlmodel.SearchItem;

import uk.ac.rl.esc.catutils.CheckedProperties;

public class DataServiceManager {

	final static Logger logger = LoggerFactory.getLogger(DataServiceManager.class);

	private XmlFileManager xmlFileManager;

	private ICAT icat;

	private static List<String> DATASET_FIELDS_TO_USE;
	private static Map<String, ParameterValueType> DATASET_FIELD_TYPE_TO_PARAMVALUETYPE_MAPPINGS;
	private Map<String, ParameterValueType> datasetFieldTypeMappings = new HashMap<String, ParameterValueType>();
	private Map<String, ParameterValueType> datasetParameterTypeMappings = null;
	private LinkedHashMap<String, ParameterValueType> mergedDatasetParameterTypeMappings = null;
	private Map<String, Method> datasetFieldMethodMappings = new HashMap<String, Method>();

	static {
		// list the types of fields of Dataset that should be searchable with the generic search
		DATASET_FIELDS_TO_USE = new ArrayList<String>();
		DATASET_FIELDS_TO_USE.add("String");	// description, name, doi, location
		DATASET_FIELDS_TO_USE.add("Date"); 		// startDate, endDate
		DATASET_FIELDS_TO_USE.add("Long"); 		// id
		
		// list how these types map onto ParameterValueTypes
		DATASET_FIELD_TYPE_TO_PARAMVALUETYPE_MAPPINGS = new HashMap<String, ParameterValueType>();
		DATASET_FIELD_TYPE_TO_PARAMVALUETYPE_MAPPINGS.put("String", ParameterValueType.STRING);
		DATASET_FIELD_TYPE_TO_PARAMVALUETYPE_MAPPINGS.put("Date", ParameterValueType.DATE_AND_TIME);
		DATASET_FIELD_TYPE_TO_PARAMVALUETYPE_MAPPINGS.put("Long", ParameterValueType.NUMERIC);
	}

	public DataServiceManager() throws ServerException {

		xmlFileManager = new XmlFileManager();
		try {
			icat = Icat.getIcat();

			CheckedProperties props = new CheckedProperties();
			props.loadFromFile(Constants.PROPERTIES_FILEPATH);
			String icatAuthType = props.getString("icat.auth.type");
			String icatUsername = props.getString("icat.username");
			String icatPassword = props.getString("icat.password");

			Map<String, String> credentials = new HashMap<String, String>();
			credentials.put("username", icatUsername);
			credentials.put("password", icatPassword);
			String sessionId = login(icatAuthType, credentials);

			populateDatasetFieldTypeAndMethodMappings();
			populateDatasetParameterTypesMap(sessionId);
			populateMergedDatasetParameterTypesMap();
			
		} catch (Exception e) {
			throw new ServerException(e.getMessage());
		}
	}

	/**
	 * take a field name such as datasetId and generate the corresponding
	 * get method name ie. getDatasetId
	 * @param fieldName
	 * @return the method name that would be called to retrieve the field
	 */
	private String getMethodName(String fieldName) {
		StringBuilder sb = new StringBuilder("get");
		sb.append(fieldName.substring(0,1).toUpperCase());
		sb.append(fieldName.substring(1));
		return sb.toString();
	}

	private void populateDatasetFieldTypeAndMethodMappings() throws IcatException_Exception, NoSuchMethodException, SecurityException {
		EntityInfo ei = icat.getEntityInfo("Dataset");
		for (EntityField field : ei.getFields()) {
			// for each field (of the types we are interested in)
			// put an entry in a field name to param value type map
			// and an entry in a field name to get method map
			if ( DATASET_FIELDS_TO_USE.contains(field.getType()) ) {
				datasetFieldTypeMappings.put(field.getName(), DATASET_FIELD_TYPE_TO_PARAMVALUETYPE_MAPPINGS.get(field.getType()));
				datasetFieldMethodMappings.put(field.getName(), Dataset.class.getMethod(getMethodName(field.getName()), new Class[0]));
			}
		} 
	}

	private void populateDatasetParameterTypesMap(String sessionId) throws IcatException_Exception {
		datasetParameterTypeMappings = new HashMap<String, ParameterValueType>();
		List<Object> resultsFromIcat = icat.search(sessionId, "ParameterType [applicableToDataset = True]");
		for ( Object resultFromIcat : resultsFromIcat ) {
			ParameterType paramType = (ParameterType) resultFromIcat;
			// convert ICAT ParameterValueTypes to PortalUtils ParameterValueTypes so they can be sent back to the client
			datasetParameterTypeMappings.put(paramType.getName(), ParameterValueType.valueOf(ParameterValueType.class, paramType.getValueType().name()));
		}
	}

	private void populateMergedDatasetParameterTypesMap() {
		Map<String, ParameterValueType> combinedMap = new HashMap<String, ParameterValueType>();
		combinedMap.putAll(datasetFieldTypeMappings);
		combinedMap.putAll(datasetParameterTypeMappings);
		List<String> keysAsList = new ArrayList<String>(combinedMap.keySet());
		Collections.sort(keysAsList);
		mergedDatasetParameterTypeMappings = new LinkedHashMap<String, ParameterValueType>();
		// loop through the ordered keys inserting them into the LinkedHashMap
		// to be returned in this order
		for ( String paramName : keysAsList ) {
			mergedDatasetParameterTypeMappings.put(paramName, combinedMap.get(paramName));
		}

	}

	public String login(String plugin, Map<String, String> credentialMap) throws SessionException {
		Credentials credentials = new Credentials();
		List<Entry> entries = credentials.getEntry();
		for (java.util.Map.Entry<String, String> mapEntries : credentialMap.entrySet()) {
			Entry e = new Entry();
			e.setKey(mapEntries.getKey());
			e.setValue(mapEntries.getValue());
			entries.add(e);
		}
		try {
			String sessionId = icat.login(plugin, credentials);
//			// use the session ID provided to retrieve a list of
//			// dataset parameters from ICAT and populate a map
//			if ( datasetParameterTypeMappings == null ) {
//				populateDatasetParameterTypesMap(sessionId);
//			}
//			// combine the dataset field and dataset parameters maps
//			// into one ordered map 
//			if ( mergedDatasetParameterTypeMappings == null ) {
//				populateMergedDatasetParameterTypesMap();
//			}
			return sessionId;
		} catch (IcatException_Exception e) {
			throw new SessionException("IcatException " + e.getFaultInfo().getType() + " "
					+ e.getMessage());
		}
	}

	private String getParameterValueAsString(DatasetParameter datasetParameter) {
		String paramName = datasetParameter.getType().getName();
		String paramValueAsString = "";
		if (mergedDatasetParameterTypeMappings.get(paramName) == null) {
			// return null for parameters not in the mappings table so we can ignore them
			return null;
		} else if (mergedDatasetParameterTypeMappings.get(paramName) == ParameterValueType.STRING) {
			String paramValueString = datasetParameter.getStringValue();
			if (paramValueString != null) {
				paramValueAsString = paramValueString;
			}
		} else if (mergedDatasetParameterTypeMappings.get(paramName) == ParameterValueType.NUMERIC) {
			Double paramValueDouble = datasetParameter.getNumericValue();
			if (paramValueDouble != null) {
				// all numeric values are stored in ICAT as Doubles
				// the basic idea here is that if we have a Double value that does not have a fractional part
				// then the value is likely to be an "integer" value and the user does not want to see values
				// like 1, 2, 3 presented as 1.0, 2.0, 3.0 etc
				// "integer" values between 9007199254740992 and -9007199254740992 can be represented exactly
				// in a Double so we will only attempt to convert values in this range
				// see: http://mindprod.com/applet/converter.html
				
				// TODO - investigate if BigDecimal longValueExact will work here and simplify the code
				
				BigDecimal paramValueBigDecimal = new BigDecimal(paramValueDouble);
				
				BigDecimal twoToPower53BigDecimal = new BigDecimal(PortalUtils.TWO_TO_POWER_53_DOUBLE);
				BigDecimal minusTwoToPower53BigDecimal = new BigDecimal(PortalUtils.MINUS_TWO_TO_POWER_53_DOUBLE);

				int compareMaxInt = paramValueBigDecimal.compareTo(twoToPower53BigDecimal);
				int compareMinInt = paramValueBigDecimal.compareTo(minusTwoToPower53BigDecimal);

				// use this simple conversion unless the conditions below are met
				// use a string representation of this BigDecimal using engineering notation if an exponent is needed.
				paramValueAsString = paramValueBigDecimal.toEngineeringString();

				// if the number is less than or equal to the maximum safe "integer" value and
				// the number is greater than or equal to the minimum safe "integer" value
				if ( (compareMaxInt == -1 || compareMaxInt == 0) &&
					 (compareMinInt ==  0 || compareMinInt == 1) ) {
					try {
						BigInteger bigInt = paramValueBigDecimal.toBigIntegerExact();
						// if the conversion on the line above worked then we now
						// have a number that looks like an "integer" - no decimal point etc
						paramValueAsString = bigInt.toString();
					} catch (ArithmeticException e) {
						// myDoubleBigDecimal has a non-zero fractional part
						// so use the simple conversion done previously
					}
				}
			}
		} else if (mergedDatasetParameterTypeMappings.get(paramName) == ParameterValueType.DATE_AND_TIME) {
			// TODO - this needs testing when we have a date parameter
			XMLGregorianCalendar paramValueDate = datasetParameter.getDateTimeValue();
			if (paramValueDate != null) {
				paramValueAsString = paramValueDate.toString();
			}
		}
		return paramValueAsString;
	}

	public List<DatasetOverview> getDatasetList(String sessionId,
			String datasetType,
			Map<String, List<String>> selectedSearchParamsMap,
			List<GenericSearchSelections> genericSearchSelectionsList)
			throws SessionException, ServerException {
		
		logger.debug("datasetType=[" + datasetType.toString() + "]");
		List<DatasetOverview> datasetOverviews = new ArrayList<DatasetOverview>();
		List<String> queriesToRun = new ArrayList<String>();
		if ( selectedSearchParamsMap.isEmpty() ) {
			logger.debug("Contents of selectedSearchParamsMap: <EMPTY>");
			// do a query that returns all datasets of that type
			// leave queriesToRun as an empty list to achieve this
		} else {
			logger.debug("Contents of selectedSearchParamsMap:");
			for (String key : selectedSearchParamsMap.keySet()) {
				String debugLine = key + " : ";
				List<String> selectedParams = selectedSearchParamsMap.get(key);
				for (String selectedParam : selectedParams) {
					debugLine += "[" + selectedParam + "]";
				}
				logger.debug(debugLine);
			}

			Map<String, SearchItem> searchItemsMap = xmlFileManager.getSearchItems().toMap();
			logger.debug("About to loop through keys again...");
			for (String key : selectedSearchParamsMap.keySet()) {
				logger.debug("Processing key [" + key + "]");
				List<String> selectedParams = selectedSearchParamsMap.get(key);
				String query = searchItemsMap.get(key).getQuery();
				query = query.replace("${datasetType}", datasetType);
				query = replaceVariableInQuery(query, "${stringValues}", selectedParams, "'");
				query = replaceVariableInQuery(query, "${numericValues}", selectedParams, "");
				query = replaceVariableInQuery(query, "${dateValues}", selectedParams, "");
				queriesToRun.add(query);
			}
		}
		
		// generate the generic search queries strings
		List<String> genericQueryStrings = createGenericSearchQueriesList(datasetType, genericSearchSelectionsList);
		logger.debug("Contents of genericQueryStrings:");
		for (String genericQuery : genericQueryStrings) {
			logger.debug("genericQuery='" + genericQuery + "'");
		}
		// add genericQueryStrings to queriesToRun
		queriesToRun.addAll(genericQueryStrings);
		
		logger.debug("Contents of queriesToRun:");
		for (String query : queriesToRun) {
			logger.debug("query='" + query + "'");
		}

		try {
			Set<Object> matchingDatasetIds = getMatchingDatasetIdObjectsList(sessionId, queriesToRun);
			String finalQuery = null;
			if ( matchingDatasetIds == null ) {
				// one of the queries returned no results, so we need to return no results 
				return new ArrayList<DatasetOverview>();
			} else if ( matchingDatasetIds.size() == 0 ) {
				// return all datasets of this type
				finalQuery = "0," + PortalUtils.MAX_RESULTS
						+ " Dataset include DatasetParameter, ParameterType [type.name = '"
						+ datasetType + "'"
						+ " AND complete = true"
						+ "]";
			} else {
				// create a query listing the dataset ids to look up
				logger.debug("Compiling datasetIdListString from cumulativeDatasetIdObjects "
						+ "containing " + matchingDatasetIds.size() + " IDs");
				String datasetIdListString = "";
				for (Object datasetIdObject : matchingDatasetIds) {
					datasetIdListString += datasetIdObject + ",";
				}
				if (datasetIdListString.length() > 0) {
					// remove the last comma
					datasetIdListString = datasetIdListString.substring(0, datasetIdListString.length()-1);
					// logger.debug(datasetIdListString);
					finalQuery = "0," + PortalUtils.MAX_RESULTS
							+ " Dataset include DatasetParameter, ParameterType [id IN ("
							+ datasetIdListString + ")"
							+ " AND complete = true"
							+ "]";
				}
			}
			
			// execute the final query
			logger.debug("finalQuery=[" + finalQuery + "]");
			List<Object> datasetsFromIcat;
			datasetsFromIcat = icat.search(sessionId, finalQuery);
			for (Object o : datasetsFromIcat) {
				Dataset datasetFromIcat = (Dataset) o;
				DatasetOverview datasetOverview = new DatasetOverview();
				List<DatasetParameter> dsParams = datasetFromIcat.getParameters();
				HashMap<String, String> dsParamsMap = new HashMap<String, String>();
				for (DatasetParameter dsParam : dsParams) {
					String paramName = dsParam.getType().getName();
					String paramValueAsString = getParameterValueAsString(dsParam);
					if (paramValueAsString != null) {
						dsParamsMap.put(paramName, paramValueAsString);
					}
				}
				// set the id (the most important value to identify a dataset)
				datasetOverview.setDatasetId(datasetFromIcat.getId());
				// populate the fields required in the datasets table
				datasetOverview.setName(datasetFromIcat.getName());
				datasetOverview.setSampleDescription(dsParamsMap.get("sampledescription"));
				datasetOverview.setUsers(dsParamsMap.get("users"));
				
				datasetOverviews.add(datasetOverview);
			}
		} catch (IcatException_Exception e) {
			IcatExceptionType type = e.getFaultInfo().getType();
			if (type == IcatExceptionType.SESSION) {
				throw new SessionException(e.getMessage());
			} else {
				throw new ServerException("IcatException " + type + " " + e.getMessage());
			}
		}
		return datasetOverviews;
	}
	
	public Map<Long, Map<String, Object>> getJobDatasetParametersForDatasets(
			String sessionId, String datasetType, List<Long> datasetIds)
			throws ServerException, SessionException {
		Map<Long, Map<String, Object>> datasetToJobDatasetParametersMap = new HashMap<Long, Map<String, Object>>();
		// get the job dataset mappings queries to run on each returned dataset
		List<JobDatasetParameter> jobDatasetParameterList = xmlFileManager.getJobDatasetMappings().getJobDatasetParametersForType(datasetType);
		try {
			for ( Long datasetId : datasetIds ) {
				// set up the job dataset parameter map
				if ( jobDatasetParameterList != null ) {
					Map<String, Object> jobDatasetParamsMap = new HashMap<String, Object>();
					for (JobDatasetParameter jobDatasetParameter : jobDatasetParameterList) {
						String query = jobDatasetParameter.getQuery();
						query = query.replace("${datasetId}", datasetId.toString());
						List<Object> queryResult = icat.search(sessionId, query);
						if ( queryResult.size() < 1 ) {
							// this is acceptable and just means that, for example, this parameter is not set for this dataset 
							logger.debug("queryResult for '" + query + "' returned " + queryResult.size() + " results");
							// set this map parameter to null so that we can check for it in the client
							jobDatasetParamsMap.put(jobDatasetParameter.getName(), null);
						} else if ( queryResult.size() == 1 ) {
							jobDatasetParamsMap.put(jobDatasetParameter.getName(), queryResult.get(0));
						} else {
							// this should not happen because currently we can only insert a single item into the map
							// and the query has returned multiple results - the query needs changing
							logger.error("queryResult for '" + query + "' returned " + queryResult.size() + " results");
							// the best thing we can do is set this map parameter to null so that we can check for it in the client
							jobDatasetParamsMap.put(jobDatasetParameter.getName(), null);
						}
					}
//					logger.debug("Contents of jobDatasetParamsMap:");
//					for ( String paramName : jobDatasetParamsMap.keySet() ) {
//						String paramValue = "null";
//						if ( jobDatasetParamsMap.get(paramName) != null ) {
//							paramValue = jobDatasetParamsMap.get(paramName).toString();
//						}
//						logger.debug(paramName + " : " + paramValue);
//					}
					datasetToJobDatasetParametersMap.put(datasetId, jobDatasetParamsMap);
				}
			}
		} catch (IcatException_Exception e) {
			IcatExceptionType type = e.getFaultInfo().getType();
			if (type == IcatExceptionType.SESSION) {
				throw new SessionException(e.getMessage());
			} else {
				throw new ServerException("IcatException " + type + " " + e.getMessage());
			}
		}
		return datasetToJobDatasetParametersMap;
	}
	
	private List<String> createGenericSearchQueriesList(String datasetType, List<GenericSearchSelections> genericSearchSelectionsList) {
		List<String> genericSearchQueriesList = new ArrayList<String>();
		for ( GenericSearchSelections genericSearchSelection : genericSearchSelectionsList ) {
			String paramName = genericSearchSelection.getSearchParamName();
			String query = "Dataset.id [type.name = '" + datasetType + "']";
			ParameterValueType paramValueType = null;
			ParameterLevelType paramLevelType = null;
			if ( (paramValueType = datasetFieldTypeMappings.get(paramName)) != null ) {
				// this is a dataset field level search
				paramLevelType = ParameterLevelType.DATASET;
				query += " <-> Dataset [";
			} else if ( (paramValueType = datasetParameterTypeMappings.get(paramName)) != null ) {
				// this is a dataset field level search
				paramLevelType = ParameterLevelType.DATASET_PARAMETER;
				query += " <-> DatasetParameter [";
			} else {
				// something has gone wrong - a parameter has been submitted to search on
				// but it is not in either of the maps - this should never happen
				logger.error("Unable to process generic search for unknown parameter '" + paramName + "'");
				continue;
			}

			switch (paramLevelType) {
				case DATASET:
					query += "(" + paramName + " ";
					break;
				case DATASET_PARAMETER:
					query += "(type.name = '" + paramName + "' AND ${valueType} ";
					break;
			}
			query += genericSearchSelection.getSearchOperator();
			
			if (paramValueType == ParameterValueType.STRING) {
				// check there is a value to search for
				if ( !genericSearchSelection.getSearchValueString().equals("") ) {
					query = query.replace("${valueType}", "stringValue");
					query += " '" + genericSearchSelection.getSearchValueString() + "'";
				} else {
					// there was no value entered to search on so skip this one
					continue;
				}
			} else if (paramValueType == ParameterValueType.NUMERIC) {
				if (genericSearchSelection.getSearchValueNumeric() != null
						|| genericSearchSelection.getFromValueNumeric() != null
						|| genericSearchSelection.getToValueNumeric() != null) {
					query = query.replace("${valueType}", "numericValue");
					if (genericSearchSelection.getSearchOperator().equals("BETWEEN")) {
						query += " " + genericSearchSelection.getFromValueNumeric() + " AND "
								+ genericSearchSelection.getToValueNumeric();
					} else {
						query += " " + genericSearchSelection.getSearchValueNumeric();
					}
				} else {
					// there was no value entered to search on so skip this one
					continue;
				}
			} else if (paramValueType == ParameterValueType.DATE_AND_TIME) {
				if (genericSearchSelection.getFromDate() != null
						&& genericSearchSelection.getToDate() != null) {
					query = query.replace("${valueType}", "dateTimeValue");
					SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					query += " {ts ";
					query += dateTimeFormat.format(genericSearchSelection.getFromDate());
					query += "} AND {ts ";
					query += dateTimeFormat.format(genericSearchSelection.getToDate());
					query += "}";
				} else {
					// there was no value entered to search on so skip this one
					continue;
				}
			} else {
				// this must be a new ParameterValueType in a future version of ICAT
				// add some new code here to deal with it!
				continue;
			}
			query += ")]";
			genericSearchQueriesList.add(query);
		}
		return genericSearchQueriesList;
	}

	private String replaceVariableInQuery(String query, String variableName, List<String> valuesToInsert, String enclosingQuote ) {
		if ( query.indexOf(variableName) == -1 ) {
			// the variable to replace is not in the query string
//			logger.debug("Variable [" + variableName + "] not found in query [" + query + "]");
			return query;
		}
		String valuesString = "";
		for (String valueToInsert : valuesToInsert) {
			if ( !valuesString.equals("") ) {
				valuesString += ", ";
			}
			valuesString += enclosingQuote;
			valuesString += valueToInsert;
			valuesString += enclosingQuote;
		}
		String returnString = query.replace(variableName, valuesString);
		return returnString;
	}

	/**
	 * @param sessionId
	 * @param queries
	 * @return a set of object ids for datasets matching all the queries provided
	 *         the set will be empty if no queries are provided
	 *         the set will be null if any of the queries return no results
	 *         or if there is no overlap between one subsearch and the list of 
	 *         ids that is being compiled from the subsearches already done 
	 * @throws IcatException_Exception
	 */
	private Set<Object> getMatchingDatasetIdObjectsList(String sessionId, List<String> queries) throws IcatException_Exception {
		Set<Object> cumulativeDatasetIdObjects = new HashSet<Object>();
		for (String query : queries) {
			logger.debug("getMatchingDatasetIdObjectsList query=[" + query + "]");
			List<Object> datasetIdsFromIcat = icat.search(sessionId, query);
			// if one of the subsearches returns zero matches then we need to return
			// zero results so just return null so that we can recognise this fact
			if (datasetIdsFromIcat.size() == 0) {
				logger.debug("Search returned 0 results: returning null");
				return null;
			} else {
				// otherwise merge the list of dataset IDs returned with the
				// cumulative list of IDs
				Set<Object> datasetIdObjects = new HashSet<Object>(datasetIdsFromIcat);
				logger.debug("Size of datasetIds: " + datasetIdObjects.size());
				if (cumulativeDatasetIdObjects.size() == 0) {
					// add this list of dataset Ids to the currently empty list
					cumulativeDatasetIdObjects.addAll(datasetIdObjects);
				} else {
					// AND this set of dataset Ids with those already in the list being built
					cumulativeDatasetIdObjects.retainAll(datasetIdObjects);
				}
				logger.debug("Size of cumulativeDatasetIdObjects: "
						+ cumulativeDatasetIdObjects.size());
				if ( cumulativeDatasetIdObjects.size() == 0 ) {
					// there is no overlap between the results of the last search
					// and the list of ids compiled from searches so far
					// therefore the overall search must return no results
					return null;
				}
			}
		}
		return cumulativeDatasetIdObjects;
	}

	public LinkedHashMap<String, String> getDatasetParameters(String sessionId, Long datasetId) throws SessionException, ServerException {
		LinkedHashMap<String, String> datasetParams = new LinkedHashMap<String, String>();
		try {
			Dataset datasetFromIcat = (Dataset) icat.get(sessionId,
					"Dataset include DatasetParameter, ParameterType", datasetId);
			Map<String, String> datasetDataItemsMap = new HashMap<String, String>();
			for ( String fieldName : datasetFieldMethodMappings.keySet() ) {
				Method method = datasetFieldMethodMappings.get(fieldName);
				Object returnObj = null;
				try {
					returnObj = method.invoke(datasetFromIcat, new Object[0]);
					if ( returnObj != null ) {
						datasetDataItemsMap.put(fieldName, returnObj.toString());
					}
				} catch (Exception e) {
					logger.error(e.getClass().getSimpleName() + " invoking method " + method.getName() + " on dataset " + datasetId);
				}
			}
			List<DatasetParameter> datasetParamList = datasetFromIcat.getParameters();
			for ( DatasetParameter dsParam : datasetParamList ) {
				datasetDataItemsMap.put(dsParam.getType().getName(), getParameterValueAsString(dsParam));
			}
			List<String> keysAsList = new ArrayList<String>(datasetDataItemsMap.keySet());
			Collections.sort(keysAsList);
			// loop through the ordered keys inserting them into the LinkedHashMap
			// to be returned in this order
			for ( String paramName : keysAsList ) {
				datasetParams.put(paramName, datasetDataItemsMap.get(paramName));
			}
		} catch (IcatException_Exception e) {
			IcatExceptionType type = e.getFaultInfo().getType();
			if (type == IcatExceptionType.SESSION) {
				throw new SessionException(e.getMessage());
			} else {
				throw new ServerException("IcatException " + type + " " + e.getMessage());
			}
		}
		return datasetParams;
	}

	public List<String> getDatasetTypesList(String sessionId) throws SessionException, ServerException {
		List<String> datasetTypesList = new ArrayList<String>();
		try {
			List<Object> resultsFromIcat = icat.search(sessionId, "DatasetType.name");
			for ( Object resultFromIcat : resultsFromIcat ) {
				datasetTypesList.add((String)resultFromIcat);
			}
		} catch (IcatException_Exception e) {
			IcatExceptionType type = e.getFaultInfo().getType();
			if (type == IcatExceptionType.SESSION) {
				throw new SessionException(e.getMessage());
			} else {
				throw new ServerException("IcatException " + type + " " + e.getMessage());
			}
		}
		return datasetTypesList;
	}

	public LinkedHashMap<String, ParameterValueType> getDatasetParameterTypesMap(String sessionId) {
		return mergedDatasetParameterTypeMappings;
	}
	
}
