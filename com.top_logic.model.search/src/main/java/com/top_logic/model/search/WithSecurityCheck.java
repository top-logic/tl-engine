/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.search;

import com.top_logic.model.search.expr.SearchExpression;

/**
 * A {@link SearchExpression} whose evaluation is subject to the current user's access rights.
 *
 * <p>
 * Such an expression either reads model elements - and must then drop those that the current user
 * is not allowed to see from its (intermediate) result - or modifies the model - and must then
 * ensure that the user has the required write permission. Whether this check is actually applied is
 * decided per expression by {@link #usesSecurity()}.
 * </p>
 *
 * <p>
 * The security check is enabled by default. A caller that wants to evaluate an expression without
 * applying the user's access rights (e.g. for an internal query that must operate on all elements
 * regardless of the current user) has to switch it off explicitly. This is normally not done on a
 * single expression but recursively on a whole expression tree, see
 * {@link com.top_logic.model.search.expr.interpreter.UpdateSecurityVisitor}.
 * </p>
 *
 * @see com.top_logic.model.search.expr.SearchExpressionWithSecurity
 * @see com.top_logic.model.search.expr.GenericMethodWithSecurity
 * @see com.top_logic.model.search.expr.interpreter.UpdateSecurityVisitor
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface WithSecurityCheck {

	/**
	 * Whether this expression applies the current user's access rights when it is evaluated.
	 *
	 * @implSpec Returns <code>true</code> by default. The security check has to be switched off
	 *           explicitly by calling {@link #setUsesSecurity(boolean)}.
	 */
	boolean usesSecurity();

	/**
	 * Enables or disables the {@link #usesSecurity() security check} for this expression.
	 *
	 * @param value
	 *        <code>true</code> to apply the user's access rights during evaluation,
	 *        <code>false</code> to evaluate without any security check.
	 */
	void setUsesSecurity(boolean value);

}
