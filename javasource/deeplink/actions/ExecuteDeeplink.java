// This file was generated by Mendix Studio Pro.
//
// WARNING: Only the following code will be retained when actions are regenerated:
// - the import list
// - the code between BEGIN USER CODE and END USER CODE
// - the code between BEGIN EXTRA CODE and END EXTRA CODE
// Other code you write will be lost the next time you deploy the project.
// Special characters, e.g., é, ö, à, etc. are supported in comments.

package deeplink.actions;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IDataType;
import com.mendix.systemwideinterfaces.core.IFeedback.MessageType;
import com.mendix.systemwideinterfaces.core.IMendixObject;
import com.mendix.webui.CustomJavaAction;
import com.mendix.webui.FeedbackHelper;
import deeplink.implementation.handler.DeeplinkExecutionHandler;
import deeplink.proxies.DeepLink;

/**
 * Executes a Pendlinglink, i.e. executes the corressponding microflow.
 * Returns true if a link was executed, false otherwise.
 * If this action returns true, no further actions should be taken in the current context.
 */
public class ExecuteDeeplink extends CustomJavaAction<java.lang.Boolean>
{
	private IMendixObject __pendinglink;
	private deeplink.proxies.PendingLink pendinglink;

	public ExecuteDeeplink(IContext context, IMendixObject pendinglink)
	{
		super(context);
		this.__pendinglink = pendinglink;
	}

	@java.lang.Override
	public java.lang.Boolean executeAction() throws Exception
	{
		this.pendinglink = __pendinglink == null ? null : deeplink.proxies.PendingLink.initialize(getContext(), __pendinglink);

		// BEGIN USER CODE
		
		HashMap<String,Object> mfInputParameterValues = new HashMap();
		
		try {
			if (this.pendinglink == null) {
				LOG.warn("Pending link not found");
				return false;
			}

			DeepLink link = this.pendinglink.getPendingLink_DeepLink();

			if (link == null) {
				LOG.warn("Pending link found, but there was no associated deeplink for user: " + this.pendinglink.getUser());
				return false;
			}

			Map<String, IDataType> mfParams = Core.getInputParameters(link.getMicroflow());
			
			//Configured deeplink expects a Mendix object
			if (!link.getUseStringArgument() && link.getObjectType() != null && !link.getObjectType().isEmpty())
			{
				try {
					IMendixObject mxObject = Core.retrieveId(getContext(), Core.createMendixIdentifier(this.pendinglink.getArgument()));
					if(mxObject!=null) {
						Map.Entry<String,IDataType> entry = mfParams.entrySet().iterator().next();
						mfInputParameterValues.put(entry.getKey().toString(), mxObject);
					}
					else {
						FeedbackHelper.addTextMessageFeedback(this.getContext(), MessageType.WARNING, "DeepLink failed to load link, since the object with ID " + this.pendinglink.getArgument() + " no longer exists or the user has no access to read the object", false);
					}
				} catch (CoreException e) {
					LOG.warn("Unable to retrieve " + this.pendinglink.getArgument(), e);
				}
			}
			//Now collect all query string parameters from persisted string argument
			String allArguments = this.pendinglink.getStringArgument();
			if(allArguments != null) {
	            allArguments = URLDecoder.decode(allArguments, StandardCharsets.UTF_8.toString());
           
	            if(allArguments.contains("?") && allArguments.contains("=")) {
	        		String[] arguments = allArguments.substring(allArguments.indexOf("?")+1).split("&");
	        		
	            	for (String argument : arguments) {
	            		processArgument(argument, mfParams, mfInputParameterValues);
	            	}
	    		}

	            if(mfInputParameterValues.size() == 0 && mfParams.size() == 1) {
	            	Map.Entry<String,IDataType> entry = mfParams.entrySet().iterator().next();
            		mfInputParameterValues.put(entry.getKey(), allArguments);
            	}
			}
            
			//invoke the microflow
			try {
				DeeplinkExecutionHandler.execute(getContext(), link.getMicroflow(), mfInputParameterValues);

			} catch (Exception e) {
			    FeedbackHelper.addTextMessageFeedback(this.getContext(), MessageType.WARNING, "Failed to execute microflow for deeplink " + link.getName() + ", check the log for details", false);
			    LOG.error("Failed to execute deeplink " + link.getName(), e);
				return false;
			}

			//remove the pendinglink, unless it should be reused during this session..
			if (link.getUseAsHome()) { //do not remove if used as home.
				this.pendinglink.setSessionId(this.getContext().getSession().getId().toString());
				Core.commit(this.getContext(), this.pendinglink.getMendixObject());
			}
			else {
				Core.delete(this.getContext(), this.pendinglink.getMendixObject());
			}

			//set hitcount (note, this might not be exact)
			IContext sudoContext = getContext().createSudoClone();
			link.setHitCount(sudoContext, link.getHitCount(getContext().createSudoClone()) + 1);
			Core.commit(sudoContext, link.getMendixObject());

			return true;
		}
		catch (Exception e)
		{
		    FeedbackHelper.addTextMessageFeedback(this.getContext(), MessageType.ERROR, "General error while evaluating deeplink:\n" + e.getMessage(), true);
		    LOG.error("General error while evaluating deeplink: " + e.getMessage(), e);
			return false;
		}
		// END USER CODE
	}

	/**
	 * Returns a string representation of this action
	 */
	@java.lang.Override
	public java.lang.String toString()
	{
		return "ExecuteDeeplink";
	}

	// BEGIN EXTRA CODE
	private static final ILogNode LOG = Core.getLogger(deeplink.implementation.Commons.logNodeName);
	
	private static void processArgument(String argument, Map<String, IDataType> params, Map<String, Object> args) {
		// skip empty arguments
	    if( "".equals(argument) )
			return;

		String key;
		String value = "";
		if( argument.contains("=") ) {
			key = argument.substring(0, argument.indexOf("=") );
			value = argument.substring( argument.indexOf("=")+1 );
		} else {
			key = argument;
			// value = empty string
		}

		if( params.containsKey(key) ) {
		    // if already exists (array get param), concat to end.
		    if (args.containsKey(key)) {
		        value = args.get(key) + "-" + value;
		    }
		    args.put(key, value);
		    if(LOG.isTraceEnabled()) {
		    	LOG.trace("Adding parameter: " + key + " and value: " + value );
		    }
		} else { //Fallback to check the parameter case insensitive
			boolean paramMatched = false;
			for( Entry<String,IDataType> param : params.entrySet() ) {
			    String testKey = param.getKey();
				if( testKey.equalsIgnoreCase(key) ) {
					if (args.containsKey(testKey)) {
					    value = args.get(testKey) + "-" + value;
					}
				    args.put(param.getKey(), value);

				    if(LOG.isTraceEnabled()) {
				    	LOG.trace("Adding parameter: " + param.getKey() + " from key: " + key + " and value: " + value );
				    }

					paramMatched = true;
					break;
				}
			}
			if( !paramMatched )
				LOG.warn("Parameter: (" + key + ") found, but no matching mf parameter exists");
		}

	}
	// END EXTRA CODE
}
