/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.search.expr.DynamicReferers;
import com.top_logic.model.search.expr.Referers;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SearchExpressionFactory;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.dom.Expr.Literal;

/**
 * {@link MethodBuilder} creating {@link Referers} or {@link DynamicReferers} expressions.
 * 
 * @see SearchExpressionFactory#referers(SearchExpression, com.top_logic.model.TLReference)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ReferersBuilder extends SingleArgMethodBuilder<SearchExpression> {

	/**
	 * Creates a {@link ReferersBuilder}.
	 */
	public ReferersBuilder(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	protected SearchExpression internalBuild(Expr expr, SearchExpression self, SearchExpression arg)
			throws ConfigurationException {
		if (arg instanceof Literal) {
			return SearchExpressionFactory.referers(self, resolveReference(expr, arg));
		} else {
			return new DynamicReferers(getName(), self, new SearchExpression[] { arg });
		}
	}

}
