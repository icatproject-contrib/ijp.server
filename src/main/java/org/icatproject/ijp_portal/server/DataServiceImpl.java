package org.icatproject.ijp_portal.server;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.servlet.UnavailableException;

import org.icatproject.ijp_portal.client.service.DataService;
import org.icatproject.ijp_portal.server.ejb.entity.Job;
import org.icatproject.ijp_portal.server.ejb.session.JobManagementBean;
import org.icatproject.ijp_portal.server.ejb.session.MachineEJB;
import org.icatproject.ijp_portal.server.manager.DataServiceManager;
import org.icatproject.ijp_portal.server.manager.XmlFileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.rl.esc.catutils.CheckedProperties;
import org.icatproject.ijp_portal.shared.AccountDTO;
import org.icatproject.ijp_portal.shared.Constants;
import org.icatproject.ijp_portal.shared.DatasetOverview;
import org.icatproject.ijp_portal.shared.ForbiddenException;
import org.icatproject.ijp_portal.shared.GenericSearchSelections;
import org.icatproject.ijp_portal.shared.InternalException;
import org.icatproject.ijp_portal.shared.JobDTO;
import org.icatproject.ijp_portal.shared.PortalUtils.DatasetType;
import org.icatproject.ijp_portal.shared.PortalUtils.OutputType;
import org.icatproject.ijp_portal.shared.PortalUtils.ParameterValueType;
import org.icatproject.ijp_portal.shared.ProjectOverview;
import org.icatproject.ijp_portal.shared.ServerException;
import org.icatproject.ijp_portal.shared.SessionException;
import org.icatproject.ijp_portal.shared.xmlmodel.JobTypeMappings;
import org.icatproject.ijp_portal.shared.xmlmodel.SearchItems;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class DataServiceImpl extends RemoteServiceServlet implements DataService {

	final static Logger logger = LoggerFactory.getLogger(DataServiceImpl.class);

	private DataServiceManager dataServiceManager;
	private XmlFileManager xmlFileManager;

	@EJB
	private JobManagementBean jobManagementBean;

	private MachineManager machineManager;

	@EJB
	private MachineEJB machineEJB;

	private String poolPrefix;

	@Override
	public void init() throws UnavailableException {
		CheckedProperties portalProps = new CheckedProperties();
		try {
			portalProps.loadFromFile(Constants.PROPERTIES_FILEPATH);
			poolPrefix = portalProps.getString("poolPrefix");
			machineManager = new MachineManager(machineEJB);
			dataServiceManager = new DataServiceManager();
			xmlFileManager = new XmlFileManager();
		} catch (Exception e) {
			logger.error("Fatal error " + e.getClass() + " reports " + e.getMessage());
			throw new UnavailableException(e.getMessage());
		}
	}

	@Override
	public List<ProjectOverview> getProjectList(String sessionId, DatasetType datasetType,
			List<String> users, List<String> instruments, List<String> exptTypes,
			List<String> numChannels, GenericSearchSelections genSearchSelections)
			throws SessionException, ServerException {
		logger.debug("In DataServiceImpl.getProjectList()");
		return dataServiceManager.getProjectList(sessionId, datasetType, users, instruments,
				exptTypes, numChannels, genSearchSelections);
	}

	@Override
	public List<DatasetOverview> getDatasetList(String sessionId,
			String datasetType,
			Map<String, List<String>> selectedSearchParamsMap,
			List<GenericSearchSelections> genericSearchSelectionsList)
			throws SessionException, ServerException {
		logger.debug("In DataServiceImpl.getDatasetList()");
		return dataServiceManager.getDatasetList(sessionId, datasetType,
				selectedSearchParamsMap, genericSearchSelectionsList);
	}

	@Override
	public LinkedHashMap<String, String> getProjectParameters(String sessionId, Long datasetId)
			throws SessionException, ServerException {
		logger.debug("In DataServiceImpl.getProjectParameters()");
		return dataServiceManager.getProjectParameters(sessionId, datasetId);
	}

	@Override
	public LinkedHashMap<String, String> getDatasetParameters(String sessionId, Long datasetId)
			throws SessionException, ServerException {
		logger.debug("In DataServiceImpl.getDatasetParameters()");
		return dataServiceManager.getDatasetParameters(sessionId, datasetId);
	}

	@Override
	public String login(String plugin, Map<String, String> credentials) throws SessionException {
		logger.debug("In DataServiceImpl.login()");
		return dataServiceManager.login(plugin, credentials);
	}

	@Override
	public String submitDataset(String sessionId, String username, Long datasetId)
			throws SessionException {
		logger.debug("In DataServiceImpl.submitDataset()");
		Job job = jobManagementBean.submitDataset(sessionId, username, datasetId);
		// create a thread to continuously check qstat until the job is reported as completed
		// or a particular timeout is reached (TBD)
		// new Thread(new JobMonitor(job,jobManagementBean)).start();
		// return the jobId to the client
		return job.getId();
	}

	@Override
	public List<JobDTO> getJobsForUser(String sessionId) throws SessionException {
		logger.debug("In DataServiceImpl.getJobsForUser()");
		List<Job> jobList = jobManagementBean.getJobsForUser(sessionId);
		return convertJobListToJobDTOList(jobList);
	}

	private List<JobDTO> convertJobListToJobDTOList(List<Job> jobList) {
		List<JobDTO> jobDTOList = new ArrayList<JobDTO>();
		for (Job job : jobList) {
			// add a JobDTO to the list, converting the status flag
			// to a proper string in the process
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
	public AccountDTO getAccountFor(String username, String sessionId, Long dsid, String command)
			throws ServerException {
		logger.debug("In DataServiceImpl.getAccountFor()");
		return machineManager.prepareMachine(username, sessionId, dsid, command).getDTO(poolPrefix);
	}

	@Override
	public List<String> getDatasetTypesList(String sessionId) throws SessionException,
			ServerException {
		logger.debug("In DataServiceImpl.getDatasetTypesList()");
		return dataServiceManager.getDatasetTypesList(sessionId);
	}

	@Override
	public LinkedHashMap<String, ParameterValueType> getDatasetParameterTypesMap(
			String sessionId) throws SessionException, ServerException {
		logger.debug("In DataServiceImpl.getDatasetParameterTypesMap()");
		return dataServiceManager.getDatasetParameterTypesMap(sessionId);
	}

	@Override
	public JobTypeMappings getJobTypeMappings() throws ServerException {
		logger.debug("In DataServiceImpl.getJobTypeMappings()");
		return xmlFileManager.getJobTypeMappings();
	}

	@Override
	public Map<Long, Map<String, Object>> getJobDatasetParametersForDatasets(
			String sessionId, String datasetType, List<Long> datasetIds)
			throws ServerException, SessionException {
		logger.debug("In DataServiceImpl.getJobDatasetParametersForDatasets()");
		return dataServiceManager.getJobDatasetParametersForDatasets(sessionId, datasetType, datasetIds);
	}
			
	@Override
	public Double addDoubleToSerializationPolicy(Double aDouble) {
		return null;
	}

}
