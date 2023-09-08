/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.workItem.layout.commandHandler;

import java.util.Collections;
import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.workItem.wrap.PersistentWrapperWorkItem;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.InViewModeExecutable;

/**
 * Creates a new workitem with the current model (wrapper) as subject
 *
 * @author     <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class NewPersistentWorkItemCommandHandler extends AbstractCommandHandler {

	public static final String COMMAND_ID = "newPersistentWorkItem";

    public NewPersistentWorkItemCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
    }

    @Override
	@Deprecated
	public ExecutabilityRule createExecutabilityRule() {
    	return InViewModeExecutable.INSTANCE;
    }

    @Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent,
            Object model, Map<String, Object> someArguments) {
        try {
			Wrapper theWrapper = (Wrapper) model;
            Person   theAssignee = PersonManager.getManager().getCurrentPerson();
            PersistentWrapperWorkItem theItem = this.createWorkItem(theWrapper, theAssignee);

            aComponent.invalidateButtons();

            boolean success = theItem.getKnowledgeBase().commit();
            if (success) {
                return HandlerResult.DEFAULT_RESULT;
            } else {
                HandlerResult theResult = new HandlerResult();
				theResult.addErrorMessage(I18NConstants.ERROR_COMMIT_FAILED);
                return theResult;
            }
        }
        catch (Exception e) {
            HandlerResult theResult = new HandlerResult();
			theResult.addErrorMessage(I18NConstants.ERROR_COMMIT_FAILED);
            return theResult;
        }
    }

	public PersistentWrapperWorkItem createWorkItem(Wrapper aWrapper, Person anAssignee) {
        return PersistentWrapperWorkItem.createWorkItem(
                this.getWorkItemName(aWrapper), aWrapper, null, null, Collections.singleton(anAssignee));
    }

    /**
     * Return the name of the new work item to be created.
     *
     * @param    aWrapper    The wrapper to get the name for a work item.
     * @return   The name of the given wrapper.
     */
    protected String getWorkItemName(Wrapper aWrapper) {
        return aWrapper.getName();
    }

}
