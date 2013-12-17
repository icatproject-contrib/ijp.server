package org.icatproject.ijp.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface IjpResources extends ClientBundle {

	public static final IjpResources INSTANCE = GWT.create(IjpResources.class);

	@Source("portal.css")
	public IjpCssResource css();

	public interface IjpCssResource extends CssResource {

		@ClassName("scroll-panel")
		String scrollPanel();

		String errorMessage();

		String bolder();

		String red();

		String black();

	}

}
