/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.Locale;

import com.top_logic.model.search.expr.visit.Visitor;
import com.top_logic.util.TLContext;

/**
 * {@link SearchExpression} representing {@link #getLeft()} "ends with" {@link #getRight()}
 * operation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StringEndsWith extends StringOperation {

	/**
	 * Creates a {@link StringEndsWith}.
	 * 
	 * @param left
	 *        See {@link #getLeft()}.
	 * @param right
	 *        See {@link #getRight()}.
	 * @param caseSensitive
	 *        See {@link #isCaseSensitive()}
	 */
	StringEndsWith(SearchExpression left, SearchExpression right, boolean caseSensitive) {
		super(left, right, caseSensitive);
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visitStringEndsWith(this, arg);
	}

	@Override
	protected Boolean evalInternal(String left, String right, boolean caseSensitive) {
		if (caseSensitive) {
			return left.endsWith(right);
		} else {
			Locale locale = TLContext.getLocale();
			return left.toLowerCase(locale).endsWith(right.toLowerCase(locale));
		}
	}

}
