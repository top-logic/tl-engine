/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import static com.top_logic.model.search.expr.SearchExpressionFactory.*;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.search.expr.ContainsSome;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SearchExpressionFactory;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * {@link MethodBuilder} creating {@link ContainsSome} expressions.
 * 
 * @see SearchExpressionFactory#foreach(SearchExpression, SearchExpression)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ContainsSomeBuilder extends TwoArgsMethodBuilder<ContainsSome> {

	/**
	 * Creates a {@link ContainsSomeBuilder}.
	 */
	public ContainsSomeBuilder(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	protected ContainsSome internalBuild(Expr expr, SearchExpression arg0, SearchExpression arg1) {
		return containsSome(arg0, arg1);
	}

}
