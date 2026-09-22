/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.wizard;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link ViewCommand} that moves the enclosing {@link WizardScope wizard} to the following step.
 *
 * <p>
 * Resolves the wizard from the {@link ViewContext}, so the command works wherever it sits inside a
 * step. Guard it with a {@link WizardHasNext &lt;wizard-has-next&gt;} rule so that the button
 * disappears on the last step. To move on as one step of a longer command, use
 * {@link WizardNextAction the action of the same tag} inside a
 * {@link com.top_logic.layout.view.command.GenericViewCommand &lt;generic-command&gt;}.
 * </p>
 *
 * @implNote Delegates to {@link WizardNextAction}.
 */
@InApp
public class WizardNextCommand implements ViewCommand {

	/**
	 * Configuration for {@link WizardNextCommand}.
	 */
	@TagName(WizardNextAction.Config.TAG_NAME)
	public interface Config extends ViewCommand.Config {

		@Override
		@ClassDefault(WizardNextCommand.class)
		Class<? extends ViewCommand> getImplementationClass();
	}

	private final WizardNextAction _action;

	/**
	 * Creates a new {@link WizardNextCommand}.
	 */
	@CalledByReflection
	public WizardNextCommand(InstantiationContext context, Config config) {
		_action = new WizardNextAction();
	}

	@Override
	public HandlerResult execute(ReactContext context, Object input) {
		_action.execute(context, input);
		return HandlerResult.DEFAULT_RESULT;
	}
}
