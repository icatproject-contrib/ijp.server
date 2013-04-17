package org.icatproject.ijp_portal.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

// using a ResourceBundle appears to be the recommended way of handling css files etc
// and allows css settings to be available in UIBinder files as well as within code
public interface PortalResources extends ClientBundle {

	  public static final PortalResources INSTANCE =  GWT.create(PortalResources.class);

	  @Source("portal.css")
	  public PortalCssResource css();
	
	  // the PortalCssResource interface needs to match what is available in the css file
	  // currently if the css file is modified then this interface needs to be modified as well
	  // this can be automated by running:
	  // java -cp gwt-dev-2.4.0.jar:gwt-user-2.4.0.jar com.google.gwt.resources.css.InterfaceGenerator \
	  // -standalone -typeName some.package.MyCssResource -css portal.css
	  // in the future this could be automated further by using the GWT maven plugin:
	  // http://mojo.codehaus.org/gwt-maven-plugin/user-guide/css-interface-generator.html
	  public interface PortalCssResource extends CssResource {
		  String black();
		  
		  String bolder();
		  
		  String red();
		  
		  @ClassName("scroll-panel")
		  String scrollPanel();
	  }
	
}
