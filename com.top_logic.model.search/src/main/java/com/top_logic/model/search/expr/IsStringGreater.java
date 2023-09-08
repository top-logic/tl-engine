/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import com.top_logic.model.search.expr.visit.Visitor;

/**
 * {@link SearchExpression} representing the "is greater than" comparison of {@link #getLeft()} and
 * {@link #getRight()}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class IsStringGreater extends StringOperation {

	/**
	 * Creates a {@link IsStringGreater}.
	 * 
	 * @param left
	 *        See {@link #getLeft()}.
	 * @param right
	 *        See {@link #getRight()}.
	 * @param caseSensitive
	 *        See {@link #isCaseSensitive()}
	 */
	IsStringGreater(SearchExpression left, SearchExpression right, boolean caseSensitive) {
		super(left, right, caseSensitive);
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visitStringGreater(this, arg);
	}

	@Override
	protected Boolean evalInternal(String left, String right, boolean caseSensitive) {
		if (caseSensitive) {
			return left.compareTo(right) > 0;
		} else {
			return left.compareToIgnoreCase(right) > 0;
		}
	}

}
