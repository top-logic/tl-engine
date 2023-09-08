/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.combinator;

import static com.top_logic.basic.CollectionUtil.*;
import static com.top_logic.model.search.expr.SearchExpressionFactory.*;
import static java.util.Collections.singleton;

import java.util.List;

import com.top_logic.model.search.expr.SearchExpression;

/**
 * Implements code that probably all {@link SearchExpressionCombinator} need.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class AbstractSearchExpressionCombinator implements SearchExpressionCombinator {

	@Override
	public SearchExpression combine(List<SearchExpression> expressions) {
		List<SearchExpression> expressionsNonNull = nonNull(expressions);
		expressionsNonNull.removeAll(singleton(null));
		if (expressionsNonNull.isEmpty()) {
			return literal(true);
		}
		return combineInternal(expressionsNonNull);
	}

	/**
	 * Is called by {@link #combine(List)} with normalized arguments.
	 * 
	 * @param expressions
	 *        Is not null and does not contain null. Is not empty.
	 * @return Never null.
	 */
	protected abstract SearchExpression combineInternal(List<SearchExpression> expressions);

}
