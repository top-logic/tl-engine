/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import static com.top_logic.model.search.expr.SearchExpressionFactory.*;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SearchExpressionFactory;
import com.top_logic.model.search.expr.SearchExpressions;
import com.top_logic.model.search.expr.Union;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * {@link MethodBuilder} creating {@link Union}s.
 * 
 * @see SearchExpressionFactory#union(SearchExpression, SearchExpression)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class UnionBuilder extends AbstractSimpleMethodBuilder<SearchExpression> {
	/**
	 * Creates a {@link UnionBuilder}.
	 */
	public UnionBuilder(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public SearchExpression build(Expr expr, SearchExpression[] args)
			throws ConfigurationException {
		if (args.length == 0) {
			return SearchExpressions.literalEmptySet();
		}
		
		SearchExpression result = args[0];
		if (args.length == 1) {
			result = new ToSet("toSet", new SearchExpression[] { result });
		} else {
			for (int i = 1; i < args.length; i++) {
				result = union(result, args[i]);
			}
		}
		return result;
	}

}
