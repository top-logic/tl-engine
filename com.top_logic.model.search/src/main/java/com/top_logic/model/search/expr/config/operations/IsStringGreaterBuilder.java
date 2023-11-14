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
public class IsStringGreaterBuilder extends ThreeArgsOptionalBooleanMethodBuilder<SearchExpression> {

	/**
	 * Creates a {@link IsStringGreaterBuilder}.
	 */
	public IsStringGreaterBuilder(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override SearchExpression internalBuild(Expr expr, SearchExpression arg0, SearchExpression arg1,
			SearchExpression arg2, SearchExpression[] allArgs) {
		return ifElse(arg2, isStringGreater(arg0, arg1, true), isStringGreater(copy(arg0), copy(arg1), false));
	}

}
