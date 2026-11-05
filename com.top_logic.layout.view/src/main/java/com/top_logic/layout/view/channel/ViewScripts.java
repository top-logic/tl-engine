/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.dom.Expr.Define;
import com.top_logic.model.search.expr.config.dom.Expr.Wrapped;
import com.top_logic.model.search.expr.config.dom.FreeVariables;
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
 * <p>
 * So is a script using a variable that is neither one of the variables it sees nor defined by the
 * script itself: the check works on the parsed script, so the error is reported when the view is
 * instantiated, also where the script is not compiled against the application model.
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
	 *         If the script is not a valid script of a view, see {@link #checkScript(Expr, List)}.
	 */
	public static QueryExecutor compile(Expr script, List<String> variables) {
		checkScript(script, variables);
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
	 * Checks that the given script is a valid script of a view: it declares no parameter and uses
	 * no undefined variable.
	 *
	 * @param script
	 *        The script to check.
	 * @param variables
	 *        The variables the script sees.
	 * @throws ConfigurationError
	 *         If the script declares a parameter, see {@link #checkNoParameter(Expr, List)}, or uses
	 *         an undefined variable, see {@link #checkVariables(Expr, Collection)}.
	 */
	public static void checkScript(Expr script, List<String> variables) {
		checkNoParameter(script, variables);
		checkVariables(script, variables);
	}

	/**
	 * Checks that the given script uses no variable except the given ones and those it defines
	 * itself.
	 *
	 * <p>
	 * This check also applies to a script that is a function by contract, which
	 * {@link #checkNoParameter(Expr, List)} would reject.
	 * </p>
	 *
	 * @param script
	 *        The script to check.
	 * @param variables
	 *        The variables the script sees.
	 * @throws ConfigurationError
	 *         If the script uses an undefined variable, see {@link FreeVariables}.
	 */
	public static void checkVariables(Expr script, Collection<String> variables) {
		Set<String> undefined = FreeVariables.undefined(script, variables);
		if (undefined.isEmpty()) {
			return;
		}
		if (variables.isEmpty()) {
			throw new ConfigurationError(
				I18NConstants.ERROR_UNDEFINED_VARIABLES_WITHOUT_VARIABLES__UNDEFINED.fill(dollarNames(undefined)));
		}
		throw new ConfigurationError(
			I18NConstants.ERROR_UNDEFINED_VARIABLES__UNDEFINED_VARIABLES.fill(dollarNames(undefined),
				dollarNames(variables)));
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

	private static String dollarNames(Collection<String> variables) {
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
