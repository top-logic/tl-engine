/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.channel.NamedInputs;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.ui.ScriptContextVariables;

/**
 * {@link ViewAction} that evaluates a configured TL-Script.
 *
 * <p>
 * The script uses the arguments of the chain as variables, without declaring them: the arguments
 * the trigger of the chain supplies, the results of the actions it is nested in, and the
 * {@link Config#getInputs() inputs} of the action itself. Its value is the result of this action,
 * seen by its {@link WithResultActions follow-up actions}.
 * </p>
 *
 * <p>
 * Example: In a command with the input channel {@code draft}, persist the transient object it
 * holds, write the created object to a channel and close the dialog:
 * </p>
 *
 * <pre>
 * &lt;execute-script function="$draft.copy(transient: false)"&gt;
 *   &lt;with-result as="created"&gt;
 *     &lt;write-channel name="selection"/&gt;
 *     &lt;close-dialog/&gt;
 *   &lt;/with-result&gt;
 * &lt;/execute-script&gt;
 * </pre>
 *
 * <p>
 * Example: add the object the chain works on to the container held by the channel
 * {@code context}:
 * </p>
 *
 * <pre>
 * &lt;execute-script function="$context.add(`my.module:Container#children`, $draft.copy(transient: false))"&gt;
 *     &lt;inputs&gt;
 *         &lt;input channel="context"/&gt;
 *     &lt;/inputs&gt;
 * &lt;/execute-script&gt;
 * </pre>
 */
@InApp
public class ExecuteScriptAction extends ArgumentsViewAction {

	/**
	 * Configuration for {@link ExecuteScriptAction}.
	 */
	@TagName("execute-script")
	public interface Config
			extends PolymorphicConfiguration<ExecuteScriptAction>, NamedInputs, WithResultActions {

		/** Configuration name for {@link #getFunction()}. */
		String FUNCTION = "function";

		@Override
		@ClassDefault(ExecuteScriptAction.class)
		Class<? extends ExecuteScriptAction> getImplementationClass();

		/**
		 * TL-Script to evaluate.
		 *
		 * <p>
		 * Uses the arguments of the chain and the values of the {@link #getInputs() inputs} as
		 * variables named after them, without declaring them.
		 * </p>
		 */
		@Name(FUNCTION)
		@Mandatory
		@ScriptContextVariables(ActionScriptVariables.class)
		Expr getFunction();
	}

	private final ActionScript _function;

	/**
	 * Creates a new {@link ExecuteScriptAction}.
	 */
	@CalledByReflection
	public ExecuteScriptAction(InstantiationContext context, Config config) {
		this(ActionScript.compile(config.getFunction(), ActionScope.of(context), config.getInputs()));
	}

	/**
	 * Creates a new {@link ExecuteScriptAction}.
	 *
	 * @param function
	 *        The script to evaluate.
	 */
	public ExecuteScriptAction(ActionScript function) {
		_function = function;
	}

	@Override
	public void execute(ReactContext context, ActionArguments arguments, Continuation continuation) {
		continuation.resume(_function.execute(context, arguments));
	}
}
