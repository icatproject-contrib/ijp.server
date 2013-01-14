package org.icatproject.ijp_portal.server.manager;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.datatype.XMLGregorianCalendar;

import org.icatproject.Dataset;
import org.icatproject.DatasetParameter;
import org.icatproject.ICAT;
import org.icatproject.IcatExceptionType;
import org.icatproject.IcatException_Exception;
import org.icatproject.Login.Credentials;
import org.icatproject.Login.Credentials.Entry;
import org.icatproject.ijp_portal.server.Icat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.icatproject.ijp_portal.shared.DatasetOverview;
import org.icatproject.ijp_portal.shared.GenericSearchSelections;
import org.icatproject.ijp_portal.shared.InternalException;
import org.icatproject.ijp_portal.shared.ParameterDescriptor;
import org.icatproject.ijp_portal.shared.PortalUtils;
import org.icatproject.ijp_portal.shared.PortalUtils.DatasetType;
import org.icatproject.ijp_portal.shared.PortalUtils.ParameterLevelType;
import org.icatproject.ijp_portal.shared.PortalUtils.ParameterValueType;
import org.icatproject.ijp_portal.shared.ProjectOverview;
import org.icatproject.ijp_portal.shared.ServerException;
import org.icatproject.ijp_portal.shared.SessionException;
import org.icatproject.ijp_portal.shared.xmlmodel.JobDatasetParameter;
import org.icatproject.ijp_portal.shared.xmlmodel.SearchItem;

public class DataServiceManager {

	final static Logger logger = LoggerFactory.getLogger(DataServiceManager.class);

	private XmlFileManager xmlFileManager;

	private ICAT icat;

	public DataServiceManager() throws ServerException {

		xmlFileManager = new XmlFileManager();
		try {
			icat = Icat.getIcat();
		} catch (InternalException e) {
			throw new ServerException(e.getMessage());
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
			return icat.login(plugin, credentials);
		} catch (IcatException_Exception e) {
			throw new SessionException("IcatException " + e.getFaultInfo().getType() + " "
					+ e.getMessage());
		}
	}

	private void addSearchStringIfNeeded(List<String> selectedItems, String paramName,
			ParameterValueType paramValueType, List<String> searchStrings) {
		String paramTypeString = null;
		if (paramValueType == ParameterValueType.STRING) {
			paramTypeString = "string";
		} else if (paramValueType == ParameterValueType.NUMERIC) {
			paramTypeString = "numeric";
		} else {
			System.err
					.println("DataServiceManager addSearchStringIfNeeded: Unknown ParameterValueType: "
							+ paramValueType.toString());
			return;
		}

		// section to deal with the multiple select boxes
		String listAsString = "";
		for (String selectedItem : selectedItems) {
			if (!listAsString.equals("")) {
				listAsString += ", ";
			}
			listAsString += "'" + selectedItem + "'";
		}
		if (!listAsString.equals("")) {
			// String searchString = "(datasetParameterPK.name = '" + paramName + "' AND " +
			// paramTypeString + "Value IN (";
			String searchString = "(type.name = '" + paramName + "' AND " + paramTypeString
					+ "Value IN (";
			if (paramValueType == ParameterValueType.NUMERIC) {
				// remove the apostrophes from the list string
				listAsString = listAsString.replaceAll("'", "");
			}
			searchString += listAsString;
			searchString += "))";
			searchStrings.add(searchString);
		}
	}

	public List<ProjectOverview> getProjectList(String sessionId, DatasetType datasetType,
			List<String> users, List<String> instruments, List<String> exptTypes,
			List<String> numChannels, GenericSearchSelections genSearchSelections)
			throws SessionException, ServerException {
		System.out.println("DataServiceManager.getProjectList(): " + "searchCriteria: " + users
				+ " " + instruments + " " + exptTypes + " " + numChannels + " "
				+ "genSearchSelections: " + genSearchSelections.toString());
		List<ProjectOverview> projects = new ArrayList<ProjectOverview>();
		try {
			// temporary section for testing exception handling
			// if ( true ) {
			// throw new SessionException_Exception("KP generated SessionException_Exception",
			// null);
			// throw new
			// IcatInternalException_Exception("KP generated IcatInternalException_Exception",
			// null);
			// }
			List<String> dsParamSearchStrings = new ArrayList<String>();
			List<String> datasetSearchStrings = new ArrayList<String>();

			// section to deal with the multiple select boxes
			addSearchStringIfNeeded(users, "users", ParameterValueType.STRING, dsParamSearchStrings);
			addSearchStringIfNeeded(instruments, "instrument", ParameterValueType.STRING,
					dsParamSearchStrings);
			addSearchStringIfNeeded(exptTypes, "experiment_type", ParameterValueType.STRING,
					dsParamSearchStrings);
			addSearchStringIfNeeded(numChannels, "nchannels", ParameterValueType.NUMERIC,
					dsParamSearchStrings);

			// section to deal with the generic search box
			ParameterDescriptor paramDescriptor = PortalUtils.PARAM_DESCRIPTOR_MAPPINGS
					.get(genSearchSelections.getSearchParamName());
			// if a string search parameter is selected
			if (paramDescriptor.getParameterValueType() == ParameterValueType.STRING) {
				// check there is a value to search for
				if (!genSearchSelections.getSearchValueString().equals("")) {
					String searchString = "";
					if (paramDescriptor.getParameterLevelType() == ParameterLevelType.DATASET) {
						searchString += "(" + paramDescriptor.getIcatParameterName() + " ";
					} else if (paramDescriptor.getParameterLevelType() == ParameterLevelType.DATASET_PARAMETER) {
						// searchString += "(datasetParameterPK.name = '" +
						// genSearchSelections.getSearchParamName() + "'";
						searchString += "(type.name = '" + genSearchSelections.getSearchParamName()
								+ "'";
						searchString += " AND stringValue ";
					} else {
						// TODO - this case cannot happen at the moment but put something in just in
						// case
					}
					searchString += genSearchSelections.getSearchOperator() + " ";
					if (genSearchSelections.getSearchOperator().equals("LIKE")) {
						searchString += "'%" + genSearchSelections.getSearchValueString() + "%'";
					} else {
						searchString += "'" + genSearchSelections.getSearchValueString() + "'";
					}
					searchString += ")";
					if (paramDescriptor.getParameterLevelType() == ParameterLevelType.DATASET) {
						datasetSearchStrings.add(searchString);
					} else if (paramDescriptor.getParameterLevelType() == ParameterLevelType.DATASET_PARAMETER) {
						dsParamSearchStrings.add(searchString);
					} else {
						// TODO - this case cannot happen at the moment but put something in just in
						// case
					}
				}
				// if a numeric search parameter is selected
			} else if (PortalUtils.PARAM_DESCRIPTOR_MAPPINGS.get(
					genSearchSelections.getSearchParamName()).getParameterValueType() == ParameterValueType.NUMERIC) {
				if (genSearchSelections.getSearchValueNumeric() != null
						|| genSearchSelections.getFromValueNumeric() != null
						|| genSearchSelections.getToValueNumeric() != null) {
					// String searchString = "(datasetParameterPK.name = '" +
					// genSearchSelections.getSearchParamName() + "'";
					String searchString = "(type.name = '"
							+ genSearchSelections.getSearchParamName() + "'";
					searchString += " AND numericValue " + genSearchSelections.getSearchOperator();
					if (genSearchSelections.getSearchOperator().equals("BETWEEN")) {
						searchString += " " + genSearchSelections.getFromValueNumeric() + " AND "
								+ genSearchSelections.getToValueNumeric();
					} else {
						searchString += " " + genSearchSelections.getSearchValueNumeric();
					}
					searchString += ")";
					dsParamSearchStrings.add(searchString);
				}
				// if a date/time search parameter is selected
			} else if (PortalUtils.PARAM_DESCRIPTOR_MAPPINGS.get(
					genSearchSelections.getSearchParamName()).getParameterValueType() == ParameterValueType.DATE_AND_TIME) {
				// TODO - get this working for DatasetParameters as well as Dataset fields once we
				// have some of type date/time
				if (genSearchSelections.getFromDate() != null
						&& genSearchSelections.getToDate() != null) {
					SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String searchString = "(";
					// startDate or endDate
					searchString += PortalUtils.PARAM_DESCRIPTOR_MAPPINGS.get(
							genSearchSelections.getSearchParamName()).getIcatParameterName();
					searchString += " BETWEEN {ts ";
					searchString += dateTimeFormat.format(genSearchSelections.getFromDate());
					searchString += "} AND {ts ";
					searchString += dateTimeFormat.format(genSearchSelections.getToDate());
					searchString += "})";
					datasetSearchStrings.add(searchString);
				}
			}

			String finalQuery = null;
			Set<Object> cumulativeDatasetIdObjects = new HashSet<Object>();
			if (dsParamSearchStrings.size() > 0 || datasetSearchStrings.size() > 0) {
				// compile search query containing a list of all dataset ids
				// that match the search parameters
				System.out.println("Doing dataset parameter level searches");
				cumulativeDatasetIdObjects = addToCumulativeDatasetIdObjectsList(sessionId,
						dsParamSearchStrings, cumulativeDatasetIdObjects,
						ParameterLevelType.DATASET_PARAMETER, datasetType);
				// only do the dataset level search if cumulativeDatasetIdObjects is not null
				// if it is null then one of the dsParam level searches returned zero results
				// so we do not need to bother
				if (cumulativeDatasetIdObjects != null) {
					System.out.println("Doing dataset level searches");
					cumulativeDatasetIdObjects = addToCumulativeDatasetIdObjectsList(sessionId,
							datasetSearchStrings, cumulativeDatasetIdObjects,
							ParameterLevelType.DATASET, datasetType);
				}
				// if cumulativeDatasetIdObjects is null at this point then there are no
				// results to return so we can skip creating a final query of dataset IDs
				if (cumulativeDatasetIdObjects != null) {
					System.out
							.println("Compiling datasetIdListString from cumulativeDatasetIdObjects "
									+ "containing " + cumulativeDatasetIdObjects.size() + " IDs");
					String datasetIdListString = "";
					for (Object datasetIdObject : cumulativeDatasetIdObjects) {
						datasetIdListString += datasetIdObject + ",";
					}
					if (datasetIdListString.length() > 0) {
						// remove the last comma
						datasetIdListString = datasetIdListString.substring(0,
								datasetIdListString.length() - 1);
						// System.out.println(datasetIdListString);
						finalQuery = "0," + PortalUtils.MAX_RESULTS
								+ " Dataset include DatasetParameter, ParameterType [id IN ("
								+ datasetIdListString + ")" + " AND complete = true" + "]";
					} else {
						// there are no datasets matching the overall search
						// leave finalQuery set to null so we can check for this later
					}
				}
			} else {
				// search for all datasets
				// finalQuery = "0," + PortalUtils.MAX_RESULTS +
				// " Dataset include DatasetParameter [datasetType = '" + datasetType.toString() +
				// "']";
				finalQuery = "0," + PortalUtils.MAX_RESULTS
						+ " Dataset include DatasetParameter, ParameterType [type.name = '"
						+ datasetType.toString() + "'" + " AND complete = true" + "]";
			}

			// only do the final query if there are a list of objects to fetch
			// otherwise leave the list of ProjectOverview objects empty
			if (finalQuery != null) {
				// execute the query
				System.out.println("finalQuery=[" + finalQuery + "]");
				List<Object> datasetsFromIcat;
				datasetsFromIcat = icat.search(sessionId, finalQuery);
				for (Object o : datasetsFromIcat) {
					Dataset datasetFromIcat = (Dataset) o;
					ProjectOverview project = new ProjectOverview();
					// List<DatasetParameter> dsParams =
					// datasetFromIcat.getDatasetParameterCollection();
					List<DatasetParameter> dsParams = datasetFromIcat.getParameters();
					HashMap<String, String> dsParamsMap = new HashMap<String, String>();
					for (DatasetParameter dsParam : dsParams) {
						// String paramName = dsParam.getDatasetParameterPK().getName();
						String paramName = dsParam.getType().getName();
						// the number of channels needs putting into the ProjectOverview
						// object because it is needed when creating the Project actions menu
						if (paramName.equals("nchannels")) {
							Double paramValueDouble = dsParam.getNumericValue();
							if (paramValueDouble != null) {
								// nframes can safely be converted to an integer
								project.setNumChannels(paramValueDouble.intValue());
							}
						}
						String paramValueAsString = getParameterValueAsString(dsParam);
						if (paramValueAsString != null) {
							dsParamsMap.put(paramName, paramValueAsString);
						}
					}
					project.setDatasetId(datasetFromIcat.getId());
					project.setName(datasetFromIcat.getName());
					project.setSampleDescription(dsParamsMap.get("sampledescription"));
					project.setUsers(dsParamsMap.get("users"));

					if (datasetType == DatasetType.LSF_DATASET) {
						// whether datasets have beads, bias etc is determined
						// by the presence of a parameter containing the ID
						// of a dependency dataset
						if (dsParamsMap.containsKey("bead_dataset")) {
							project.setHasBeads(true);
						}
						if (dsParamsMap.containsKey("bias_dataset")) {
							project.setHasBias(true);
						}
						if (dsParamsMap.containsKey("dark_dataset")) {
							project.setHasDark(true);
						}
						if (dsParamsMap.containsKey("flatfield_dataset")) {
							project.setHasFlatfield(true);
						}
						if (dsParamsMap.containsKey("check_dataset")) {
							project.setHasCheck(true);
						}
						// new method of setting the hasWhitelight flag
						// do a search for whitelight datafiles in the dataset
						// this is effectively an OR search returning one or more instances of the
						// dataset id being searched depending on how many of the files are found
						// TODO - I think that the whitelight/raw_image has been replaced by
						// whitelight_stack/raw_image
						// so this can probably be removed from the search string
						String whitelightQuery = "Dataset.id [id="
								+ datasetFromIcat.getId()
								+ "] <-> "
								+ "Datafile [datafileFormat.name IN ('whitelight/raw_image', 'whitelight_stack/raw_image')]";
						List<Object> whitelightDatasetsIds = icat
								.search(sessionId, whitelightQuery);
						if (whitelightDatasetsIds.size() > 0) {
							project.setHasWhitelight(true);
						}

					} else if (datasetType == DatasetType.LSF_PROJECT) {
						// set the hasWhitelight flag if needed
						// note that whitelight file types are different to those for datasets
						String whitelightQuery = "Dataset.id [id="
								+ datasetFromIcat.getId()
								+ "] <-> "
								+ "Datafile [datafileFormat.name IN ('whitelight_stack/proc_frame', 'whitelight_stack/reg_frame')]";
						List<Object> whitelightDatasetsIds = icat
								.search(sessionId, whitelightQuery);
						if (whitelightDatasetsIds.size() > 0) {
							project.setHasWhitelight(true);
						}

						// setting of hasBead/Bias/Flatfield for projects is determined by the
						// presence of certain file types in the datafiles

						// set the hasBeads flag if needed
						String beadsQuery = "Dataset.id [id="
								+ datasetFromIcat.getId()
								+ "] <-> "
								+ "Datafile [datafileFormat.name IN ('bead_stack/proc_frame', 'bead_stack/reg_frame')]";
						List<Object> beadsDatasetsIds = icat.search(sessionId, beadsQuery);
						if (beadsDatasetsIds.size() > 0) {
							project.setHasBeads(true);
						}

						// set the hasFlatfield flag if needed
						String flatfieldQuery = "Dataset.id [id="
								+ datasetFromIcat.getId()
								+ "] <-> "
								+ "Datafile [datafileFormat.name IN ('flatfield_stack/proc_frame', 'flatfield_stack/reg_frame')]";
						List<Object> flatfieldDatasetsIds = icat.search(sessionId, flatfieldQuery);
						if (flatfieldDatasetsIds.size() > 0) {
							project.setHasFlatfield(true);
						}

						// set the hasBias flag if needed
						String biasQuery = "Dataset.id [id="
								+ datasetFromIcat.getId()
								+ "] <-> "
								+ "Datafile [datafileFormat.name = 'bias_stack/average/proc_frame']";
						List<Object> biasDatasetsIds = icat.search(sessionId, biasQuery);
						if (biasDatasetsIds.size() > 0) {
							project.setHasBias(true);
						}

						// set the hasEvidenceMaps flag if needed
						String evMapsQuery = "Dataset.id [id=" + datasetFromIcat.getId() + "] <-> "
								+ "Datafile [name = 'evidencemapsframe_list']";
						List<Object> evMapsDatasetsIds = icat.search(sessionId, evMapsQuery);
						if (evMapsDatasetsIds.size() > 0) {
							project.setHasEvidenceMaps(true);
						}

						// set the hasRegErrorMaps flag if needed
						String nonRefRegQuery = "Dataset.id [id=" + datasetFromIcat.getId()
								+ "] <-> "
								+ "Datafile [name = 'non_ref_bootstrap_registration_error.h5']";
						List<Object> nonRefRegDatasetsIds = icat.search(sessionId, nonRefRegQuery);
						if (nonRefRegDatasetsIds.size() > 0) {
							// as well as this file we need to query for another file as well
							// 2 queries are needed because it is currently not possible in one
							// query
							String refRegQuery = "Dataset.id [id=" + datasetFromIcat.getId()
									+ "] <-> "
									+ "Datafile [name = 'ref_bootstrap_registration_error.h5']";
							List<Object> refRegDatasetsIds = icat.search(sessionId, refRegQuery);
							if (refRegDatasetsIds.size() > 0) {
								project.setHasRegErrorMaps(true);
							}
						}
					}
					projects.add(project);
				}
			}
			// } catch ( Exception e ) {
			// System.err.println( "DataServiceManager.getProjectList(): " + e.getClass().getName()
			// + " " + e.getMessage() );
			// e.printStackTrace();
			// }
		} catch (IcatException_Exception e) {
			IcatExceptionType type = e.getFaultInfo().getType();
			if (type == IcatExceptionType.SESSION) {
				throw new SessionException(e.getMessage());
			} else {
				throw new ServerException("IcatException " + type + " " + e.getMessage());
			}
		}
		return projects;
	}

	private Set<Object> addToCumulativeDatasetIdObjectsList(String sessionId,
			List<String> searchStrings, Set<Object> cumulativeDatasetIdObjects,
			ParameterLevelType paramLevelType, DatasetType datasetType)
			throws IcatException_Exception {
		for (int i = 0; i < searchStrings.size(); i++) {
			String query = "";
			if (paramLevelType == ParameterLevelType.DATASET) {
				// begin the dataset specific part of the query
				query += "Dataset.id [type.name = '" + datasetType.toString() + "'] <-> Dataset [";
			} else if (paramLevelType == ParameterLevelType.DATASET_PARAMETER) {
				// begin the dataset parameter specific part of the query
				query += "Dataset.id [type.name = '" + datasetType.toString()
						+ "'] <-> DatasetParameter [";
			} else {
				// TODO - make sure this never happens
			}
			query += searchStrings.get(i);
			// close the query string
			query += "]";
			System.out.println("query=[" + query + "]");
			List<Object> datasetIdsFromIcat = icat.search(sessionId, query);
			// if one of the subsearches returns zero matches then we need to return
			// zero results so just return null so that we can recognise this fact
			if (datasetIdsFromIcat.size() == 0) {
				System.out.println("Search returned 0 results: returning null");
				return null;
			} else {
				// otherwise merge the list of dataset IDs returned with the
				// cumulative list of IDs
				Set<Object> datasetIdObjects = new HashSet<Object>(datasetIdsFromIcat);
				System.out.println("Size of datasetIds: " + datasetIdObjects.size());
				if (cumulativeDatasetIdObjects.size() == 0) {
					// add this list of dataset Ids to the currently empty list
					cumulativeDatasetIdObjects.addAll(datasetIdObjects);
				} else {
					// AND this set of dataset Ids with those already in the list being built
					cumulativeDatasetIdObjects.retainAll(datasetIdObjects);
				}
				System.out.println("Size of cumulativeDatasetIdObjects: "
						+ cumulativeDatasetIdObjects.size());
			}
		}
		return cumulativeDatasetIdObjects;
	}

	public LinkedHashMap<String, String> getProjectParameters(String sessionId, Long datasetId)
			throws SessionException, ServerException {
		LinkedHashMap<String, String> projectParams = new LinkedHashMap<String, String>();
		try {
			Dataset datasetFromIcat = (Dataset) icat.get(sessionId,
					"Dataset include DatasetParameter, ParameterType", datasetId);
			projectParams.put("Dataset ID", datasetId.toString());
			projectParams.put("Name", datasetFromIcat.getName());
			List<DatasetParameter> dsParams = datasetFromIcat.getParameters();
			for (DatasetParameter dsParam : dsParams) {
				String paramName = dsParam.getType().getName();
				String paramValueAsString = getParameterValueAsString(dsParam);
				if (paramValueAsString != null) {
					projectParams.put(paramName, paramValueAsString);
				}
			}
			if (datasetFromIcat.getStartDate() != null) {
				projectParams.put("Created", datasetFromIcat.getStartDate().toString());
			}
			if (datasetFromIcat.getEndDate() != null) {
				projectParams.put("Done", datasetFromIcat.getEndDate().toString());
			}
		} catch (IcatException_Exception e) {
			IcatExceptionType type = e.getFaultInfo().getType();
			if (type == IcatExceptionType.SESSION) {
				throw new SessionException(e.getMessage());
			} else {
				throw new ServerException("IcatException " + type + " " + e.getMessage());
			}
		}
		return projectParams;
	}

	private String getParameterValueAsString(DatasetParameter datasetParameter) {
		// String paramName = datasetParameter.getDatasetParameterPK().getName();
		String paramName = datasetParameter.getType().getName();
		String paramValueAsString = "";
		if (PortalUtils.PARAM_DESCRIPTOR_MAPPINGS.get(paramName) == null) {
			// return null for parameters not in the mappings table so we can ignore them
			return null;
		} else if (PortalUtils.PARAM_DESCRIPTOR_MAPPINGS.get(paramName).getParameterValueType() == ParameterValueType.STRING) {
			String paramValueString = datasetParameter.getStringValue();
			if (paramValueString != null) {
				paramValueAsString = paramValueString;
			}
		} else if (PortalUtils.PARAM_DESCRIPTOR_MAPPINGS.get(paramName).getParameterValueType() == ParameterValueType.NUMERIC) {
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
		} else if (PortalUtils.PARAM_DESCRIPTOR_MAPPINGS.get(paramName).getParameterValueType() == ParameterValueType.DATE_AND_TIME) {
			// TODO - this needs testing when we have a date parameter
			XMLGregorianCalendar paramValueDate = datasetParameter.getDateTimeValue();
			if (paramValueDate != null) {
				paramValueAsString = paramValueDate.toString();
			}
		}
		return paramValueAsString;
	}

	public List<DatasetOverview> getDatasetList(String sessionId, String datasetType, Map<String, List<String>> selectedSearchParamsMap) throws SessionException, ServerException {
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
			for (String query : queriesToRun) {
				logger.debug("query=[" + query + "]");
			}
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
				System.out.println("Compiling datasetIdListString from cumulativeDatasetIdObjects "
						+ "containing " + matchingDatasetIds.size() + " IDs");
				String datasetIdListString = "";
				for (Object datasetIdObject : matchingDatasetIds) {
					datasetIdListString += datasetIdObject + ",";
				}
				if (datasetIdListString.length() > 0) {
					// remove the last comma
					datasetIdListString = datasetIdListString.substring(0, datasetIdListString.length()-1);
					// System.out.println(datasetIdListString);
					finalQuery = "0," + PortalUtils.MAX_RESULTS
							+ " Dataset include DatasetParameter, ParameterType [id IN ("
							+ datasetIdListString + ")"
							+ " AND complete = true"
							+ "]";
						
				}
			}
			// get the job dataset mappings queries to run on each returned dataset
//			List<JobDatasetParameter> jobDatasetParameterList = xmlFileManager.getJobDatasetMappings().toMap().get(datasetType);
			List<JobDatasetParameter> jobDatasetParameterList = xmlFileManager.getJobDatasetMappings().getJobDatasetParametersForType(datasetType);
			
			// execute the final query
			System.out.println("finalQuery=[" + finalQuery + "]");
			List<Object> datasetsFromIcat;
			datasetsFromIcat = icat.search(sessionId, finalQuery);
			for (Object o : datasetsFromIcat) {
				Dataset datasetFromIcat = (Dataset) o;
				DatasetOverview datasetOverview = new DatasetOverview();
				// List<DatasetParameter> dsParams =
				// datasetFromIcat.getDatasetParameterCollection();
				List<DatasetParameter> dsParams = datasetFromIcat.getParameters();
				HashMap<String, String> dsParamsMap = new HashMap<String, String>();
				for (DatasetParameter dsParam : dsParams) {
					// String paramName = dsParam.getDatasetParameterPK().getName();
					String paramName = dsParam.getType().getName();
					// the number of channels needs putting into the ProjectOverview
					// object because it is needed when creating the Project actions menu
					if (paramName.equals("nchannels")) {
						Double paramValueDouble = dsParam.getNumericValue();
						if (paramValueDouble != null) {
							// nframes can safely be converted to an integer
							datasetOverview.setNumChannels(paramValueDouble.intValue());
						}
					}
					String paramValueAsString = getParameterValueAsString(dsParam);
					if (paramValueAsString != null) {
						dsParamsMap.put(paramName, paramValueAsString);
					}
				}
				datasetOverview.setDatasetId(datasetFromIcat.getId());
				datasetOverview.setName(datasetFromIcat.getName());
				datasetOverview.setSampleDescription(dsParamsMap.get("sampledescription"));
				datasetOverview.setUsers(dsParamsMap.get("users"));
				
				// set up the job dataset parameter map
				if ( jobDatasetParameterList != null ) {
					for (JobDatasetParameter jobDatasetParameter : jobDatasetParameterList) {
						String query = jobDatasetParameter.getQuery();
						query = query.replace("${datasetId}", datasetFromIcat.getId().toString());
						List<Object> queryResult = icat.search(sessionId, query);
						if ( queryResult.size() < 1 ) {
							// this is acceptable and just means that, for example, this parameter is not set for this dataset 
							logger.debug("queryResult for '" + query + "' returned " + queryResult.size() + " results");
							// set this map parameter to null so that we can check for it in the client
							datasetOverview.getJobDatasetParameters().put(jobDatasetParameter.getName(), null);
						} else if ( queryResult.size() == 1 ) {
							datasetOverview.getJobDatasetParameters().put(jobDatasetParameter.getName(), queryResult.get(0));
						} else {
							// this should not happen because currently we can only insert a single item into the map
							// and the query has returned multiple results - the query needs changing
							logger.error("queryResult for '" + query + "' returned " + queryResult.size() + " results");
							// the best thing we can do is set this map parameter to null so that we can check for it in the client
							datasetOverview.getJobDatasetParameters().put(jobDatasetParameter.getName(), null);
						}
					}
				}
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
	
	private String replaceVariableInQuery(String query, String variableName, List<String> valuesToInsert, String enclosingQuote ) {
		if ( query.indexOf(variableName) == -1 ) {
			// the variable to replace is not in the query string
			logger.debug("Variable [" + variableName + "] not found in query [" + query + "]");
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
	 * @throws IcatException_Exception
	 */
	private Set<Object> getMatchingDatasetIdObjectsList(String sessionId, List<String> queries) throws IcatException_Exception {
		Set<Object> cumulativeDatasetIdObjects = new HashSet<Object>();
		for (String query : queries) {
			System.out.println("query=[" + query + "]");
			List<Object> datasetIdsFromIcat = icat.search(sessionId, query);
			// if one of the subsearches returns zero matches then we need to return
			// zero results so just return null so that we can recognise this fact
			if (datasetIdsFromIcat.size() == 0) {
				System.out.println("Search returned 0 results: returning null");
				return null;
			} else {
				// otherwise merge the list of dataset IDs returned with the
				// cumulative list of IDs
				Set<Object> datasetIdObjects = new HashSet<Object>(datasetIdsFromIcat);
				System.out.println("Size of datasetIds: " + datasetIdObjects.size());
				if (cumulativeDatasetIdObjects.size() == 0) {
					// add this list of dataset Ids to the currently empty list
					cumulativeDatasetIdObjects.addAll(datasetIdObjects);
				} else {
					// AND this set of dataset Ids with those already in the list being built
					cumulativeDatasetIdObjects.retainAll(datasetIdObjects);
				}
				System.out.println("Size of cumulativeDatasetIdObjects: "
						+ cumulativeDatasetIdObjects.size());
			}
		}
		return cumulativeDatasetIdObjects;
	}

	public LinkedHashMap<String, String> getDatasetParameters(String sessionId, Long datasetId) throws SessionException, ServerException {
		// TODO - make this method return more generic dataset parameters
		return getProjectParameters(sessionId, datasetId);
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

}
