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
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.ui.ScriptContextVariables;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link ViewExecutabilityRule} that disables a command while a configured TL-Script over the
 * command's arguments reports a reason preventing the execution.
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
 * The script uses the arguments of the command as variables, see {@link ScriptedRule}. The rule is
 * evaluated whenever one of the command's inputs changes. A script that inspects objects beyond the
 * input objects must announce their types in the command's
 * {@link ViewCommand.Config#getObservedTypes()}, because a change of such an object is otherwise
 * invisible to the command.
 * </p>
 *
 * <p>
 * Example - a command that requires a status to be assigned:
 * </p>
 *
 * <pre>
 * &lt;generic-command inputs="selection as ticket"&gt;
 *   &lt;executability&gt;
 *     &lt;disabled-if expr="if($ticket.get(`demo.tickets:Ticket#status`) == null, #('Please assign a status first.'@en))"/&gt;
 *   &lt;/executability&gt;
 *   ...
 * &lt;/generic-command&gt;
 * </pre>
 */
public class DisabledIf extends ScriptedRule {

	/**
	 * Configuration for {@link DisabledIf}.
	 */
	@TagName("disabled-if")
	public interface Config extends ViewExecutabilityRule.Config, ConfigPart {

		/** Configuration name for {@link #getExpr()}. */
		String EXPR = "expr";

		@Override
		@ClassDefault(DisabledIf.class)
		Class<? extends ViewExecutabilityRule> getImplementationClass();

		/**
		 * TL-Script computing the reason why the command must not be executed.
		 *
		 * <p>
		 * The script uses the arguments of the command as variables, like the predicate
		 * {@link VisibleIf.Config#getExpr()}.
		 * </p>
		 *
		 * <p>
		 * No value or <code>false</code> keeps the command executable. A resource key or a text is
		 * used as reason for disabling the command, <code>true</code> disables it with a generic
		 * reason.
		 * </p>
		 */
		@Name(EXPR)
		@Mandatory
		@ScriptContextVariables(ActionScriptVariables.class)
		Expr getExpr();
	}

	/**
	 * Creates a new {@link DisabledIf} from configuration.
	 */
	@CalledByReflection
	public DisabledIf(InstantiationContext context, Config config) {
		super(context, config.getExpr());
	}

	@Override
	protected ExecutableState stateFor(Object result) {
		return reasonState(result);
	}

	/**
	 * The {@link ExecutableState} described by the given reason value.
	 *
	 * @param reason
	 *        The value computed by the configured {@link Config#EXPR} script.
	 * @return {@link ExecutableState#EXECUTABLE}, if the reason is <code>null</code> or
	 *         <code>false</code>. A disabled state explained by
	 *         {@link ExecutableState#NOT_EXEC_DISABLED_REASON} for <code>true</code>, by the given
	 *         {@link ResKey}, or by the given value rendered as text.
	 */
	public static ExecutableState reasonState(Object reason) {
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
