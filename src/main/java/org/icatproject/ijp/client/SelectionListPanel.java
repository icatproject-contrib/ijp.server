package org.icatproject.ijp.client;

import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;

/**
 * Composite widget to define selection lists/tables for Datasets and Datafiles.
 * @author Brian Ritchie
 *
 */
public class SelectionListPanel extends Composite {

	private static SelectionListPanelUiBinder uiBinder = GWT
			.create(SelectionListPanelUiBinder.class);

	interface SelectionListPanelUiBinder extends
			UiBinder<Widget, SelectionListPanel> {
	}

	public SelectionListPanel() {
		this("Current Selection");
	}

	@UiField
	Label titleLabel;
	
	@UiField
	ScrollPanel selectionTableHolder;

	CellTable<SelectionListContent> selectionTable;
	
	// "Remove Selected" items from the list
	@UiField
	Button removeSelectedButton;
	
	// "Remove All" items from the list
	@UiField
	Button removeAllButton;
	
	// "Do something" with the current selection set
	// Caller will want to set the click handler for this button
	@UiField 
	Button acceptButton;
	
	private boolean acceptButtonHasHandler = false;
	
	// "Cancel" (e.g. close containing dialog and forget everything)
	// Caller may not want to use this button.
	@UiField
	Button cancelButton;
	
	private boolean cancelButtonHasHandler = false;
	
	private ListDataProvider<SelectionListContent> selectionListModel = new ListDataProvider<SelectionListContent>();
	
	// Perhaps this should be called selectionSelectionModel by analogy with selectionListModel :-)
	final MultiSelectionModel<SelectionListContent> selectionModel = new MultiSelectionModel<SelectionListContent>();

	private HandlerRegistration removeSelectedButtonHandler;

	private HandlerRegistration removeAllButtonHandler;
	
	private EventBus eventBus;

	public SelectionListPanel(String title) {
		
		initWidget(uiBinder.createAndBindUi(this));
		
	    // Explicit: leave the event bus unset unless by setEventBus(...)
		eventBus = null;
		
		selectionTable = new CellTable<SelectionListContent>();
		selectionTable.setWidth("100%");
		
		selectionListModel.addDataDisplay(selectionTable);
		
		selectionTable.setSelectionModel(selectionModel);
		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			public void onSelectionChange(SelectionChangeEvent event) {
				Set<SelectionListContent> selectedContents = selectionModel.getSelectedSet();
				if( selectedContents.size() == 0 ){
					removeSelectedButton.setEnabled(false);
					// Note: the Accept button operates on the whole cart,
					// so its state is independent of the selection
				} else {
					removeSelectedButton.setEnabled(true);
					removeAllButton.setEnabled(true);
				}
			}
		});
		
		selectionTable.setRowCount(0);
		
		selectionTableHolder.add(selectionTable);

		// Store the HandlerRegistration of these buttons in case we replace the handler later
		
		removeSelectedButtonHandler = removeSelectedButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// Remove each selected element from the table
				// by removing them from the list then repopulating the table
				boolean atLeastOneRemoved = false;
				for( SelectionListContent item : selectionModel.getSelectedSet() ){
					selectionListModel.getList().remove(item);
					atLeastOneRemoved = true;
				}
				if( atLeastOneRemoved ){
					selectionListModel.refresh();
					selectionModel.clear();
					removeSelectedButton.setEnabled(false);
					checkAcceptButtonEnabled();
					// If everything has been removed, disabled the remove buttons
					// Do we want to hide the Panel completely?
					if( selectionListModel.getList().size() == 0 ){
						removeAllButton.setEnabled(false);
					}
					fireChangeEventIfEnabled();
				}
			}
		});

		removeAllButtonHandler = removeAllButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				removeAllButton.setEnabled(false);
				removeSelectedButton.setEnabled(false);
				// Note: will it ever make sense to Accept an empty cart? This assumes that it won't.
				acceptButton.setEnabled(false);
				// Clear will fire a change event if enabled, so no need to fire one here
				clear();
			}
		});
		
		acceptButton.setText("Use Selected");
		
		// Accept button should be enabled iff we have some content; initially we don't
		acceptButton.setEnabled(false);
		
		// Similarly, the removeSelected and removeAll buttons should be disabled initially
		removeSelectedButton.setEnabled(false);
		removeAllButton.setEnabled(false);
		
		// Hide and disable the cancel button - caller must use addCancelHandler() to see/enable it
		cancelButton.setText("Cancel");
		cancelButton.setEnabled(false);
		cancelButton.setVisible(false);
	}

	public void setTitle(String text) {
		titleLabel.setText(text);
	}

	public String getTitle() {
		return titleLabel.getText();
	}
	
	public void setEventBus( EventBus eventBus ){
		this.eventBus = eventBus;
	}
	
	// Expose the firing mechanism so it can be used in replacement button handlers
	//
	public void fireChangeEventIfEnabled(){
		if( eventBus != null ){
			eventBus.fireEvent( new SelectionListChangeEvent( this ));
		}
	}
	
	public void setColumnsFrom( SelectionListContent sampleContent ){
		
		for( final String columnName : sampleContent.availableColumns() ){
			TextColumn<SelectionListContent> column = new TextColumn<SelectionListContent>() {
				@Override
				public String getValue(SelectionListContent content) {
					return content.getColumn(columnName);
				}
			};
			selectionTable.addColumn(column, columnName);
		}
	}
	
	private void checkAcceptButtonEnabled(){
		if( selectionListModel.getList().size() > 0 ){
			acceptButton.setEnabled(true);
		} else {
			acceptButton.setEnabled(false);
		}
	}
	
	/**
	 * Add the supplied contentItem to the selection table, if it is not already in there.
	 * 
	 * @param contentItem
	 */
	public void addItem(SelectionListContent contentItem){
		if( ! selectionListModel.getList().contains(contentItem) ){
			selectionListModel.getList().add(contentItem);
			selectionListModel.refresh();
			selectionTable.setPageSize(selectionListModel.getList().size());
			acceptButton.setEnabled(true);
			removeAllButton.setEnabled(true);
			fireChangeEventIfEnabled();
		}
	}
	
	/**
	 * Add the supplied list to the selection list.
	 * Duplicates will be ignored.
	 * 
	 * @param contents
	 */
	public void addContent( List<? extends SelectionListContent> contents ){
		boolean atLeastOneAdded = false;
		for( SelectionListContent item : contents ){
			if( ! selectionListModel.getList().contains(item) ){
				selectionListModel.getList().add(item);
				atLeastOneAdded = true;
			}
		}
		if( atLeastOneAdded ){
			selectionTable.setPageSize(selectionListModel.getList().size());
			selectionListModel.refresh();
			checkAcceptButtonEnabled();
			removeAllButton.setEnabled(true);
			fireChangeEventIfEnabled();
		}
	}
	
	/**
	 * Use the supplied list of contents as the selection list contents.
	 * This will replace any existing contents.
	 * Duplicates in the contents will only be added once.
	 * 
	 * @param contents
	 */
	public void setContent( List<? extends SelectionListContent> contents ){
		selectionListModel.getList().clear();
		this.addContent(contents);
		if( contents.size() > 0 ){
			removeAllButton.setEnabled(true);
		}
		selectionModel.clear();
		checkAcceptButtonEnabled();
		fireChangeEventIfEnabled();
	}
	
	public void addAcceptHandler( ClickHandler clickHandler ){
		acceptButtonHasHandler = true;
		acceptButton.addClickHandler(clickHandler);
	}
	
	public void addCancelHandler( ClickHandler clickHandler ){
		cancelButtonHasHandler = true;
		cancelButton.addClickHandler(clickHandler);
		cancelButton.setEnabled(true);
		cancelButton.setVisible(true);
	}
	
	/**
	 * Change the name and handler for the Remove Selected button.
	 * This removes any previous click handler.
	 * Passing a null for ClickHandler will remove the button.
	 * 
	 * @param name
	 * @param clickHandler
	 */
	public void changeRemoveSelectedButton( String name, ClickHandler clickHandler ){
		if( removeSelectedButtonHandler != null ){
			removeSelectedButtonHandler.removeHandler();
		}
		removeSelectedButton.setText(name);
		if( clickHandler != null ){
			removeSelectedButtonHandler = removeSelectedButton.addClickHandler(clickHandler);
		} else {
			removeSelectedButtonHandler = null;
			removeSelectedButton.setVisible(false);
		}
	}
	
	/**
	 * Change the name and handler for the Remove All button.
	 * This removes any previous click handler.
	 * Passing a null for ClickHandler will remove the button.
	 * 
	 * @param name
	 * @param clickHandler
	 */
	public void changeRemoveAllButton( String name, ClickHandler clickHandler ){
		if( removeAllButtonHandler != null ){
			removeAllButtonHandler.removeHandler();
		}
		removeAllButton.setText(name);
		if( clickHandler != null ){
			removeAllButtonHandler = removeAllButton.addClickHandler(clickHandler);
		} else {
			removeAllButtonHandler = null;
			removeAllButton.setVisible(false);
		}
	}
	
	public void setAcceptButtonText(String title){
		acceptButton.setText(title);
	}
	
	public Set<SelectionListContent> getSelection(){
		return selectionModel.getSelectedSet();
	}
	
	public List<SelectionListContent> getEverything() {
		return selectionListModel.getList();
	}

	public void setVisible(boolean b){
		titleLabel.setVisible(b);
		selectionTableHolder.setVisible(b);
		// The Remove Selected / All buttons should only be shown if they have defined handlers
		removeSelectedButton.setVisible( (removeSelectedButtonHandler != null) && b);
		removeAllButton.setVisible( (removeAllButtonHandler != null) && b);
		acceptButton.setVisible(b);
		// Only show the cancel button if it has a handler defined
		cancelButton.setVisible(cancelButtonHasHandler && b);
	}
	
	public boolean isVisible(){
		return titleLabel.isVisible();
	}

	public void clear() {
		selectionListModel.getList().clear();
		selectionListModel.refresh();
		selectionModel.clear();
		acceptButton.setEnabled(false);
		fireChangeEventIfEnabled();
	}
	
}
