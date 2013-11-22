package org.icatproject.ijp.server;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.servlet.UnavailableException;

import org.icatproject.ijp.client.service.DataService;
import org.icatproject.ijp.server.ejb.entity.Job;
import org.icatproject.ijp.server.ejb.session.JobManagementBean;
import org.icatproject.ijp.server.manager.DataServiceManager;
import org.icatproject.ijp.server.manager.XmlFileManager;
import org.icatproject.ijp.shared.AccountDTO;
import org.icatproject.ijp.shared.Constants;
import org.icatproject.ijp.shared.DatasetOverview;
import org.icatproject.ijp.shared.ForbiddenException;
import org.icatproject.ijp.shared.GenericSearchSelections;
import org.icatproject.ijp.shared.InternalException;
import org.icatproject.ijp.shared.JobDTO;
import org.icatproject.ijp.shared.ParameterException;
import org.icatproject.ijp.shared.ServerException;
import org.icatproject.ijp.shared.SessionException;
import org.icatproject.ijp.shared.PortalUtils.OutputType;
import org.icatproject.ijp.shared.PortalUtils.ParameterValueType;
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

	private String idsUrlString;

	@Override
	public void init() throws UnavailableException {
		try {
			dataServiceManager = new DataServiceManager();
			xmlFileManager = new XmlFileManager();
			CheckedProperties props = new CheckedProperties();
			props.loadFromFile(Constants.PROPERTIES_FILEPATH);
			if (props.has("javax.net.ssl.trustStore")) {
				System.setProperty("javax.net.ssl.trustStore",
						props.getProperty("javax.net.ssl.trustStore"));
			}
			idsUrlString = props.getURL("ids.url").toExternalForm() + "/ids/";
		} catch (Exception e) {
			logger.error("Fatal error " + e.getClass() + " reports " + e.getMessage());
			throw new UnavailableException(e.getMessage());
		}
	}

	@Override
	public List<DatasetOverview> getDatasetList(String sessionId, String datasetType,
			Map<String, List<String>> selectedSearchParamsMap,
			List<GenericSearchSelections> genericSearchSelectionsList) throws SessionException,
			ServerException {
		return dataServiceManager.getDatasetList(sessionId, datasetType, selectedSearchParamsMap,
				genericSearchSelectionsList);
	}

	@Override
	public LinkedHashMap<String, String> getDatasetParameters(String sessionId, Long datasetId)
			throws SessionException, ServerException {
		return dataServiceManager.getDatasetParameters(sessionId, datasetId);
	}

	@Override
	public String login(String plugin, Map<String, String> credentials) throws SessionException {
		return dataServiceManager.login(plugin, credentials);
	}

	@Override
	public List<JobDTO> getJobsForUser(String sessionId) throws SessionException {
		List<Job> jobList = jobManagementBean.getJobsForUser(sessionId);
		return convertJobListToJobDTOList(jobList);
	}

	private List<JobDTO> convertJobListToJobDTOList(List<Job> jobList) {
		List<JobDTO> jobDTOList = new ArrayList<JobDTO>();
		for (Job job : jobList) {
			jobDTOList.add(job.getJobDTO());
		}
		return jobDTOList;
	}

	@Override
	public String getJobOutput(String sessionId, String jobId, OutputType outputType)
			throws SessionException, ForbiddenException, InternalException {
		return jobManagementBean.getJobOutput(sessionId, jobId, outputType);
	}

	@Override
	public SearchItems getSearchItems() throws ServerException {
		return xmlFileManager.getSearchItems();
	}

	@Override
	public List<String> getDatasetTypesList(String sessionId) throws SessionException,
			ServerException {
		return dataServiceManager.getDatasetTypesList(sessionId);
	}

	@Override
	public LinkedHashMap<String, ParameterValueType> getDatasetParameterTypesMap(String sessionId)
			throws SessionException, ServerException {
		return dataServiceManager.getDatasetParameterTypesMap(sessionId);
	}

	@Override
	public JobTypeMappings getJobTypeMappings() throws ServerException {
		return xmlFileManager.getJobTypeMappings();
	}

	@Override
	public Map<Long, Map<String, Object>> getJobDatasetParametersForDatasets(String sessionId,
			String datasetType, List<Long> datasetIds) throws ServerException, SessionException {
		return dataServiceManager.getJobDatasetParametersForDatasets(sessionId, datasetType,
				datasetIds);
	}

	@Override
	public String submitBatch(String sessionId, JobType jobType, List<String> parameters)
			throws ParameterException, SessionException, InternalException {
		return jobManagementBean.submitBatch(sessionId, jobType, parameters);
	}

	@Override
	public AccountDTO submitInteractive(String sessionId, JobType jobType, List<String> parameters)
			throws ServerException, InternalException {
		return jobManagementBean.submitInteractive(sessionId, jobType, parameters);
	}

	@Override
	public Double addDoubleToSerializationPolicy(Double aDouble) {
		return null;
	}

	@Override
	public String getIdsUrlString() {
		return idsUrlString;
	}

}
