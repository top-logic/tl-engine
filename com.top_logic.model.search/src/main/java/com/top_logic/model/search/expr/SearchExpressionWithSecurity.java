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
 * Holds the {@link #usesSecurity() security flag} (enabled by default) so that subclasses only have
 * to guard their actual security check - filtering read results by the user's read rights, or
 * verifying write permissions - with {@link #usesSecurity()}.
 * </p>
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 *
 * @see GenericMethodWithSecurity
 *      Counterpart for {@link GenericMethod} based expressions.
 */
public abstract class SearchExpressionWithSecurity extends SearchExpression implements WithSecurityCheck {

	private boolean _usesSecurity = true;

	/**
	 * Creates a {@link SearchExpressionWithSecurity}.
	 */
	protected SearchExpressionWithSecurity() {
		super();
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

