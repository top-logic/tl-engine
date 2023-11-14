/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import static com.top_logic.model.search.expr.SearchExpressionFactory.*;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.search.expr.IsEqual;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SearchExpressionFactory;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * {@link MethodBuilder} creating {@link IsEqual} expressions.
 * 
 * @see SearchExpressionFactory#isEqual(SearchExpression, SearchExpression)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class IsEqualBuilder extends TwoArgsMethodBuilder<IsEqual> {

	/**
	 * Creates a {@link IsEqualBuilder}.
	 */
	public IsEqualBuilder(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	protected IsEqual internalBuild(Expr expr, SearchExpression arg0, SearchExpression arg1, SearchExpression[] allArgs) {
		return isEqual(arg0, arg1);
	}

}
