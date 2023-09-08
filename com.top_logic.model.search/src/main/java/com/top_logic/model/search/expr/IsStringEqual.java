/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import com.top_logic.model.search.expr.visit.Visitor;

/**
 * {@link SearchExpression} representing the equality comparison of {@link #getLeft()} and
 * {@link #getRight()}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class IsStringEqual extends StringOperation {

	/**
	 * Creates a {@link IsStringEqual}.
	 * 
	 * @param left
	 *        See {@link #getLeft()}.
	 * @param right
	 *        See {@link #getRight()}.
	 * @param caseSensitive
	 *        See {@link #isCaseSensitive()}
	 */
	IsStringEqual(SearchExpression left, SearchExpression right, boolean caseSensitive) {
		super(left, right, caseSensitive);
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visitStringEquals(this, arg);
	}

	@Override
	protected Boolean evalInternal(String left, String right, boolean caseSensitive) {
		if (caseSensitive) {
			return left.equals(right);
		} else {
			return left.equalsIgnoreCase(right);
		}
	}

}
