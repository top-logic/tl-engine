/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import java.util.Collections;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ActivateCommand;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandCommandHandler;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.scripting.action.LabeledButtonAction;
import com.top_logic.layout.scripting.recorder.ref.ui.button.LabeledButtonNaming;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.mig.html.layout.CommandDispatcher;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Searches for a clickable button with the given label (<code>config.getLabel()</code>) and clicks
 * it. Asserts to find exactly one matching button.
 * 
 * @see LabeledButtonNaming
 * 
 * @author <a href=mailto:Jan.Stolzenburg@top-logic.com>Jan Stolzenburg</a>
 */
public class AbstractLabeledButtonActionOp<A extends LabeledButtonAction> extends ComponentActionOp<A> {

	public AbstractLabeledButtonActionOp(InstantiationContext context, A config) {
		super(context, config);
	}

	/**
	 * Search for a clickable button with the given label (<code>config.getLabel()</code>) and click
	 * it.
	 * 
	 * @see com.top_logic.layout.scripting.runtime.action.ComponentActionOp#process(com.top_logic.layout.scripting.runtime.ActionContext,
	 *      com.top_logic.mig.html.layout.LayoutComponent, java.lang.Object)
	 */
	@Override
	public Object process(ActionContext actionContext, LayoutComponent component, Object argument) {
		ButtonControl button =
			LabeledButtonNaming.findButton(getConfig().getLabel(), component.getEnclosingFrameScope(),
				actionContext.resolve(getConfig().getBusinessObject()));
		if (button.isDisabled()) {
			ApplicationAssertions.fail(config, "Button '" + button + "' is disabled: " + button.getDisabledReason());
		}
		if (!button.isVisible()) {
			ApplicationAssertions.fail(config, "Button '" + button + "' is not visible.");
		}
		DisplayContext displayContext = actionContext.getDisplayContext();

		// Fake clicking the button
		ButtonControl before = displayContext.get(Command.EXECUTING_CONTROL);
		try {
			HandlerResult executeResult = execute(button, displayContext);

			if (executeResult.isSuspended()) {
				// This is normally done by CommandDispatcher filling the "click" action. This does
				// not
				// happend during test, since the command is not triggered by a control.
				executeResult.initContinuation(CommandCommandHandler.newHandler(new Command() {

					@Override
					public HandlerResult executeCommand(DisplayContext context) {
						return execute(button, context);
					}
				}), actionContext.getMainLayout(), Collections.<String, Object> emptyMap());
			}
			return executeResult;
		} finally {
			displayContext.set(Command.EXECUTING_CONTROL, before);
		}
	}

	/**
	 * Executes the button click command on the given context.
	 */
	protected HandlerResult execute(ButtonControl button, DisplayContext displayContext) {
		return button.executeCommand(CommandDispatcher.approved(displayContext), ActivateCommand.INSTANCE.getID(),
			Collections.emptyMap());
	}

}
