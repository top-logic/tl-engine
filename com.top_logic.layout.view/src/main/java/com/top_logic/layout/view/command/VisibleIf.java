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
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link ViewExecutabilityRule} that shows a command only while a configured TL-Script predicate,
 * evaluated on the command's input, returns {@code true}; otherwise the command is hidden.
 *
 * <p>
 * A reusable, condition-driven alternative to the fixed {@link NullInputHidden} /
 * {@link com.top_logic.layout.view.login.AnonymousOnly} rules: bind the command's {@code input} to
 * the channel the predicate inspects, so the toolbar
 * re-evaluates the rule whenever that channel changes. Example - show only while a boolean
 * {@code running} channel is set: {@code <visible-if expr="x -> $x == true"/>}.
 * </p>
 */
public class VisibleIf implements ViewExecutabilityRule {

	/**
	 * Configuration for {@link VisibleIf}.
	 */
	@TagName("visible-if")
	public interface Config extends ViewExecutabilityRule.Config {

		@Override
		@ClassDefault(VisibleIf.class)
		Class<? extends ViewExecutabilityRule> getImplementationClass();

		/**
		 * TL-Script predicate function called with the command's current input value; the command is
		 * shown when it returns {@code true}.
		 */
		@Name("expr")
		@Mandatory
		Expr getExpr();
	}

	private final QueryExecutor _predicate;

	/**
	 * Creates a new {@link VisibleIf} from configuration.
	 */
	@CalledByReflection
	public VisibleIf(InstantiationContext context, Config config) {
		_predicate = QueryExecutor.compile(config.getExpr());
	}

	@Override
	public ExecutableState isExecutable(Object input) {
		return Boolean.TRUE.equals(_predicate.execute(input))
			? ExecutableState.EXECUTABLE
			: ExecutableState.NOT_EXEC_HIDDEN;
	}
}
