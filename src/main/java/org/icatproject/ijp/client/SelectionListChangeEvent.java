package org.icatproject.ijp.client;

import com.google.gwt.event.shared.GwtEvent;

// import com.google.web.bindery.event.shared.binder.GenericEvent;

// For web.bindery this extended GenericEvent, and didn't need getAssociatedType() or dispatch(); but it wouldn't build...

public class SelectionListChangeEvent extends GwtEvent<SelectionListChangeEventHandler> {
	
    public static Type<SelectionListChangeEventHandler> TYPE = new Type<SelectionListChangeEventHandler>();

    private final SelectionListPanel selectionListPanel;
	
	public SelectionListChangeEvent( SelectionListPanel selectionListPanel ){
		this.selectionListPanel = selectionListPanel;
	}
	
	public SelectionListPanel getSelectionListPanel() {
		return selectionListPanel;
	}

    @Override
    public Type<SelectionListChangeEventHandler> getAssociatedType() {
        return TYPE;
    }
    
    @Override
    protected void dispatch(SelectionListChangeEventHandler handler) {
        handler.onSelectionListChanged(this);
    }
}
