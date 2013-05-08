package org.icatproject.ijp_portal.client;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.icatproject.ijp_portal.shared.PortalUtils.OutputType;
import org.icatproject.ijp_portal.shared.PortalUtils.ParameterValueType;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Portal implements EntryPoint {
	// Annotation can be used to change the name of the associated xml file
	// @UiTemplate("Portal.ui.xml")
//	interface MyUiBinder extends UiBinder<Widget, Portal> {
//	}

	DatasetsPanel datasetsPanel;

	DialogBox loginDialog;
	LoginPanel loginPanel;

	List<PortalDialogBox> portalDialogBoxes = new ArrayList<PortalDialogBox>();
	 
	PortalDialogBox jobStatusDialog = new PortalDialogBox(this, false, false);
	JobStatusPanel jobStatusPanel;

	PortalDialogBox jobOutputDialog = new PortalDialogBox(this, false, false);
	JobOutputPanel jobStandardOutputPanel;

	PortalDialogBox jobErrorDialog = new PortalDialogBox(this, false, false);
	JobOutputPanel jobErrorOutputPanel;

	PortalDialogBox jobOptionsDialog = new PortalDialogBox(this, false, true);
	JobOptionsPanel jobOptionsPanel;

	private String username;
	private String sessionId;

	private LinkedHashMap<String, ParameterValueType> mergedDatasetParameterTypeMappings = null;

//	private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

	public void onModuleLoad() {

//		RootLayoutPanel.get().add(uiBinder.createAndBindUi(this));
		
		datasetsPanel = new DatasetsPanel(this);
		RootPanel.get().add(datasetsPanel);
		Window.addResizeHandler(new ResizeHandler() {
			public void onResize(ResizeEvent event) {
				datasetsPanel.onResize();
			}
		});

		jobStatusPanel = new JobStatusPanel(this);
		jobStatusDialog.setText("Job Status");
		jobStatusDialog.setWidget(jobStatusPanel);
		jobStatusDialog.hide();

		loginPanel = new LoginPanel(this);

		jobStandardOutputPanel = new JobOutputPanel(this, jobOutputDialog,
				OutputType.STANDARD_OUTPUT);
		jobErrorOutputPanel = new JobOutputPanel(this, jobErrorDialog,
				OutputType.ERROR_OUTPUT);

		jobOptionsPanel = new JobOptionsPanel(this, jobOptionsDialog);

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
		
		datasetsPanel.onResize();
		
		portalDialogBoxes.add(jobStatusDialog);
		portalDialogBoxes.add(jobOutputDialog);
		portalDialogBoxes.add(jobErrorDialog);
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
