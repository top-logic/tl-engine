/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.List;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link ViewExecutabilityRule} deciding by a TL-Script over the {@link ActionArguments arguments}
 * of what it guards.
 *
 * <p>
 * The script sees the arguments as variables, without declaring them, exactly like the scripts of
 * an action chain (see {@link ActionScript}): the names are those of the {@link ActionScope} the
 * rule is instantiated in. For a command these are its {@link CommandArguments arguments} - its
 * inputs, and the value its trigger supplies; for a rule guarding an element's single value -
 * dragging a row, say - the value under the name the element gives it.
 * </p>
 *
 * @implNote The configuration of a subclass declares its script with
 *           {@link com.top_logic.model.search.ui.ScriptContextVariables} of
 *           {@link ActionScriptVariables}, and is a {@link com.top_logic.basic.config.container.ConfigPart},
 *           so that the script editor finds the same names via {@link ActionScopes#scopeAt}.
 */
public abstract class ScriptedRule implements ViewExecutabilityRule {

	private final ActionScope _scope;

	private final ActionScript _script;

	/**
	 * Creates a {@link ScriptedRule}.
	 *
	 * @param context
	 *        The context the rule is instantiated in, carrying its {@link ActionScope}.
	 * @param script
	 *        The script, using the names of the scope as variables.
	 */
	protected ScriptedRule(InstantiationContext context, Expr script) {
		_scope = ActionScope.of(context);
		_script = ActionScript.compile(script, _scope, List.of());
	}

	/**
	 * Decides over the given value as the default argument, every other argument of the scope being
	 * {@code null}, see {@link ActionScope#arguments(Object)}.
	 */
	@Override
	public final ExecutableState isExecutable(Object input) {
		return isExecutableFor(_scope.arguments(input));
	}

	@Override
	public final ExecutableState isExecutableFor(ActionArguments arguments) {
		return stateFor(_script.execute(null, arguments));
	}

	/**
	 * The state the result of the script stands for.
	 *
	 * @param result
	 *        The value the script computed.
	 */
	protected abstract ExecutableState stateFor(Object result);
}
