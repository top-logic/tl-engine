/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import com.top_logic.model.TLStructuredType;

/**
 * Base class for {@link SearchExpression}s implementing type checks.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class TypeCheck extends SearchExpression {

	private SearchExpression _value;

	private TLStructuredType _checkType;

	/**
	 * Creates a {@link TypeCheck}.
	 * @param value
	 *        See {@link #getValue()}.
	 * @param type
	 *        See {@link #getCheckType()}.
	 */
	TypeCheck(SearchExpression value, TLStructuredType type) {
		_value = value;
		_checkType = type;
	}

	/**
	 * The {@link TLStructuredType} type to test against.
	 */
	public TLStructuredType getCheckType() {
		return _checkType;
	}

	/**
	 * @see #getCheckType()
	 */
	public void setCheckType(TLStructuredType type) {
		_checkType = type;
	}

	/**
	 * The value to test its type against {@link #getCheckType()}.
	 */
	public SearchExpression getValue() {
		return _value;
	}

	/**
	 * @see #getValue()
	 */
	public void setValue(SearchExpression value) {
		_value = value;
	}

}
