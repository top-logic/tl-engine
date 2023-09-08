/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.assistent.commandhandler;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.assistent.AbstractAssistentController;
import com.top_logic.tool.boundsec.assistent.AssistentComponent;


/**
 * Abstract handler for switching between steps in the AssistentComponent.
 * 
 * @author    <a href="mailto:skr@top-logic.com">Sylwester Kras</a>
 */
public abstract class AbstractAssistentSwitchCommandHandler extends AbstractCommandHandler {

	public AbstractAssistentSwitchCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

    /** the name of step to jump to */
    protected abstract ComponentName getNewStepName(AbstractAssistentController aController, ComponentName currentStepName);
    

    @Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, 
                                       Object model, Map<String, Object> someArguments) {
        HandlerResult theResult = new HandlerResult();

		{
            AssistentComponent          theAssistent       = (AssistentComponent) aComponent;
            AbstractAssistentController theController      =  theAssistent.getController();
			ComponentName theCurrentStepName = theAssistent.getCurrentStepInfoName();
			ComponentName theNewStepName = this.getNewStepName(theController, theCurrentStepName);
            LayoutComponent             theNewStep         = theAssistent.getStepByStepName(theNewStepName);
            
            if (theNewStep != null) {
                theAssistent.showStep(theNewStep);
            } /* else // assume it is OK ... KHA breaks in risk NewRiskPerfController
               theResult.addError("assistant.error.step.nextMissing");
            } */
        }
        return theResult;
    }

}
