/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.regex;

import java.util.List;
import java.util.regex.Pattern;

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
 * {@link GenericMethod} creating a regular expression pattern from a string.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Regex extends SimpleGenericMethod {

	/**
	 * Creates a {@link Regex}.
	 */
	protected Regex(String name, SearchExpression self, SearchExpression[] arguments) {
		super(name, self, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression self, SearchExpression[] arguments) {
		return new Regex(getName(), self, arguments);
	}

	@Override
	public TLType getType(TLType selfType, List<TLType> argumentTypes) {
		return null;
	}

	@Override
	public Object eval(Object self, Object[] arguments) {
		String pattern = asString(notNull(getArguments()[0], arguments[0]));
		return Pattern.compile(pattern);
	}

	/**
	 * {@link MethodBuilder} creating {@link Regex}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<Regex> {
		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public Regex build(Expr expr, SearchExpression self, SearchExpression[] args) throws ConfigurationException {
			checkSingleArg(expr, args);
			return new Regex(getConfig().getName(), self, args);
		}

	}
}
