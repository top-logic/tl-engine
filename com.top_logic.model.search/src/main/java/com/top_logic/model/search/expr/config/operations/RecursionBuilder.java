/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.search.expr.Recursion;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SearchExpressionFactory;
import com.top_logic.model.search.expr.SearchExpressions;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * {@link MethodBuilder} creating {@link Recursion} expressions.
 * 
 * @see SearchExpressionFactory#recursion(SearchExpression, SearchExpression, SearchExpression,
 *      SearchExpression)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class RecursionBuilder extends AbstractSimpleMethodBuilder<Recursion> {

	private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
		.mandatory("start")
		.mandatory("fun")
		.optional("minDepth", 0)
		.optional("maxDepth", -1)
		.build();

	/**
	 * Creates a {@link RecursionBuilder}.
	 */
	public RecursionBuilder(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public ArgumentDescriptor descriptor() {
		return DESCRIPTOR;
	}

	@Override
	public Recursion build(Expr expr, SearchExpression[] args)
			throws ConfigurationException {
		return SearchExpressions.recursion(args[0], args[1], args[2], args[3]);
	}

}
