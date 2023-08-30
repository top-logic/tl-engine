/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import static com.top_logic.model.search.expr.SearchExpressionFactory.*;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.search.expr.DynamicSet;
import com.top_logic.model.search.expr.Literal;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SearchExpressionFactory;
import com.top_logic.model.search.expr.Update;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * {@link MethodBuilder} creating {@link Update} expressions.
 * 
 * @see SearchExpressionFactory#update(SearchExpression, com.top_logic.model.TLStructuredTypePart,
 *      SearchExpression)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SetBuilder extends TwoArgMethodBuilder<SearchExpression> {

	/**
	 * Creates a {@link SetBuilder}.
	 */
	public SetBuilder(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	protected SearchExpression internalBuild(Expr expr, SearchExpression self, SearchExpression arg1,
			SearchExpression arg2) throws ConfigurationException {
		if (arg1 instanceof Literal) {
			return update(self, resolvePart(expr, arg1), arg2);
		} else {
			return new DynamicSet(getConfig().getName(), self, new SearchExpression[] { arg1, arg2 });
		}
	}

}
