/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import static com.top_logic.model.search.expr.SearchExpressionFactory.*;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.search.expr.ContainsAll;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SearchExpressionFactory;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * {@link MethodBuilder} creating {@link ContainsAll} expressions.
 * 
 * @see SearchExpressionFactory#containsAll(SearchExpression, SearchExpression)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ContainsAllBuilder extends TwoArgsMethodBuilder<ContainsAll> {

	/**
	 * Creates a {@link ContainsAllBuilder}.
	 */
	public ContainsAllBuilder(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	protected ContainsAll internalBuild(Expr expr, SearchExpression arg0, SearchExpression arg1) {
		return containsAll(arg0, arg1);
	}

}
