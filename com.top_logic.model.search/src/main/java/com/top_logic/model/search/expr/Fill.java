/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import static com.top_logic.model.search.expr.SearchExpressionFactory.*;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.util.TLContext;

/**
 * Adds dynamic arguments to a {@link ResKey}.
 * 
 * @see ResKey#internalFill(Object...)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Fill extends GenericMethod {

	/**
	 * The method name that invokes {@link Fill} from a search expression.
	 */
	public static final String METHOD_NAME = "fill";

	/**
	 * Creates a {@link Fill}.
	 * @param arguments
	 *        The arguments to set on the {@link ResKey}, see {@link #getArguments()}.
	 */
	Fill(SearchExpression[] arguments) {
		super(METHOD_NAME, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return SearchExpressionFactory.reskeyArguments(arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		// No type.
		return null;
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		Object input = arguments[0];
		if (input instanceof ResKey) {
			ResKey reskey = (ResKey) input;
			return reskey.asResKeyN().fill(Arrays.copyOfRange(arguments, 1, arguments.length));
		}
		if (input instanceof String) {
			String pattern = (String) input;
			MessageFormat format = new MessageFormat(pattern, TLContext.getLocale());
			return format.format(Arrays.copyOfRange(arguments, 1, arguments.length));
		}

		// Null, strange value?
		return input;
	}

	/**
	 * Factory for {@link Fill} methods.
	 */
	public static class Builder extends AbstractSimpleMethodBuilder<Fill> {
		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public Fill build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			return reskeyArguments(args);
		}

	}

}
