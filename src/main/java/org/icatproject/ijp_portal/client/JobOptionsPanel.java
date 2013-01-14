package org.icatproject.ijp_portal.client;

import java.util.LinkedHashMap;
import java.util.Map;

import org.icatproject.ijp_portal.client.parser.ExpressionEvaluator;
import org.icatproject.ijp_portal.client.parser.ParserException;
import org.icatproject.ijp_portal.client.service.DataService;
import org.icatproject.ijp_portal.client.service.DataServiceAsync;
import org.icatproject.ijp_portal.shared.DatasetOverview;
import org.icatproject.ijp_portal.shared.xmlmodel.JobOption;
import org.icatproject.ijp_portal.shared.xmlmodel.JobType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.LongBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.ValueBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class JobOptionsPanel extends VerticalPanel {

	private DataServiceAsync dataService = GWT.create(DataService.class);

	Portal portal;
	DialogBox dialogBox;

	Button closeButton = new Button("Close");
	Button submitButton = new Button("Submit");

	String executablePath;
	
	Map<JobOption, Widget> jobOptionToFormWidgetMap;
	
	public JobOptionsPanel(final Portal portal, final DialogBox dialogBox) {
		this.portal = portal;
		this.dialogBox = dialogBox;
		
		addCloseButton();

		closeButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				dialogBox.hide();
				portal.datasetsPanelNew.datasetActionListBox.setSelectedIndex(0);
			}
		});
	
		submitButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				String commandString = executablePath;
				DatasetOverview datasetOverview = portal.datasetsPanelNew.selectionModel.getSelectedObject();
				commandString += " --datasetId " + datasetOverview.getDatasetId();
				for ( JobOption jobOption : jobOptionToFormWidgetMap.keySet() ) {
					// firstly check that this is a program parameter we can set
					// the standard "View" option for MSMM Viewer appears on the form but no parameters
					// get added to the command line if it is selected, for example
					if ( jobOption.getProgramParameter() != null && !jobOption.getProgramParameter().equals("") ) {
						Widget formWidget = jobOptionToFormWidgetMap.get(jobOption);
						if (formWidget.getClass() == RadioButton.class || formWidget.getClass() == CheckBox.class) {
	//						formString += jobOption.programParameter + ":" + ((RadioButton)formWidget).getValue() + "\n";
	//						formString += jobOption.programParameter + ":" + ((CheckBox)formWidget).getValue() + "\n";
							// we can do this because RadioButton extends CheckBox
							if ( ((CheckBox)formWidget).getValue() == true ) {
								commandString += " " + jobOption.getProgramParameter();
							}
						} else if (formWidget.getClass() == ListBox.class) {
	//						formString += jobOption.programParameter + ":" + ((ListBox)formWidget).getValue(((ListBox)formWidget).getSelectedIndex()) + "\n";
							String selectedListBoxValue = ((ListBox)formWidget).getValue(((ListBox)formWidget).getSelectedIndex());
							if ( selectedListBoxValue != null && !selectedListBoxValue.equals("") ) {
								commandString += " " + jobOption.getProgramParameter() + " " + selectedListBoxValue;
							}
						} else if (formWidget.getClass() == LongBox.class || formWidget.getClass() == DoubleBox.class) {
//							formString += jobOption.programParameter + ":" + ((LongBox)formWidget).getValue() + "\n";
//							formString += jobOption.programParameter + ":" + ((DoubleBox)formWidget).getValue() + "\n";
							Object numericBoxValueObject = ((ValueBox)formWidget).getValue();
							if ( numericBoxValueObject != null ) {
								commandString += " " + jobOption.getProgramParameter() + " " + numericBoxValueObject.toString();
							}
						}
					}
				}
				Window.alert(commandString);
			}
		});
	}

	void populateAndShowForm(String jobName) {
		// firstly clear the current form
		clear();
		jobOptionToFormWidgetMap = new LinkedHashMap<JobOption, Widget>();
		Map<String, HorizontalPanel> nameToPanelMap = new LinkedHashMap<String, HorizontalPanel>();
		JobType jobType = portal.datasetsPanelNew.jobTypeMappings.getJobTypesMap().get(jobName);
		// set the executable to be used when the form is submitted
		executablePath = jobType.getExecutable();
		for (JobOption jobOption : jobType.getJobOptions()) {
			// firstly work out if this option should be available for the selected dataset
			boolean makeOptionAvailable = false;
			String condition = jobOption.getCondition();
			if ( condition == null || condition.equals("") ) {
				// options with an empty condition are offered for all datasets of this type
//						datasetActionListBox.addItem( jobType.name + " -> " + jobOption.name );
				makeOptionAvailable = true;
			} else {
				// options with a non-empty condition are only offered if the condition is met
				boolean conditionMatch = false;
				try {
					conditionMatch = ExpressionEvaluator.isTrue(condition, portal.datasetsPanelNew.selectionModel.getSelectedObject().getJobDatasetParameters());
				} catch (ParserException e) {
					Window.alert("ParserException: " + e.getMessage());
				}
				if ( conditionMatch ) {
//							datasetActionListBox.addItem( jobType.name + " -> " + jobOption.name );
					makeOptionAvailable = true;
				}
			}
			
			if (makeOptionAvailable) {
				HorizontalPanel hp = new HorizontalPanel();
				if ( jobOption.getType().equals("boolean") ) {
					if ( jobOption.getGroupName() != null && !jobOption.getGroupName().equals("") ) {
						RadioButton radioButton = new RadioButton(jobOption.getGroupName(), jobOption.getName());
						HorizontalPanel existingPanel = nameToPanelMap.get(jobOption.getGroupName());
						if ( existingPanel != null ) {
							// set this as the panel we are going to add to - it already has a label
							hp = existingPanel;
						} else {
							// this is a new panel and needs a label
							hp.add(new HTML("<b><i>" + jobOption.getGroupName() + "</i></b>&nbsp;") );
							// this is the first of a group of buttons so check this one
							radioButton.setValue(true);
							// also add it to the map so it can be added to later
							nameToPanelMap.put(jobOption.getGroupName(), hp);
						}
						hp.add(radioButton);
						jobOptionToFormWidgetMap.put(jobOption, radioButton);
					} else {
						CheckBox checkBox = new CheckBox();
						hp.add(new HTML("<b><i>" + jobOption.getName() + "</i></b>&nbsp;") );
						hp.add(checkBox);
						nameToPanelMap.put(jobOption.getName(), hp);
						jobOptionToFormWidgetMap.put(jobOption, checkBox);
					}
				} else if (jobOption.getType().equals("enumeration") ) {
					ListBox listBox = new ListBox();
					for (String value : jobOption.getValues() ) {
						listBox.addItem(value);
					}
					hp.add(new HTML("<b><i>" + jobOption.getName() + "</i></b>&nbsp;") );
					hp.add(listBox);
					nameToPanelMap.put(jobOption.getName(), hp);
					jobOptionToFormWidgetMap.put(jobOption, listBox);
				} else if (jobOption.getType().equals("integer") ) {
					LongBox longBox = new LongBox();
					hp.add(new HTML("<b><i>" + jobOption.getName() + "</i></b>&nbsp;") );
					hp.add(longBox);
					if ( jobOption.getDefaultValue() != null && !jobOption.getDefaultValue().equals("") ) {
						hp.add(new HTML("&nbsp;<i>(default=" + jobOption.getDefaultValue() + ")</i>"));
					}
					nameToPanelMap.put(jobOption.getName(), hp);
					jobOptionToFormWidgetMap.put(jobOption, longBox);
				} else if (jobOption.getType().equals("float") ) {
					DoubleBox doubleBox = new DoubleBox();
					hp.add(new HTML("<b><i>" + jobOption.getName() + "</i></b>&nbsp;") );
					hp.add(doubleBox);
					if ( jobOption.getDefaultValue() != null && !jobOption.getDefaultValue().equals("") ) {
						hp.add(new HTML("&nbsp;<i>(default=" + jobOption.getDefaultValue() + ")</i>"));
					}
					nameToPanelMap.put(jobOption.getName(), hp);
					jobOptionToFormWidgetMap.put(jobOption, doubleBox);
				}
			}
		}
		
		for ( String name : nameToPanelMap.keySet() ) {
			HorizontalPanel hp = nameToPanelMap.get(name);
			add(hp);
			add(new HTML("<hr></hr>"));
		}
		
		addCloseButton();
		addSubmitButton();

		portal.jobOptionsDialog.setText(jobName + " Options");
		portal.jobOptionsDialog.show();
	}
	
	void addCloseButton() {
		this.add(closeButton);
	}

	void addSubmitButton() {
		this.add(submitButton);
	}

	
	
}
