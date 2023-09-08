/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.services.simpleajax;

import java.io.PrintWriter;

import com.top_logic.basic.Logger;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.mig.html.layout.MainLayout.NotifyUnload;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandScriptWriter;
import com.top_logic.util.Resources;

/**
 * The AjaxCommandScriptWriter writes a JS function that triggers the ajax execution for
 * the given command.
 * 
 * function aCommand.getID() (writeJSCommandParameters()) {
 *  services.ajax.execute("aCommand.getID()", getArgumentObject());
 * }
 * 
 * @author    <a href=mailto:fsc@top-logic.com>fsc</a>
 */
public class AJAXCommandScriptWriter implements CommandScriptWriter {

    public static final CommandScriptWriter INSTANCE = new AJAXCommandScriptWriter();

    /**
     * Write a JScript function to submit the form for the given command.
     * 
     * @param    aPath       The context path of the application.
     * @param    aRes        The resources for resolving I18N tags.
     * @param    anOut       The writer to write to.
     * @param    aCommand    The command to write the script for.
     */
    @Override
	public void writeCommandScript(String aPath, Resources aRes,
			PrintWriter anOut, LayoutComponent aComponent, CommandHandler aCommand) {
        String theCmdName = aCommand.getID();
        
        if (theCmdName != null) {
            
            String clientSideCheck = this.generateJSForConfirmingCommand();
            if (clientSideCheck != null) {
                anOut.println("function execCommand_" + theCmdName + "() {");
                anOut.println(clientSideCheck);
                anOut.println('}');
            }
            
            // JS function declaration
            // function MyCommandID ( ... arguments ... ) {
            anOut.write("function " + theCmdName + "("); 
            this.writeJSCommandParameters(anOut, aCommand);
            anOut.println(") {");
    
            // write the function body
            this.writeCommandScriptBody(aPath, aRes, anOut, aComponent, aCommand);
    
            anOut.println('}');
        }
        else {
            Logger.error("The command '" + aCommand + "' has no ID!", new NullPointerException(), this);
        }
    }
    
    protected String generateJSForConfirmingCommand() {
        return null;
    }
    
    /**
     * writes the javascript parameters of a bound command.
     * 
     * @param anOut writer for html output.
     * @param aCommand the command for which to write parameters.
     */
	protected void writeJSCommandParameters(PrintWriter anOut, CommandHandler aCommand) {
		String[] attrNames = aCommand.getAttributeNames();
        int len = 0;
        
        /* write all the needed parameters */                
        if(attrNames != null && (len = attrNames.length) > 0) {                
        
            anOut.write(attrNames[0]);
        
            for(int i=1; i<len; i++) {
                anOut.write(", ");
                anOut.write(attrNames[i]);
            }                    
        }
    }

    
	protected final void writeCommandScriptBody(String aPath, Resources aRes, PrintWriter anOut,
			LayoutComponent aComponent, CommandHandler aCommand) {
        this.appendBodyStatements(anOut, aComponent, aCommand);
    }
    
	/**
     * Get the argument map for the command passed to the ajax execute call
     */
	protected String getArgumentObject(CommandHandler aCommand) {
		String[] parameterNames = aCommand.getAttributeNames();
		if (parameterNames != null) {
			StringBuffer theMap = new StringBuffer();
			theMap.append('{');
			for (int n = 0, cnt = parameterNames.length; n < cnt; n++) {
				if (n > 0)
					theMap.append(", ");
				theMap.append(parameterNames[n]);
				theMap.append(": arguments[" + n + "]");
			}
			theMap.append('}');
			return theMap.toString();
		}
	    return "arguments[0]";
	}

	protected void appendBodyStatements(PrintWriter anOut, LayoutComponent aComponent, CommandHandler aCommand) {
        appendCallStatement(aComponent, anOut, aCommand, this.getArgumentObject(aCommand));
    }

	protected void appendCallStatement(LayoutComponent aComponent, PrintWriter anOut, CommandHandler aCommand,
			String argumentObject) {
		boolean invokeConcurrent = aCommand.isConcurrent();
        
		boolean unloadCommand = aCommand.getID().equals(MainLayout.NotifyUnload.COMMAND_ID);
		if (invokeConcurrent) {
            anOut.write("services.ajax.invokeRead(");
        } else {
            anOut.write("services.ajax.execute(");
        }
		TagUtil.writeJsString(anOut, aCommand.getID());
        anOut.write(',');
        anOut.write(argumentObject);
		if (invokeConcurrent) {
			if (unloadCommand) {
				anOut.append(',');
				anOut.append(AJAXCommandHandler.NO_ON_ERROR);
				anOut.append(',');
				anOut.append(NotifyUnload.UNLOAD_SEQUENTIAL);
			}
		} else {
			anOut.append(',');
			appendUseWaitPane(anOut, aComponent, aCommand);

			if (unloadCommand) {
				anOut.append(',');
				anOut.append(AJAXCommandHandler.NO_SERVER_RESONSE_CALLBACK);
				anOut.append(',');
				anOut.append(AJAXCommandHandler.NO_ON_ERROR);
				anOut.append(',');
				anOut.append(AJAXCommandHandler.NO_CONTEXT_INFORMATION);
				anOut.append(',');
				anOut.append(NotifyUnload.UNLOAD_SEQUENTIAL);
			}
		}

        anOut.write("); ");
        anOut.write("return false;");
    }

    /**
     * Append the JS value for the "use wait pane" parameter to the output.
     */
	protected void appendUseWaitPane(PrintWriter anOut, LayoutComponent aComponent, CommandHandler aCommand) {
		// Workaround for bug in IE: Do not show the wait pane for commands of
		// components opened in separate browser windows, because disabling the
		// wait pane may fail in the invokeLater() method, if the window is
		// closed in response to the command. The error message that IE reports
		// is "document is null".
		boolean isInMainWindow = aComponent.getEnclosingWindow() == null;
		if (isInMainWindow) {
			anOut.write(AJAXCommandHandler.USE_WAIT_PANE);
		} else {
			anOut.write(AJAXCommandHandler.DONT_USE_WAIT_PANE);
		}
	}
}
