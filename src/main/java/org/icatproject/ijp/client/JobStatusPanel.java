package org.icatproject.ijp.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.icatproject.ijp.client.service.DataService;
import org.icatproject.ijp.client.service.DataServiceAsync;
import org.icatproject.ijp.shared.JobDTO;
import org.icatproject.ijp.shared.JobDTO.Status;
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
	Button jobCancelButton;

	@UiField
	Button jobDeleteButton;

	@UiField
	Button closeButton;

	List<JobDTO> jobList = new ArrayList<JobDTO>();
	final SingleSelectionModel<JobDTO> selectionModel = new SingleSelectionModel<JobDTO>();
	long selectedJobId;
	long previouslySelectedJobId;

	private DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss");

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
				return Long.toString(job.getId());
			}
		};
		jobsTable.addColumn(nameColumn, "Job ID");

		// Add a text column to show the executable
		TextColumn<JobDTO> jobColumn = new TextColumn<JobDTO>() {
			@Override
			public String getValue(JobDTO job) {
				return job.getJobType();
			}
		};
		jobsTable.addColumn(jobColumn, "Name");

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
				return job.getStatus().name();
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
				if (selectedJob.getStatus() == Status.Running
						|| selectedJob.getStatus() == Status.Completed
						|| selectedJob.getStatus() == Status.Cancelled) {
					portal.jobStandardOutputPanel.getOutputForJob(selectedJob.getId());
				} else {
					Window.alert("Job output is only available when the job status is Running, Completed or Cancelled");
				}
			}
		});

		jobErrorButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				JobDTO selectedJob = selectionModel.getSelectedObject();
				// check that the job status is RUNNING or COMPLETED before attempting to show the
				// log file
				if (selectedJob.getStatus() == Status.Running
						|| selectedJob.getStatus() == Status.Completed
						|| selectedJob.getStatus() == Status.Cancelled) {
					portal.jobErrorOutputPanel.getOutputForJob(selectedJob.getId());
				} else {
					Window.alert("Job output is only available when the job status is Running, Completed or Cancelled");
				}
				
			}
		});

		jobCancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				JobDTO selectedJob = selectionModel.getSelectedObject();
				if (selectedJob.getStatus() == Status.Completed
						|| selectedJob.getStatus() == Status.Cancelled) {
					Window.alert("Can't cancel a job with status " + selectedJob.getStatus());
				} else {
					dataService.cancelJob(portal.getSessionId(), selectedJob.getId(),
							new AsyncCallback<Void>() {
								@Override
								public void onFailure(Throwable caught) {
									Window.alert(caught.getMessage());
								}

								@Override
								public void onSuccess(Void arg0) {
									refreshJobList();
								}
							});
				}
			}
		});

		jobDeleteButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				JobDTO selectedJob = selectionModel.getSelectedObject();
				// check that the job status is RUNNING or COMPLETED before attempting to show the
				// log file
				if (selectedJob.getStatus() == Status.Completed
						|| selectedJob.getStatus() == Status.Cancelled) {
					dataService.deleteJob(portal.getSessionId(), selectedJob.getId(),
							new AsyncCallback<Void>() {
								@Override
								public void onFailure(Throwable caught) {
									Window.alert(caught.getMessage());
								}

								@Override
								public void onSuccess(Void arg0) {
									refreshJobList();
								}
							});
				} else {
					Window.alert("Can only delete jobs that are Completed or Cancelled");
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
				jobsTable.setRowData(jobList);
				JobDTO requiredJob = null;
				for (JobDTO job : portal.jobStatusPanel.jobList) {
					if (job.getId() == previouslySelectedJobId) {
						requiredJob = job;
						break;
					}
				}
				if (requiredJob == null) {
					selectionModel.setSelected(jobList.get(0), true);
				} else {
					selectionModel.setSelected(requiredJob, true);
				}
			}
		};

		// make the call to the server
		dataService.getJobsForUser(portal.getSessionId(), callback);

	}

	@Override
	public void onResize() {
		int parentHeight = Window.getClientHeight();
		int scrollPanelHeight = (int) (parentHeight * 0.5);
		jobsScrollPanel.setHeight(scrollPanelHeight + "px");
	}

}
