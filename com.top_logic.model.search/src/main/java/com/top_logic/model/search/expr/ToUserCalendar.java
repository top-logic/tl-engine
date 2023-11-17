/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link GenericMethod} wrapping a {@link Date} in a user {@link Calendar}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ToUserCalendar extends AbstractDateMethod {

	/**
	 * Creates a {@link ToUserCalendar}.
	 */
	protected ToUserCalendar(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new ToUserCalendar(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.DATE_TIME_TYPE);
	}

	@Override
	public boolean isSideEffectFree() {
		return false;
	}

	/**
	 * The timezone of the server may change, so the expression must not be evaluated at compile
	 * time.
	 */
	@Override
	public boolean canEvaluateAtCompileTime(Object[] arguments) {
		return arguments[0] == null;
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		if (arguments[0] == null) {
			return null;
		}
		Calendar result = CalendarUtil.createCalendarInUserTimeZone();
		result.setTime(asDate(arguments[0]));
		return result;
	}

	/**
	 * {@link MethodBuilder} creating {@link ToUserCalendar}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<ToUserCalendar> {
		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public ToUserCalendar build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			checkSingleArg(expr, args);
			return new ToUserCalendar(getName(), args);
		}

	}
}
