/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import java.util.Collections;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.CommandCommandHandler;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.scripting.action.CommandExecution;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * {@link AbstractModelActionOp} that executes a {@link CommandModel}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CommandExecutionOp extends AbstractModelActionOp<CommandExecution> {

	/**
	 * Creates a {@link CommandExecutionOp} from configuration.
	 */
	public CommandExecutionOp(InstantiationContext context, CommandExecution config) {
		super(context, config);
	}

	@Override
	public Object processInternal(ActionContext context, Object argument) {
		CommandModel commandModel = findCommandModel(context);
		HandlerResult result = commandModel.executeCommand(context.getDisplayContext());

		if (result.isSuspended()) {
			// This is normally done by CommandDispatcher filling the "click" action. This does not
			// happend during test, since the command is not triggered by a control.
			result.initContinuation(CommandCommandHandler.newHandler(commandModel), context.getMainLayout(),
				Collections.<String, Object> emptyMap());
		}

		boolean success = result.isSuccess() || result.isSuspended();
		if (success == config.getFailureExpected()) {
			if (success) {
				ApplicationAssertions.fail(config,
					"Command '" + commandModel.getLabel() + "' succeeded but a failure was expected.");
			} else {
				String message = "Command '" + commandModel.getLabel() + "' failed.";
				for (ResKey i18n : result.getEncodedErrors()) {
					message += "\n" + Resources.getLogInstance().getString(i18n);
				}
				ApplicationAssertions.fail(config, message, result.getException());
			}
		}

		return argument;
	}

	/**
	 * The {@link CommandModel} this action is about.
	 */
	protected CommandModel findCommandModel(ActionContext context) {
		return (CommandModel) findModel(context);
	}

}
