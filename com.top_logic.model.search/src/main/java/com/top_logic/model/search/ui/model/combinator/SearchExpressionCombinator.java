/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.combinator;

import java.util.List;

import com.top_logic.model.search.expr.SearchExpression;

/**
 * Combines multiple expressions to a single expressions.
 * <p>
 * Don't implement this directly, but {@link AbstractSearchExpressionCombinator}.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface SearchExpressionCombinator {

	/**
	 * Combines the given {@link SearchExpression}s into a single {@link SearchExpression}s.
	 * 
	 * @param expressions
	 *        Is allowed to be or contain null.
	 * @return Never null.
	 */
	SearchExpression combine(List<SearchExpression> expressions);

}
