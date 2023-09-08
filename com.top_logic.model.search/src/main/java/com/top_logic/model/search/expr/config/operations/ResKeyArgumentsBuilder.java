/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import static com.top_logic.model.search.expr.SearchExpressionFactory.*;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.search.expr.ResKeyArguments;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SearchExpressionFactory;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * {@link MethodBuilder} creating {@link ResKeyArguments} expressions.
 * 
 * @see SearchExpressionFactory#reskeyArguments(SearchExpression, SearchExpression...)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ResKeyArgumentsBuilder extends AbstractSimpleMethodBuilder<ResKeyArguments> {
	/**
	 * Creates a {@link ResKeyArgumentsBuilder}.
	 */
	public ResKeyArgumentsBuilder(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public ResKeyArguments build(Expr expr, SearchExpression self, SearchExpression[] args)
			throws ConfigurationException {
		return reskeyArguments(self, args);
	}

}
