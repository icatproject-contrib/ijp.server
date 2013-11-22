package org.icatproject.ijp.client;

import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.DialogBox;

public class PortalDialogBox extends DialogBox {

	Portal portal;
	
	public PortalDialogBox(Portal portal, boolean autoHide, boolean modal) {
		super(autoHide, modal);
		this.portal = portal;
	}
	
	/* 
	 * When one of the PortalDialogBoxes is clicked on bring it to the front
	 * (raise its z-index) and send the others to the back (lower their z-index).
	 * This is required because the default behaviour is for them to remain
	 * layered in the order they are opened, and this can be problematic
	 * particularly on smaller browser windows.
	 */
	@Override
	public void onBrowserEvent(Event event) {
		if (event.getTypeInt() == Event.ONMOUSEDOWN) {
			for ( PortalDialogBox portalDialogBox : portal.portalDialogBoxes ) {
				if ( this == portalDialogBox ) {
					// bring this dialog box to the front
					portalDialogBox.bringToFront();
				} else {
					// send the others to the background
					portalDialogBox.sendToBack();
				}
			}
		}
		super.onBrowserEvent(event);
	}
	
	void bringToFront() {
		this.getElement().getStyle().setZIndex(1);
	}
	
	void sendToBack() {
		this.getElement().getStyle().setZIndex(0);
	}
}
