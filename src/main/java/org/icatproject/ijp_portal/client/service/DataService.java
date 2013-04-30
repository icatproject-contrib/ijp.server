package org.icatproject.ijp_portal.client.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.icatproject.ijp_portal.shared.AccountDTO;
import org.icatproject.ijp_portal.shared.DatasetOverview;
import org.icatproject.ijp_portal.shared.ForbiddenException;
import org.icatproject.ijp_portal.shared.GenericSearchSelections;
import org.icatproject.ijp_portal.shared.InternalException;
import org.icatproject.ijp_portal.shared.JobDTO;
import org.icatproject.ijp_portal.shared.ParameterException;
import org.icatproject.ijp_portal.shared.PortalUtils.OutputType;
import org.icatproject.ijp_portal.shared.PortalUtils.ParameterValueType;
import org.icatproject.ijp_portal.shared.ServerException;
import org.icatproject.ijp_portal.shared.SessionException;
import org.icatproject.ijp_portal.shared.xmlmodel.JobType;
import org.icatproject.ijp_portal.shared.xmlmodel.JobTypeMappings;
import org.icatproject.ijp_portal.shared.xmlmodel.SearchItems;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("DataService")
public interface DataService extends RemoteService {
	List<DatasetOverview> getDatasetList(String sessionId, String datasetType,
			Map<String, List<String>> selectedSearchParamsMap,
			List<GenericSearchSelections> genericSearchSelectionsList)
			throws SessionException, ServerException;

	LinkedHashMap<String, String> getDatasetParameters(String sessionId, Long datasetId)
			throws SessionException, ServerException;

	String login(String plugin, Map<String, String> credentials) throws SessionException;

	List<JobDTO> getJobsForUser(String sessionId) throws SessionException;

	String getJobOutput(String sessionId, String jobId, OutputType outputType)
			throws SessionException, ForbiddenException, InternalException;

	SearchItems getSearchItems() throws ServerException;

	List<String> getDatasetTypesList(String sessionId) throws SessionException, ServerException;

	JobTypeMappings getJobTypeMappings() throws ServerException;

	LinkedHashMap<String, ParameterValueType> getDatasetParameterTypesMap(String sessionId)
			throws SessionException, ServerException;

	Map<Long, Map<String, Object>> getJobDatasetParametersForDatasets(
			String sessionId, String datasetType, List<Long> datasetIds)
			throws ServerException, SessionException;

	String submitBatch(String sessionId, JobType jobType, List<String> parameters)
			throws ParameterException, SessionException, InternalException;
	
	AccountDTO submitInteractive(String sessionId, JobType jobType, List<String> parameters)
			throws ServerException, InternalException;
	
	// dummy methods to get other objects we want to use added to the GWT SerializationPolicy
	Double addDoubleToSerializationPolicy(Double aDouble);

}