/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SimpleGenericMethod;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * {@link GenericMethod} creating a list of consecutive integer values.
 * 
 * <p>
 * The call <code>count(start, stop, step)</code> creates the list starting with <code>start</code>,
 * and ending just before <code>stop</code> (exclusive). Two consecutive numbers in the resulting
 * list differ by <code>step</code>. The <code>step</code> argument is optional and defaults to
 * <code>1</code>.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Count extends SimpleGenericMethod {

	/**
	 * Creates a {@link Count}.
	 */
	protected Count(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new Count(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return argumentTypes.get(0);
	}

	@Override
	public Object eval(Object[] arguments) {
		int start = asInt(arguments[0]);
		int stop = asInt(arguments[1]);
		int step;
		if (arguments.length > 2) {
			step = asInt(arguments[2]);
			if (step == 0) {
				// For safety reasons.
				step = 1;
			}
		} else {
			step = 1;
		}
		ArrayList<Double> result = new ArrayList<>(Math.max(0, stop - start));
		if (step > 0) {
			for (int n = start; n < stop; n += step) {
				result.add(SearchExpression.toNumber(n));
			}
		} else {
			for (int n = start; n > stop; n += step) {
				result.add(SearchExpression.toNumber(n));
			}
		}
		return result;
	}

	/**
	 * {@link MethodBuilder} creating {@link Count}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<Count> {
		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public Count build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			checkArgs(expr, args, 2, 3);
			return new Count(getConfig().getName(), args);
		}

	}
}
