/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SearchExpressions;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * {@link MethodBuilder} creating {@link SearchExpression}s representing the empty set.
 * 
 * @see SearchExpressions#literalEmptySet()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class EmptySetBuilder extends AbstractSimpleMethodBuilder<SearchExpression> {
	/**
	 * Creates a {@link EmptySetBuilder}.
	 */
	public EmptySetBuilder(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public SearchExpression build(Expr expr, SearchExpression[] args)
			throws ConfigurationException {
		checkNoArguments(expr, args);
		return SearchExpressions.literalEmptySet();
	}

}
