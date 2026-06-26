/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.search.expr;

import com.top_logic.model.search.WithSecurityCheck;

/**
 * Base class for a {@link SearchExpression} that is a {@link WithSecurityCheck}.
 *
 * <p>
 * The {@link #usesSecurity() security flag} is a mandatory constructor argument so that it cannot be
 * lost when an expression is reconstructed (e.g. during a {@link com.top_logic.model.search.expr.visit.Copy copy}
 * or by a rewriter that replaces a node). A fresh expression is normally constructed with security
 * enabled.
 * </p>
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 *
 * @see GenericMethodWithSecurity
 *      Counterpart for {@link GenericMethod} based expressions.
 */
public abstract class SearchExpressionWithSecurity extends SearchExpression implements WithSecurityCheck {

	private boolean _usesSecurity;

	/**
	 * Creates a {@link SearchExpressionWithSecurity}.
	 *
	 * @param usesSecurity
	 *        See {@link #usesSecurity()}.
	 */
	protected SearchExpressionWithSecurity(boolean usesSecurity) {
		super();
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

