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
	protected Concat(String name, SearchExpression self, SearchExpression[] arguments) {
		super(name, self, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression self, SearchExpression[] arguments) {
		return new Concat(getName(), self, arguments);
	}

	@Override
	public TLType getType(TLType selfType, List<TLType> argumentTypes) {
		return selfType;
	}

	@Override
	public Object eval(Object self, Object[] arguments) {
		List<Object> result = new ArrayList<>();
		add(result, self);
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
		public Concat build(Expr expr, SearchExpression self, SearchExpression[] args)
				throws ConfigurationException {
			return new Concat("concat", self, args);
		}
	}
}
