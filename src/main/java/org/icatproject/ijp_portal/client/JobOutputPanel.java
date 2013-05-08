package org.icatproject.ijp_portal.client;

import org.icatproject.ijp_portal.client.service.DataService;
import org.icatproject.ijp_portal.client.service.DataServiceAsync;

import org.icatproject.ijp_portal.shared.JobDTO;
import org.icatproject.ijp_portal.shared.PortalUtils;
import org.icatproject.ijp_portal.shared.PortalUtils.OutputType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

public class JobOutputPanel extends Composite {

	// Annotation can be used to change the name of the associated xml file
	// @UiTemplate("JobOutputPanel.ui.xml")
	interface MyUiBinder extends UiBinder<Widget, JobOutputPanel> {
	}

	@UiField
	ScrollPanel jobOutputScrollPanel;

	@UiField
	Label jobOutputLabel;

	@UiField
	Button closeButton;

	private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
	private DataServiceAsync dataService = GWT.create(DataService.class);

	Portal portal;
	DialogBox dialogBox;
	PortalUtils.OutputType outputType;
	String currentJobId;

	Timer outputRefreshTimer = new Timer() {
		public void run() {
			getOutputForJob(currentJobId);
		}
	};

	public JobOutputPanel(Portal portal, DialogBox dialogBox, OutputType outputType) {
		this.portal = portal;
		this.dialogBox = dialogBox;
		this.outputType = outputType;
		initWidget(uiBinder.createAndBindUi(this));

		closeButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				closePopup();
			}
		});

	}

	void closePopup() {
		dialogBox.hide();
		outputRefreshTimer.cancel();
	}

	void getOutputForJob(final String jobId) {
		// save the current job ID so that it can be
		// used when the timer refreshes the output
		currentJobId = jobId;
		AsyncCallback<String> callback = new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
				Window.alert("Server error: " + caught.getMessage());
			}

			public void onSuccess(String result) {
				dialogBox.setText(outputType.toString() + " from Job " + jobId);
				jobOutputLabel.setText(result);
				jobOutputScrollPanel.scrollToBottom();
				// if the job is not COMPLETED refresh the output contents regularly
//				JobDTO selectedJob = portal.jobStatusPanel.selectionModel.getSelectedObject();
				JobDTO requiredJob = null;
				for ( JobDTO job : portal.jobStatusPanel.jobList ) {
					if ( job.getId().equals(currentJobId) ) {
						requiredJob = job;
						break;
					}
				}
				if ( requiredJob == null ) {
					// this should never happen
					Window.alert("Job with id '" + currentJobId + "' not found in Job Status Panel");
				}
				if (!requiredJob.getStatus().equals(PortalUtils.JOB_STATUS_MAPPINGS.get("C"))) {
					// set a repeating timer going with a period of 10 secs
					outputRefreshTimer.scheduleRepeating(10000);
				} else {
					// we are now displaying the whole log file
					// for a completed job so cancel the timer
					outputRefreshTimer.cancel();
				}
			}
		};
		// make the call to the server
		System.out.println("JobOutputPanel: making call to DataService");
		dataService.getJobOutput(portal.getSessionId(), jobId, outputType, callback);
	}

}
