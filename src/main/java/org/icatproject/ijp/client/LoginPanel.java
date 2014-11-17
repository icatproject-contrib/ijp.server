package org.icatproject.ijp.client;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.icatproject.ijp.client.service.DataService;
import org.icatproject.ijp.client.service.DataServiceAsync;
import org.icatproject.ijp.shared.Authenticator;
import org.icatproject.ijp.shared.CredType;
import org.icatproject.ijp.shared.CredType.Visibility;
import org.icatproject.ijp.shared.LoginResult;
import org.icatproject.ijp.shared.PortalUtils.ParameterValueType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class LoginPanel extends Composite {

	interface MyUiBinder extends UiBinder<Widget, LoginPanel> {
	}

	@UiField
	ListBox authnList;

	@UiField
	FlexTable credentials;

	@UiField
	Label messageLabel;

	private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
	private DataServiceAsync dataService = GWT.create(DataService.class);

	private TextBox first;

	private Map<String, List<CredType>> credTypes = new HashMap<String, List<CredType>>();

	private Portal portal;

	public LoginPanel(Portal portal) {
		initWidget(uiBinder.createAndBindUi(this));
		this.portal = portal;
		dataService.getAuthenticators(new AsyncCallback<List<Authenticator>>() {

			@Override
			public void onFailure(Throwable caught) {
				Window.alert("Error " + caught);
			}

			@Override
			public void onSuccess(List<Authenticator> authenticators) {
				for (Authenticator authenticator : authenticators) {
					String authnName = authenticator.getAuthnName();
					LoginPanel.this.credTypes.put(authnName, authenticator.getCredTypes());
					authnList.addItem(authenticator.getFriendlyName(), authnName);
				}
				authnList.setSelectedIndex(0);
				buildForm();

			}
		});

	}

	@UiHandler("authnList")
	void handleAuthnClick(ClickEvent e) {
		buildForm();
	}

	private void buildForm() {
		String method = authnList.getValue(authnList.getSelectedIndex());
		List<CredType> creds = credTypes.get(method);
		credentials.removeAllRows();
		int i = 0;
		for (CredType cred : creds) {
			credentials.setWidget(i, 0, new Label(cred.getName()));
			if (cred.getVisibility() == Visibility.EXPOSED) {
				credentials.setWidget(i, 1, new TextBox());
			} else {
				credentials.setWidget(i, 1, new PasswordTextBox());
			}
			i++;
		}
		if (credentials.getRowCount() > 0) {
			TextBox widget = (TextBox) credentials.getWidget(i - 1, 1);
			widget.addKeyUpHandler(new KeyUpHandler() {
				@Override
				public void onKeyUp(KeyUpEvent event) {
					if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
						login();
					}
				}
			});
			first = (TextBox) credentials.getWidget(0, 1);
		} else {
			first = null;
		}
	}

	@UiHandler("login")
	void handleClick(ClickEvent e) {
		login();
	}

	void setMessageText(String messageText) {
		messageLabel.setText(messageText);
	}

	void handlePasswordEneter(KeyUpEvent event) {
		if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
			login();
		}
	}

	private void login() {

		String method = authnList.getValue(authnList.getSelectedIndex());
		Map<String, String> credMap = new HashMap<String, String>();
		for (int i = 0; i < credentials.getRowCount(); i++) {
			String key = ((Label) credentials.getWidget(i, 0)).getText();
			String value = ((TextBox) credentials.getWidget(i, 1)).getText();
			credMap.put(key, value);
		}

		dataService.login(method, credMap, new AsyncCallback<LoginResult>() {

			@Override
			public void onFailure(Throwable caught) {
				messageLabel.setText("Error: " + caught.getMessage());
			}

			@Override
			public void onSuccess(LoginResult loginResult) {

				messageLabel.setText("");
				portal.setSessionId(loginResult.getSessionId());
				portal.setUsername(loginResult.getUserName());
				portal.loginDialog.hide();
				portal.datasetsPanel.getJobTypesFromServer();
				portal.datasetsPanel.populateJobTypesListBox();
				getMergedDatasetParameterTypeMappings();
			}

		});
	}

	protected void getMergedDatasetParameterTypeMappings() {
		dataService.getDatasetParameterTypesMap(portal.getSessionId(),
				new AsyncCallback<LinkedHashMap<String, ParameterValueType>>() {
					@Override
					public void onFailure(Throwable caught) {
						Window.alert("Server error: " + caught.getMessage());
					}

					@Override
					public void onSuccess(
							LinkedHashMap<String, ParameterValueType> datasetParameterTypesMap) {
						portal.setMergedDatasetParameterTypeMappings(datasetParameterTypesMap);
					}
				});

	}

	public void focus() {
		if (first != null) {
			first.setFocus(true);
		}
	}

}
