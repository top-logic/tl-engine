/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

/**
 * Base class for {@link SearchExpression} models representing an operation with two operands.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class BinaryOperation extends SearchExpression {
	private SearchExpression _left;

	private SearchExpression _right;

	/**
	 * Creates a {@link BinaryOperation}.
	 * 
	 * @param left
	 *        See {@link #getLeft()}.
	 * @param right
	 *        See {@link #getRight()}.
	 */
	BinaryOperation(SearchExpression left, SearchExpression right) {
		_left = left;
		_right = right;
	}

	/**
	 * The left operand of this {@link BinaryOperation}.
	 */
	public SearchExpression getLeft() {
		return _left;
	}

	/**
	 * @see #getLeft()
	 */
	public void setLeft(SearchExpression left) {
		_left = left;
	}

	/**
	 * The right operand of this {@link BinaryOperation}.
	 */
	public SearchExpression getRight() {
		return _right;
	}

	/**
	 * @see #getRight()
	 */
	public void setRight(SearchExpression right) {
		_right = right;
	}

}