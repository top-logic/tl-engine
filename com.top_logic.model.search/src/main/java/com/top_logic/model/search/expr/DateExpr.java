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
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link GenericMethod} constructing a {@link Date} from year, month and day in the system
 * calendar.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DateExpr extends SimpleGenericMethod {

	/**
	 * Creates a {@link DateExpr}.
	 */
	protected DateExpr(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new DateExpr(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.DATE_TYPE);
	}

	@Override
	public Object eval(Object[] arguments) {
		Calendar calendar = CalendarUtil.createCalendar();
		setCalendarParts(this, calendar, arguments);
		return calendar.getTime();
	}

	private static void setCalendarParts(SearchExpression context, Calendar calendar, Object[] arguments) {
		int year = asInt(context, arguments[0]);
		int month = asInt(context, arguments[1]);
		int day = asInt(context, arguments[2]);
		calendar.set(year, month, day);
	}

	/**
	 * {@link MethodBuilder} creating {@link DateExpr}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<DateExpr> {

		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("year")
			.mandatory("month")
			.mandatory("day")
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
		public DateExpr build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			return new DateExpr(getName(), args);
		}

	}
}
