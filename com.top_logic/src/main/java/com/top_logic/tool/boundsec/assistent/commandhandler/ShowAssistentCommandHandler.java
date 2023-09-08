/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.assistent.commandhandler;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.assistent.AbstractAssistentController;

/**
 * To show the current step in the AssistentComponent.
 * 
 * @author    <a href="mailto:skr@top-logic.com">Sylwester Kras</a>
 */
public class ShowAssistentCommandHandler extends
        AbstractAssistentSwitchCommandHandler {
    
    public static final String COMMAND = "switchAssistentShow";

    public ShowAssistentCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
    }

    /**
     * overridden to track steps on a stack.
     */
    @Override
	public HandlerResult handleCommand(DisplayContext aContext,
            LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
        
        HandlerResult theRetVal = super.handleCommand(aContext, aComponent, model, someArguments);
        
        /* push this step on stack */
//        if(theRetVal.isSuccess()) {
//            AssistentComponent theAssistent = (AssistentComponent) aComponent;
//            theAssistent.getStepStack().push(theAssistent.getCurrentStepInfo());
//        }
        
        return theRetVal;
    }

    @Override
	protected ComponentName getNewStepName(AbstractAssistentController aController, ComponentName currentStepName) {
        return currentStepName;
    }
    
    @Override
	@Deprecated
	public ResKey getDefaultI18NKey() {
		return I18NConstants.SWITCH_ASSISTENT_SHOW;
    }
}
