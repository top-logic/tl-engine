/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import static com.top_logic.model.search.expr.SearchExpressionFactory.*;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.search.expr.Access;
import com.top_logic.model.search.expr.DynamicGet;
import com.top_logic.model.search.expr.Literal;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SearchExpressionFactory;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * {@link MethodBuilder} creating {@link Access} or {@link DynamicGet} expressions.
 * 
 * @see SearchExpressionFactory#access(SearchExpression, com.top_logic.model.TLStructuredTypePart)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class GetBuilder extends TwoArgsMethodBuilder<SearchExpression> {

	/**
	 * Creates a {@link GetBuilder}.
	 */
	public GetBuilder(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	protected SearchExpression internalBuild(Expr expr, SearchExpression arg0, SearchExpression arg1)
			throws ConfigurationException {
		if (arg1 instanceof Literal) {
			return access(arg0, resolvePart(expr, arg1));
		} else {
			return new DynamicGet(getConfig().getName(), new SearchExpression[] { arg0, arg1 });
		}
	}

}
