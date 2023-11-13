/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;

/**
 * Constructor for a {@link DecimalFormat} with pattern argument.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NumerFormatExpr extends GenericMethod {

	/**
	 * Creates a {@link NumerFormatExpr}.
	 */
	protected NumerFormatExpr(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new NumerFormatExpr(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return null;
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		if (arguments[0] == null) {
			return null;
		}
		return new DecimalFormat(asString(arguments[0]), DecimalFormatSymbols.getInstance(ThreadContext.getLocale()));
	}

	/**
	 * {@link MethodBuilder} for {@link NumerFormatExpr}.
	 */
	public static class Builder extends AbstractSimpleMethodBuilder<NumerFormatExpr> {

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public NumerFormatExpr build(Expr expr, SearchExpression self, SearchExpression[] args)
				throws ConfigurationException {
			checkSingleArg(expr, args);
			return new NumerFormatExpr(getName(), args);
		}

	}

}
