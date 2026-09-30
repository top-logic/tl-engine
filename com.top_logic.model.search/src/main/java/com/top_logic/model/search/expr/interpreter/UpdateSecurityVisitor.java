/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.search.expr.interpreter;

import com.top_logic.model.search.WithSecurityCheck;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.visit.DefaultDescendingVisitor;

/**
 * Visitor that switches the {@link WithSecurityCheck#usesSecurity() security check} on or off for a
 * whole expression tree.
 *
 * <p>
 * The visitor descends into the complete tree and calls
 * {@link WithSecurityCheck#setUsesSecurity(boolean)} on every visited node that is a
 * {@link WithSecurityCheck}. Nodes that are not security relevant are simply passed through.
 * </p>
 *
 * <p>
 * The typical use is {@link #disableSecurity(SearchExpression)} on a freshly compiled expression of
 * an internal query that must operate regardless of the current user's access rights (e.g. the
 * navigation rules in security computation itself).
 * </p>
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class UpdateSecurityVisitor extends DefaultDescendingVisitor<Void, Boolean> {

	/** Singleton {@link UpdateSecurityVisitor} instance. */
	public static final UpdateSecurityVisitor INSTANCE = new UpdateSecurityVisitor();

	/**
	 * Creates a new {@link UpdateSecurityVisitor}.
	 */
	protected UpdateSecurityVisitor() {
		// singleton instance
	}

	/**
	 * Disables the security check for the given expression and all of its sub-expressions.
	 *
	 * <p>
	 * Shortcut for {@link #updateSecurity(SearchExpression, boolean) updateSecurity(expr, false)}.
	 * </p>
	 *
	 * @param expr
	 *        The root of the expression tree to evaluate without applying the user's access rights.
	 */
	public static void disableSecurity(SearchExpression expr) {
		updateSecurity(expr, false);
	}

	/**
	 * Sets {@link WithSecurityCheck#usesSecurity()} to the given value for the given expression and
	 * all of its sub-expressions.
	 *
	 * @param expr
	 *        The root of the expression tree to update.
	 * @param value
	 *        New value for {@link WithSecurityCheck#usesSecurity()}; <code>false</code> to evaluate
	 *        without any security check.
	 */
	public static void updateSecurity(SearchExpression expr, boolean value) {
		expr.visit(INSTANCE, value);
	}

	@Override
	protected Void compose(SearchExpression expr, Boolean arg, Void result) {
		if (expr instanceof WithSecurityCheck secured) {
			secured.setUsesSecurity(arg);
		}
		return super.compose(expr, arg, result);
	}
}

