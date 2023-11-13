/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.model.search.expr.CompareKind;
import com.top_logic.model.search.expr.CompareOp;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SearchExpressionFactory;
import com.top_logic.model.search.expr.SearchExpressions;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * {@link MethodBuilder} creating {@link CompareOp} expressions.
 * 
 * @see SearchExpressionFactory#compareOp(com.top_logic.model.search.expr.CompareKind,
 *      SearchExpression, SearchExpression)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CompareOpBuilder<C extends CompareOpBuilder.Config<?>> extends AbstractMethodBuilder<C, CompareOp> {

	/**
	 * Configuration options for {@link CompareOpBuilder}.
	 */
	public interface Config<I extends CompareOpBuilder<?>> extends AbstractMethodBuilder.Config<I> {
		/**
		 * The comparison operator.
		 */
		@Mandatory
		CompareKind getOperator();
	}

	/**
	 * Creates a {@link CompareOpBuilder} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public CompareOpBuilder(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public CompareOp build(Expr expr, SearchExpression self, SearchExpression[] args)
			throws ConfigurationException {
		checkTwoArgs(expr, args);
		return SearchExpressions.compareOp(operator(), args[0], args[1]);
	}

	private CompareKind operator() {
		return getConfig().getOperator();
	}

	@Override
	public Object getId() {
		return operator();
	}

}
