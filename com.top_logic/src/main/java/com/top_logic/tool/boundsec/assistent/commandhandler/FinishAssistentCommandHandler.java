/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.assistent.commandhandler;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.progress.ProgressFinishedRule;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CloseModalDialogCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.assistent.AssistentComponent;
import com.top_logic.tool.execution.ExecutabilityRule;

/**
 * To close eventaully opened assistent dialog and perform a callback.
 *
 * @author    <a href="mailto:skr@top-logic.com">Sylwester Kras</a>
 */
public class FinishAssistentCommandHandler extends
        CloseModalDialogCommandHandler {

    public static final String COMMAND = "assistentFinish";

    public FinishAssistentCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
    }
    
    @Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
        
        HandlerResult result = HandlerResult.DEFAULT_RESULT;
        if(aComponent.openedAsDialog()) {
            result = super.handleCommand(aContext, aComponent, model, someArguments);
            result.setCloseDialog(true);
        }
        
        AssistentComponent theAssistent;

        if(aComponent instanceof AssistentComponent){
        	theAssistent = (AssistentComponent) aComponent;
        }
        else{
        	 theAssistent = AssistentComponent.getEnclosingAssistentComponent(aComponent); 
        }
        
		theAssistent.dialogFinished();
        
        return result;
    }
    
    @Override
	@Deprecated
	public ExecutabilityRule createExecutabilityRule() {
		return ProgressFinishedRule.INSTANCE;
    }
}
