/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import static com.top_logic.model.search.expr.SearchExpressionFactory.*;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.search.expr.Call;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SearchExpressionFactory;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * {@link MethodBuilder} creating {@link Call} expressions.
 * 
 * @see SearchExpressionFactory#call(SearchExpression, SearchExpression)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ApplyBuilder extends AbstractSimpleMethodBuilder<Call> {

	/**
	 * Creates a {@link ApplyBuilder}.
	 */
	public ApplyBuilder(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public Call build(Expr expr, SearchExpression[] args) throws ConfigurationException {
		checkMinArgs(expr, args, 2);

		SearchExpression result = args[0];
		for (int i = 1; i < args.length; i++) {
			result = call(result, args[i]);
		}
		return (Call) result;
	}

}
