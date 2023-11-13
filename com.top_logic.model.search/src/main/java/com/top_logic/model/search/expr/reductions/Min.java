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
 * Returns the minimum value from the argument list.
 *
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class Min extends SimpleGenericMethod {

	/** Creates a {@link Min}. */
	protected Min(String name, SearchExpression self, SearchExpression[] arguments) {
		super(name, self, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression self, SearchExpression[] arguments) {
		return new Min(getName(), self, arguments);
	}

	@Override
	public TLType getType(TLType selfType, List<TLType> argumentTypes) {
		return selfType;
	}

	@Override
	public Object eval(Object self, Object[] arguments) {
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
						_value = Math.min(_value, next);
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

	/** {@link MethodBuilder} creating {@link Min}. */
	public static final class Builder extends AbstractSimpleMethodBuilder<Min> {

		/** Creates a {@link Builder}. */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public Min build(Expr expr, SearchExpression self, SearchExpression[] args)
				throws ConfigurationException {
			return new Min("min", self, args);
		}

	}

}
