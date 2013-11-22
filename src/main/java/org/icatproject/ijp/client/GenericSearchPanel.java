package org.icatproject.ijp.client;

import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.icatproject.ijp.shared.GenericSearchSelections;
import org.icatproject.ijp.shared.PortalUtils.ParameterValueType;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.datepicker.client.DateBox;

public class GenericSearchPanel extends Composite {

	Portal portal;
	
	HorizontalPanel horizontalPanel = new HorizontalPanel();

	ListBox paramSelectListBox = new ListBox();
	ListBox operatorSelectListBox = new ListBox();
	// TODO - use a DoubleBox for now as the TextBox has a different height and makes the layout a bit untidy
	DoubleBox paramValueTextBox = new DoubleBox();  
	DoubleBox paramValueDoubleBox = new DoubleBox();
	DoubleBox fromValueDoubleBox = new DoubleBox();
	DoubleBox toValueDoubleBox = new DoubleBox();
	DateBox fromDateBox = new DateBox();
	DateBox toDateBox = new DateBox();
	Button removeButton = new Button();
	
	private static HashMap<ParameterValueType, List<String>> PARAM_OPERATOR_MAPPINGS;
	
	static {
		PARAM_OPERATOR_MAPPINGS = new HashMap<ParameterValueType, List<String>>();
		PARAM_OPERATOR_MAPPINGS.put(ParameterValueType.STRING, Arrays.asList("=", "!=", "LIKE"));
		PARAM_OPERATOR_MAPPINGS.put(ParameterValueType.NUMERIC,	Arrays.asList("=", "!=", "<", "<=", ">", ">=", "BETWEEN"));
		PARAM_OPERATOR_MAPPINGS.put(ParameterValueType.DATE_AND_TIME, Arrays.asList("BETWEEN"));
	}

	public GenericSearchPanel(Portal portal) {
		this.portal = portal;
		
		removeButton.setText("X");
		removeButton.setWidth("2em");
		removeButton.setHeight("2em");
		removeButton.setTitle("Remove this search");
		horizontalPanel.add(removeButton);
		horizontalPanel.add(paramSelectListBox);
		horizontalPanel.add(operatorSelectListBox);
		
		removeButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
			    removeFromParent();
			}
		});

		for (String paramName : portal.getMergedDatasetParameterTypeMappings().keySet()) {
			paramSelectListBox.addItem(paramName);
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

		initWidget(horizontalPanel);
	}
	
	private void setOperatorsListBoxValues() {
		int selectedIndex = paramSelectListBox.getSelectedIndex();
		String selectedValue = paramSelectListBox.getValue(selectedIndex);
		ParameterValueType paramType = portal.getMergedDatasetParameterTypeMappings().get(selectedValue);
		List<String> operatorListBoxValues = PARAM_OPERATOR_MAPPINGS.get(paramType);
		operatorSelectListBox.clear();
		for (String newListBoxValue : operatorListBoxValues) {
			operatorSelectListBox.addItem(newListBoxValue);
		}
		removeAndEmptyCustomValueBoxes();
		if (paramType == ParameterValueType.DATE_AND_TIME) {
			horizontalPanel.add(fromDateBox);
			horizontalPanel.add(toDateBox);
		} else if (paramType == ParameterValueType.STRING) {
			horizontalPanel.add(paramValueTextBox);
		} else if (paramType == ParameterValueType.NUMERIC) {
			horizontalPanel.add(paramValueDoubleBox);
		}
	}
	
	private void setCustomSearchValueBoxes() {
		int paramSelectedIndex = paramSelectListBox.getSelectedIndex();
		String paramSelectedValue = paramSelectListBox.getValue(paramSelectedIndex);
		int operatorSelectedIndex = operatorSelectListBox.getSelectedIndex();
		String operatorSelectedValue = operatorSelectListBox.getValue(operatorSelectedIndex);
		ParameterValueType paramType = portal.getMergedDatasetParameterTypeMappings().get(paramSelectedValue);
		// reset the tool tip on the text box in case LIKE was selected previously 
		paramValueTextBox.setTitle("");
		if (paramType == ParameterValueType.NUMERIC) {
			removeAndEmptyCustomValueBoxes();
			if ( operatorSelectedValue.equals("BETWEEN") ) {
				horizontalPanel.add(fromValueDoubleBox);
				horizontalPanel.add(toValueDoubleBox);
			} else {
				horizontalPanel.add(paramValueDoubleBox);
			}
		} else if (paramType == ParameterValueType.STRING) {
			if ( operatorSelectedValue.equals("LIKE") ) {
				paramValueTextBox.setTitle("Use % as a wildcard on either/both sides of your text");
			}
		}
	}
	
	private void removeAndEmptyCustomValueBoxes() {
		horizontalPanel.remove(paramValueTextBox);
		horizontalPanel.remove(paramValueDoubleBox);
		horizontalPanel.remove(fromValueDoubleBox);
		horizontalPanel.remove(fromDateBox);
		horizontalPanel.remove(toValueDoubleBox);
		horizontalPanel.remove(toDateBox);
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

	public GenericSearchSelections validateAndGetGenericSearchSelections() throws Exception {
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
            throw new Exception("Parameter: " + selectedParamName + " - Search value is not a valid number: " + paramValueDoubleBox.getText());
		}
		try {
			genSearchSelections.setFromValueNumeric(fromValueDoubleBox.getValueOrThrow());
		} catch (ParseException e) {
			throw new Exception("Parameter: " + selectedParamName + " - 'From' value is not a valid number: " + fromValueDoubleBox.getText());
		}
		try {
			genSearchSelections.setToValueNumeric(toValueDoubleBox.getValueOrThrow());
		} catch (ParseException e) {
			throw new Exception("Parameter: " + selectedParamName + " - 'To' value is not a valid number: " + toValueDoubleBox.getText());
		}
		genSearchSelections.setFromDate(fromDateBox.getValue());
		genSearchSelections.setToDate(toDateBox.getValue());
		if ( portal.getMergedDatasetParameterTypeMappings().get(selectedParamName) == ParameterValueType.NUMERIC ) {
			if ( (genSearchSelections.getFromValueNumeric() != null && genSearchSelections.getToValueNumeric() == null) ||
				 (genSearchSelections.getFromValueNumeric() == null && genSearchSelections.getToValueNumeric() != null) ) {
				throw new Exception("Parameter: " + selectedParamName + " - Both fields must contain a value when BETWEEN is selected");
			}
		}
		if ( portal.getMergedDatasetParameterTypeMappings().get(selectedParamName) == ParameterValueType.DATE_AND_TIME ) {
			if ( (genSearchSelections.getFromDate() != null && genSearchSelections.getToDate() == null) ||
			     (genSearchSelections.getFromDate() == null && genSearchSelections.getToDate() != null) ) {
				throw new Exception("Parameter: " + selectedParamName + " - Both date fields must be completed");
			}
		}
		return genSearchSelections;
	}
	
}
