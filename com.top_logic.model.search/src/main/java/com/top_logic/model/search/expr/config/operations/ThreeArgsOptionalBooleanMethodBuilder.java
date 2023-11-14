/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import static com.top_logic.model.search.expr.SearchExpressionFactory.*;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * {@link MethodBuilder} for methods with two mandatory and an optional third boolean argument.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ThreeArgsOptionalBooleanMethodBuilder<E extends SearchExpression>
		extends AbstractSimpleMethodBuilder<E> {

	/**
	 * Creates a {@link ThreeArgsOptionalBooleanMethodBuilder}.
	 */
	public ThreeArgsOptionalBooleanMethodBuilder(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public E build(Expr expr, SearchExpression[] args)
			throws ConfigurationException {
		if (args.length < 2) {
			throw error(I18NConstants.ERROR_AT_LEAST_ONE_ARGUMENT_EXPECTED__EXPR.fill(toString(expr)));
		}
		if (args.length > 3) {
			throw error(I18NConstants.ERROR_AT_MOST_ARGUMENTS_EXPECTED__CNT_EXPR.fill(2, toString(expr)));
		}
		SearchExpression secondArg;
		if (args.length > 2) {
			secondArg = args[2];
		} else {
			secondArg = literal(false);
		}
		return internalBuild(expr, args[0], args[1], secondArg);
	}

	/**
	 * Implementation of {@link #build(Expr, SearchExpression[])}
	 */
	abstract E internalBuild(Expr expr, SearchExpression arg0,
			SearchExpression arg1, SearchExpression arg2) throws ConfigurationException;

}
