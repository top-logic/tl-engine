/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import static com.top_logic.model.search.expr.SearchExpressionFactory.*;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.search.expr.IfElse;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SearchExpressionFactory;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * {@link MethodBuilder} creating {@link IfElse} expressions.
 * 
 * @see SearchExpressionFactory#flatten(SearchExpression)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class IfElseBuilder extends AbstractSimpleMethodBuilder<IfElse> {
	/**
	 * Creates a {@link IfElseBuilder}.
	 */
	public IfElseBuilder(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public IfElse build(Expr expr, SearchExpression self, SearchExpression[] args)
			throws ConfigurationException {
		checkArgs(expr, args, 1, 2);
		return ifElse(self, args[0], args.length >= 2 ? args[1] : literal(null));
	}

}
