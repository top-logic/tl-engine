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
 * {@link GenericMethod} wrapping a given {@link Date} in a system {@link Calendar}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ToSystemCalendar extends AbstractDateMethod {

	/**
	 * Creates a {@link ToSystemCalendar}.
	 */
	protected ToSystemCalendar(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new ToSystemCalendar(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.DATE_TIME_TYPE);
	}

	@Override
	public boolean isSideEffectFree() {
		return false;
	}

	@Override
	public Object eval(Object[] arguments) {
		if (arguments[0] == null) {
			return null;
		}
		return CalendarUtil.createCalendar(asDate(arguments[0]));
	}

	/**
	 * {@link MethodBuilder} creating a {@link ToSystemCalendar}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<ToSystemCalendar> {
		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public ToSystemCalendar build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			checkSingleArg(expr, args);
			return new ToSystemCalendar(getName(), args);
		}

	}
}
