/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;

/**
 * Constructor for a {@link DateFormat} with a pattern and an optional time zone argument.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DateFormatExpr extends GenericMethod {

	/**
	 * Creates a {@link DateFormatExpr}.
	 */
	protected DateFormatExpr(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new DateFormatExpr(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return null;
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		if (arguments[0] == null) {
			return null;
		}
		SimpleDateFormat format = new SimpleDateFormat(asString(arguments[0]), ThreadContext.getLocale());
		if (arguments[1] != null) {
			format.setTimeZone(asTimeZone(arguments[1]));
		}
		return format;
	}

	/**
	 * {@link DateFormatExpr} creates an {@link DateFormat} which depends on the locale of the user,
	 * so it can not evaluated at compile time.
	 */
	@Override
	public boolean canEvaluateAtCompileTime(Object[] arguments) {
		return false;
	}

	/**
	 * {@link MethodBuilder} for {@link DateFormatExpr}.
	 */
	public static class Builder extends AbstractSimpleMethodBuilder<DateFormatExpr> {

		/** Description of parameters for a {@link DateFormatExpr}. */
		public static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("pattern")
			.optional("timeZone")
			.build();

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public ArgumentDescriptor descriptor() {
			return DESCRIPTOR;
		}

		@Override
		public DateFormatExpr build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			return new DateFormatExpr(getName(), args);
		}

	}

}
