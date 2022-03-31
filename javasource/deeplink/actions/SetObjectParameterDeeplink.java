// This file was generated by Mendix Studio Pro.
//
// WARNING: Only the following code will be retained when actions are regenerated:
// - the import list
// - the code between BEGIN USER CODE and END USER CODE
// - the code between BEGIN EXTRA CODE and END EXTRA CODE
// Other code you write will be lost the next time you deploy the project.
// Special characters, e.g., é, ö, à, etc. are supported in comments.

package deeplink.actions;

import java.util.List;
import com.mendix.core.Core;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;
import com.mendix.webui.CustomJavaAction;
import deeplink.proxies.DeepLink;
import deeplink.proxies.microflows.Microflows;
import deeplink.utils.DeeplinkUtils;

/**
 * Creates a deeplink configuration using DeepLinkname as identifier.
 * Any existing configurations with this identifier will be deleted.
 */
public class SetObjectParameterDeeplink extends CustomJavaAction<java.lang.Void>
{
	private java.lang.String Microflow;
	private java.lang.String Description;
	private java.lang.String DeepLinkName;
	private java.lang.String Entity;
	private java.lang.String Attribute;
	private java.lang.Boolean ForceLogin;
	private java.lang.Boolean KeepAsHomeDeeplink;

	public SetObjectParameterDeeplink(IContext context, java.lang.String Microflow, java.lang.String Description, java.lang.String DeepLinkName, java.lang.String Entity, java.lang.String Attribute, java.lang.Boolean ForceLogin, java.lang.Boolean KeepAsHomeDeeplink)
	{
		super(context);
		this.Microflow = Microflow;
		this.Description = Description;
		this.DeepLinkName = DeepLinkName;
		this.Entity = Entity;
		this.Attribute = Attribute;
		this.ForceLogin = ForceLogin;
		this.KeepAsHomeDeeplink = KeepAsHomeDeeplink;
	}

	@java.lang.Override
	public java.lang.Void executeAction() throws Exception
	{
		// BEGIN USER CODE
		DeeplinkUtils.updateOrCreateDeeplink(getContext(), DeepLinkName, ForceLogin, Description, Microflow, KeepAsHomeDeeplink, Entity, Attribute);
		
		return null;
		// END USER CODE
	}

	/**
	 * Returns a string representation of this action
	 */
	@java.lang.Override
	public java.lang.String toString()
	{
		return "SetObjectParameterDeeplink";
	}

	// BEGIN EXTRA CODE

	// END EXTRA CODE
}
