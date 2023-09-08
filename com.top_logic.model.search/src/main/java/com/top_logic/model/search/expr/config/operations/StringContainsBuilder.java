/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import static com.top_logic.model.search.expr.SearchExpressionFactory.*;
import static com.top_logic.model.search.expr.visit.Copy.*;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SearchExpressions;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * {@link MethodBuilder} creating {@link StringContainsBuilder} expressions.
 * 
 * @see SearchExpressions#stringContains(SearchExpression, SearchExpression, boolean)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StringContainsBuilder extends TwoArgOptionalBooleanMethodBuilder<SearchExpression> {

	/**
	 * Creates a {@link StringContainsBuilder}.
	 */
	public StringContainsBuilder(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	protected SearchExpression internalBuild(Expr expr, SearchExpression self, SearchExpression arg1,
			SearchExpression arg2) {
		return ifElse(arg2, stringContains(self, arg1, true), stringContains(copy(self), copy(arg1), false));
	}

}
