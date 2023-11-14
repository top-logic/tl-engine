/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * {@link MethodBuilder} for methods with three arguments.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ThreeArgsMethodBuilder<E extends SearchExpression> extends AbstractSimpleMethodBuilder<E> {
	/**
	 * Creates a {@link ThreeArgsMethodBuilder}.
	 */
	public ThreeArgsMethodBuilder(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public E build(Expr expr, SearchExpression[] args)
			throws ConfigurationException {
		checkThreeArgs(expr, args);
		return internalBuild(expr, args[0], args[1], args[2]);
	}

	/**
	 * Implementation of {@link #build(Expr, SearchExpression[])}
	 */
	protected abstract E internalBuild(Expr expr, SearchExpression arg0,
			SearchExpression arg1, SearchExpression arg2) throws ConfigurationException;

}
