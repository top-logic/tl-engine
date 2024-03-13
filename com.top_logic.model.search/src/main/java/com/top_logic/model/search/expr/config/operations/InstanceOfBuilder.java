/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import static com.top_logic.model.search.expr.SearchExpressionFactory.*;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.search.expr.InstanceOf;
import com.top_logic.model.search.expr.DynamicInstanceOf;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SearchExpressionFactory;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.interpreter.ConstantFolding;

/**
 * {@link MethodBuilder} creating {@link InstanceOf} expressions.
 * 
 * @see SearchExpressionFactory#instanceOf(SearchExpression, com.top_logic.model.TLStructuredType)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class InstanceOfBuilder extends TwoArgsMethodBuilder<SearchExpression> {

	/**
	 * Creates a {@link InstanceOfBuilder}.
	 */
	public InstanceOfBuilder(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	protected SearchExpression internalBuild(Expr expr, SearchExpression arg0, SearchExpression arg1,
			SearchExpression[] allArgs)
			throws ConfigurationException {
		if (ConstantFolding.isLiteral(arg1)) {
			return instanceOf(arg0, resolveStructuredType(expr, arg1));
		} else {
			return new DynamicInstanceOf(getConfig().getName(), allArgs);
		}
	}

}
