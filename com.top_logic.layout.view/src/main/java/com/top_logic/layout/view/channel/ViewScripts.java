/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import java.util.List;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.dom.Expr.Define;
import com.top_logic.model.search.expr.config.dom.Expr.Wrapped;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * Compilation of the TL-Scripts of a view: scripts that see the values they work on as variables
 * they do not declare.
 *
 * <p>
 * Every script of a view - the script of an action in a chain, the expression of a derived
 * channel, a script of an element - is evaluated with its values bound to implicitly defined
 * variables, never with positional arguments. A script declaring a parameter (a lambda
 * <code>x -&gt; ...</code> as its top-level expression) would therefore not compute a value but
 * yield a function, and nothing it describes would happen. Such a script is rejected as a
 * configuration error when it is compiled.
 * </p>
 *
 * @see InputScript
 */
public final class ViewScripts {

	private ViewScripts() {
		// Static utilities only.
	}

	/**
	 * Compiles a script of a view with implicitly defined variables.
	 *
	 * @param script
	 *        The script, using the variables without declaring them.
	 * @param variables
	 *        The names of the variables, in the order of their values on evaluation, see
	 *        {@link QueryExecutor#compile(Expr, List)}.
	 * @return The executor taking the values of the variables, in the order of their names.
	 * @throws ConfigurationError
	 *         If the script declares a parameter, see {@link #checkNoParameter(Expr, List)}.
	 */
	public static QueryExecutor compile(Expr script, List<String> variables) {
		checkNoParameter(script, variables);
		return QueryExecutor.compile(script, variables);
	}

	/**
	 * Compiles an optional script, see {@link #compile(Expr, List)}.
	 *
	 * @return The executor, {@code null} for no script.
	 */
	public static QueryExecutor compileOptional(Expr script, List<String> variables) {
		if (script == null) {
			return null;
		}
		return compile(script, variables);
	}

	/**
	 * Checks that the given script does not declare a parameter.
	 *
	 * <p>
	 * A script of a view receives its values as variables and is evaluated without positional
	 * arguments. A top-level lambda would therefore make its result a function instead of a value.
	 * This also holds for a script without any variables.
	 * </p>
	 *
	 * @param script
	 *        The script to check.
	 * @param variables
	 *        The variables the script sees, for the error message.
	 * @throws ConfigurationError
	 *         If the top-level expression of the script is a lambda.
	 */
	public static void checkNoParameter(Expr script, List<String> variables) {
		Expr top = script;
		while (top instanceof Wrapped wrapped) {
			top = wrapped.getExpr();
		}
		if (top instanceof Define lambda) {
			if (variables.isEmpty()) {
				throw new ConfigurationError(
					I18NConstants.ERROR_SCRIPT_PARAMETER_WITHOUT_VARIABLES__PARAMETER.fill(lambda.getName()));
			}
			throw new ConfigurationError(
				I18NConstants.ERROR_SCRIPT_PARAMETER__PARAMETER_VARIABLES.fill(lambda.getName(),
					dollarNames(variables)));
		}
	}

	private static String dollarNames(List<String> variables) {
		StringBuilder result = new StringBuilder();
		for (String name : variables) {
			if (result.length() > 0) {
				result.append(", ");
			}
			result.append('$').append(name);
		}
		return result.toString();
	}
}
