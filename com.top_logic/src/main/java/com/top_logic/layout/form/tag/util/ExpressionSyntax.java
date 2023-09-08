/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag.util;

import javax.servlet.jsp.PageContext;

/**
 * Utility class to parse and expand XPath-style variable expressions of the
 * form <code>${var-name}</code>.
 * 
 * <p>
 * The variable value is assumed to be a {@link PageContext} attribute.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ExpressionSyntax {

	private static final String VARIABLE_EXPANSION_PREFIX = "${";
	private static final char VARIABLE_EXPANSION_SUFFIX = '}';

	/**
	 * This class only provides static methods, it must not be instantiated.
	 */
	private ExpressionSyntax() {}

	/**
	 * Expand all variables of the form <code>${var-name}</code> in the given
	 * string with {@link PageContext} attribute values of the given
	 * {@link PageContext}.
	 * 
	 * @return The given string with all occurrences of variables replaced with
	 *     their respective values.
	 */
	public static String expandVariables(PageContext pageContext, String expression) {
		int varStartIndex = expression.indexOf(VARIABLE_EXPANSION_PREFIX);

		// To prevent unnecessary object allocation, make sure that no
		// copying occurs, unless at least one variable is expanded.
		if (varStartIndex < 0) {
			return expression;
		}

		StringBuffer expansion = new StringBuffer();

		int currentIndex = 0;
		do {
			int varNameStartIndex = varStartIndex + 2;
			int varStopIndex = expression.indexOf(VARIABLE_EXPANSION_SUFFIX,
					varNameStartIndex);
			if (varStartIndex < 0) {
				throw new IllegalArgumentException(
						"Unterminated variable found: " + expression);
			}

			String varName = 
				expression.substring(varNameStartIndex, varStopIndex);

			// Lookup the value of the variable from the page context.
			Object value = pageContext.getAttribute(varName);

			if (value == null) {
				throw new IllegalArgumentException(
						"Reference to uninitialized variable found: " + varName);
			}

			// Append the static string contents from the start of the
			// expression (or the end of the last expansion) up to the start of
			// the current variable.
			if (varStartIndex > currentIndex) {
				expansion.append(expression.substring(currentIndex,
						varStartIndex));
			}

			// Append the value of the expanded variable.
			expansion.append(value.toString());

			// Advance the position in the expression up to which the processing
			// is finished.
			currentIndex = varStopIndex + 1;
			
			// Lookup the next variable to expand.
			varStartIndex = 
				expression.indexOf(VARIABLE_EXPANSION_PREFIX, currentIndex);
		} while (varStartIndex >= 0);

		// Append the trailing part of the expression (after the last variable
		// expansion).
		if (currentIndex < expression.length()) {
			expansion.append(currentIndex);
		}
		
		return expansion.toString();
	}
}
