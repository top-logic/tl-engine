/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.webfolder;

import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * Create a folder in another folder.
 * 
 * This handler will use the folder from the component ot its dialogParent, if it's an instance of
 * {@link com.top_logic.knowledge.gui.layout.webfolder.WebFolderAware}. 
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class CreateFolderHandler extends AbstractCommandHandler {

    /** The command provided by this instance. */
    public static final String COMMAND = "createFolder";

    /** The command provided by this instance. */
    public static final String FIELD_NAME = "newName";

	/**
	 * Configuration for {@link CreateFolderHandler}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractCommandHandler.Config {

		@FormattedDefault(SimpleBoundCommandGroup.WRITE_NAME)
		@Override
		CommandGroupReference getGroup();

	}

	public CreateFolderHandler(InstantiationContext context, Config config) {
		super(context, config);
    }

    /**
     * Handle the createFolder command.
     */
    @Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent,
                                       Object model, Map<String, Object> someArguments) {
        FormContext theContext = ((FormComponent) aComponent).getFormContext();
        HandlerResult theResult = new HandlerResult();
        if (theContext.checkAll()) {
            String        theName  = (String) theContext.getField(FIELD_NAME).getValue();
    
            WebFolder       theFolder = null;
            WebFolder       theParentFolder = null;
            WebFolderAware  theComp = null;
            
            if (aComponent instanceof WebFolderAware) {
                theComp   = (WebFolderAware) aComponent;
                theParentFolder = theComp.getWebFolder();
                theFolder = this.createFolder(theName, theParentFolder); 
            }
            
            if(theFolder == null) {
				theResult.addErrorText("Unable to create a folder named '" + theName + "'!");
                return theResult;
            }
            
			{
                if (theFolder.getKnowledgeBase().commit()) {
                    theResult.addProcessed(theFolder);
                    theResult.setCloseDialog(true);
                }
                else {
					theResult.addErrorText("Failed to commit creation of folder!");
                }
            }
        }
        else {
			theResult.addErrorText(theContext.getField(FIELD_NAME).getError());
        }
        return theResult;
    }

    /**
     * Create a folder in the given one and return the created.
     * 
     * This method will not commit the knowledgebase, this should be done in
     * another place.
     * 
     * @param    aName      The name of the new folder, must not be <code>null</code> or empty.
     * @param    aFolder    The folder to create the new one in, must not be <code>null</code>.
     * @return   The created folder or <code>null</code>, if creation fails.
     */
    public WebFolder createFolder(String aName, WebFolder aFolder) {
        if ((aFolder != null) && !StringServices.isEmpty(aName)) {
            try {
                return (aFolder.createSubFolder(aName));
            }
            catch (Exception ex) {
                Logger.error("Unable to create subfolder '" + aName + "'!", ex, this);
            }
        }

        return (null);
    }
    
}
