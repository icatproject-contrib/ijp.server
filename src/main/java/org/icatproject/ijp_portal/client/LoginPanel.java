package org.icatproject.ijp_portal.client;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.icatproject.ijp_portal.client.service.DataService;
import org.icatproject.ijp_portal.client.service.DataServiceAsync;
import org.icatproject.ijp_portal.shared.PortalUtils.ParameterValueType;


import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class LoginPanel extends Composite {
	// Annotation can be used to change the name of the associated xml file
	// @UiTemplate("LoginPanel.ui.xml")
	interface MyUiBinder extends UiBinder<Widget, LoginPanel> {
	}

	@UiField
	TextBox username;

	@UiField
	PasswordTextBox password;

	@UiField
	Label messageLabel;

	@UiField 
	Button login;

	private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
	private DataServiceAsync dataService = GWT.create(DataService.class);

	Portal portal;

	public LoginPanel(Portal portal) {
		this.portal = portal;
		initWidget(uiBinder.createAndBindUi(this));
		
	}

	@UiHandler("login")
	void handleClick(ClickEvent e) {
		AsyncCallback<String> callback = new AsyncCallback<String>() {
	    	public void onFailure(Throwable caught) {
	    		// TODO - do something with errors
	    		System.err.println("LoginPanel.handleClick(): " + caught.getMessage());
	    		messageLabel.setText("Error: " + caught.getMessage());
	    	}

	    	public void onSuccess(String result) {
	    		portal.setSessionId(result);
	    		portal.setUsername(username.getText());
//	    		Window.alert("sessionId: " + portal.getSessionId() );
	    		portal.loginDialog.hide();
	    		portal.datasetsPanelNew.populateDatasetTypeListBox();
	    		portal.datasetsPanelNew.getJobTypesFromServer();
	    		getMergedDatasetParameterTypeMappings();
//	    		portal.projectsPanel.refreshProjectsList();
//	    		portal.datasetsPanel.refreshProjectsList();
//	    		portal.jobStatusPanel.refreshJobList();
	    		// force the panels to resize
	    		portal.datasetsPanelNew.onResize();
	    		portal.projectsPanel.onResize();
	    		portal.datasetsPanel.onResize();
	    		portal.beadDatasetsPanel.onResize();
	    		portal.biasDatasetsPanel.onResize();
	    		portal.darkDatasetsPanel.onResize();
	    		portal.flatfieldDatasetsPanel.onResize();
	    		portal.checkDatasetsPanel.onResize();
	    		portal.userDatasetsPanel.onResize();
	    		portal.jobStatusPanel.onResize();
	    	}
		};

		// make the call to the server
		System.out.println("LoginPanel: making call to DataService");
		Map<String, String> credentials = new HashMap<String, String>();
		credentials.put("username", username.getText());
		credentials.put("password", password.getText());
		dataService.login("db", credentials, callback);
	}

	protected void getMergedDatasetParameterTypeMappings() {
		dataService.getDatasetParameterTypesMap(portal.getSessionId(), new AsyncCallback<LinkedHashMap<String, ParameterValueType>>() {
			@Override
			public void onFailure(Throwable caught) {
				Window.alert("Server error: " + caught.getMessage());
			}
	
			@Override
			public void onSuccess(LinkedHashMap<String, ParameterValueType> datasetParameterTypesMap) {
				portal.setMergedDatasetParameterTypeMappings(datasetParameterTypesMap);
			}
		});
		
	}

	@UiHandler("password")
	void onKeyPress(KeyPressEvent event)
    {
        if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER)
        {
            login.click();
        }
    }
	
	void setMessageText( String messageText ) {
		messageLabel.setText( messageText );
	}
	
}
