/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.assistent.commandhandler;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.assistent.AssistentComponent;

/**
 * @author    <a href="mailto:tri@top-logic.com">tri</a>
 */
public class ForwardAssistantCommandHandlerInvalidate extends ForwardAssistentCommandHandler {

	public static final String COMMAND = "switchAssistentForwardInvalidate";

	/**
	 * Creates a {@link ForwardAssistantCommandHandlerInvalidate}.
	 */
	public ForwardAssistantCommandHandlerInvalidate(InstantiationContext context, Config config) {
		super(context, config);
	}

	/**
	 * Additionally invalidate the new (next) step that is now current So the next step initially
	 * gets always invalidated
	 * 
	 * @see com.top_logic.tool.boundsec.assistent.commandhandler.ForwardAssistentCommandHandler#handleCommand(com.top_logic.layout.DisplayContext,
	 *      com.top_logic.mig.html.layout.LayoutComponent, Object, Map)
	 */
	@Override
	public HandlerResult handleCommand(DisplayContext aContext,
			LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
		HandlerResult theRetVal = super.handleCommand(aContext, aComponent, model, someArguments);
		if (theRetVal.isSuccess()) {
			AssistentComponent theAssistent = (AssistentComponent) aComponent;
			LayoutComponent theNewStep = theAssistent.getStepByStepName(theAssistent.getCurrentStepInfoName());
			if (theNewStep != null) {
				theNewStep.invalidate();
			}
		}
		return theRetVal;
	}
}

