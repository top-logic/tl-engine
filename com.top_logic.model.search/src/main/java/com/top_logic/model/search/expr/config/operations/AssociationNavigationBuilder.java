/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import static com.top_logic.model.search.expr.SearchExpressionFactory.*;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.search.expr.AssociationNavigation;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SearchExpressions;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * {@link MethodBuilder} creating {@link AssociationNavigation} expressions.
 * 
 * @see SearchExpressions#associationNavigation(SearchExpression,
 *      com.top_logic.model.TLAssociationEnd, com.top_logic.model.TLAssociationEnd)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AssociationNavigationBuilder extends TwoArgMethodBuilder<AssociationNavigation> {

	/**
	 * Creates a {@link AssociationNavigationBuilder}.
	 */
	public AssociationNavigationBuilder(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	protected AssociationNavigation internalBuild(Expr expr, SearchExpression self, SearchExpression arg1,
			SearchExpression arg2) throws ConfigurationException {
		return associationNavigation(self,
			resolveEnd(expr, arg1),
			resolveEnd(expr, arg2));
	}

}
