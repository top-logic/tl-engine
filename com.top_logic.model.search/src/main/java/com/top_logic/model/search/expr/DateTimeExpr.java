/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import static java.util.Calendar.*;

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
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link GenericMethod} constructing a {@link Date} from year, month, day, hour, minute, seconds
 * and milliseconds in user calendar.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DateTimeExpr extends GenericMethod {

	/**
	 * Creates a {@link DateTimeExpr}.
	 */
	protected DateTimeExpr(String name, SearchExpression self, SearchExpression[] arguments) {
		super(name, self, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression self, SearchExpression[] arguments) {
		return new DateTimeExpr(getName(), self, arguments);
	}

	@Override
	public TLType getType(TLType selfType, List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.DATE_TIME_TYPE);
	}

	@Override
	protected Object eval(Object self, Object[] arguments, EvalContext definitions) {
		Calendar calendar = CalendarUtil.createCalendarInUserTimeZone();
		setCalendarParts(this, calendar, arguments);
		return calendar.getTime();
	}

	private static void setCalendarParts(SearchExpression context, Calendar calendar, Object[] arguments) {
		int year = asInt(context, arguments[0]);
		int month = asInt(context, arguments[1]);
		int day = asInt(context, arguments[2]);
		int hour = asInt(context, arguments[3]);
		int minute = asInt(context, arguments[4]);
		int second = asInt(context, arguments[5]);
		int milli = asInt(context, arguments[6]);

		calendar.set(year, month, day, hour, minute, second);
		calendar.set(MILLISECOND, milli);
	}

	/**
	 * {@link MethodBuilder} creating {@link DateTimeExpr}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<DateTimeExpr> {

		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("year")
			.mandatory("month")
			.mandatory("day")
			.optional("hour", 0)
			.optional("minute", 0)
			.optional("second", 0)
			.optional("millisecond", 0)
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
		public DateTimeExpr build(Expr expr, SearchExpression self, SearchExpression[] args)
				throws ConfigurationException {
			return new DateTimeExpr(getName(), self, args);
		}

	}
}
