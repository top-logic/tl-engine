/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import static com.top_logic.model.search.expr.SearchExpressionFactory.sort;
import static com.top_logic.model.search.expr.SearchExpressions.sort;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SearchExpressionFactory;
import com.top_logic.model.search.expr.Sort;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * {@link MethodBuilder} creating {@link Sort} expressions.
 * 
 * @see SearchExpressionFactory#sort(SearchExpression, SearchExpression)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SortBuilder extends AbstractSimpleMethodBuilder<Sort> {
	/**
	 * Creates a {@link SortBuilder}.
	 */
	public SortBuilder(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public Sort build(Expr expr, SearchExpression self, SearchExpression[] args)
			throws ConfigurationException {
		checkArgs(expr, args, 1, 2);
		if (args.length == 1) {
			return sort(args[0]);
		} else {
			return sort(args[0], args[1]);
		}
	}

}
