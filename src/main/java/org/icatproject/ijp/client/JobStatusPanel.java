package org.icatproject.ijp.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.icatproject.ijp.client.service.DataService;
import org.icatproject.ijp.client.service.DataServiceAsync;
import org.icatproject.ijp.shared.JobDTO;
import org.icatproject.ijp.shared.PortalUtils;
import org.icatproject.ijp.shared.SessionException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

public class JobStatusPanel extends Composite implements RequiresResize {
	// Annotation can be used to change the name of the associated xml file
	// @UiTemplate("JobStatusPanel.ui.xml")
	interface MyUiBinder extends UiBinder<Widget, JobStatusPanel> {
	}

	private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
	private DataServiceAsync dataService = GWT.create(DataService.class);

	@UiField
	ScrollPanel jobsScrollPanel;

	@UiField
	CellTable<JobDTO> jobsTable;

	@UiField
	Button refreshButton;

	@UiField
	Button jobOutputButton;

	@UiField
	Button jobErrorButton;

	@UiField
	Button closeButton;

	List<JobDTO> jobList = new ArrayList<JobDTO>();
	final SingleSelectionModel<JobDTO> selectionModel = new SingleSelectionModel<JobDTO>();
	String selectedJobId;
	String previouslySelectedJobId;

	private DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat("dd-MM-yyyy HH:mm:ss");

	Timer tableRefreshTimer = new Timer() {
		public void run() {
			refreshJobList();
		}
	};

	Portal portal;

	public JobStatusPanel(final Portal portal) {
		this.portal = portal;
		initWidget(uiBinder.createAndBindUi(this));

		closeButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				tableRefreshTimer.cancel();
				portal.jobStatusDialog.hide();
			}
		});

		// Add a text column to show the Job ID
		TextColumn<JobDTO> nameColumn = new TextColumn<JobDTO>() {
			@Override
			public String getValue(JobDTO job) {
				return job.getId();
			}
		};
		jobsTable.addColumn(nameColumn, "Job ID");

		// Add a text column to show the worker node
		TextColumn<JobDTO> workerNodeColumn = new TextColumn<JobDTO>() {
			@Override
			public String getValue(JobDTO job) {
				return job.getWorkerNode();
			}
		};
		jobsTable.addColumn(workerNodeColumn, "Worker Node");

		// Add a text column to show the worker node
		TextColumn<JobDTO> batchFilenameColumn = new TextColumn<JobDTO>() {
			@Override
			public String getValue(JobDTO job) {
				return job.getBatchFilename();
			}
		};
		jobsTable.addColumn(batchFilenameColumn, "Batch Filename");

		// Add a text column to show the Submitted timestamp
		TextColumn<JobDTO> submittedColumn = new TextColumn<JobDTO>() {
			@Override
			public String getValue(JobDTO job) {
				Date submitDate = job.getSubmitDate();
				return dateTimeFormat.format(submitDate);
			}
		};
		jobsTable.addColumn(submittedColumn, "Submitted");

		// Add a text column to show the Job Status
		TextColumn<JobDTO> statusColumn = new TextColumn<JobDTO>() {
			@Override
			public String getValue(JobDTO job) {
				return job.getStatus();
			}
		};
		jobsTable.addColumn(statusColumn, "Status");

		// configure the selection model to handle user selection of jobs
		jobsTable.setSelectionModel(selectionModel);

		refreshButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				refreshJobList();
			}
		});

		jobOutputButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				JobDTO selectedJob = selectionModel.getSelectedObject();
				// check that the job status is RUNNING or COMPLETED before attempting to show the
				// log file
				if (selectedJob.getStatus().equals(PortalUtils.JOB_STATUS_MAPPINGS.get("R"))
						|| selectedJob.getStatus().equals(PortalUtils.JOB_STATUS_MAPPINGS.get("C"))) {
					portal.jobOutputDialog.show();
					portal.jobStandardOutputPanel.getOutputForJob(selectedJob.getId());
				} else {
					Window.alert("Job output is only available when the job status is RUNNING or COMPLETED");
				}
				// ensure this dialog is in front of others that are open
				portal.jobOutputDialog.bringToFront();
			}
		});

		jobErrorButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				JobDTO selectedJob = selectionModel.getSelectedObject();
				// check that the job status is RUNNING or COMPLETED before attempting to show the
				// log file
				if (selectedJob.getStatus().equals(PortalUtils.JOB_STATUS_MAPPINGS.get("R"))
						|| selectedJob.getStatus().equals(PortalUtils.JOB_STATUS_MAPPINGS.get("C"))) {
					portal.jobErrorDialog.show();
					portal.jobErrorOutputPanel.getOutputForJob(selectedJob.getId());
				} else {
					Window.alert("Job output is only available when the job status is RUNNING or COMPLETED");
				}
				// ensure this dialog is in front of others that are open
				portal.jobErrorDialog.bringToFront();
			}
		});

		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			public void onSelectionChange(SelectionChangeEvent event) {
				// store the selected job ID each time a new job is selected
				// this is used for reselecting this job when the job table is refreshed
				selectedJobId = selectionModel.getSelectedObject().getId();
			}
		});

	}

	void refreshJobList() {
		// set up the callback object
		AsyncCallback<List<JobDTO>> callback = new AsyncCallback<List<JobDTO>>() {
			public void onFailure(Throwable caught) {
				// deal with possible exceptions
				System.err.println("JobStatusPanel.refreshJobList(): " + caught.getMessage());
				if (caught.getClass() == SessionException.class) {
					System.err.println("caught is a SessionException");
					portal.loginPanel.setMessageText(caught.getMessage());
					portal.loginDialog.show();
				} else {
					Window.alert("Server error: " + caught.getMessage());
				}
			}

			public void onSuccess(List<JobDTO> result) {
				previouslySelectedJobId = selectedJobId;
				jobList = result;
				// set the page size to the number of projects returned
				// otherwise we only see the first 15 by default
				jobsTable.setPageSize(jobList.size());
				// push the data into the widget.
				jobsTable.setRowData(0, jobList);
				JobDTO requiredJob = null;
				for (JobDTO job : portal.jobStatusPanel.jobList) {
					if (job.getId().equals(previouslySelectedJobId)) {
						requiredJob = job;
						break;
					}
				}
				if (requiredJob == null) {
					// this should never happen but for some reason the
					// previously selected job is no longer in the table
					// so just select the first job instead
					selectionModel.setSelected(jobList.get(0), true);
				} else {
					selectionModel.setSelected(requiredJob, true);
				}
			}
		};

		// make the call to the server
		System.out.println("JobStatusPanel: making call to DataService");
		dataService.getJobsForUser(portal.getSessionId(), callback);

	}

	@Override
	public void onResize() {
		int parentHeight = Window.getClientHeight();
		int scrollPanelHeight = (int) (parentHeight * 0.5);
		jobsScrollPanel.setHeight(scrollPanelHeight + "px");
	}

}
