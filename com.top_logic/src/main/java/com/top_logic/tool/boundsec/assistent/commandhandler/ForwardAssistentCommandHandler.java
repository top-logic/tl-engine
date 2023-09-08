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
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.assistent.AbstractAssistentController;
import com.top_logic.tool.boundsec.assistent.AssistentComponent;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * For switching forward in the AssistentComponent.
 * Also other commands as "finish" or "end" are realized with this handler.
 * 
 * @author    <a href="mailto:skr@top-logic.com">Sylwester Kras</a>
 */
public class ForwardAssistentCommandHandler extends
        AbstractAssistentSwitchCommandHandler {
    
    public static final String COMMAND = "switchAssistentForward";

	private static ExecutabilityRule EXEC = new ExecutabilityRule() {
		@Override
		public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
			AssistentComponent assistant = AssistentComponent.getEnclosingAssistentComponent(aComponent);
			ComponentName stepName = assistant.getCurrentStepInfoName();
			if (!assistant.getController().showForwardButton(stepName)) {
				return ExecutableState.NOT_EXEC_HIDDEN;
			}
			if (assistant.getController().disableForwardButton(stepName)) {
				return ExecutableState.NOT_EXEC_DISABLED;
			}
			return ExecutableState.EXECUTABLE;
		}
	};
    
	public ForwardAssistentCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

    /**
     * overridden to track steps on a stack.
     */
    @Override
	public HandlerResult handleCommand(DisplayContext aContext,
            LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
        
        AssistentComponent theAssistent     = (AssistentComponent) aComponent;
        LayoutComponent    currentComponent = theAssistent.getCurrentStep();
    	
        if (currentComponent instanceof FormComponent) {
        	FormComponent currentFormComponent = (FormComponent) currentComponent;
        	
			if (currentFormComponent.hasFormContext()) {
				FormContext formContext = currentFormComponent.getFormContext();
				if (!formContext.checkAll()) {
					currentComponent.invalidate();
					return AbstractApplyCommandHandler.createErrorResult(formContext);
				}
        	}
        }
        
        HandlerResult theRetVal = super.handleCommand(aContext, aComponent, model, someArguments);
        
        /* push this step on stack */
        if(theRetVal.isSuccess()) {
            theAssistent.getStepStack().push(theAssistent.getCurrentStepInfo());
        }
        
        return theRetVal;
    }

    @Override
	protected ComponentName getNewStepName(AbstractAssistentController aController, ComponentName currentStepName) {
        return aController.getNameOfNextStep(currentStepName);
    }
    
    @Override
	protected ExecutabilityRule intrinsicExecutability() {
		return EXEC;
	}

	@Override
	@Deprecated
	public ResKey getDefaultI18NKey() {
		return I18NConstants.SWITCH_ASSISTENT_FORWARD;
    }
}
