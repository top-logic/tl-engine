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
 * {@link ViewCommand} that moves the enclosing {@link WizardScope wizard} to the preceding step.
 *
 * <p>
 * Resolves the wizard from the {@link ViewContext}, so the command works wherever it sits inside a
 * step. Guard it with a {@link WizardHasBack &lt;wizard-has-back&gt;} rule so that the button
 * disappears on the first step. To step back as one step of a longer command, use
 * {@link WizardBackAction the action of the same tag} inside a
 * {@link com.top_logic.layout.view.command.GenericViewCommand &lt;generic-command&gt;}.
 * </p>
 *
 * @implNote Delegates to {@link WizardBackAction}.
 */
@InApp
public class WizardBackCommand implements ViewCommand {

	/**
	 * Configuration for {@link WizardBackCommand}.
	 */
	@TagName(WizardBackAction.Config.TAG_NAME)
	public interface Config extends ViewCommand.Config {

		@Override
		@ClassDefault(WizardBackCommand.class)
		Class<? extends ViewCommand> getImplementationClass();
	}

	private final WizardBackAction _action;

	/**
	 * Creates a new {@link WizardBackCommand}.
	 */
	@CalledByReflection
	public WizardBackCommand(InstantiationContext context, Config config) {
		_action = new WizardBackAction();
	}

	@Override
	public HandlerResult execute(ReactContext context, Object input) {
		_action.execute(context, input);
		return HandlerResult.DEFAULT_RESULT;
	}
}
