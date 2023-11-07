/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import static com.top_logic.model.search.expr.SearchExpressionFactory.*;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.search.expr.Round;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SearchExpressionFactory;
import com.top_logic.model.search.expr.SearchExpressions;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * {@link MethodBuilder} creating {@link Round} expressions.
 * 
 * @see SearchExpressionFactory#round(SearchExpression, SearchExpression)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class RoundBuilder extends AbstractSimpleMethodBuilder<SearchExpression> {
	/**
	 * Creates a {@link RoundBuilder}.
	 */
	public RoundBuilder(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public SearchExpression build(Expr expr, SearchExpression self, SearchExpression[] args)
			throws ConfigurationException {
		return round(self, optionalPrecision(expr, args));
	}

	private SearchExpression optionalPrecision(Expr expr, SearchExpression[] args)
			throws ConfigurationException {
		if (args.length > 1) {
			throw error(I18NConstants.ERROR_AT_LEAST_ONE_ARGUMENT_EXPECTED__EXPR.fill(toString(expr)));
		}
		if (args.length == 0) {
			return SearchExpressions.literal(SearchExpression.toNumber(0));
		}
		return args[0];
	}

}
