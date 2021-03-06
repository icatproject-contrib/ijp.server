package org.icatproject.ijp.client.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.icatproject.ijp.shared.Authenticator;
import org.icatproject.ijp.shared.DatafileOverview;
import org.icatproject.ijp.shared.DatasetOverview;
import org.icatproject.ijp.shared.GenericSearchSelections;
import org.icatproject.ijp.shared.JobDTO;
import org.icatproject.ijp.shared.LoginResult;
import org.icatproject.ijp.shared.PortalUtils.OutputType;
import org.icatproject.ijp.shared.PortalUtils.ParameterValueType;
import org.icatproject.ijp.shared.xmlmodel.JobType;
import org.icatproject.ijp.shared.xmlmodel.JobTypeMappings;
import org.icatproject.ijp.shared.xmlmodel.SearchItems;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface DataServiceAsync {

	public static final class Util {
		private static DataServiceAsync instance;

		public static final DataServiceAsync getInstance() {
			if (instance == null) {
				instance = (DataServiceAsync) GWT.create(DataService.class);
			}
			return instance;
		}

		private Util() {
		}
	}

	void getDatasetList(String sessionId, String datasetType, Map<String, List<String>> selectedSearchParamsMap,
			List<GenericSearchSelections> genericSearchSelectionsList, AsyncCallback<List<DatasetOverview>> callback);

	void getDatafileList(String sessionId, String datasetType, Long datasetId,
			Map<String, List<String>> selectedSearchParamsMap,
			List<GenericSearchSelections> genericSearchSelectionsList, AsyncCallback<List<DatafileOverview>> callback);

	void getDatasetParameters(String sessionId, Long datasetId, AsyncCallback<LinkedHashMap<String, String>> callback);

	void login(String plugin, Map<String, String> credentials, AsyncCallback<LoginResult> asyncCallback);

	void getJobsForUser(String sessionId, AsyncCallback<List<JobDTO>> callback);

	void getJobOutput(String sessionId, long jobId, OutputType outputType, AsyncCallback<String> callback);

	void cancelJob(String sessionId, long jobId, AsyncCallback<Void> callback);

	void getDatasetSearchItems(AsyncCallback<SearchItems> callback);

	void getDatafileSearchItems(AsyncCallback<SearchItems> callback);

	void getJobTypeMappings(AsyncCallback<JobTypeMappings> callback);

	void getDatasetTypesList(String sessionId, AsyncCallback<List<String>> callback);

	void getDatasetParameterTypesMap(String sessionId, AsyncCallback<LinkedHashMap<String, ParameterValueType>> callback);

	void getDatafileParameterTypesMap(String sessionId,
			AsyncCallback<LinkedHashMap<String, ParameterValueType>> callback);

	void getJobDatasetParametersForDatasets(String sessionId, String datasetType, List<Long> datasetIds,
			AsyncCallback<Map<Long, Map<String, Object>>> callback);

	void submitBatch(String sessionId, JobType jobType, List<String> parameters, AsyncCallback<String> callback);

	void submitInteractive(String sessionId, JobType jobType, List<String> parameters, AsyncCallback<String> callback);

	void addDoubleToSerializationPolicy(Double aDouble, AsyncCallback<Double> callback);

	void getDataUrl(String sessionId, List<Long> investigationIds, List<Long> datasetIds, List<Long> datafileIds,
			String outname, AsyncCallback<String> callback);

	void getIdsUrlString(AsyncCallback<String> asyncCallback);

	void getAuthenticators(AsyncCallback<List<Authenticator>> asyncCallback);

	void deleteJob(String sessionId, long jobId, AsyncCallback<Void> callback);

}
