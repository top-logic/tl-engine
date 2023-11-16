/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import static com.top_logic.model.search.expr.SearchExpressionFactory.*;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.search.expr.Length;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SearchExpressionFactory;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * {@link MethodBuilder} creating {@link Length} expressions.
 * 
 * @see SearchExpressionFactory#length(SearchExpression)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LengthBuilder extends AbstractSimpleMethodBuilder<Length> {
	/**
	 * Creates a {@link LengthBuilder}.
	 */
	public LengthBuilder(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public Length build(Expr expr, SearchExpression[] args)
			throws ConfigurationException {
		checkSingleArg(expr, args);
		return length(args[0]);
	}

}
