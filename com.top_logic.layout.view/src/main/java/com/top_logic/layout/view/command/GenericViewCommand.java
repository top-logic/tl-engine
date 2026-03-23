/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A {@link ViewCommand} that executes a configured chain of {@link ViewAction}s.
 *
 * <p>
 * Each action's return value becomes the input of the next action. The first action receives the
 * command's input (typically {@code null} for toolbar buttons). This allows composing complex
 * behaviors from simple, reusable building blocks.
 * </p>
 *
 * <p>
 * Example: Create a transient object and open a dialog with it:
 * </p>
 *
 * <pre>
 * &lt;generic-command label="New"&gt;
 *   &lt;execute-script function="x -&gt; new(`my.module:MyType`, transient: true)"/&gt;
 *   &lt;open-dialog dialog-view="demo/create.view.xml" bind-input-to="model"/&gt;
 * &lt;/generic-command&gt;
 * </pre>
 */
public class GenericViewCommand implements ViewCommand {

	/**
	 * Configuration for {@link GenericViewCommand}.
	 */
	@TagName("generic-command")
	public interface Config extends ViewCommand.Config {

		@Override
		@ClassDefault(GenericViewCommand.class)
		Class<? extends ViewCommand> getImplementationClass();

		/**
		 * The chain of actions to execute sequentially.
		 */
		@DefaultContainer
		List<PolymorphicConfiguration<ViewAction>> getActions();
	}

	private final List<ViewAction> _actions;

	/**
	 * Creates a new {@link GenericViewCommand}.
	 */
	@CalledByReflection
	public GenericViewCommand(InstantiationContext context, Config config) {
		_actions = config.getActions().stream()
			.map(c -> context.getInstance(c))
			.toList();
	}

	@Override
	public HandlerResult execute(ReactContext context, Object input) {
		Object current = input;
		for (ViewAction action : _actions) {
			current = action.execute(context, current);
		}
		return HandlerResult.DEFAULT_RESULT;
	}
}
