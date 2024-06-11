/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.webfolder;

import java.util.Map;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.TLID;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.event.ModelTrackingService;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.BoundComponent;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * Handler to delete a specific object (ko) from a webfolder of a component.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class DeleteHandler extends AbstractCommandHandler {
    
    /** WebFolder ID. */
    public static final String CONTAINER_ID = "cid";

    /** The command provided by this instance. */
    public static final String COMMAND = "deleteObject";

	/**
	 * Configuration for {@link DeleteHandler}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractCommandHandler.Config {

		@FormattedDefault(SimpleBoundCommandGroup.DELETE_NAME)
		@Override
		CommandGroupReference getGroup();

	}

    public DeleteHandler(InstantiationContext context, Config config) {
		super(context, config);
    }

    @Override
	@Deprecated
	public ResKey getDefaultI18NKey() {
		return (I18NConstants.CONFIRM_DELETE);
    }

    /**
     * Do the actual delete command.
     */
    @Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent,
                                       Object model, Map<String, Object> someArguments) {
        HandlerResult theResult = new HandlerResult();
		WebFolder theFolder = this.getFolder(aComponent, someArguments);
        if (theFolder != null) {
			{
                Wrapper       theRemoveWrap = this.getObjectToRemove(aComponent, someArguments);
				KnowledgeBase theBase = theFolder.getKnowledgeBase();
                if (theRemoveWrap != null) {
                    
                    if (this.deleteDocument(theFolder, theRemoveWrap, aComponent)) {
                        theBase.commit();
                        aComponent.invalidate();
                    } else {
						theResult.addErrorMessage(I18NConstants.REMOVE);
                        theBase.rollback();
                    }
                }
                else {
					theResult.addErrorMessage(I18NConstants.REMOVE);
                    theBase.rollback();
                }
            }
        }
        else {
			theResult.addErrorMessage(I18NConstants.NO_FOLDER);
        }

        return (theResult);
    }
    
    protected WebFolder getFolder(LayoutComponent aComponent, Map someArguments) {
        WebFolder     theFolder = null;
		TLID theContID = IdentifierUtil.fromExternalForm(BoundComponent.getParameter(someArguments, CONTAINER_ID));
        
        if (theContID != null) {
            theFolder = WebFolder.getInstance(theContID);
        }
        else if (aComponent instanceof WebFolderAware) {
            WebFolderAware theComp   = (WebFolderAware) aComponent;
            theFolder  = theComp.getWebFolder();
        }
            
        return theFolder;
    }
    
    protected Wrapper getObjectToRemove(LayoutComponent aComponent, Map someArguments) {
        KnowledgeBase theBase = PersistencyLayer.getKnowledgeBase();
		TLID theID = IdentifierUtil.fromExternalForm(BoundComponent.getParameter(someArguments, OBJECT_ID));

        if (theID != null) {
            String          theType = BoundComponent.getParameter(someArguments, TYPE);
            KnowledgeObject theKO   = theBase.getKnowledgeObject(theType, theID);

            if (theKO != null) {
            	return WrapperFactory.getWrapper(theKO);
            }
        }
        
        return null;
    }

    /**
     * Delete the wrapper
     */
    protected boolean deleteDocument(WebFolder theFolder, Wrapper aWrapper, LayoutComponent aComponent) {
        // inform the system about deletion
        ModelTrackingService.sendDeleteEvent(aWrapper, theFolder);
        
		return theFolder.remove(aWrapper);
    }
    
    /**
     * @see com.top_logic.tool.boundsec.CommandHandler#getAttributeNames()
     */
    @Override
	public String[] getAttributeNames() {
        return (new String[] {OBJECT_ID, TYPE, CONTAINER_ID});
    }

    @Override
	public boolean needsConfirm() {
        return (true);
    }
    
}
