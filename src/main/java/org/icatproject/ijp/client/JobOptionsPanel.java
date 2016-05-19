package org.icatproject.ijp.client;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.icatproject.ijp.client.parser.ExpressionEvaluator;
import org.icatproject.ijp.client.parser.ParserException;
import org.icatproject.ijp.client.service.DataService;
import org.icatproject.ijp.client.service.DataServiceAsync;
import org.icatproject.ijp.shared.DatafileOverview;
import org.icatproject.ijp.shared.DatasetOverview;
import org.icatproject.ijp.shared.PortalUtils;
import org.icatproject.ijp.shared.PortalUtils.MultiJobTypes;
import org.icatproject.ijp.shared.xmlmodel.JobOption;
import org.icatproject.ijp.shared.xmlmodel.JobType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.LongBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class JobOptionsPanel extends VerticalPanel {

	private DataServiceAsync dataService = GWT.create(DataService.class);

	Portal portal;
	DialogBox dialogBox;

	CheckBox confirmMultipleCheckBox = new CheckBox();
	ListBox multipleDatasetsOptionListBox = new ListBox();

	Button closeButton = new Button("Close");
	Button submitButton = new Button("Submit");

	Map<JobOption, Widget> jobOptionToFormWidgetMap;

	CellTable<String> submittedJobsTable = new CellTable<String>();
	List<String> submittedJobsList;

	int jobIdCounter = 0;

	public JobOptionsPanel(final Portal portal, final DialogBox dialogBox) {
		this.portal = portal;
		this.dialogBox = dialogBox;

		IjpResources.INSTANCE.css().ensureInjected();

		multipleDatasetsOptionListBox.addItem("Please select...", "");
		multipleDatasetsOptionListBox.addItem("Submit multiple datasets/datafiles to one job",
				MultiJobTypes.MULTIPLE_DATASETS_ONE_JOB.name());
		multipleDatasetsOptionListBox.addItem("Run multiple jobs - one dataset/datafile per job",
				MultiJobTypes.ONE_DATASET_PER_JOB.name());

		closeButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				dialogBox.hide();
			}
		});

		// Add a text column to show the Job ID
		TextColumn<String> jobIdColumn = new TextColumn<String>() {
			@Override
			public String getValue(String jobId) {
				return jobId;
			}
		};
		submittedJobsTable.addColumn(jobIdColumn, "Submitted Job IDs");

		submitButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				List<String> optionsList = new ArrayList<String>();
				List<String> formErrors = new ArrayList<String>();
				// most jobs are of this type - change it further down in
				// specific cases
				PortalUtils.MultiJobTypes multiJobType = MultiJobTypes.ONE_DATASET_PER_JOB;

				String jobName = portal.datasetsPanel.jobTypeListBox.getValue(portal.datasetsPanel.jobTypeListBox
						.getSelectedIndex());
				JobType jobType = portal.datasetsPanel.jobTypeMappings.getJobTypesMap().get(jobName);
				int numSelectedDatasets = portal.datasetsPanel.selectedDatasets.size();
				int numSelectedDatafiles = portal.datasetsPanel.selectedDatafiles.size();
				if (numSelectedDatasets + numSelectedDatafiles > 1) {
					if (jobType.getType().equalsIgnoreCase("interactive")) {
						multiJobType = MultiJobTypes.MULTIPLE_DATASETS_ONE_JOB;
					} else if (jobType.getType().equalsIgnoreCase("batch")) {
						if (jobType.getMultiple() == false) {
							if (confirmMultipleCheckBox.getValue() == false) {
								formErrors
										.add("Please tick the box at the top of the form to confirm your intention to run multiple jobs");
							}
						} else {
							if (multipleDatasetsOptionListBox.getSelectedIndex() == 0) {
								formErrors
										.add("Please select the type of job you intend to run at the top of the form");
							} else {
								multiJobType = MultiJobTypes.valueOf(multipleDatasetsOptionListBox
										.getValue(multipleDatasetsOptionListBox.getSelectedIndex()));
							}
						}
					}
				}

				for (JobOption jobOption : jobOptionToFormWidgetMap.keySet()) {
					// firstly check that this is a program parameter we can set
					// the standard "View" option for MSMM Viewer appears on the
					// form but no
					// parameters
					// get added to the command line if it is selected, for
					// example
					if (jobOption.getProgramParameter() != null && !jobOption.getProgramParameter().equals("")) {
						Widget formWidget = jobOptionToFormWidgetMap.get(jobOption);
						if (formWidget.getClass() == RadioButton.class || formWidget.getClass() == CheckBox.class) {
							// we can do this because RadioButton extends
							// CheckBox
							if (((CheckBox) formWidget).getValue() == true) {
								optionsList.add(jobOption.getProgramParameter());
							}
						} else if (formWidget.getClass() == ListBox.class) {
							String selectedListBoxValue = ((ListBox) formWidget).getValue(((ListBox) formWidget)
									.getSelectedIndex());
							if (selectedListBoxValue != null && !selectedListBoxValue.equals("")) {
								optionsList.add(jobOption.getProgramParameter());
								optionsList.add(selectedListBoxValue);
							}
						} else if (formWidget.getClass() == TextBox.class) {
							String textBoxValue = ((TextBox) formWidget).getValue();
							if (!textBoxValue.equals("")) {
								optionsList.add(jobOption.getProgramParameter());
								optionsList.add(textBoxValue);
							}
						} else if (formWidget.getClass() == LongBox.class || formWidget.getClass() == DoubleBox.class) {
							Number numericBoxValueNumber = (Number) ((ValueBox) formWidget).getValue();
							if (numericBoxValueNumber != null) {
								double numericBoxValueDouble = numericBoxValueNumber.doubleValue();
								String minValueString = jobOption.getMinValue();
								if (minValueString != null && !minValueString.equals("")) {
									// TODO - should these parse checks have
									// already been done when
									// reading the XML for example?
									try {
										double minValueDouble = Double.parseDouble(minValueString);
										if (numericBoxValueDouble < minValueDouble) {
											formErrors.add("Submitted value '" + numericBoxValueDouble
													+ "' for job option '" + jobOption.getName()
													+ "' must be greater than '" + minValueString + "'");
										}
									} catch (NumberFormatException e) {
										formErrors.add("minValue '" + minValueString + "' of job option '"
												+ jobOption.getName() + "' is not a valid number");
									}
								}
								String maxValueString = jobOption.getMaxValue();
								if (maxValueString != null && !maxValueString.equals("")) {
									// TODO - should these parse checks have
									// already been done when
									// reading the XML for example?
									try {
										double maxValueDouble = Double.parseDouble(maxValueString);
										if (numericBoxValueDouble > maxValueDouble) {
											formErrors.add("Submitted value '" + numericBoxValueDouble
													+ "' for job option '" + jobOption.getName()
													+ "' must be less than '" + maxValueString + "'");
										}
									} catch (NumberFormatException e) {
										formErrors.add("maxValue '" + maxValueString + "' of job option '"
												+ jobOption.getName() + "' is not a valid number");
									}
								}
								optionsList.add(jobOption.getProgramParameter());
								optionsList.add(String.valueOf(numericBoxValueDouble));
							}
						}
					}
				}
				if (formErrors.size() > 0) {
					// show all the errors in one alert message
					StringBuilder formErrorsMessage = new StringBuilder(
							"Job cannot be submitted due to the following errors:\n");
					for (String formError : formErrors) {
						formErrorsMessage.append(" - ");
						formErrorsMessage.append(formError);
						formErrorsMessage.append("\n");
					}
					Window.alert(formErrorsMessage.toString());
				} else {
					// The job is "job-only" (takes no datasets or datafiles as
					// inputs) if it explicitly says so,
					// or if it contains no datasetTypes at all
					boolean isJobOnlyJob = jobType.getDatasetTypes().contains(
							DatasetsPanel.DATASET_TYPES_LIST_JOB_ONLY_OPTION)
							|| jobType.getDatasetTypes().isEmpty();
					List<String> datasetIdsList = new ArrayList<String>();
					List<String> datafileIdsList = new ArrayList<String>();
					for (DatasetOverview selectedDataset : portal.datasetsPanel.selectedDatasets) {
						String datasetId = Long.toString(selectedDataset.getDatasetId());
						datasetIdsList.add(datasetId);
					}
					for (DatafileOverview selectedDatafile : portal.datasetsPanel.selectedDatafiles) {
						datafileIdsList.add(Long.toString(selectedDatafile.getId()));
					}
					if (isJobOnlyJob) {
						// there can be no datasets selected for this type of
						// job
						// but we need to add one entry that is null so that the
						// loop
						// for submitBatch further down gets executed once
						datasetIdsList.add(null);
					} else if (multiJobType == MultiJobTypes.MULTIPLE_DATASETS_ONE_JOB) {
						// create a comma separated string from the dataset IDs
						// empty the existing datasetIdsList and
						// put the comma separated string as a single entry
						String datasetIdsString = PortalUtils.createStringFromList(datasetIdsList, ",");
						datasetIdsList = new ArrayList<String>();
						datasetIdsList.add(datasetIdsString);
						// Do the same for the datafileIds
						String datafileIdsString = PortalUtils.createStringFromList(datafileIdsList, ",");
						datafileIdsList = new ArrayList<String>();
						datafileIdsList.add(datafileIdsString);
					}

					if (jobType.getType().equalsIgnoreCase("interactive")) {
						List<String> parameters = new ArrayList<String>();
						// add any dataset IDs as the first item in the
						// parameters list
						// but only do this if the job type is not "job only"
						// (no datasets required)
						if (!isJobOnlyJob) {
							// We may have only datafileIds or only datasetIds,
							// so need to test for empty lists
							if (datasetIdsList.size() > 0) {
								parameters.add("--datasetIds=" + PortalUtils.createStringFromList(datasetIdsList, ","));
							}
							if (datafileIdsList.size() > 0) {
								parameters.add("--datafileIds="
										+ PortalUtils.createStringFromList(datafileIdsList, ","));
							}
						}
						parameters.addAll(optionsList);

						dataService.submitInteractive(portal.getSessionId(), jobType, parameters,
								new AsyncCallback<String>() {

									@Override
									public void onFailure(Throwable caught) {
										Window.alert("Server error: " + caught.getMessage());
									}

									@Override
									public void onSuccess(String json) {

										try {
											JSONObject js = new JSONObject(JsonUtils.safeEval(json));
											JSONValue rdpv = js.get("rdp");
											if (rdpv != null) {
												JSONObject rdp = rdpv.isObject();
												String host = rdp.get("host").isString().stringValue();
												String account = rdp.get("username").isString().stringValue();
												String password = rdp.get("password").isString().stringValue();
												if (Window.Navigator.getPlatform().startsWith("Win")) {
													portal.datasetsPanel.hostNameField.setValue(host);
													portal.datasetsPanel.accountNameField.setValue(account);
													portal.datasetsPanel.rdpForm.submit();
													Window.alert("Execute the downloaded file and specify '" + password
															+ "' as the password. The password has a short lifetime!");
												} else {
													Window.alert("Please paste into a terminal:     rdesktop -u "
															+ account + " -p " + password + " " + host);
												}
												return;
											} else {
												Window.alert("Bad response from batch service " + json);
											}
										} catch (IllegalArgumentException e) {
											Window.alert("Bad response from batch service " + json);
										}

									}
								});
					} else if (jobType.getType().equalsIgnoreCase("batch")) {

						jobIdCounter = 0;
						submittedJobsTable.setVisible(true);
						submittedJobsList = new ArrayList<String>();
						for (String datasetId : datasetIdsList) {
							// If we are running one job for all
							// datasets/datafiles,
							// we only need one entry, and we will get it from
							// the single value in datasetIdsList
							submittedJobsList.add("Waiting for job ID...");
						}
						if (multiJobType != MultiJobTypes.MULTIPLE_DATASETS_ONE_JOB) {
							// Will need a separate entry for each datafile too
							for (String datafileId : datafileIdsList) {
								submittedJobsList.add("Waiting for job ID...");
							}
						}
						submittedJobsTable.setRowData(0, submittedJobsList);
						submittedJobsTable.setRowCount(submittedJobsList.size());

						for (String datasetId : datasetIdsList) {
							// add the dataset ID as the first item in the
							// parameters list
							List<String> parameters = new ArrayList<String>();
							// only add a dataset ID if the job type is not
							// "job only" (no datasets/datafiles required)
							if (!isJobOnlyJob) {
								parameters.add("--datasetIds=" + datasetId);
								// If we're running a single job for all
								// datasets/datafiles, then this is it,
								// and we need to add the comma-separated list
								// of datafileIds too, if there is one
								if (multiJobType == MultiJobTypes.MULTIPLE_DATASETS_ONE_JOB
										&& datafileIdsList.size() > 0) {
									parameters.add("--datafileIds=" + datafileIdsList.get(0));
								}
							}
							parameters.addAll(optionsList);

							dataService.submitBatch(portal.getSessionId(), jobType, parameters,
									new AsyncCallback<String>() {

										@Override
										public void onFailure(Throwable caught) {
											Window.alert("Server error: " + caught.getMessage());
										}

										@Override
										public void onSuccess(String json) {
											// Window.alert(message);
											// message should now be a JSON string - extract jobId from it
											try {
												JSONObject js = new JSONObject(JsonUtils.safeEval(json));
												String jobId = js.get("jobId").isString().stringValue();
												submittedJobsList.set(jobIdCounter, jobId);
												submittedJobsTable.setRowData(0, submittedJobsList);
												jobIdCounter++;
											} catch (IllegalArgumentException e) {
												Window.alert("Bad response from batch service: " + json);
											}
										}
									});
						}
						// If we're running separate jobs, will need to repeat
						// for the datafileIds as well
						if (multiJobType != MultiJobTypes.MULTIPLE_DATASETS_ONE_JOB) {
							for (String datafileId : datafileIdsList) {
								// add the datafile ID as the first item in the
								// parameters list
								List<String> parameters = new ArrayList<String>();
								parameters.add("--datafileIds=" + datafileId);
								parameters.addAll(optionsList);

								dataService.submitBatch(portal.getSessionId(), jobType, parameters,
										new AsyncCallback<String>() {

											@Override
											public void onFailure(Throwable caught) {
												Window.alert("Server error: " + caught.getMessage());
											}

											@Override
											public void onSuccess(String message) {
												// Window.alert(message);
												submittedJobsList.set(jobIdCounter, message);
												submittedJobsTable.setRowData(0, submittedJobsList);
												jobIdCounter++;
											}
										});
							}
						}
					} else {
						Window.alert("Error: job '" + jobName + "' is of an unrecognised type - '" + jobType.getType()
								+ "'");
					}
				}
			}
		});
	}

	void populateAndShowForm() {
		// clear the current form
		clear();
		confirmMultipleCheckBox.setValue(false);
		multipleDatasetsOptionListBox.setSelectedIndex(0);

		// get a list of selected dataset ids
		// TODO modify dataService to take datafileIds too?
		List<Long> selectedDatasetIds = new ArrayList<Long>();
		for (DatasetOverview selectedDataset : portal.datasetsPanel.selectedDatasets) {
			selectedDatasetIds.add(selectedDataset.getDatasetId());
		}
		String datasetType = portal.datasetsPanel.datasetTypeListBox.getValue(portal.datasetsPanel.datasetTypeListBox
				.getSelectedIndex());

		dataService.getJobDatasetParametersForDatasets(portal.getSessionId(), datasetType, selectedDatasetIds,
				new AsyncCallback<Map<Long, Map<String, Object>>>() {
					@Override
					public void onFailure(Throwable caught) {
						Window.alert("Server error: " + caught.getMessage());
					}

					@Override
					public void onSuccess(Map<Long, Map<String, Object>> jobDatasetParametersForDatasets) {
						String jobName = portal.datasetsPanel.jobTypeListBox
								.getValue(portal.datasetsPanel.jobTypeListBox.getSelectedIndex());
						jobOptionToFormWidgetMap = new LinkedHashMap<JobOption, Widget>();
						Map<String, HorizontalPanel> nameToPanelMap = new LinkedHashMap<String, HorizontalPanel>();
						JobType jobType = portal.datasetsPanel.jobTypeMappings.getJobTypesMap().get(jobName);

						for (JobOption jobOption : jobType.getJobOptions()) {
							// firstly work out if this option should be
							// available for the selected
							// dataset
							boolean makeOptionAvailable = false;
							String condition = jobOption.getCondition();
							if (condition == null || condition.equals("")) {
								// options with an empty condition are offered
								// for all datasets of
								// this type
								makeOptionAvailable = true;
							} else {
								// first check that the call to the server was
								// successful
								if (jobDatasetParametersForDatasets != null) {
									// options with a non-empty condition are
									// only offered if the
									// condition is met
									// and the condition has to be met for all
									// of the selected
									// datasets
									// for ( DatasetOverview selectedDataset :
									// portal.datasetsPanelNew.selectionModel.getSelectedSet()
									// ) {
									for (Long selectedDatasetId : jobDatasetParametersForDatasets.keySet()) {
										boolean conditionMatch = false;
										try {
											conditionMatch = ExpressionEvaluator.isTrue(condition,
													jobDatasetParametersForDatasets.get(selectedDatasetId));
										} catch (ParserException e) {
											Window.alert("ParserException: " + e.getMessage());
										}
										if (conditionMatch) {
											makeOptionAvailable = true;
										} else {
											makeOptionAvailable = false;
											// Window.alert("Condition '" +
											// condition
											// + "' for Job Option '" +
											// jobOption.getName()
											// + "' not met for Dataset "
											// +
											// selectedDataset.getDatasetId());
											// no need to check any more
											// datasets so stop looping
											// now
											break;
										}
									}
								}
							}

							if (makeOptionAvailable) {
								HorizontalPanel hp = new HorizontalPanel();
								boolean optionLabelRequired = true;
								String optionName = jobOption.getName();
								Widget formWidget = null;
								if (jobOption.getType().equals("boolean")) {
									if (jobOption.getGroupName() != null && !jobOption.getGroupName().equals("")) {
										RadioButton radioButton = new RadioButton(jobOption.getGroupName(), jobOption
												.getName());
										HorizontalPanel existingPanel = nameToPanelMap.get(jobOption.getGroupName());
										if (existingPanel != null) {
											// set this as the panel we are
											// going to add to - it
											// already has a label
											hp = existingPanel;
											// the panel we are adding to will
											// already have a label
											// so set a flag so that another one
											// is not added
											optionLabelRequired = false;
										} else {
											// this is the first of a group of
											// buttons so check this
											// one
											radioButton.setValue(true);
										}
										// for radio buttons use the group name
										// for the name of the
										// option
										optionName = jobOption.getGroupName();
										formWidget = radioButton;
									} else {
										formWidget = new CheckBox();
									}
								} else if (jobOption.getType().equals("enumeration")) {
									ListBox listBox = new ListBox();
									for (String value : jobOption.getValues()) {
										listBox.addItem(value);
									}
									formWidget = listBox;
								} else if (jobOption.getType().equals("string")) {
									formWidget = new TextBox();
								} else if (jobOption.getType().equals("integer")) {
									formWidget = new LongBox();
								} else if (jobOption.getType().equals("float")) {
									formWidget = new DoubleBox();
								}

								// add an option label and a tool tip if needed
								if (optionLabelRequired) {
									HTML optionNameHTML = new HTML("<b>" + optionName + "</b>&nbsp;");
									hp.add(optionNameHTML);
									if (jobOption.getTip() != null && !jobOption.getTip().equals("")) {
										optionNameHTML.setTitle(jobOption.getTip());
									}
								}

								// add the form element itself
								hp.add(formWidget);
								nameToPanelMap.put(optionName, hp);
								jobOptionToFormWidgetMap.put(jobOption, formWidget);

								// add any extra info - default, min, max values
								// etc
								if (jobOption.getDefaultValue() != null && !jobOption.getDefaultValue().equals("")) {
									hp.add(new HTML("&nbsp;<i>(default=" + jobOption.getDefaultValue() + ")</i>"));
								}
								if (jobOption.getMinValue() != null && !jobOption.getMinValue().equals("")) {
									hp.add(new HTML("&nbsp;<i>(min=" + jobOption.getMinValue() + ")</i>"));
								}
								if (jobOption.getMaxValue() != null && !jobOption.getMaxValue().equals("")) {
									hp.add(new HTML("&nbsp;<i>(max=" + jobOption.getMaxValue() + ")</i>"));
								}
							}
						}

						int numSelectedDatasets = portal.datasetsPanel.selectedDatasets.size();
						int numSelectedDatafiles = portal.datasetsPanel.selectedDatafiles.size();
						int numSelectedSetsOrFiles = numSelectedDatasets + numSelectedDatafiles;
						if (jobType.getType().equalsIgnoreCase("batch") && numSelectedSetsOrFiles > 1) {
							HorizontalPanel warningPanel = new HorizontalPanel();
							if (jobType.getMultiple() == false) {
								// warn user that they are submitting multiple
								// jobs
								// get them to tick a checkbox to confirm
								warningPanel.add(new HTML("<font color='red'><b>"
										+ "Please tick the box to confirm that you are intending to run <br> "
										+ numSelectedSetsOrFiles + " '" + jobType.getName()
										+ "' jobs - one job per selected dataset/datafile  &nbsp;</b></font>"));
								warningPanel.add(confirmMultipleCheckBox);
							} else {
								// warn the user and offer the following two
								// options
								// multiple datasets to one job or separate job
								// for each dataset
								warningPanel.add(new HTML("<font color='red'><b>"
										+ "Please select the type of job you intend to run &nbsp;</b></font>"));
								warningPanel.add(multipleDatasetsOptionListBox);
							}
							add(warningPanel);
							add(new HTML("<hr></hr>"));
						}

						for (String name : nameToPanelMap.keySet()) {
							HorizontalPanel hp = nameToPanelMap.get(name);
							add(hp);
							add(new HTML("<hr></hr>"));
						}

						HorizontalPanel footerPanel = new HorizontalPanel();
						footerPanel.setWidth("100%");
						footerPanel.add(submitButton);
						footerPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
						footerPanel.add(closeButton);
						add(footerPanel);

						submittedJobsTable.setVisible(false);
						submittedJobsTable.addStyleName(IjpResources.INSTANCE.css().scrollPanel());
						HorizontalPanel jobsPanel = new HorizontalPanel();
						// jobsPanel.setBorderWidth(1);
						jobsPanel.setWidth("100%");
						jobsPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
						jobsPanel.add(submittedJobsTable);
						add(jobsPanel);

						portal.jobOptionsDialog.setText(jobName + " Options");
						portal.jobOptionsDialog.center();
					}
				});

	}

}
