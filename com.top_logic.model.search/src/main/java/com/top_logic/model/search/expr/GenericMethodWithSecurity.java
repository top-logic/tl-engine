/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.search.expr;

import com.top_logic.model.search.WithSecurityCheck;

/**
 * Base class for a {@link GenericMethod} that is a {@link WithSecurityCheck}.
 *
 * <p>
 * The {@link #usesSecurity() security flag} is a mandatory constructor argument so that it cannot be
 * lost when an expression is reconstructed (e.g. during {@link #copy(SearchExpression[]) copying} or
 * by a rewriter that replaces a node). Subclasses must forward the flag through their constructor
 * and reproduce it in their {@link #copy(SearchExpression[])} implementation. A fresh expression
 * (created by a {@link com.top_logic.model.search.expr.config.operations.MethodBuilder}) is normally
 * constructed with security enabled.
 * </p>
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 *
 * @see SearchExpressionWithSecurity
 *      Counterpart for plain {@link SearchExpression} based expressions.
 */
public abstract class GenericMethodWithSecurity extends GenericMethod implements WithSecurityCheck {

	private boolean _usesSecurity;

	/**
	 * Creates a new {@link GenericMethodWithSecurity}.
	 *
	 * @param usesSecurity
	 *        See {@link #usesSecurity()}.
	 */
	protected GenericMethodWithSecurity(String name, SearchExpression[] arguments, boolean usesSecurity) {
		super(name, arguments);
		_usesSecurity = usesSecurity;
	}

	@Override
	public boolean usesSecurity() {
		return _usesSecurity;
	}

	@Override
	public void setUsesSecurity(boolean value) {
		_usesSecurity = value;
	}
}

