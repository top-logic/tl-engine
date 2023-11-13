/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.reductions;

import static com.top_logic.model.util.TLModelUtil.*;

import java.util.Arrays;
import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.TLType;
import com.top_logic.model.builtin.TLCore;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SimpleGenericMethod;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;

/**
 * Returns the sum of the values in the argument list.
 * <p>
 * The result is a double to avoid overflows.
 * </p>
 *
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class Sum extends SimpleGenericMethod {

	/** Creates a {@link Sum}. */
	protected Sum(String name, SearchExpression self, SearchExpression[] arguments) {
		super(name, self, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression self, SearchExpression[] arguments) {
		return new Sum(getName(), self, arguments);
	}

	@Override
	public TLType getType(TLType selfType, List<TLType> argumentTypes) {
		return findType(TLCore.TL_CORE, "Double");
	}

	@Override
	public Double eval(Object self, Object[] arguments) {
		return sum(this, arguments);
	}

	/**
	 * Evaluates the sum function on the given arguments.
	 */
	public static double sum(SearchExpression context, Object[] arguments) {
		return sumIterable(context, Arrays.asList(arguments));
	}

	private static double sumIterable(SearchExpression context, Iterable<?> arguments) {
		double result = 0.0;
		for (Object arg : arguments) {
			result += sumAny(context, arg);
		}
		return result;
	}

	private static double sumAny(SearchExpression context, Object arg) {
		if (arg instanceof Iterable<?>) {
			return sumIterable(context, (Iterable<?>) arg);
		} else {
			return asDouble(context, arg);
		}
	}

	/** {@link MethodBuilder} creating {@link Sum}. */
	public static final class Builder extends AbstractSimpleMethodBuilder<Sum> {

		/** Creates a {@link Builder}. */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public Sum build(Expr expr, SearchExpression self, SearchExpression[] args)
				throws ConfigurationException {
			return new Sum("sum", self, args);
		}

	}

}
