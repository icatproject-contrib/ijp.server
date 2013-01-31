package org.icatproject.ijp_portal.client;

import java.util.LinkedHashMap;

import org.icatproject.ijp_portal.shared.PortalUtils.DatasetType;
import org.icatproject.ijp_portal.shared.PortalUtils.OutputType;
import org.icatproject.ijp_portal.shared.PortalUtils.ParameterValueType;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Portal implements EntryPoint {
	// Annotation can be used to change the name of the associated xml file
	// @UiTemplate("LsfPortal.ui.xml")
	interface MyUiBinder extends UiBinder<Widget, Portal> {
	}

	@UiField
	TabLayoutPanel tabLayoutPanel;
	// ResizableTabLayoutPanel tabLayoutPanel;
	// TabLayoutPanel tabLayoutPanel = new TabLayoutPanel(1.5, Unit.EM);
	// ResizableTabLayoutPanel tabLayoutPanel = new ResizableTabLayoutPanel(1.5, Unit.EM);

	DialogBox loginDialog;
	LoginPanel loginPanel;

	DialogBox quincyOutputDialog = new DialogBox(false, false);
	JobOutputPanel quincyOutputPanel;

	DialogBox jobOutputDialog = new DialogBox(false, false);
	JobOutputPanel jobStandardOutputPanel;

	DialogBox jobErrorDialog = new DialogBox(false, false);
	JobOutputPanel jobErrorOutputPanel;

	DialogBox jobOptionsDialog = new DialogBox(false, true);
	JobOptionsPanel jobOptionsPanel;

	DatasetsPanel datasetsPanelNew;

	ProjectsPanel projectsPanel;
	ProjectsPanel datasetsPanel;
	ProjectsPanel beadDatasetsPanel;
	ProjectsPanel biasDatasetsPanel;
	ProjectsPanel darkDatasetsPanel;
	ProjectsPanel flatfieldDatasetsPanel;
	ProjectsPanel checkDatasetsPanel;
	ProjectsPanel userDatasetsPanel;

	JobStatusPanel jobStatusPanel;

	private String username;
	private String sessionId;

	private LinkedHashMap<String, ParameterValueType> mergedDatasetParameterTypeMappings = null;

	private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

	public void onModuleLoad() {

		RootLayoutPanel.get().add(uiBinder.createAndBindUi(this));
		// RootPanel.get().add(uiBinder.createAndBindUi(this));

		loginPanel = new LoginPanel(this);

		// TODO eliminate quincyOutputPanel properly
		// quincyOutputPanel = new JobOutputPanel(this, quincyOutputDialog, OutputType.QUINCY_OUTPUT);
		jobStandardOutputPanel = new JobOutputPanel(this, jobOutputDialog,
				OutputType.STANDARD_OUTPUT);
		jobErrorOutputPanel = new JobOutputPanel(this, jobErrorDialog, OutputType.ERROR_OUTPUT);

		jobOptionsPanel = new JobOptionsPanel(this, jobOptionsDialog);

		datasetsPanelNew = new DatasetsPanel(this);
		datasetsPanelNew.setVisible(true);

		projectsPanel = new ProjectsPanel(this, DatasetType.LSF_PROJECT);
		projectsPanel.setVisible(true);

		datasetsPanel = new ProjectsPanel(this, DatasetType.LSF_DATASET);
		datasetsPanel.setVisible(true);

		beadDatasetsPanel = new ProjectsPanel(this, DatasetType.LSF_BEAD_DATASET);
		beadDatasetsPanel.setVisible(true);

		biasDatasetsPanel = new ProjectsPanel(this, DatasetType.LSF_BIAS_DATASET);
		biasDatasetsPanel.setVisible(true);

		darkDatasetsPanel = new ProjectsPanel(this, DatasetType.LSF_DARK_DATASET);
		darkDatasetsPanel.setVisible(true);

		flatfieldDatasetsPanel = new ProjectsPanel(this, DatasetType.LSF_FLATFIELD_DATASET);
		flatfieldDatasetsPanel.setVisible(true);

		checkDatasetsPanel = new ProjectsPanel(this, DatasetType.LSF_CHECK_DATASET);
		checkDatasetsPanel.setVisible(true);

		userDatasetsPanel = new ProjectsPanel(this, DatasetType.LSF_USER_DATASET);
		userDatasetsPanel.setVisible(true);

		jobStatusPanel = new JobStatusPanel(this);
		jobStatusPanel.setVisible(true);

		// final TabLayoutPanel tabLayoutPanel = new TabLayoutPanel(1.5, Unit.EM);
		// tabLayoutPanel.setSize("2000px", "1500px");
		tabLayoutPanel.add(datasetsPanelNew, "Datasets (New)");
		tabLayoutPanel.add(projectsPanel, "Projects");
		tabLayoutPanel.add(datasetsPanel, "Datasets");
		tabLayoutPanel.add(beadDatasetsPanel, "Beads");
		tabLayoutPanel.add(biasDatasetsPanel, "Bias");
		tabLayoutPanel.add(darkDatasetsPanel, "Dark");
		tabLayoutPanel.add(flatfieldDatasetsPanel, "FlatField");
		tabLayoutPanel.add(checkDatasetsPanel, "CheckImage");
		tabLayoutPanel.add(userDatasetsPanel, "User Datasets");
		tabLayoutPanel.add(jobStatusPanel, "Job Status");
		tabLayoutPanel.addSelectionHandler(new SelectionHandler<Integer>() {
			public void onSelection(SelectionEvent<Integer> event) {
				int tabId = event.getSelectedItem();
				Widget tabWidget = tabLayoutPanel.getWidget(tabId);
				if (tabWidget != null) {
					if (tabWidget == jobStatusPanel) {
						// refresh the table of jobs in the job status panel
						jobStatusPanel.refreshJobList();
						// set a repeating timer going with a period of 1 minute
						jobStatusPanel.tableRefreshTimer.scheduleRepeating(60000);
					} else if (tabWidget == datasetsPanelNew) {
						((DatasetsPanel) tabWidget).refreshDatasetsList();
					} else {
						jobStatusPanel.tableRefreshTimer.cancel();
						// one of the dataset panels has been selected
						// so reload the data displayed on it
						((ProjectsPanel) tabWidget).refreshProjectsList();
					}
				}
			}
		});

		// if the tabPanel is added to RootPanel and not RootLayoutPanel
		// then the contents of the tab panels are not displayed - only the titles
		// RootLayoutPanel.get().add(tabLayoutPanel);

		loginDialog = new DialogBox();
		loginDialog.setText("Login");
		loginDialog.setGlassEnabled(true);
		loginDialog.setAnimationEnabled(true);
		loginDialog.setWidget(loginPanel);
		loginDialog.center(); // includes show()

		// put the cursor in the username box
		loginPanel.username.setFocus(true);

		quincyOutputDialog.setWidget(quincyOutputPanel);
		quincyOutputDialog.hide();

		jobOutputDialog.setWidget(jobStandardOutputPanel);
		jobOutputDialog.hide();

		jobErrorDialog.setWidget(jobErrorOutputPanel);
		jobErrorDialog.hide();

		jobOptionsDialog.setWidget(jobOptionsPanel);
		jobOptionsDialog.hide();

	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public LinkedHashMap<String, ParameterValueType> getMergedDatasetParameterTypeMappings() {
		return mergedDatasetParameterTypeMappings;
	}

	public void setMergedDatasetParameterTypeMappings(
			LinkedHashMap<String, ParameterValueType> mergedDatasetParameterTypeMappings) {
		this.mergedDatasetParameterTypeMappings = mergedDatasetParameterTypeMappings;
	}

}
