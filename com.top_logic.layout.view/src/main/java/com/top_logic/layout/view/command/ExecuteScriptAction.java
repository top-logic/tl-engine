/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link ViewAction} that evaluates a configured TL-Script function.
 *
 * <p>
 * The configured expression must be a function that takes one argument. The chain's current input
 * value is passed as the argument. The function's return value becomes the output of this action.
 * </p>
 *
 * <p>
 * Example: Create a transient object:
 * </p>
 *
 * <pre>
 * &lt;execute-script function="x -&gt; new(`test.constraints:ConstraintTestType`, transient: true)"/&gt;
 * </pre>
 */
public class ExecuteScriptAction implements ViewAction {

	/**
	 * Configuration for {@link ExecuteScriptAction}.
	 */
	@TagName("execute-script")
	public interface Config extends com.top_logic.basic.config.PolymorphicConfiguration<ExecuteScriptAction> {

		@Override
		@ClassDefault(ExecuteScriptAction.class)
		Class<? extends ExecuteScriptAction> getImplementationClass();

		/**
		 * TL-Script function expression. Called with the current input as argument.
		 */
		@Name("function")
		@Mandatory
		Expr getFunction();
	}

	private final QueryExecutor _executor;

	/**
	 * Creates a new {@link ExecuteScriptAction}.
	 */
	@CalledByReflection
	public ExecuteScriptAction(InstantiationContext context, Config config) {
		_executor = QueryExecutor.compile(config.getFunction());
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		return _executor.execute(input);
	}
}
