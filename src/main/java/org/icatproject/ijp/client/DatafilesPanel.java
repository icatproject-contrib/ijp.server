package org.icatproject.ijp.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.icatproject.ijp.client.service.DataServiceAsync;
import org.icatproject.ijp.shared.DatasetOverview;
import org.icatproject.ijp.shared.GenericSearchSelections;
import org.icatproject.ijp.shared.PortalUtils;
import org.icatproject.ijp.shared.SessionException;
import org.icatproject.ijp.shared.xmlmodel.ListOption;
import org.icatproject.ijp.shared.xmlmodel.SearchItem;
import org.icatproject.ijp.shared.xmlmodel.SearchItems;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class DatafilesPanel extends Composite {

	private static DatafilesPanelUiBinder uiBinder = GWT
			.create(DatafilesPanelUiBinder.class);

	interface DatafilesPanelUiBinder extends UiBinder<Widget, DatafilesPanel> {
	}

	public DatafilesPanel() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@UiField
	HorizontalPanel searchListsPanel;

	@UiField
	Button searchButton;

	@UiField
	VerticalPanel genericSearchesVerticalPanel;

	@UiField
	Button addGenericSearchButton;

	@UiField
	Label messageLabel;

	@UiField
	SelectionListPanel matchingDatafilesPanel;
	
	@UiField
	SelectionListPanel datafilesCartPanel;
	
	private DataServiceAsync dataService = DataServiceAsync.Util.getInstance();

	Portal portal;
	DialogBox dialogBox;
	
	DatasetOverview datasetOverview;

	Map<String, ListBox> searchItemsListBoxMap = new HashMap<String, ListBox>();

	public DatafilesPanel(final Portal portal, final DialogBox dialogBox) {
		initWidget(uiBinder.createAndBindUi(this));
		
		this.portal = portal;
		this.dialogBox = dialogBox;
		
		searchButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				refreshDatafilesList();
			}
		});

		addGenericSearchButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				genericSearchesVerticalPanel.add(new GenericSearchPanel(portal.getMergedDatafileParameterTypeMappings()));
			}
		});

		matchingDatafilesPanel.setTitle("Matching datafiles");
		matchingDatafilesPanel.setColumnsFrom( new DatafileListContent(null, null, null, null, null, null) );
		
		// Define/redefine button names / handlers:
		// Remove Selected --> gone
		// Remove All --> gone
		// Accept --> Add to Current Selection
		
		matchingDatafilesPanel.changeRemoveAllButton("", null);
		matchingDatafilesPanel.changeRemoveSelectedButton("", null);
		matchingDatafilesPanel.setAcceptButtonText("Add to Current Selection");
		matchingDatafilesPanel.addAcceptHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// add selected datafiles to the local cart
				datafilesCartPanel.addContent(new ArrayList<SelectionListContent>(matchingDatafilesPanel.getSelection()));
			}
		});

		datafilesCartPanel.setTitle("Current Selection");
		datafilesCartPanel.setColumnsFrom( new DatafileListContent(null, null, null, null, null, null) );
		
		// Define buttons:
		// Accept --> Use This Selection
		// Cancel --> Cancel

		datafilesCartPanel.setAcceptButtonText("Add to Main Cart");
		datafilesCartPanel.addAcceptHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// Add all datafiles to the global cart
				List<SelectionListContent> datafiles = datafilesCartPanel.getEverything();
				// TODO remove this alert?
				Window.alert( datafiles.size() + " datafiles added to the global cart");
				portal.datasetsPanel.datafilesCartPanel.addContent(datafiles);
				// Ensure that the global datafiles cart is visible.
				if( datafiles.size() > 0 ){
					portal.datasetsPanel.datafilesCartPanel.setVisible(true);
					// If the datasets cart is not empty, show and enable the global submit button too
					if( ! portal.datasetsPanel.datasetsCartPanel.getEverything().isEmpty() ){
						portal.datasetsPanel.datasetsCartPanel.setVisible(true);
						portal.datasetsPanel.submitJobForCartButton.setEnabled(true);
						portal.datasetsPanel.submitJobForCartButton.setVisible(true);
					}
				}
				dialogBox.hide();
			}
		});
		
		datafilesCartPanel.addCancelHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// Cancel and close the dialog, changing nothing
				dialogBox.hide();
			}
		});

		addSearchBoxesAndPopulateTextArea();
		
	}

	protected void openForDatasetOverview( DatasetOverview datasetOverview ){
		matchingDatafilesPanel.clear();
		datafilesCartPanel.clear();
		this.datasetOverview = datasetOverview;
		dialogBox.setText("Select datafiles in dataset " + datasetOverview.getName());
		messageLabel.setText("Select datafiles in dataset " + datasetOverview.getName());
		dialogBox.center();
		dialogBox.show();
	}
	
	protected void refreshDatafilesList() {
		
		// set up the callback object
		AsyncCallback<List<DatafileListContent>> callback = new AsyncCallback<List<DatafileListContent>>() {
			public void onFailure(Throwable caught) {
				// deal with possible exceptions
				System.err.println("DatasetsPanel.refreshDatasetsList(): " + caught.getMessage());
				if (caught.getClass() == SessionException.class) {
					System.err.println("caught is a SessionException");
					portal.loginPanel.setMessageText(caught.getMessage());
					portal.loginDialog.show();
				} else {
					Window.alert("Server error: " + caught.getMessage());
				}
			}

			public void onSuccess(List<DatafileListContent> result) {
				// if ( result == null ) {
				// System.out.println("Result is null");
				// } else {
				int resultSize = result.size();
				System.out.println("Result size: " + resultSize);
				if (resultSize == PortalUtils.MAX_RESULTS) {
					// TODO - get the colour of the message changing to work
					messageLabel.setText("Results limit of " + PortalUtils.MAX_RESULTS
							+ " reached. Please refine your search.");
				} else {
					messageLabel.setText(resultSize + " datafiles found.");
				}
				// }
				
				matchingDatafilesPanel.setContent(result);				
			}
		};

		List<GenericSearchSelections> genSearchSelectionsList = getGenericSearchSelectionsList();
		if (genSearchSelectionsList != null) {
			StringBuilder sb = new StringBuilder("genSearchSelectionsList:\n");
			for (GenericSearchSelections genericSearchSelections : genSearchSelectionsList) {
				sb.append(genericSearchSelections.toString());
				sb.append("\n");
			}
			// debugTextArea.setText(sb.toString());
			// Window.alert(sb.toString());
		}
		
		// TODO Either grab the datasetType or remove it from the interface - do we really need it?
		String datasetType = "unknown";

		Map<String, List<String>> selectedSearchParamsMap = getSearchParamsMap();

		// make the call to the server
		System.out.println("DatasetsPanel: making call to DataService");
		dataService.getDatafileList(portal.getSessionId(), datasetType, datasetOverview.getDatasetId(), selectedSearchParamsMap, genSearchSelectionsList, callback);
	}

	private List<GenericSearchSelections> getGenericSearchSelectionsList() {
		List<GenericSearchSelections> genSearchSelectionsList = new ArrayList<GenericSearchSelections>();
		for (int i = 0; i < genericSearchesVerticalPanel.getWidgetCount(); i++) {
			try {
				GenericSearchPanel genSearchPanel = (GenericSearchPanel) genericSearchesVerticalPanel
						.getWidget(i);
				genSearchSelectionsList.add(genSearchPanel.validateAndGetGenericSearchSelections());
			} catch (Exception e) {
				Window.alert(e.getMessage());
				return null;
			}
		}
		return genSearchSelectionsList;
	}

	private void addSearchBoxesAndPopulateTextArea() {
		// put the SearchItems looked up from XML into a TextArea
		dataService.getDatafileSearchItems(new AsyncCallback<SearchItems>() {
			@Override
			public void onFailure(Throwable caught) {
				Window.alert("Server error: " + caught.getMessage());
			}

			@Override
			public void onSuccess(SearchItems searchItems) {
				if (searchItems == null) {
					// debugTextArea.setText("searchItems is null");
				} else {
					// debugTextArea.setText(searchItems.toString());
					for (SearchItem searchItem : searchItems.getSearchItemList()) {
						ListBox listBox = new ListBox(searchItem.isMultipleSelect());
						// listBox.setName(searchItem.paramName);
						for (ListOption listOption : searchItem.getListOptions()) {
							listBox.addItem(listOption.getDisplayValue(),
									listOption.getSubmitValue());
						}
						listBox.setVisibleItemCount(searchItem.getVisibleItemCount());
						listBox.setSelectedIndex(0);
						searchListsPanel.add(listBox);
						searchItemsListBoxMap.put(searchItem.getParamName(), listBox);
					}
				}
			}
		});
	}

	private Map<String, List<String>> getSearchParamsMap() {
		Map<String, List<String>> searchParamsMap = new HashMap<String, List<String>>();
		for (String key : searchItemsListBoxMap.keySet()) {
			List<String> selectedItemsList = new ArrayList<String>();
			ListBox listBox = searchItemsListBoxMap.get(key);
			for (int i = 0; i < listBox.getItemCount(); i++) {
				// add selected items whose values are not empty strings
				// ie. ignore the "title" option at the top
				if (listBox.isItemSelected(i) && !listBox.getValue(i).equals("")) {
					selectedItemsList.add(listBox.getValue(i));
				}
			}
			if (selectedItemsList.size() > 0) {
				searchParamsMap.put(key, selectedItemsList);
			}
		}
		return searchParamsMap;
	}

}
