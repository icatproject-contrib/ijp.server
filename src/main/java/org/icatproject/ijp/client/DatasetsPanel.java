package org.icatproject.ijp.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.icatproject.ijp.client.service.DataServiceAsync;
import org.icatproject.ijp.shared.DatasetOverview;
import org.icatproject.ijp.shared.GenericSearchSelections;
import org.icatproject.ijp.shared.PortalUtils;
import org.icatproject.ijp.shared.PortalUtils.ParameterValueType;
import org.icatproject.ijp.shared.SessionException;
import org.icatproject.ijp.shared.xmlmodel.JobType;
import org.icatproject.ijp.shared.xmlmodel.JobTypeMappings;
import org.icatproject.ijp.shared.xmlmodel.ListOption;
import org.icatproject.ijp.shared.xmlmodel.SearchItem;
import org.icatproject.ijp.shared.xmlmodel.SearchItems;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;

public class DatasetsPanel extends Composite implements RequiresResize {

	interface MyUiBinder extends UiBinder<Widget, DatasetsPanel> {
	}
	
	@UiField
	ListBox jobTypeListBox;

	@UiField
	ListBox datasetTypeListBox;

	@UiField
	Button jobStatusButton;

	@UiField
	HorizontalPanel searchListsPanel;

	@UiField
	Button searchButton;

	@UiField
	Button doStuffButton;

	@UiField
	TextArea debugTextArea;

	@UiField
	VerticalPanel genericSearchesVerticalPanel;

	@UiField
	Button addGenericSearchButton;

	@UiField
	Label messageLabel;

	@UiField
	Button datasetDownloadButton;

	@UiField
	Button datasetDownloadUrlButton;

	@UiField
	Button datasetInfoButton;

	CellTable<DatasetOverview> datasetsTable;

	CellTable<DatasetInfoItem> datasetInfoTable;

	@UiField
	VerticalPanel verticalSplitPanelHolder;
	
	SplitLayoutPanel datasetSplitPanel;

	@UiField
	Button submitJobButton;

	@UiField
	FormPanel rdpForm;
	@UiField
	Hidden hostNameField;
	@UiField
	Hidden accountNameField;

	@UiField
	FormPanel downloadForm;
	@UiField
	Hidden sessionIdField;
	@UiField
	Hidden datasetIdsField;
	@UiField
	Hidden outnameField;

	private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
	private DataServiceAsync dataService = DataServiceAsync.Util.getInstance();

	private List<DatasetOverview> datasetList = new ArrayList<DatasetOverview>();
	final MultiSelectionModel<DatasetOverview> selectionModel = new MultiSelectionModel<DatasetOverview>();

	private static final String JOB_TYPES_LIST_FIRST_OPTION = "Job types ...";
	private static final String DATASET_TYPES_LIST_FIRST_OPTION = "Dataset types ...";
	static final String DATASET_TYPES_LIST_JOB_ONLY_OPTION = "none (job only)";
	private static final String OPTIONS_LIST_FIRST_OPTION = "Options ...";
	private static final String OPTIONS_LIST_DOWNLOAD_OPTION = "Download";
	private static final String OPTIONS_LIST_DOWNLOAD_URL_OPTION = "Show Download URL";
	private static final String DEFAULT_MESSAGE = "Select a Dataset Type and do a Search";

	Portal portal;
	Map<String, ListBox> searchItemsListBoxMap = new HashMap<String, ListBox>();
	JobTypeMappings jobTypeMappings;

	public DatasetsPanel(final Portal portal) {
		initWidget(uiBinder.createAndBindUi(this));

		dataService.getIdsUrlString(new AsyncCallback<String>() {

			@Override
			public void onFailure(Throwable caught) {
				Window.alert("Server error: " + caught.getMessage());
			}

			@Override
			public void onSuccess(String idsUrlString) {
				// for the downloadForm to request a dataset zip file download
				downloadForm.setEncoding(FormPanel.ENCODING_URLENCODED);
				downloadForm.setMethod(FormPanel.METHOD_GET);
				downloadForm.setAction(idsUrlString + "getData");
			}
		});

		this.portal = portal;

		IjpResources.INSTANCE.css().ensureInjected();

		datasetsTable = new CellTable<DatasetOverview>();
		datasetInfoTable = new CellTable<DatasetInfoItem>();
		datasetsTable.setWidth("100%");
		datasetInfoTable.setWidth("100%");
		datasetSplitPanel = new SplitLayoutPanel();
		datasetSplitPanel.addStyleName(IjpResources.INSTANCE.css().scrollPanel());
		datasetSplitPanel.addNorth(datasetsTable, 384);
		datasetSplitPanel.add(datasetInfoTable);
		verticalSplitPanelHolder.add(datasetSplitPanel);
		
		jobTypeListBox.addItem(JOB_TYPES_LIST_FIRST_OPTION);

		datasetTypeListBox.addItem(DATASET_TYPES_LIST_FIRST_OPTION);

		jobStatusButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				portal.jobStatusPanel.onResize();
				portal.jobStatusDialog.show();
				// refresh the table of jobs in the job status panel
				portal.jobStatusPanel.refreshJobList();
				// set a repeating timer going with a period of 1 minute
				portal.jobStatusPanel.tableRefreshTimer.scheduleRepeating(60000);
			}
		});

		searchButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				refreshDatasetsList();
			}
		});

		addGenericSearchButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				genericSearchesVerticalPanel.add(new GenericSearchPanel(portal));
			}
		});

		doStuffButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// displayJobTypesInTextArea();
				// displayDatasetParameterTypesMapInTextArea();
				Window.alert("Window.Navigator.getPlatform() = '" + Window.Navigator.getPlatform()
						+ "'");
			}
		});

		datasetDownloadButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				doDownloadForm();
			}
		});
		
		datasetDownloadUrlButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				showDownloadUrl();
			}
		});
		
		datasetInfoButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Set<DatasetOverview> selectedDatasets = selectionModel.getSelectedSet();
				if (selectedDatasets.size() == 0) {
					Window.alert("No datasets selected");
				} else {
					// get a list of selected dataset ids
					List<Long> selectedDatasetIds = new ArrayList<Long>();
					for (DatasetOverview selectedDataset : selectedDatasets) {
						selectedDatasetIds.add(selectedDataset.getDatasetId());
					}
					String datasetType = datasetTypeListBox.getValue(datasetTypeListBox
							.getSelectedIndex());
					// make a call to the server to get the job dataset parameters for the selected
					// dataset(s)
					dataService.getJobDatasetParametersForDatasets(portal.getSessionId(),
							datasetType, selectedDatasetIds,
							new AsyncCallback<Map<Long, Map<String, Object>>>() {
								@Override
								public void onFailure(Throwable caught) {
									Window.alert("Server error: " + caught.getMessage());
								}

								@Override
								public void onSuccess(
										Map<Long, Map<String, Object>> jobDatasetParametersForDatasets) {
									String message = "";
									if (jobDatasetParametersForDatasets == null) {
										message += "jobDatasetParametersForDatasets is null";
									} else if (jobDatasetParametersForDatasets.isEmpty()) {
										message += "jobDatasetParametersForDatasets is empty";
									} else {
										message += "Contents of jobDatasetParametersForDatasets"
												+ "\n\n";
										Set<Long> datasetIds = jobDatasetParametersForDatasets
												.keySet();
										for (Long datasetId : datasetIds) {
											message += "datasetId: " + datasetId + "\n";
											Map<String, Object> jobDatasetParameters = jobDatasetParametersForDatasets
													.get(datasetId);
											if (jobDatasetParameters == null) {
												message += "jobDatasetParameters is null" + "\n";
											} else if (jobDatasetParameters.isEmpty()) {
												message += "jobDatasetParameters is empty" + "\n";
											} else {
												for (String key : jobDatasetParameters.keySet()) {
													Object dsParamObject = jobDatasetParameters
															.get(key);
													String dsParamAsString = "null";
													if (dsParamObject != null) {
														dsParamAsString = dsParamObject.toString();
													}
													message += "JobDsParam: " + key + ": "
															+ dsParamAsString + " ";
													if (dsParamObject != null) {
														// need to reduce the length of this string
														// to prevent line wrapping in the alert box
														// cannot use Class getSimpleName() - not
														// implemented in GWT I think
														String classFullName = jobDatasetParameters
																.get(key).getClass().getName();
														String classSimpleName = classFullName
																.substring(classFullName
																		.lastIndexOf(".") + 1);
														message += " (" + classSimpleName + ")";
													}
													message += "\n";
												}
											}
											message += "\n";
										}
									}
									Window.alert(message);
								}
							});
				}
			}
		});

		// Add a text column to show the name.
		TextColumn<DatasetOverview> nameColumn = new TextColumn<DatasetOverview>() {
			@Override
			public String getValue(DatasetOverview dataset) {
				return dataset.getName();
			}
		};
		datasetsTable.addColumn(nameColumn, "Name");

		// Add a text column to show the sample description.
		TextColumn<DatasetOverview> sampleDescriptionColumn = new TextColumn<DatasetOverview>() {
			@Override
			public String getValue(DatasetOverview dataset) {
				return dataset.getSampleDescription();
			}
		};
		datasetsTable.addColumn(sampleDescriptionColumn, "Sample Description");

		// Add a text column to show the directory.
		TextColumn<DatasetOverview> usersColumn = new TextColumn<DatasetOverview>() {
			@Override
			public String getValue(DatasetOverview dataset) {
				return dataset.getUsers();
			}
		};
		datasetsTable.addColumn(usersColumn, "Users");

		// configure the selection model to handle user selection of datasets
		datasetsTable.setSelectionModel(selectionModel);
		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			public void onSelectionChange(SelectionChangeEvent event) {
				Set<DatasetOverview> selectedDatasets = selectionModel.getSelectedSet();
				// It's OK to have no datasets selected if the job doesn't need them
				if( selectedDatasets.size() == 0 && ! jobOnlyJobSelected() ){
					submitJobButton.setEnabled(false);
				} else {
					submitJobButton.setEnabled(true);
				}
				if (selectedDatasets.size() == 1) {
					DatasetOverview selectedDataset = selectedDatasets.iterator().next();
					refreshDatasetInformation(selectedDataset.getDatasetId());
				} else {
					// empty the dataset info table - details cannot be displayed for more than one
					// dataset
					clearDatasetInfoTable();
				}
			}
		});

		// Add a text column to show the name.
		TextColumn<DatasetInfoItem> infoNameColumn = new TextColumn<DatasetInfoItem>() {
			@Override
			public String getValue(DatasetInfoItem datasetInfoItem) {
				return datasetInfoItem.getName();
			}
		};
		datasetInfoTable.addColumn(infoNameColumn);

		// Add a text column to show the value.
		TextColumn<DatasetInfoItem> infoValueColumn = new TextColumn<DatasetInfoItem>() {
			@Override
			public String getValue(DatasetInfoItem datasetInfoItem) {
				return datasetInfoItem.getValue();
			}
		};
		datasetInfoTable.addColumn(infoValueColumn);

		// 2 lines below needed to get rid of the 3 flashing "progress indicator" boxes
		datasetsTable.setRowCount(0);
		datasetInfoTable.setRowCount(0);

		messageLabel.setText(DEFAULT_MESSAGE);

		submitJobButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// check that the selected job type accepts multiple datasets
				String jobName = jobTypeListBox.getValue(jobTypeListBox.getSelectedIndex());
				JobType jobType = jobTypeMappings.getJobTypesMap().get(jobName);
				Set<DatasetOverview> selectedDatasets = selectionModel.getSelectedSet();
				if (selectedDatasets.size() > 1 && !jobType.getMultiple()
						&& jobType.getType().equalsIgnoreCase("INTERACTIVE")) {
					Window.alert("'" + jobName + "' does not allow multiple datasets to be selected");
				} else {
					// popup a form containing the options for this job
					// with options relevant to the selected dataset
					portal.jobOptionsPanel.populateAndShowForm();
				}
			}
		});
		
		// for the rdpForm to request an rdp file download
		rdpForm.setEncoding(FormPanel.ENCODING_URLENCODED);
		rdpForm.setMethod(FormPanel.METHOD_GET);
		rdpForm.setAction(GWT.getHostPageBaseURL() + "rdp");

		addSearchBoxesAndPopulateTextArea();
		
		// Hide search boxes until they are enabled
		setSearchesEnabled(false);

		// TODO - remove these later - just for development purposes
		doStuffButton.setVisible(false);
		debugTextArea.setVisible(false);
	}

	/**
	 * @return true if a job type is selected and specifies no dataset types.
	 */
	protected boolean jobOnlyJobSelected() {
		
		String selectedJobType = JOB_TYPES_LIST_FIRST_OPTION;
		int selectedIndex = jobTypeListBox.getSelectedIndex();
		if( selectedIndex > -1 ){
			selectedJobType = jobTypeListBox.getValue(jobTypeListBox.getSelectedIndex());
		}
		if( selectedJobType == null || selectedJobType.equals(JOB_TYPES_LIST_FIRST_OPTION) ){
			return false;
		} else {
			JobType jobType = jobTypeMappings.getJobTypesMap().get(selectedJobType);
			List<String> datasetTypesList = jobType.getDatasetTypes();
			if( datasetTypesList.isEmpty() || datasetTypesList.get(0).equals(DATASET_TYPES_LIST_JOB_ONLY_OPTION) ){
				return true;
			} else {
				return false;
			}
		}
	}

	@UiHandler("jobTypeListBox")
	void handleJobTypeListBoxChange(ChangeEvent event){
		// Re-populate the datasetTypeListBox, etc.
		String selectedJobType = JOB_TYPES_LIST_FIRST_OPTION;
		int selectedIndex = jobTypeListBox.getSelectedIndex();
		if( selectedIndex > -1 ){
			selectedJobType = jobTypeListBox.getValue(jobTypeListBox.getSelectedIndex());
		}
		if( selectedJobType == null || selectedJobType.equals(JOB_TYPES_LIST_FIRST_OPTION) ){
			// clear and disable the selectedDataTypesBoxList
			repopulateDatasetTypeListBox( new ArrayList<String>() );
			datasetTypeListBox.setEnabled(false);
			setSearchesEnabled(false);
			submitJobButton.setEnabled(false);
			messageLabel.setText("Select a Job Type");
		} else {
			JobType jobType = jobTypeMappings.getJobTypesMap().get(selectedJobType);
			List<String> datasetTypesList = jobType.getDatasetTypes();
			repopulateDatasetTypeListBox(datasetTypesList);
			// Handle job-only jobs: will either have no types or just "none (job only)"
			// dataset list will just have the "title" item, but disable it anyway
			// (Or should we hide it? If so, would have to absorb the "Dataset Types" label and hide that too.)
			if( datasetTypesList.isEmpty() || datasetTypesList.get(0).equals(DATASET_TYPES_LIST_JOB_ONLY_OPTION) ){
				datasetTypeListBox.setEnabled(false);
				setSearchesEnabled(false);
				submitJobButton.setEnabled(true);
				messageLabel.setText("No need to select dataset(s) to submit jobs of this type"); 
			} else {
				datasetTypeListBox.setEnabled(true);
				// Nudge the dataset type list change handler - will decide whether to enable or disable search inputs
				handleDatasetTypeListBoxChange(null);
				messageLabel.setText(DEFAULT_MESSAGE);
			}
		}
	}
	
	private void setSearchesEnabled(boolean b) {
		// Hide the inputs, don't just disable them.
		// (This means we don't have to enable/disable each search filter, just show/hide the container panels)
		searchButton.setVisible(b);
		addGenericSearchButton.setVisible(b);
		searchListsPanel.setVisible(b);
		genericSearchesVerticalPanel.setVisible(b);
		// Show/hide the datasets results panels, and buttons
		datasetSplitPanel.setVisible(b);
		datasetDownloadButton.setVisible(b);
		datasetDownloadUrlButton.setVisible(b);
		datasetInfoButton.setVisible(b);
	}

	@UiHandler("datasetTypeListBox")
	void handleDatasetTypeListBoxChange(ChangeEvent event) {
		String selectedValue = datasetTypeListBox.getValue(datasetTypeListBox.getSelectedIndex());
		// Window.alert("Value is:'" + selectedValue + "'");

		// remove any datasets listed in the Datasets Table
		datasetList = new ArrayList<DatasetOverview>();
		datasetsTable.setRowData(datasetList);
		datasetsTable.setRowCount(0);
		clearDatasetInfoTable();
		// make sure any selections in the Datasets Table are cleared
		selectionModel.clear();
		if (selectedValue.equals(DATASET_TYPES_LIST_JOB_ONLY_OPTION)) {
			// enable the dataset actions box because it is not necessary
			// (or possible) to select a dataset of this type
			submitJobButton.setEnabled(true);
			// disable the search button - if the user presses it
			// the Options... box gets disabled
			setSearchesEnabled(false);
		} else if (selectedValue.equals(DATASET_TYPES_LIST_FIRST_OPTION)) {
			// No dataset type selected yet, but should be
			submitJobButton.setEnabled(false);
			setSearchesEnabled(false);
		} else {
			// disable the dataset actions box - it will be re-enabled when a search
			// is done and a new list of datasets appears
			submitJobButton.setEnabled(false);
			setSearchesEnabled(true);
		}
		// remove any message text ("3 datasets found" etc)
		messageLabel.setText(DEFAULT_MESSAGE);

	}

	private void clearDatasetInfoTable() {
		// remove any data displayed in the Dataset Information area
		List<DatasetInfoItem> infoItemList = new ArrayList<DatasetInfoItem>();
		datasetInfoTable.setRowData(infoItemList);
		datasetInfoTable.setRowCount(0);
	}

	private void doDownloadForm(){
		Set<DatasetOverview> selectedDatasets = selectionModel.getSelectedSet();
		if( selectedDatasets.size() == 0 ){
			Window.alert("Select a dataset first");
		} else if (selectedDatasets.size() > 1) {
			Window.alert("'" + OPTIONS_LIST_DOWNLOAD_OPTION + "' does not allow multiple datasets to be selected");
		} else {
			sessionIdField.setValue(portal.getSessionId());
			DatasetOverview selectedDataset = selectionModel.getSelectedSet().iterator().next();
			datasetIdsField.setValue(selectedDataset.getDatasetId().toString());
			outnameField.setValue(selectedDataset.getName());
			Window.alert(downloadForm.getAction());
			downloadForm.submit();
		}		
	}
	
	private void showDownloadUrl(){
		Set<DatasetOverview> selectedDatasets = selectionModel.getSelectedSet();
		if( selectedDatasets.size() == 0 ){
			Window.alert("Select a dataset first");
		} else if (selectedDatasets.size() > 1) {
			Window.alert("'" + OPTIONS_LIST_DOWNLOAD_URL_OPTION + "' does not allow multiple datasets to be selected");
		} else {
			DatasetOverview selectedDataset = selectionModel.getSelectedSet().iterator().next();
			List<Long> mtList = Collections.emptyList();
			dataService.getDataUrl(portal.getSessionId(), mtList,
					Arrays.asList(selectedDataset.getDatasetId()), mtList,
					selectedDataset.getName(), new AsyncCallback<String>() {

						@Override
						public void onFailure(Throwable caught) {
							if (caught.getClass() == SessionException.class) {
								System.err.println("caught is a SessionException");
								portal.loginPanel.setMessageText(caught.getMessage());
								portal.loginDialog.show();
							} else {
								Window.alert("Server error: " + caught.getMessage());
							}
						}

						@Override
						public void onSuccess(String url) {
							Window.alert(url);

						}
					});
		}		
	}
	
	/**
	 * Add job types from jobTypeMappings (which must be populated previously)
	 * to the jobTypesListBox.
	 */
	private void populateJobTypesListBox() {
		
		// Store the currently selected value (if any) so it can be re-selected
		// when the list box is repopulated
		String selectedJobType = JOB_TYPES_LIST_FIRST_OPTION;
		int selectedIndex = jobTypeListBox.getSelectedIndex();
		if( selectedIndex > -1 ){
			selectedJobType = jobTypeListBox.getValue(jobTypeListBox.getSelectedIndex());
		}
		jobTypeListBox.clear();
		jobTypeListBox.addItem(JOB_TYPES_LIST_FIRST_OPTION);
		int selectedItemIndex = 0;
		int itemCount = 1;
		for (String jobName : jobTypeMappings.getJobTypesMap().keySet()) {
			jobTypeListBox.addItem(jobName);
			if( jobName.equals(selectedJobType)){
				selectedItemIndex = itemCount;
			}
			itemCount++;
		}
		if (selectedItemIndex == 0) {
			// May need to explicitly select the first option?
			jobTypeListBox.setSelectedIndex(selectedItemIndex);
			// leave the first option selected in the list box but
			// force the datasets table etc to update accordingly as
			// this event does not seem to be fired automatically
			// (Without this, the Submit Job button is not disabled.)
			handleJobTypeListBoxChange(null);
		} else {
			jobTypeListBox.setSelectedIndex(selectedItemIndex);
		}
	}
	
	private void repopulateDatasetTypeListBox( List<String> datasetTypesList ) {
		// store the currently selected value in the datasetTypeListBox so
		// that it can be re-selected when the list box is re-populated
		String selectedDsType = datasetTypeListBox.getValue(datasetTypeListBox
				.getSelectedIndex());
		datasetTypeListBox.clear();
		datasetTypeListBox.addItem(DATASET_TYPES_LIST_FIRST_OPTION);
		int itemCount = 1;
		int selectedItemIndex = 0;
		for (String datasetType : datasetTypesList) {
			datasetTypeListBox.addItem(datasetType);
			if (datasetType.equals(selectedDsType)) {
				selectedItemIndex = itemCount;
			}
			itemCount++;
		}
		if (selectedItemIndex == 0) {
			// leave the first option selected in the list box but
			// force the datasets table etc to update accordingly as
			// this event does not seem to be fired automatically
			handleDatasetTypeListBoxChange(null);
		} else {
			datasetTypeListBox.setSelectedIndex(selectedItemIndex);
		}
	}

	protected void getJobTypesFromServer() {
		// put the JobTypes looked up from XML on the server into a variable
		// and populate the Job Types list from them.
		dataService.getJobTypeMappings(new AsyncCallback<JobTypeMappings>() {
			@Override
			public void onFailure(Throwable caught) {
				Window.alert("Server error: " + caught.getMessage());
			}

			@Override
			public void onSuccess(JobTypeMappings jobTypeMappings) {
				setJobTypeMappings(jobTypeMappings);
				populateJobTypesListBox();
			}
		});
	}

	private void displayJobTypesInTextArea() {
		if (jobTypeMappings == null) {
			debugTextArea.setText("jobTypeMappings is null");
		} else {
			debugTextArea.setText(jobTypeMappings.toString());
		}
	}

	private void displayDatasetParameterTypesMapInTextArea() {
		dataService.getDatasetParameterTypesMap(portal.getSessionId(),
				new AsyncCallback<LinkedHashMap<String, ParameterValueType>>() {
					@Override
					public void onFailure(Throwable caught) {
						Window.alert("Server error: " + caught.getMessage());
					}

					@Override
					public void onSuccess(
							LinkedHashMap<String, ParameterValueType> datasetParameterTypesMap) {
						StringBuffer sb = new StringBuffer();
						for (String paramName : datasetParameterTypesMap.keySet()) {
							sb.append(paramName);
							sb.append(" : ");
							sb.append(datasetParameterTypesMap.get(paramName).name());
							sb.append("\n");
						}
						debugTextArea.setText(sb.toString());
					}
				});
	}

	private void setJobTypeMappings(JobTypeMappings jobTypeMappings) {
		this.jobTypeMappings = jobTypeMappings;
	}

	private void addSearchBoxesAndPopulateTextArea() {
		// put the SearchItems looked up from XML into a TextArea
		dataService.getSearchItems(new AsyncCallback<SearchItems>() {
			@Override
			public void onFailure(Throwable caught) {
				Window.alert("Server error: " + caught.getMessage());
			}

			@Override
			public void onSuccess(SearchItems searchItems) {
				if (searchItems == null) {
					// debugTextArea.setText("searchItems is null");
				} else {
					// debugTextArea.setText(searchItems.toString());
					for (SearchItem searchItem : searchItems.getSearchItemList()) {
						ListBox listBox = new ListBox(searchItem.isMultipleSelect());
						// listBox.setName(searchItem.paramName);
						for (ListOption listOption : searchItem.getListOptions()) {
							listBox.addItem(listOption.getDisplayValue(),
									listOption.getSubmitValue());
						}
						listBox.setVisibleItemCount(searchItem.getVisibleItemCount());
						listBox.setSelectedIndex(0);
						searchListsPanel.add(listBox);
						searchItemsListBoxMap.put(searchItem.getParamName(), listBox);
					}
				}
			}
		});
	}

	private void refreshDatasetInformation(Long datasetId) {
		AsyncCallback<LinkedHashMap<String, String>> callback = new AsyncCallback<LinkedHashMap<String, String>>() {
			public void onFailure(Throwable caught) {
				// deal with possible exceptions
				System.err.println("DatasetsPanel.refreshDatasetInformation(): "
						+ caught.getMessage());
				if (caught.getClass() == SessionException.class) {
					System.err.println("caught is a SessionException");
					portal.loginPanel.setMessageText(caught.getMessage());
					portal.loginDialog.show();
				} else {
					Window.alert(caught.getClass().getName() + " " + caught.getMessage());
				}
			}

			public void onSuccess(LinkedHashMap<String, String> result) {
				Set<String> paramNames = result.keySet();
				Iterator<String> paramNamesIterator = paramNames.iterator();
				List<DatasetInfoItem> infoItemList = new ArrayList<DatasetInfoItem>();
				while (paramNamesIterator.hasNext()) {
					String paramName = paramNamesIterator.next();
					infoItemList.add(new DatasetInfoItem(paramName, result.get(paramName)));
				}
				datasetInfoTable.setRowData(infoItemList);
			}
		};
		// make the call to the server
		System.out.println("DatasetsPanel: making call to DataService");
		dataService.getDatasetParameters(portal.getSessionId(), datasetId, callback);
	}

	protected void refreshDatasetsList() {
		String selectedDatasetType = null;
		if (datasetTypeListBox.getSelectedIndex() == 0) {
			Window.alert("Please select a dataset type");
			return;
		} else {
			selectedDatasetType = datasetTypeListBox
					.getValue(datasetTypeListBox.getSelectedIndex());
		}

		// set up the callback object
		AsyncCallback<List<DatasetOverview>> callback = new AsyncCallback<List<DatasetOverview>>() {
			public void onFailure(Throwable caught) {
				// deal with possible exceptions
				System.err.println("DatasetsPanel.refreshDatasetsList(): " + caught.getMessage());
				if (caught.getClass() == SessionException.class) {
					System.err.println("caught is a SessionException");
					portal.loginPanel.setMessageText(caught.getMessage());
					portal.loginDialog.show();
				} else {
					Window.alert("Server error: " + caught.getMessage());
				}
			}

			public void onSuccess(List<DatasetOverview> result) {
				// clear the selection model otherwise current selections will be added to
				selectionModel.clear();
				// if ( result == null ) {
				// System.out.println("Result is null");
				// } else {
				int resultSize = result.size();
				System.out.println("Result size: " + resultSize);
				if (resultSize == PortalUtils.MAX_RESULTS) {
					// TODO - get the colour of the message changing to work
					messageLabel.setText("Results limit of " + PortalUtils.MAX_RESULTS
							+ " reached. Please refine your search.");
				} else {
					messageLabel.setText(resultSize + " datasets found.");
				}
				// }
				datasetList = result;
				// set the page size to the number of datasets returned
				// otherwise we only see the first 15 by default
				datasetsTable.setPageSize(datasetList.size());
				// Set the total row count. This isn't strictly necessary, but it affects
				// paging calculations, so its good habit to keep the row count up to date
				// KP - this doesn't seem to work ??
				// datasetsTable.setRowCount(datasetList.size(), true);
				// Push the data into the widget.
				datasetsTable.setRowData(0, datasetList);
				if (datasetList.size() == 0) {
					// remove any data displayed in the Dataset Information area
					List<DatasetInfoItem> infoItemList = new ArrayList<DatasetInfoItem>();
					// TODO - should I show a list of parameter names with blank values???
					datasetInfoTable.setRowData(infoItemList);
				} else {
					// set the first item in the list to selected
					// so that the DatasetOverview information pane gets populated
					selectionModel.setSelected(datasetList.get(0), true);
				}
			}
		};

		List<GenericSearchSelections> genSearchSelectionsList = getGenericSearchSelectionsList();
		if (genSearchSelectionsList != null) {
			StringBuilder sb = new StringBuilder("genSearchSelectionsList:\n");
			for (GenericSearchSelections genericSearchSelections : genSearchSelectionsList) {
				sb.append(genericSearchSelections.toString());
				sb.append("\n");
			}
			// debugTextArea.setText(sb.toString());
			// Window.alert(sb.toString());
		}

		// make the call to the server
		System.out.println("DatasetsPanel: making call to DataService");
		dataService.getDatasetList(portal.getSessionId(), selectedDatasetType,
				getSearchParamsMap(), genSearchSelectionsList, callback);
	}

	private List<GenericSearchSelections> getGenericSearchSelectionsList() {
		List<GenericSearchSelections> genSearchSelectionsList = new ArrayList<GenericSearchSelections>();
		for (int i = 0; i < genericSearchesVerticalPanel.getWidgetCount(); i++) {
			try {
				GenericSearchPanel genSearchPanel = (GenericSearchPanel) genericSearchesVerticalPanel
						.getWidget(i);
				genSearchSelectionsList.add(genSearchPanel.validateAndGetGenericSearchSelections());
			} catch (Exception e) {
				Window.alert(e.getMessage());
				return null;
			}
		}
		return genSearchSelectionsList;
	}

	private Map<String, List<String>> getSearchParamsMap() {
		Map<String, List<String>> searchParamsMap = new HashMap<String, List<String>>();
		for (String key : searchItemsListBoxMap.keySet()) {
			List<String> selectedItemsList = new ArrayList<String>();
			ListBox listBox = searchItemsListBoxMap.get(key);
			for (int i = 0; i < listBox.getItemCount(); i++) {
				// add selected items whose values are not empty strings
				// ie. ignore the "title" option at the top
				if (listBox.isItemSelected(i) && !listBox.getValue(i).equals("")) {
					selectedItemsList.add(listBox.getValue(i));
				}
			}
			if (selectedItemsList.size() > 0) {
				searchParamsMap.put(key, selectedItemsList);
			}
		}
		return searchParamsMap;
	}

	@Override
	public void onResize() {
		int parentHeight = Window.getClientHeight();
		int parentWidth = Window.getClientWidth();
		int splitPanelHeight = (int) (parentHeight * 0.7);
		int splitPanelWidth = parentWidth - 40;
		datasetSplitPanel.setSize(splitPanelWidth + "px", splitPanelHeight + "px");
	}

}
