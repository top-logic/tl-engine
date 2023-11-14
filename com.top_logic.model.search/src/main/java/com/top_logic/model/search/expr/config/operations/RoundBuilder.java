/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import static com.top_logic.model.search.expr.SearchExpressionFactory.*;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.search.expr.Round;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SearchExpressionFactory;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * {@link MethodBuilder} creating {@link Round} expressions.
 * 
 * @see SearchExpressionFactory#round(SearchExpression, SearchExpression)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class RoundBuilder extends AbstractSimpleMethodBuilder<SearchExpression> {

	private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
		.mandatory("value")
		.optional("precision", 0)
		.build();

	/**
	 * Creates a {@link RoundBuilder}.
	 */
	public RoundBuilder(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public SearchExpression build(Expr expr, SearchExpression[] args)
			throws ConfigurationException {
		return round(args[0], args[1]);
	}

	@Override
	public ArgumentDescriptor descriptor() {
		return DESCRIPTOR;
	}

}
