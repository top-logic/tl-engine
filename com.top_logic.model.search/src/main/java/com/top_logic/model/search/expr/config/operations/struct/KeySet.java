/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.struct;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.I18NConstants;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.util.error.TopLogicException;

/**
 * Transforms a {@link StructValue} to it's key set.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class KeySet extends GenericMethod {

	/**
	 * Creates a {@link KeySet}.
	 */
	protected KeySet(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new KeySet(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return null;
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		Object input = arguments[0];
		if (input == null) {
			return null;
		}
		if (input instanceof Map<?, ?>) {
			return asMap(input).keySet();
		}
		if (input instanceof ConfigurationItem) {
			return ((ConfigurationItem) input).descriptor().getProperties().stream().map(p -> p.getPropertyName())
				.collect(Collectors.toSet());
		}
		throw new TopLogicException(I18NConstants.ERROR_NOT_A_STRUCT__VAL_EXPR.fill(input, this));
	}

	/**
	 * {@link MethodBuilder} creating {@link KeySet}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<KeySet> {
		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public KeySet build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			checkSingleArg(expr, args);
			return new KeySet(getConfig().getName(), args);
		}

	}

}
