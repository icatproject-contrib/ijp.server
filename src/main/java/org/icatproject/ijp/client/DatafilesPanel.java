package org.icatproject.ijp.client;

import java.util.ArrayList;
import java.util.List;

import org.icatproject.ijp.shared.DatasetOverview;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
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
	
	Portal portal;
	DialogBox dialogBox;
	
	DatasetOverview datasetOverview;

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
				// TODO Still using generic data*sets* search panel
				genericSearchesVerticalPanel.add(new GenericSearchPanel(portal));
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
				// TODO remove this alert
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
				// TODO remove this alert?
				Window.alert( "Selection cancelled");
				dialogBox.hide();
			}
		});

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
		// TODO Implement this properly
		Window.alert("Search not yet implemented - generating dummy results");
		// Build a dummy set of "datafiles" to exercise rest of GUI
		List<SelectionListContent> datafileListContents = new ArrayList<SelectionListContent>();
		for( int i=20; i < 30; i++ ){
			DatafileListContent datafileListContent = new DatafileListContent(
					(long) i,
					"Datafile " + i,
					"Description of datafile " + i,
					i*10 + "MB",
					"24 Nov 2014 15:28",
					"24 Nov 2014 15:28"
					);
			datafileListContents.add(datafileListContent);
		}
		matchingDatafilesPanel.setContent(datafileListContents);
	}


}
