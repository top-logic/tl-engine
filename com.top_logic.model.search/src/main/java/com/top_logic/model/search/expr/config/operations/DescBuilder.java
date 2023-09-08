/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import static com.top_logic.model.search.expr.SearchExpressionFactory.*;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.search.expr.Desc;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SearchExpressionFactory;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * {@link MethodBuilder} creating {@link Desc} expressions.
 * 
 * @see SearchExpressionFactory#desc(SearchExpression)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DescBuilder extends AbstractSimpleMethodBuilder<Desc> {
	/**
	 * Creates a {@link DescBuilder}.
	 */
	public DescBuilder(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public Desc build(Expr expr, SearchExpression self, SearchExpression[] args)
			throws ConfigurationException {
		checkNoArguments(expr, self, args);
		return desc(self);
	}

}
