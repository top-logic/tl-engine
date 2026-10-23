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
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.ui.ScriptContextVariables;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link ViewExecutabilityRule} that shows a command only while a configured TL-Script predicate
 * over the command's arguments returns {@code true}; otherwise the command is hidden.
 *
 * <p>
 * A reusable, condition-driven alternative to the fixed {@link NullInputHidden} /
 * {@link com.top_logic.layout.view.login.AnonymousOnly} rules: name the channel the predicate
 * inspects among the command's inputs, so the toolbar re-evaluates the rule whenever that channel
 * changes. The predicate uses the inputs as variables, see {@link ScriptedRule}. Example - show
 * only while a boolean {@code running} channel is set:
 * </p>
 *
 * <pre>
 * &lt;generic-command inputs="running"&gt;
 *   &lt;executability&gt;
 *     &lt;visible-if expr="$running == true"/&gt;
 *   &lt;/executability&gt;
 *   ...
 * &lt;/generic-command&gt;
 * </pre>
 */
public class VisibleIf extends ScriptedRule {

	/**
	 * Configuration for {@link VisibleIf}.
	 */
	@TagName("visible-if")
	public interface Config extends ViewExecutabilityRule.Config, ConfigPart {

		/** Configuration name for {@link #getExpr()}. */
		String EXPR = "expr";

		@Override
		@ClassDefault(VisibleIf.class)
		Class<? extends ViewExecutabilityRule> getImplementationClass();

		/**
		 * TL-Script predicate over the arguments of the command; the command is shown when it
		 * returns {@code true}.
		 *
		 * <p>
		 * The predicate uses the arguments as variables, without declaring them: the inputs of the
		 * command named after their channels or by their {@code as} names, and the value the
		 * trigger of the command supplies - e.g. the activated row - under the name the trigger
		 * gives it. A rule guarding a single value of an element - the row of a table being
		 * dragged, the object of a form being edited - sees that value under the name the element
		 * gives it.
		 * </p>
		 */
		@Name(EXPR)
		@Mandatory
		@ScriptContextVariables(ActionScriptVariables.class)
		Expr getExpr();
	}

	/**
	 * Creates a new {@link VisibleIf} from configuration.
	 */
	@CalledByReflection
	public VisibleIf(InstantiationContext context, Config config) {
		super(context, config.getExpr());
	}

	@Override
	protected ExecutableState stateFor(Object result) {
		return Boolean.TRUE.equals(result)
			? ExecutableState.EXECUTABLE
			: ExecutableState.NOT_EXEC_HIDDEN;
	}
}
