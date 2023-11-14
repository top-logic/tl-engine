/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;

/**
 * Concatenation of argument lists.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Concat extends SimpleGenericMethod {

	/**
	 * Creates a {@link Concat}.
	 */
	protected Concat(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new Concat(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return argumentTypes.get(0);
	}

	@Override
	public Object eval(Object[] arguments) {
		List<Object> result = new ArrayList<>();
		for (Object arg : arguments) {
			add(result, arg);
		}
		return result;
	}

	private void add(List<Object> result, Object value) {
		if (value instanceof Collection<?>) {
			result.addAll((Collection<?>) value);
		} else if (value == null) {
			// Ignore.
		} else {
			result.add(value);
		}
	}

	/**
	 * {@link MethodBuilder} creating {@link Concat}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<Concat> {
		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public Concat build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			return new Concat("concat", args);
		}

	}

}
