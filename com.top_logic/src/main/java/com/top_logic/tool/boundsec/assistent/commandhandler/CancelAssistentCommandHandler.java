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
import com.top_logic.tool.boundsec.CloseModalDialogCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.assistent.AssistentComponent;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * For cancle the AssistentComponent.
 * 
 * @author    <a href="mailto:skr@top-logic.com">Sylwester Kras</a>
 */
public class CancelAssistentCommandHandler extends CloseModalDialogCommandHandler {
    
    public static final String COMMAND = "assistentCancel";

    public CancelAssistentCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
    }
    
    @Override
	@Deprecated
	public ExecutabilityRule createExecutabilityRule() {
		return CancelAssistantRule.INSTANCE;
    }
    
    @Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments){
        
        if(aComponent.openedAsDialog()) {
            super.handleCommand(aContext, aComponent, model, someArguments);
        }
        
        AssistentComponent theAssistent = (AssistentComponent) aComponent;
        theAssistent.dialogCanceled();
        theAssistent.invalidate();
        return new HandlerResult();
    }

    private static class CancelAssistantRule implements ExecutabilityRule {

    	public static final CancelAssistantRule INSTANCE = new CancelAssistantRule();
    	
		@Override
		public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
			if (aComponent instanceof AssistentComponent) {
				AssistentComponent assistant = (AssistentComponent) aComponent;
				
				boolean isEmbeddedAssistant = !assistant.openedAsDialog();
				if (isEmbeddedAssistant) {
					boolean isFirstStep = assistant.getCurrentStep().equals(assistant.getChild(0));
					if (isFirstStep) {
						return ExecutableState.NOT_EXEC_HIDDEN;
					}
				}

				ComponentName stepName = assistant.getCurrentStepInfoName();
				if (!assistant.getController().showCancelButton(stepName)) {
					return ExecutableState.NOT_EXEC_HIDDEN;
				}
				if (assistant.getController().disableCancelButton(stepName)) {
					return ExecutableState.NOT_EXEC_DISABLED;
				}
			}
			
			return ExecutableState.EXECUTABLE;
		}
    	
    }
}
