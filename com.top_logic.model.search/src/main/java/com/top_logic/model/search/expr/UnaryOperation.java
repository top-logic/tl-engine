/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

/**
 * Base class for {@link SearchExpression} models implementing operators that take a single
 * {@link #getArgument()}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class UnaryOperation extends SearchExpression {

	private SearchExpression _argument;

	/**
	 * Creates a {@link UnaryOperation}.
	 * 
	 * @param argument
	 *        See {@link #getArgument()}.
	 */
	UnaryOperation(SearchExpression argument) {
		_argument = argument;
	}

	/**
	 * The single operand to this {@link UnaryOperation}.
	 */
	public SearchExpression getArgument() {
		return _argument;
	}

	/**
	 * @see #getArgument()
	 */
	public void setArgument(SearchExpression argument) {
		_argument = argument;
	}

}