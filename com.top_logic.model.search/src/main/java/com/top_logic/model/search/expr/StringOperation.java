/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import com.top_logic.model.search.expr.query.Args;

/**
 * {@link SearchExpression} representing the equality comparison of {@link #getLeft()} and
 * {@link #getRight()}.
 * <p>
 * Normalizes null to the empty {@link String}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class StringOperation extends BinaryOperation implements BooleanExpression {

	private boolean _caseSensitive;

	/**
	 * Creates a {@link StringOperation}.
	 * 
	 * @param left
	 *        See {@link #getLeft()}.
	 * @param right
	 *        See {@link #getRight()}.
	 * @param caseSensitive
	 *        See {@link #isCaseSensitive()}
	 */
	StringOperation(SearchExpression left, SearchExpression right, boolean caseSensitive) {
		super(left, right);
		_caseSensitive = caseSensitive;
	}

	/**
	 * Whether the comparison is case sensitive.
	 */
	public boolean isCaseSensitive() {
		return _caseSensitive;
	}

	/**
	 * @see #isCaseSensitive()
	 */
	public void setCaseSensitive(boolean caseSensitive) {
		_caseSensitive = caseSensitive;
	}

	@Override
	public Boolean internalEval(EvalContext definitions, Args args) {
		Object left = getLeft().evalWith(definitions, args);
		if (!(left instanceof String)) {
			return null;
		}
		Object right = getRight().evalWith(definitions, args);
		if (!(right instanceof String)) {
			return null;
		}
		return evalInternal((String) left, (String) right, isCaseSensitive());
	}

	/**
	 * @see #evalWith(EvalContext, Args)
	 * 
	 * @param left
	 *        Never null.
	 * @param right
	 *        Never null.
	 */
	protected abstract Boolean evalInternal(String left, String right, boolean caseSensitive);

}
