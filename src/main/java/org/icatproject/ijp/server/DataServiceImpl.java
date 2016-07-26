package org.icatproject.ijp.server;

import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.servlet.UnavailableException;

import org.icatproject.ijp.client.service.DataService;
import org.icatproject.ijp.server.ejb.session.JobManagementBean;
import org.icatproject.ijp.server.manager.DataServiceManager;
import org.icatproject.ijp.server.manager.XmlFileManager;
import org.icatproject.ijp.shared.Authenticator;
import org.icatproject.ijp.shared.Constants;
import org.icatproject.ijp.shared.DatafileOverview;
import org.icatproject.ijp.shared.DatasetOverview;
import org.icatproject.ijp.shared.ForbiddenException;
import org.icatproject.ijp.shared.GenericSearchSelections;
import org.icatproject.ijp.shared.InternalException;
import org.icatproject.ijp.shared.JobDTO;
import org.icatproject.ijp.shared.LoginResult;
import org.icatproject.ijp.shared.ParameterException;
import org.icatproject.ijp.shared.PortalUtils.OutputType;
import org.icatproject.ijp.shared.PortalUtils.ParameterValueType;
import org.icatproject.ijp.shared.SessionException;
import org.icatproject.ijp.shared.xmlmodel.JobType;
import org.icatproject.ijp.shared.xmlmodel.JobTypeMappings;
import org.icatproject.ijp.shared.xmlmodel.SearchItems;
import org.icatproject.utils.CheckedProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class DataServiceImpl extends RemoteServiceServlet implements DataService {

	private final static Logger logger = LoggerFactory.getLogger(DataServiceImpl.class);

	private DataServiceManager dataServiceManager;
	private XmlFileManager xmlFileManager;

	@EJB
	private JobManagementBean jobManagementBean;

	@Override
	public void init() throws UnavailableException {
		try {
			dataServiceManager = new DataServiceManager();
			xmlFileManager = new XmlFileManager();
			CheckedProperties props = new CheckedProperties();
			props.loadFromFile(Constants.PROPERTIES_FILEPATH);
			if (props.has("javax.net.ssl.trustStore")) {
				System.setProperty("javax.net.ssl.trustStore",
						props.getString("javax.net.ssl.trustStore"));
			}
		} catch (Exception e) {
			logger.error("Fatal error " + e.getClass() + " reports " + e.getMessage());
			throw new UnavailableException(e.getMessage());
		}
	}

	@Override
	public List<DatasetOverview> getDatasetList(String sessionId, String datasetType,
			Map<String, List<String>> selectedSearchParamsMap,
			List<GenericSearchSelections> genericSearchSelectionsList) throws SessionException,
			InternalException {
		return dataServiceManager.getDatasetList(sessionId, datasetType, selectedSearchParamsMap,
				genericSearchSelectionsList);
	}
	
	@Override
	public List<DatafileOverview> getDatafileList(String sessionId, String datasetType, Long datasetId,
			Map<String, List<String>> selectedSearchParamsMap,
			List<GenericSearchSelections> genericSearchSelectionsList) throws SessionException,
			InternalException {
		return dataServiceManager.getDatafileList(sessionId, datasetType, datasetId, selectedSearchParamsMap, genericSearchSelectionsList);
	}

	@Override
	public LinkedHashMap<String, String> getDatasetParameters(String sessionId, Long datasetId)
			throws SessionException, InternalException {
		return dataServiceManager.getDatasetParameters(sessionId, datasetId);
	}

	@Override
	public LoginResult login(String plugin, Map<String, String> credentials)
			throws SessionException, InternalException {
		return dataServiceManager.login(plugin, credentials);
	}

	private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Override
	public List<JobDTO> getJobsForUser(String sessionId) throws SessionException,
			InternalException, ForbiddenException, ParameterException {
		List<JobDTO> result = new ArrayList<>();
		try (JsonReader jsonReader = Json.createReader(new StringReader(jobManagementBean
				.listStatus(sessionId)))) {
			JsonArray jobs = jsonReader.readArray();
			synchronized (dateTimeFormat) {
				for (JsonValue jobv : jobs) {
					JsonObject job = (JsonObject) jobv;
					JobDTO jobDTO = new JobDTO();
					jobDTO.setId(job.getInt("jobId"));
					jobDTO.setJobType(job.getString("name"));
					try {
						jobDTO.setStatus(JobDTO.Status.valueOf(job.getString("status")));
					} catch (IllegalArgumentException e) {
						throw new InternalException("Batch system returned bad status "
								+ job.getString("status"));
					}
					jobDTO.setSubmitDate(dateTimeFormat.parse(job.getString("date")));
					result.add(jobDTO);
				}
			}
		} catch (ParseException e) {
			throw new InternalException(e.getClass() + " " + e.getMessage());
		}
		return result;
	}

	@Override
	public String getJobOutput(String sessionId, long id, OutputType outputType)
			throws SessionException, ForbiddenException, InternalException, ParameterException {
		return jobManagementBean.getJobOutput(sessionId, id, outputType);
	}

	@Override
	public SearchItems getDatasetSearchItems() throws InternalException {
		return xmlFileManager.getDatasetSearchItems();
	}

	@Override
	public SearchItems getDatafileSearchItems() throws InternalException {
		return xmlFileManager.getDatafileSearchItems();
	}

	@Override
	public List<String> getDatasetTypesList(String sessionId) throws SessionException,
			InternalException {
		return dataServiceManager.getDatasetTypesList(sessionId);
	}

	@Override
	public LinkedHashMap<String, ParameterValueType> getDatasetParameterTypesMap(String sessionId)
			throws SessionException {
		return dataServiceManager.getDatasetParameterTypesMap(sessionId);
	}

	@Override
	public LinkedHashMap<String, ParameterValueType> getDatafileParameterTypesMap(String sessionId)
			throws SessionException {
		return dataServiceManager.getDatafileParameterTypesMap(sessionId);
	}

	@Override
	public JobTypeMappings getJobTypeMappings() throws InternalException {
		return xmlFileManager.getJobTypeMappings();
	}

	@Override
	public Map<Long, Map<String, Object>> getJobDatasetParametersForDatasets(String sessionId,
			String datasetType, List<Long> datasetIds) throws SessionException, InternalException {
		return dataServiceManager.getJobDatasetParametersForDatasets(sessionId, datasetType,
				datasetIds);
	}

	@Override
	public String submitBatch(String sessionId, JobType jobType, List<String> parameters)
			throws ParameterException, SessionException, InternalException, ForbiddenException {
		return jobManagementBean.submitBatch(sessionId, jobType, parameters);
	}

	@Override
	public String submitInteractive(String sessionId, JobType jobType, List<String> parameters)
			throws InternalException, ForbiddenException, ParameterException, SessionException {
		return jobManagementBean.submitInteractive(sessionId, jobType, parameters);
	}

	@Override
	public Double addDoubleToSerializationPolicy(Double aDouble) {
		return null;
	}

	@Override
	public String getDataUrl(String sessionId, List<Long> investigationIds, List<Long> datasetIds,
			List<Long> datafileIds, String outname) {
		return dataServiceManager.getDataUrl(sessionId, investigationIds, datasetIds, datafileIds,
				outname);
	}

	@Override
	public String getIdsUrlString() {
		return dataServiceManager.getIdsUrlString();
	}

	@Override
	public List<Authenticator> getAuthenticators() {
		return dataServiceManager.getAuthenticators();
	}

	@Override
	public void cancelJob(String sessionId, long id) throws SessionException, ForbiddenException,
			InternalException, ParameterException {
		jobManagementBean.cancel(sessionId, id);
	}

	@Override
	public void deleteJob(String sessionId, long id) throws SessionException, ForbiddenException,
			InternalException, ParameterException {
		jobManagementBean.delete(sessionId, id);
	}
}
