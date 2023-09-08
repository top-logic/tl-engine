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
import com.top_logic.tool.boundsec.assistent.AssistentComponent;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * For switching back in the AssistentComponent.
 * 
 * @author    <a href="mailto:skr@top-logic.com">Sylwester Kras</a>
 */
public class BackwardAssistentCommandHandler extends
        AbstractAssistentSwitchCommandHandler {

    public static final String COMMAND = "switchAssistentBackward";

	private static final ExecutabilityRule EXEC = new ExecutabilityRule() {
		@Override
		public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
			AssistentComponent assistant = AssistentComponent.getEnclosingAssistentComponent(aComponent);
			ComponentName stepName = assistant.getCurrentStepInfoName();
			if (!assistant.getController().showBackwardButton(stepName)) {
				return ExecutableState.NOT_EXEC_HIDDEN;
			}
			if (assistant.getController().disableBackwardButton(stepName)) {
				return ExecutableState.NOT_EXEC_DISABLED;
			}
			return ExecutableState.EXECUTABLE;
		}
	};

    public BackwardAssistentCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
    }

    /**
     * overridden to track steps on a stack.
     */
    @Override
	public HandlerResult handleCommand(DisplayContext aContext,
            LayoutComponent aComponent, Object model, Map<String, Object> someArguments){
        
        HandlerResult theRetVal = super.handleCommand(aContext, aComponent, model, someArguments);
        
        /* pop the stack */
        if(theRetVal.isSuccess()) {
            AssistentComponent theAssistent = (AssistentComponent) aComponent;
            theAssistent.getStepStack().pop();
        }
        
        return theRetVal;
    }

    @Override
	protected ComponentName getNewStepName(AbstractAssistentController aController, ComponentName theCurrentStepName) {
        return aController.getNameOfPreviousStep(theCurrentStepName);
    }
    
    @Override
	protected ExecutabilityRule intrinsicExecutability() {
		return EXEC;
	}

	@Override
	@Deprecated
	public ResKey getDefaultI18NKey() {
		return I18NConstants.SWITCH_ASSISTENT_BACKWARD;
    }
}
