/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SearchExpressions;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * {@link MethodBuilder} creating {@link IsFloatGreaterBuilder} expressions.
 * 
 * @see SearchExpressions#isFloatGreater(SearchExpression, SearchExpression, SearchExpression)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class IsFloatGreaterBuilder extends TwoArgMethodBuilder<SearchExpression> {

	/**
	 * Creates a {@link IsFloatGreaterBuilder}.
	 */
	public IsFloatGreaterBuilder(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	protected SearchExpression internalBuild(Expr expr, SearchExpression self, SearchExpression arg1,
			SearchExpression arg2) {
		return SearchExpressions.isFloatGreater(self, arg1, arg2);
	}

}
