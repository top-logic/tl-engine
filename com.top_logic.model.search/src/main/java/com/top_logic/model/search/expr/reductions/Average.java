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
 * Returns the average of the values in the argument list.
 * <p>
 * Returns null, if there are no values.
 * </p>
 *
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class Average extends SimpleGenericMethod {

	/** Creates a {@link Average}. */
	protected Average(String name, SearchExpression self, SearchExpression[] arguments) {
		super(name, self, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression self, SearchExpression[] arguments) {
		return new Average(getName(), self, arguments);
	}

	@Override
	public TLType getType(TLType selfType, List<TLType> argumentTypes) {
		return findType(TLCore.TL_CORE, "Double");
	}

	@Override
	public Double eval(Object[] arguments) {
		int size = size(this, arguments);
		if (size == 0) {
			return null;
		}
		double sum = Sum.sum(this, arguments);
		return sum / size;
	}

	/**
	 * Evaluates the sum function on the given arguments.
	 */
	public static int size(SearchExpression context, Object[] arguments) {
		return sizeIterable(context, Arrays.asList(arguments));
	}

	private static int sizeIterable(SearchExpression context, Iterable<?> arguments) {
		int result = 0;
		for (Object arg : arguments) {
			result += sizeAny(context, arg);
		}
		return result;
	}

	private static int sizeAny(SearchExpression context, Object arg) {
		if (arg instanceof Iterable<?>) {
			return sizeIterable(context, (Iterable<?>) arg);
		} else if (arg == null) {
			return 0;
		} else {
			return 1;
		}
	}

	/** {@link MethodBuilder} creating {@link Average}. */
	public static final class Builder extends AbstractSimpleMethodBuilder<Average> {

		/** Creates a {@link Builder}. */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public Average build(Expr expr, SearchExpression self, SearchExpression[] args)
				throws ConfigurationException {
			return new Average("average", self, args);
		}

	}

}
