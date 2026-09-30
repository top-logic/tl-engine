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
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link ViewExecutabilityRule} that disables a command while a configured TL-Script function,
 * evaluated on the command's input, reports a reason preventing the execution.
 *
 * <p>
 * The result of the function decides: no value or <code>false</code> keeps the command executable,
 * <code>true</code> disables it with a generic reason, a resource key or a text disables it with
 * that reason. The reason is displayed as the tooltip of the disabled button, so that the user
 * learns what is missing.
 * </p>
 *
 * <p>
 * Counterpart of {@link VisibleIf}: A command that the user may not execute yet stays visible
 * together with its reason, while a command that does not belong to the user at all is hidden.
 * </p>
 *
 * <p>
 * The rule is evaluated whenever the command's input changes. A function that inspects objects
 * beyond the input object must announce their types in the command's
 * {@link ViewCommand.Config#getObservedTypes()}, because a change of such an object is otherwise
 * invisible to the command.
 * </p>
 *
 * <p>
 * Example - a command that requires a status to be assigned:
 * </p>
 *
 * <pre>
 * &lt;disabled-if expr="t -&gt; if($t.get(`demo.tickets:Ticket#status`) == null, #('Please assign a status first.'@en))"/&gt;
 * </pre>
 */
public class DisabledIf implements ViewExecutabilityRule {

	/**
	 * Configuration for {@link DisabledIf}.
	 */
	@TagName("disabled-if")
	public interface Config extends ViewExecutabilityRule.Config {

		/** Configuration name for {@link #getExpr()}. */
		String EXPR = "expr";

		@Override
		@ClassDefault(DisabledIf.class)
		Class<? extends ViewExecutabilityRule> getImplementationClass();

		/**
		 * TL-Script function called with the command's current input value, computing the reason
		 * why the command must not be executed.
		 *
		 * <p>
		 * No value or <code>false</code> keeps the command executable. A resource key or a text is
		 * used as reason for disabling the command, <code>true</code> disables it with a generic
		 * reason.
		 * </p>
		 */
		@Name(EXPR)
		@Mandatory
		Expr getExpr();
	}

	private final QueryExecutor _reason;

	/**
	 * Creates a new {@link DisabledIf} from configuration.
	 */
	@CalledByReflection
	public DisabledIf(InstantiationContext context, Config config) {
		_reason = QueryExecutor.compile(config.getExpr());
	}

	@Override
	public ExecutableState isExecutable(Object input) {
		return stateFor(_reason.execute(input));
	}

	/**
	 * The {@link ExecutableState} described by the given reason value.
	 *
	 * @param reason
	 *        The value computed by the configured {@link Config#EXPR} function.
	 * @return {@link ExecutableState#EXECUTABLE}, if the reason is <code>null</code> or
	 *         <code>false</code>. A disabled state explained by
	 *         {@link ExecutableState#NOT_EXEC_DISABLED_REASON} for <code>true</code>, by the given
	 *         {@link ResKey}, or by the given value rendered as text.
	 */
	public static ExecutableState stateFor(Object reason) {
		if (reason == null || Boolean.FALSE.equals(reason)) {
			return ExecutableState.EXECUTABLE;
		}
		if (Boolean.TRUE.equals(reason)) {
			return ExecutableState.createDisabledState(ExecutableState.NOT_EXEC_DISABLED_REASON);
		}
		ResKey key = SearchExpression.asResKey(reason);
		if (key == null) {
			key = ResKey.text(SearchExpression.asString(reason));
		}
		return ExecutableState.createDisabledState(key);
	}
}
