/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import static com.top_logic.model.search.expr.SearchExpressionFactory.*;
import static com.top_logic.model.search.expr.visit.Copy.*;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.search.expr.IsStringGreater;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SearchExpressions;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * {@link MethodBuilder} creating {@link IsStringGreater} expressions.
 * 
 * @see SearchExpressions#isStringGreater(SearchExpression, SearchExpression, boolean)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class IsStringGreaterBuilder extends TwoArgOptionalBooleanMethodBuilder<SearchExpression> {

	/**
	 * Creates a {@link IsStringGreaterBuilder}.
	 */
	public IsStringGreaterBuilder(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	protected SearchExpression internalBuild(Expr expr, SearchExpression self, SearchExpression arg1,
			SearchExpression arg2) {
		return ifElse(arg2, isStringGreater(self, arg1, true), isStringGreater(copy(self), copy(arg1), false));
	}

}
