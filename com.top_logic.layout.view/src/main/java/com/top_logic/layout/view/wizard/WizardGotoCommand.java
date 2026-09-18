/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.wizard;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link ViewCommand} that displays a named step of the enclosing {@link WizardScope wizard}.
 *
 * <p>
 * Resolves the wizard from the {@link ViewContext}, so the command works wherever it sits inside a
 * step. The step is the one {@link Config#getStep()} names, or - where the configuration names none
 * - the command's input value. To jump as one step of a longer command, use
 * {@link WizardGotoAction the action of the same tag} inside a
 * {@link com.top_logic.layout.view.command.GenericViewCommand &lt;generic-command&gt;}.
 * </p>
 *
 * @implNote Delegates to {@link WizardGotoAction}.
 */
@InApp
public class WizardGotoCommand implements ViewCommand {

	/**
	 * Configuration for {@link WizardGotoCommand}.
	 */
	@TagName(WizardGotoAction.Config.TAG_NAME)
	public interface Config extends ViewCommand.Config {

		@Override
		@ClassDefault(WizardGotoCommand.class)
		Class<? extends ViewCommand> getImplementationClass();

		/**
		 * The {@code id} of the step to display.
		 *
		 * <p>
		 * Without one, the command's input value names the step.
		 * </p>
		 */
		@Name(WizardGotoAction.Config.STEP)
		@Nullable
		String getStep();
	}

	private final WizardGotoAction _action;

	/**
	 * Creates a new {@link WizardGotoCommand}.
	 */
	@CalledByReflection
	public WizardGotoCommand(InstantiationContext context, Config config) {
		_action = new WizardGotoAction(config.getStep());
	}

	@Override
	public HandlerResult execute(ReactContext context, Object input) {
		_action.execute(context, input);
		return HandlerResult.DEFAULT_RESULT;
	}
}
