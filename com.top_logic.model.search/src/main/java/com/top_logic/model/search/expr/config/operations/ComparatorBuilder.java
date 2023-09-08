/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import static com.top_logic.model.search.expr.SearchExpressionFactory.*;
import static com.top_logic.model.search.expr.SearchExpressions.*;

import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * Short-cut for creating a comparator function from an accessor functions.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ComparatorBuilder extends AbstractSimpleMethodBuilder<SearchExpression> {
	/**
	 * Creates a {@link ComparatorBuilder}.
	 */
	public ComparatorBuilder(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public SearchExpression build(Expr expr, SearchExpression self, SearchExpression[] args)
			throws ConfigurationException {
		checkNoArguments(expr, self, args);
		return comparator(self);
	}

	/**
	 * Builds a comparator function from an accessor function.
	 */
	public static SearchExpression comparator(SearchExpression accessor) {
		Object o1 = new NamedConstant("o1");
		Object o2 = new NamedConstant("o2");
		Object fun = new NamedConstant("fun");
		return lambda(o1,
			lambda(o2,
				let(fun, accessor,
					compare(
						call(var(fun), var(o1)),
						call(var(fun), var(o2))))));
	}

}
