package org.icatproject.ijp_portal.client;

import java.util.LinkedHashMap;

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

	DialogBox loginDialog;
	LoginPanel loginPanel;

	DialogBox jobOutputDialog = new DialogBox(false, false);
	JobOutputPanel jobStandardOutputPanel;

	DialogBox jobErrorDialog = new DialogBox(false, false);
	JobOutputPanel jobErrorOutputPanel;

	DialogBox jobOptionsDialog = new DialogBox(false, true);
	JobOptionsPanel jobOptionsPanel;

	DatasetsPanel datasetsPanel;

	JobStatusPanel jobStatusPanel;

	private String username;
	private String sessionId;

	private LinkedHashMap<String, ParameterValueType> mergedDatasetParameterTypeMappings = null;

	private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

	public void onModuleLoad() {

		RootLayoutPanel.get().add(uiBinder.createAndBindUi(this));

		loginPanel = new LoginPanel(this);

		jobStandardOutputPanel = new JobOutputPanel(this, jobOutputDialog,
				OutputType.STANDARD_OUTPUT);
		jobErrorOutputPanel = new JobOutputPanel(this, jobErrorDialog,
				OutputType.ERROR_OUTPUT);

		jobOptionsPanel = new JobOptionsPanel(this, jobOptionsDialog);

		datasetsPanel = new DatasetsPanel(this);
		datasetsPanel.setVisible(true);

		jobStatusPanel = new JobStatusPanel(this);
		jobStatusPanel.setVisible(true);

		tabLayoutPanel.add(datasetsPanel, "Datasets");
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
					} else {
						jobStatusPanel.tableRefreshTimer.cancel();
					}
				}
			}
		});

		loginDialog = new DialogBox();
		loginDialog.setText("Login");
		loginDialog.setGlassEnabled(true);
		loginDialog.setAnimationEnabled(true);
		loginDialog.setWidget(loginPanel);
		loginDialog.center(); // includes show()

		// put the cursor in the username box
		loginPanel.username.setFocus(true);

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
