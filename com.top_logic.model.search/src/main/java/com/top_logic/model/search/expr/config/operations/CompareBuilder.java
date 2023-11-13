/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.search.expr.Compare;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SearchExpressionFactory;
import com.top_logic.model.search.expr.SearchExpressions;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * {@link MethodBuilder} creating {@link Compare} expressions.
 * 
 * @see SearchExpressionFactory#compareOp(com.top_logic.model.search.expr.CompareKind,
 *      SearchExpression, SearchExpression)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CompareBuilder extends AbstractSimpleMethodBuilder<Compare> {
	/**
	 * Creates a {@link CompareBuilder}.
	 */
	public CompareBuilder(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public Compare build(Expr expr, SearchExpression self, SearchExpression[] args)
			throws ConfigurationException {
		checkTwoArgs(expr, args);
		return SearchExpressions.compare(args[0], args[1]);
	}

}
