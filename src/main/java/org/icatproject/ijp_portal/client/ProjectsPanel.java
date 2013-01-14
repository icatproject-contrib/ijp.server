package org.icatproject.ijp_portal.client;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.icatproject.ijp_portal.client.service.DataService;
import org.icatproject.ijp_portal.client.service.DataServiceAsync;

import org.icatproject.ijp_portal.shared.AccountDTO;
import org.icatproject.ijp_portal.shared.GenericSearchSelections;
import org.icatproject.ijp_portal.shared.PortalUtils;
import org.icatproject.ijp_portal.shared.PortalUtils.DatasetType;
import org.icatproject.ijp_portal.shared.ProjectOverview;
import org.icatproject.ijp_portal.shared.PortalUtils.ParameterValueType;
import org.icatproject.ijp_portal.shared.ServerException;
import org.icatproject.ijp_portal.shared.SessionException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
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
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

public class ProjectsPanel extends Composite implements RequiresResize {
	// Annotation can be used to change the name of the associated xml file
	// @UiTemplate("LoginPanel.ui.xml")
	interface MyUiBinder extends UiBinder<Widget, ProjectsPanel> {
	}

	@UiField
	ListBox usersListBox;

	@UiField
	ListBox instrumentsListBox;

	@UiField
	ListBox exptTypesListBox;

	@UiField
	ListBox numChannelsListBox;
	
	@UiField
	Button searchButton;

	@UiField
	HorizontalPanel genericParamSearchPanel;
	
	@UiField
	ListBox paramSelectListBox;
	@UiField
	ListBox operatorSelectListBox;
	// TODO - use a DoubleBox for now as the TextBox has a different height and makes the layout a bit untidy
	@UiField
	DoubleBox paramValueTextBox;  
	@UiField
	DoubleBox paramValueDoubleBox;
	@UiField
	DoubleBox fromValueDoubleBox;
	@UiField
	DoubleBox toValueDoubleBox;
	@UiField
	DateBox fromDateBox;
	@UiField
	DateBox toDateBox;

	@UiField
	Label messageLabel;
	
	@UiField
	CellTable<ProjectOverview> projectsTable;
	
	@UiField
	ListBox datasetActionListBox;

	@UiField
	Button datasetInfoButton;

	@UiField
	Button submitDatasetButton;

	@UiField
	CellTable<ProjectInfoItem> projectInfoTable;

	@UiField
	ScrollPanel projectsScrollPanel;

	@UiField
	ScrollPanel infoScrollPanel;

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
	Hidden datasetIdField;
	@UiField
	Hidden datasetNameField;

	private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
	private DataServiceAsync dataService = GWT.create(DataService.class);

	private List<ProjectOverview> projectList = new ArrayList<ProjectOverview>();
    private final SingleSelectionModel<ProjectOverview> selectionModel = new SingleSelectionModel<ProjectOverview>();

    Portal portal;
    DatasetType datasetType;

	public ProjectsPanel(Portal portal, DatasetType datasetType) {
		this.portal = portal;
		this.datasetType = datasetType;
		initWidget(uiBinder.createAndBindUi(this));

		usersListBox.addItem("Any users","");
		usersListBox.addItem("Chris Tynan");
		usersListBox.addItem("Laura Zanetti Domingues");
		usersListBox.addItem("Sarah Needham");
		usersListBox.addItem("Stephen Webb");
//		usersListBox.setVisibleItemCount(2);
		usersListBox.setSelectedIndex(0);
		
		instrumentsListBox.addItem("Any instrument","");
		instrumentsListBox.addItem("Unknown instrument","Unknown");
		instrumentsListBox.addItem("OctopusSM2");
		instrumentsListBox.addItem("OctopusSM3");
		instrumentsListBox.setSelectedIndex(0);
		
		exptTypesListBox.addItem("Any experiment type","");
		exptTypesListBox.addItem("Unknown experiment type","Unknown");
		exptTypesListBox.addItem("Colocalisation");
		exptTypesListBox.addItem("Undefined");
		exptTypesListBox.setSelectedIndex(0);
		
		numChannelsListBox.addItem("Any number of channels","");
		// TODO - work out how to implement "Unknown" in a numeric search 
//		numChannelsListBox.addItem("Unknown number of channels","Unknown");
		numChannelsListBox.addItem("1 channel","1");
		numChannelsListBox.addItem("3 channels","3");
		numChannelsListBox.setSelectedIndex(0);

		datasetActionListBox.addItem("Options ...");
		
//		projectsTable.setPageSize(5);
//		projectsScrollPanel.setAlwaysShowScrollBars(true);
		
		searchButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
			    refreshProjectsList();
			}
		});

		datasetInfoButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
		        ProjectOverview selectedProject = selectionModel.getSelectedObject();
		        if (selectedProject != null) {
		        	String message = "";
		        	message += "ID: "+ selectedProject.getDatasetId() + "\n";
		        	message += "hasBeads: "+ selectedProject.hasBeads() + "\n";
		        	message += "hasBias: "+ selectedProject.hasBias() + "\n";
		        	message += "hasDark: "+ selectedProject.hasDark() + "\n";
		        	message += "hasFlatfield: "+ selectedProject.hasFlatfield() + "\n";
		        	message += "hasCheck: "+ selectedProject.hasCheck() + "\n";
		        	message += "hasWhitelight: "+ selectedProject.hasWhitelight() + "\n";
		        	message += "hasEvidenceMaps: "+ selectedProject.hasEvidenceMaps() + "\n";
		        	message += "hasRegErrorMaps: "+ selectedProject.hasRegErrorMaps() + "\n";
		        	message += "nchannels: "+ selectedProject.getNumChannels() + "\n";
		            Window.alert(message);
		        } else {
		            Window.alert("Please select a dataset");
		        }
			}
		});

		// only projects, datasets and user datasets should have this menu currently
//		if ( datasetType == PortalUtils.DatasetType.LSF_PROJECT || 
//			 datasetType == PortalUtils.DatasetType.LSF_DATASET || 
//			 datasetType == PortalUtils.DatasetType.LSF_USER_DATASET ) {
//			// leave the menu visible
//		} else {
//			datasetActionListBox.setVisible(false);
//		}
		
		// only the 'Datasets' tab should have a button to process a dataset
		if ( datasetType == PortalUtils.DatasetType.LSF_DATASET ) {

			submitDatasetButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
			        ProjectOverview selectedProject = selectionModel.getSelectedObject();
			        if (selectedProject != null) {
			        	Long datasetId = selectedProject.getDatasetId();
			        	submitDataset(datasetId);
			        } else {
			            Window.alert("Please select a dataset");
			        }
				}
			});

		} else {
			submitDatasetButton.setVisible(false);
		}
			
		Set<String> paramNames = PortalUtils.PARAM_DESCRIPTOR_MAPPINGS.keySet();
		Iterator<String> paramNamesIterator = paramNames.iterator();
		while ( paramNamesIterator.hasNext() ) {
			paramSelectListBox.addItem( paramNamesIterator.next() );
		}
		paramSelectListBox.addChangeHandler(new ChangeHandler()
		{
			public void onChange(ChangeEvent event)
			{
				setOperatorsListBoxValues();
			}
		});
		// set the list of operators to correspond to the first item in the list of parameters
		setOperatorsListBoxValues();

		operatorSelectListBox.addChangeHandler(new ChangeHandler()
		{
			public void onChange(ChangeEvent event)
			{
				setCustomSearchValueBoxes();
			}
		});
		
		// Add a text column to show the name.
	    TextColumn<ProjectOverview> nameColumn = new TextColumn<ProjectOverview>() {
	      @Override
	      public String getValue(ProjectOverview project) {
	        return project.getName();
	      }
	    };
	    projectsTable.addColumn(nameColumn, "Name");

	    // Add a text column to show the sample description.
	    TextColumn<ProjectOverview> sampleDescriptionColumn = new TextColumn<ProjectOverview>() {
	      @Override
	      public String getValue(ProjectOverview project) {
	        return project.getSampleDescription();
	      }
	    };
	    projectsTable.addColumn(sampleDescriptionColumn, "Sample Description");
	    
	    // Add a text column to show the directory.
	    TextColumn<ProjectOverview> usersColumn = new TextColumn<ProjectOverview>() {
	      @Override
	      public String getValue(ProjectOverview project) {
	        return project.getUsers();
	      }
	    };
	    projectsTable.addColumn(usersColumn, "Users");
	    
	    // configure the selection model to handle user selection of projects
	    projectsTable.setSelectionModel(selectionModel);
	    selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
	      public void onSelectionChange(SelectionChangeEvent event) {
	        ProjectOverview selectedProject = selectionModel.getSelectedObject();
	        if (selectedProject != null) {
	        	refreshProjectInformation(selectedProject.getDatasetId());
	        	refreshDatasetActionsListBox(selectedProject);
	        }
	      }
	    });

	    // get the list of projects from the server
//	    refreshProjectsList();
	    
	    // Add a text column to show the name.
	    TextColumn<ProjectInfoItem> infoNameColumn = new TextColumn<ProjectInfoItem>() {
	      @Override
	      public String getValue(ProjectInfoItem projectInfoItem) {
	        return projectInfoItem.getName();
	      }
	    };
	    projectInfoTable.addColumn(infoNameColumn);

	    // Add a text column to show the value.
	    TextColumn<ProjectInfoItem> infoValueColumn = new TextColumn<ProjectInfoItem>() {
	      @Override
	      public String getValue(ProjectInfoItem projectInfoItem) {
	        return projectInfoItem.getValue();
	      }
	    };
	    projectInfoTable.addColumn(infoValueColumn);

	    // for the rdpForm to request an rdp file download
	    rdpForm.setEncoding(FormPanel.ENCODING_URLENCODED);
		rdpForm.setMethod(FormPanel.METHOD_GET);
		rdpForm.setAction(GWT.getHostPageBaseURL() + "rdp");

	    // for the downloadForm to request a dataset zip file download
	    downloadForm.setEncoding(FormPanel.ENCODING_URLENCODED);
		downloadForm.setMethod(FormPanel.METHOD_GET);
		downloadForm.setAction(GWT.getHostPageBaseURL() + "download");
	}
	
	@UiHandler("datasetActionListBox")
	void handleDatasetActionListBoxChange(ChangeEvent event) {
		String command = datasetActionListBox.getValue(datasetActionListBox.getSelectedIndex());
//		Window.alert("command='" + command + "'");
		if ( command.equals("") ) {
			// do nothing - this is the list box title that has no action
		} else if ( command.equals("Download") ) {
	        ProjectOverview selectedProject = selectionModel.getSelectedObject();
			sessionIdField.setValue(portal.getSessionId());
			datasetIdField.setValue(selectedProject.getDatasetId().toString());
			datasetNameField.setValue(selectedProject.getName());
			downloadForm.submit();
		} else {
			String username = portal.getUsername();
			String sessionId = portal.getSessionId();
			Long dsid = selectionModel.getSelectedObject().getDatasetId();
			dataService.getAccountFor(username, sessionId, dsid, command, new AsyncCallback<AccountDTO>() {

				@Override
				public void onFailure(Throwable caught) {
					Window.alert("Server error: " + caught.getMessage());
				}

				@Override
				public void onSuccess(AccountDTO account) {
					if (Window.Navigator.getPlatform().equals("Win32")) {
						hostNameField.setValue(account.getHostName());
						accountNameField.setValue(account.getAccountName());
						rdpForm.submit();
						Window.alert("Execute the downloaded file and specify '"
								+ account.getPassword()
								+ "' as the password. The password has a short lifetime!");
					} else {
						Window.alert("Please paste into a terminal:     rdesktop -u "
								+ account.getAccountName() + " -p " + account.getPassword()
								+ " " + account.getHostName());
					}
				}
			});
		}
	}
	
	protected void refreshDatasetActionsListBox(ProjectOverview selectedProject) {
		datasetActionListBox.clear();
		datasetActionListBox.addItem("Options ...", "");
		datasetActionListBox.addItem("Download", "Download");
		if ( datasetType == DatasetType.LSF_DATASET ) {
			// NOTE: the space in "dataset " in the following option is intentional and required
			datasetActionListBox.addItem("View", "dataset ");
			datasetActionListBox.addItem("View (full frame)", "dataset --option=fullframe");
			datasetActionListBox.addItem("View (full frame, overlay channels)", "dataset --option=fullframe-overlay");
			if ( selectedProject.hasBeads() ) {
				datasetActionListBox.addItem("View beads", "dataset --option=beads");
				datasetActionListBox.addItem("View beads (full frame)", "dataset --option=beads-fullframe");
				datasetActionListBox.addItem("View beads (full frame, overlay channels)", "dataset --option=beads-fullframe-overlay");
			}
			if ( selectedProject.hasFlatfield() ) {
				datasetActionListBox.addItem("View flats", "dataset --option=flats");
				datasetActionListBox.addItem("View flats (full frame)", "dataset --option=flats-fullframe");
				datasetActionListBox.addItem("View flats (full frame, overlay channels)", "dataset --option=flats-fullframe-overlay");
			}
			if ( selectedProject.hasWhitelight() ) {
				datasetActionListBox.addItem("View whitelights", "dataset --option=whitelights");
				datasetActionListBox.addItem("View whitelights (full frame)", "dataset --option=whitelights-fullframe");
				datasetActionListBox.addItem("View whitelights (full frame, overlay channels)", "dataset --option=whitelights-fullframe-overlay");
			}
			if ( selectedProject.hasDark() ) {
				datasetActionListBox.addItem("View darks", "dataset --option=darks");
				datasetActionListBox.addItem("View darks (full frame)", "dataset --option=darks-fullframe");
			}
			if ( selectedProject.hasBias() ) {
				datasetActionListBox.addItem("View bias", "dataset --option=biases");
				datasetActionListBox.addItem("View bias (full frame)", "dataset --option=biases-fullframe");
			}
		} else if ( datasetType == DatasetType.LSF_PROJECT ) {
			datasetActionListBox.addItem("View", "project ");
			if ( selectedProject.hasBeads() ) {
				datasetActionListBox.addItem("View beads", "project --beads");
				if ( selectedProject.getNumChannels() > 1 ) {
					datasetActionListBox.addItem("View reg beads", "project --reg-beads");
					if ( selectedProject.hasRegErrorMaps() ) {
						datasetActionListBox.addItem("View reg errors (in non-ref coords)", "project --non-ref-reg-error");
						datasetActionListBox.addItem("View inverse reg errors (in ref coords)", "project --ref-reg-error");
					}
				}
			}
			if ( selectedProject.hasWhitelight() ) {
				datasetActionListBox.addItem("View whitelights", "project --whitelights");
				if ( selectedProject.getNumChannels() == 1 ) {
					datasetActionListBox.addItem("View whitelights (no tracks)", "project --whitelights --no-tracks");
				} else if ( selectedProject.getNumChannels() > 1 ) {
					datasetActionListBox.addItem("View reg whitelights", "project --reg-whitelights");
					datasetActionListBox.addItem("View reg whitelights (no tracks)", "project --reg-whitelights --no-tracks");
				}
			}
			if ( selectedProject.hasFlatfield() ) {
				datasetActionListBox.addItem("View flatfields", "project --flatfields");
				if ( selectedProject.getNumChannels() > 1 ) {
					datasetActionListBox.addItem("View reg flatfields", "project --reg-flatfields");
				}
			}
			if ( selectedProject.hasEvidenceMaps() ) {
				datasetActionListBox.addItem("View evidencemaps", "project --evidencemaps");
			}
			datasetActionListBox.addItem("View reg residual frames", "project --reg-residualframes");
			datasetActionListBox.addItem("View reg model frames", "project --reg-modelframes");
		}
	}

	private void setOperatorsListBoxValues() {
		int selectedIndex = paramSelectListBox.getSelectedIndex();
		String selectedValue = paramSelectListBox.getValue(selectedIndex);
		ParameterValueType paramType = PortalUtils.PARAM_DESCRIPTOR_MAPPINGS.get(selectedValue).getParameterValueType();
		List<String> operatorListBoxValues = PortalUtils.PARAM_OPERATOR_MAPPINGS.get(paramType);
		operatorSelectListBox.clear();
		for (String newListBoxValue : operatorListBoxValues) {
			operatorSelectListBox.addItem(newListBoxValue);
		}
		removeAndEmptyCustomValueBoxes();
		if (paramType == ParameterValueType.DATE_AND_TIME) {
			genericParamSearchPanel.add(fromDateBox);
			genericParamSearchPanel.add(toDateBox);
		} else if (paramType == ParameterValueType.STRING) {
			genericParamSearchPanel.add(paramValueTextBox);
		} else if (paramType == ParameterValueType.NUMERIC) {
			genericParamSearchPanel.add(paramValueDoubleBox);
		}
	}
	
	private void setCustomSearchValueBoxes() {
		int selectedIndex = paramSelectListBox.getSelectedIndex();
		String selectedValue = paramSelectListBox.getValue(selectedIndex);
		ParameterValueType paramType = PortalUtils.PARAM_DESCRIPTOR_MAPPINGS.get(selectedValue).getParameterValueType();
		if (paramType == ParameterValueType.NUMERIC) {
			removeAndEmptyCustomValueBoxes();
			selectedIndex = operatorSelectListBox.getSelectedIndex();
			selectedValue = operatorSelectListBox.getValue(selectedIndex);
			if ( selectedValue.equals("BETWEEN") ) {
				genericParamSearchPanel.add(fromValueDoubleBox);
				genericParamSearchPanel.add(toValueDoubleBox);
			} else {
				genericParamSearchPanel.add(paramValueDoubleBox);
			}
		}
	}
	
	private void removeAndEmptyCustomValueBoxes() {
		genericParamSearchPanel.remove(paramValueTextBox);
		genericParamSearchPanel.remove(paramValueDoubleBox);
		genericParamSearchPanel.remove(fromValueDoubleBox);
		genericParamSearchPanel.remove(fromDateBox);
		genericParamSearchPanel.remove(toValueDoubleBox);
		genericParamSearchPanel.remove(toDateBox);
		// empty all of the custom search box values
		// by setting them to null, otherwise the logic in
		// the server code is over-complicated due to values
		// being set in search boxes that are currently hidden
		paramValueTextBox.setValue(null);
		paramValueDoubleBox.setValue(null);
		fromValueDoubleBox.setValue(null);
		fromDateBox.setValue(null);
		toValueDoubleBox.setValue(null);
		toDateBox.setValue(null);
	}
	
	private void submitDataset(Long datasetId) {
		AsyncCallback<String> callback = new AsyncCallback<String>() {
	    	public void onFailure(Throwable caught) {
	    		// TODO - do something with errors
	    		Window.alert("Problem submitting dataset: " + caught.getMessage());
	    	}

	    	public void onSuccess(String result) {
	    		Window.alert("Job running with id: " + result);
	    	}
		};

		// make the call to the server
		System.out.println("ProjectsPanel: making call to DataService");
		dataService.submitDataset(portal.getSessionId(), portal.getUsername(), datasetId, callback);
	}
	
	private void refreshProjectInformation(Long datasetId) {
		AsyncCallback<LinkedHashMap<String,String>> callback = new AsyncCallback<LinkedHashMap<String,String>>() {
	    	public void onFailure(Throwable caught) {
	    		// deal with possible exceptions
	    		System.err.println("ProjectsPanel.refreshProjectInformation(): " + caught.getMessage());
	    		if ( caught.getClass() == SessionException.class ) {
	    			System.err.println("caught is a SessionException");
	    			portal.loginPanel.setMessageText(caught.getMessage());
	    			portal.loginDialog.show();
	    		} else if ( caught.getClass() == ServerException.class ) {
		            Window.alert("Server error: " + caught.getMessage());
	    		} else {
	    			// no other exceptions are expected
	    		}
	    	}

	    	public void onSuccess(LinkedHashMap<String,String> result) {
	    		Set<String> paramNames = result.keySet();
	    		Iterator<String> paramNamesIterator = paramNames.iterator();
	        	List<ProjectInfoItem> infoItemList = new ArrayList<ProjectInfoItem>();
	    		while ( paramNamesIterator.hasNext() ) {
	    			String paramName = paramNamesIterator.next();
		        	infoItemList.add( new ProjectInfoItem(paramName,result.get(paramName)) );
	    		}
	    		projectInfoTable.setRowData(infoItemList);
	    	}
		};
		// make the call to the server
		System.out.println("ProjectsPanel: making call to DataService");
		dataService.getProjectParameters(portal.getSessionId(), datasetId, callback);
	}

	private List<String> createSelectedItemsList( ListBox listBox ) {
		// package the selected users into a list object to send to the server
		List<String> items = new ArrayList<String>();
		for (int i=0; i<listBox.getItemCount(); i++) {
			if (listBox.isItemSelected(i)) {
				String selectedValue = listBox.getValue(i);
				// ignore selected items that I have given empty values to
				if (!selectedValue.equals("")) {
					items.add(listBox.getValue(i));
				}
			}
		}
		return items;
	}
	
	void refreshProjectsList() {

		List<String> users = createSelectedItemsList(usersListBox);
		List<String> instruments = createSelectedItemsList(instrumentsListBox);
		List<String> exptTypes = createSelectedItemsList(exptTypesListBox);
		List<String> numChannels = createSelectedItemsList(numChannelsListBox);
		
		// package the generic search selections options into an object to send to the server
		GenericSearchSelections genSearchSelections = new GenericSearchSelections();
		int selectedIndex = paramSelectListBox.getSelectedIndex();
		String selectedParamName = paramSelectListBox.getItemText(selectedIndex);
		genSearchSelections.setSearchParamName(selectedParamName);
		selectedIndex = operatorSelectListBox.getSelectedIndex();
		genSearchSelections.setSearchOperator(operatorSelectListBox.getItemText(selectedIndex));
		genSearchSelections.setSearchValueString(paramValueTextBox.getText());
		try {
			genSearchSelections.setSearchValueNumeric(paramValueDoubleBox.getValueOrThrow());
		} catch (ParseException e) {
            Window.alert("Search value is not a valid number: " + paramValueDoubleBox.getText());
            return;
		}
		try {
			genSearchSelections.setFromValueNumeric(fromValueDoubleBox.getValueOrThrow());
		} catch (ParseException e) {
            Window.alert("'From' value is not a valid number: " + fromValueDoubleBox.getText());
            return;
		}
		try {
			genSearchSelections.setToValueNumeric(toValueDoubleBox.getValueOrThrow());
		} catch (ParseException e) {
            Window.alert("'To' value is not a valid number: " + toValueDoubleBox.getText());
            return;
		}
		genSearchSelections.setFromDate(fromDateBox.getValue());
		genSearchSelections.setToDate(toDateBox.getValue());
		if ( PortalUtils.PARAM_DESCRIPTOR_MAPPINGS.get(selectedParamName).getParameterValueType() == ParameterValueType.NUMERIC ) {
			if ( (genSearchSelections.getFromValueNumeric() != null && genSearchSelections.getToValueNumeric() == null) ||
				 (genSearchSelections.getFromValueNumeric() == null && genSearchSelections.getToValueNumeric() != null) ) {
	            Window.alert("Both fields must contain a value when BETWEEN is selected");
	            return;
			}
		}
		if ( PortalUtils.PARAM_DESCRIPTOR_MAPPINGS.get(selectedParamName).getParameterValueType() == ParameterValueType.DATE_AND_TIME ) {
			if ( (genSearchSelections.getFromDate() != null && genSearchSelections.getToDate() == null) ||
			     (genSearchSelections.getFromDate() == null && genSearchSelections.getToDate() != null) ) {
		            Window.alert("Both date fields must be completed");
		            return;
			}
		}
		
	    // set up the callback object
		AsyncCallback<List<ProjectOverview>> callback = new AsyncCallback<List<ProjectOverview>>() {
	    	public void onFailure(Throwable caught) {
	    		// deal with possible exceptions
	    		System.err.println("ProjectsPanel.refreshProjectsList(): " + caught.getMessage());
	    		if ( caught.getClass() == SessionException.class ) {
	    			System.err.println("caught is a SessionException");
	    			portal.loginPanel.setMessageText(caught.getMessage());
	    			portal.loginDialog.show();
	    		} else if ( caught.getClass() == ServerException.class ) {
		            Window.alert("Server error: " + caught.getMessage());
	    		} else {
	    			// no other exceptions are expected
	    		}
	    	}

	    	public void onSuccess(List<ProjectOverview> result) {
	    		if ( result == null ) {
	    			System.out.println("Result is null");
	    		} else {
	    			int resultSize = result.size();
	    			System.out.println("Result size: " + resultSize);
	    			if ( resultSize == PortalUtils.MAX_RESULTS ) {
	    				// TODO - get the colour of the message changing to work
//	    				messageLabel.setStyleName("red");
	    				messageLabel.setText("Results limit of " + PortalUtils.MAX_RESULTS + " reached. Please refine your search.");
	    			} else {
//	    				messageLabel.setStyleName("black");
	    				if ( datasetType == DatasetType.LSF_PROJECT || datasetType == DatasetType.LSF_DATASET ) {
	    					messageLabel.setText(resultSize + " " + datasetType.toString() + "s found.");
	    				} else {
	    					messageLabel.setText(resultSize + " " + datasetType.toString() + " datasets found.");
	    				}
	    			}
	    		}
//	    		for ( ProjectOverview project : result ) {
//	    			System.out.println( project.getName() );
//	    		}
	    		projectList = result;
	    		// set the page size to the number of projects returned
	    		// otherwise we only see the first 15 by default
	    		projectsTable.setPageSize(projectList.size());
	    	    // Set the total row count. This isn't strictly necessary, but it affects
	    	    // paging calculations, so its good habit to keep the row count up to date
	    		// KP - this doesn't seem to work ??
//	    	    projectsTable.setRowCount(projectList.size(), true);
	    	    // Push the data into the widget.
	    	    projectsTable.setRowData(0, projectList);
	    	    if ( projectList.size() == 0 ) {
	    	    	// remove any data displayed in the Project Information area
		        	List<ProjectInfoItem> infoItemList = new ArrayList<ProjectInfoItem>();
		        	// TODO - should I show a list of parameter names with blank values???
		    		projectInfoTable.setRowData(infoItemList);
	    	    } else {
		    	    // set the first item in the list to selected 
		    	    // so that the ProjectOverview information pane gets populated
	    	    	selectionModel.setSelected(projectList.get(0), true);
	    	    }
	    	}
		};

		// make the call to the server
		System.out.println("ProjectsPanel: making call to DataService");
		dataService.getProjectList(portal.getSessionId(), this.datasetType, users, instruments, exptTypes, numChannels, genSearchSelections, callback);
	}

	@Override
	public void onResize() {
        int parentHeight = getParent().getOffsetHeight();
        int parentWidth = getParent().getOffsetWidth();
        int projectsTableHeight = (int)parentHeight/3;
        int infoTableHeight = (int)parentHeight/3;
        projectsScrollPanel.setHeight(projectsTableHeight+"px");
        projectsScrollPanel.setWidth(parentWidth-40+"px");
        infoScrollPanel.setHeight(infoTableHeight+"px");
        infoScrollPanel.setWidth(parentWidth-40+"px");
//        System.out.println("ProjectsPanel: projectsScroller height= " + projectsTableHeight + ": infoScroller height= " + infoTableHeight);
	}

}
