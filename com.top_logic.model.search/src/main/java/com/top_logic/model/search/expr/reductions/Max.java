/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.reductions;

import java.lang.reflect.Array;
import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SimpleGenericMethod;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;

/**
 * Returns the maximum value from the argument list.
 *
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class Max extends SimpleGenericMethod {

	/** Creates a {@link Max}. */
	protected Max(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new Max(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return argumentTypes.get(0);
	}

	@Override
	public Object eval(Object[] arguments) {
		return new Evaluator().add(arguments).getValue();
	}

	class Evaluator {
		boolean _hasValue = false;

		double _value;

		public Double getValue() {
			return _hasValue ? Double.valueOf(_value) : null;
		}

		public Evaluator add(Object arg) {
			if (arg != null) {
				if (arg instanceof Iterable<?>) {
					addIterable((Iterable<?>) arg);
				} else if (arg.getClass().isArray()) {
					addArray(arg);
				} else {
					double next = asDouble(arg);
					if (_hasValue) {
						_value = Math.max(_value, next);
					} else {
						_hasValue = true;
						_value = next;
					}
				}
			}
			return this;
		}

		private Evaluator addIterable(Iterable<?> arguments) {
			for (Object arg : arguments) {
				add(arg);
			}
			return this;
		}

		private Evaluator addArray(Object array) {
			for (int n = 0, cnt = Array.getLength(array); n < cnt; n++) {
				add(Array.get(array, n));
			}
			return this;
		}
	}

	/** {@link MethodBuilder} creating {@link Max}. */
	public static final class Builder extends AbstractSimpleMethodBuilder<Max> {

		/** Creates a {@link Builder}. */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public Max build(Expr expr, SearchExpression self, SearchExpression[] args)
				throws ConfigurationException {
			return new Max("max", args);
		}

	}

}
