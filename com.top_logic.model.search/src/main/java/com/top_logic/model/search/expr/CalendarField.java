/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.Calendar;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractMethodBuilder;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link GenericMethod} accessing a field of a {@link Calendar}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CalendarField extends SimpleGenericMethod {

	private Field _field;

	/**
	 * Specification of the {@link Calendar} field to access.
	 * 
	 * @see Calendar#get(int)
	 */
	public enum Field {
		/** See {@link Calendar#ERA } */
		ERA(Calendar.ERA),

		/** See {@link Calendar#YEAR} */
		YEAR(Calendar.YEAR),

		/** See {@link Calendar#MONTH} */
		MONTH(Calendar.MONTH), 

		/** See {@link Calendar#WEEK_OF_YEAR} */
		WEEK(Calendar.WEEK_OF_YEAR),

		/** See {@link Calendar#WEEK_OF_MONTH} */
		WEEK_OF_MONTH(Calendar.WEEK_OF_MONTH),

		/** See {@link Calendar#DAY_OF_MONTH} */
		DAY(Calendar.DAY_OF_MONTH), 

		/** See {@link Calendar#DAY_OF_YEAR} */
		DAY_OF_YEAR(Calendar.DAY_OF_YEAR),

		/** See {@link Calendar#DAY_OF_WEEK} */
		DAY_OF_WEEK(Calendar.DAY_OF_WEEK),

		/** See {@link Calendar#DAY_OF_WEEK_IN_MONTH} */
		DAY_OF_WEEK_IN_MONTH(Calendar.DAY_OF_WEEK_IN_MONTH),

		/** See {@link Calendar#HOUR_OF_DAY} */
		HOUR(Calendar.HOUR_OF_DAY),

		/** See {@link Calendar#AM_PM} */
		AM_PM(Calendar.AM_PM),

		/** See {@link Calendar#HOUR} */
		HOUR_12(Calendar.HOUR),

		/** See {@link Calendar#MINUTE} */
		MINUTE(Calendar.MINUTE), 

		/** See {@link Calendar#SECOND} */
		SECOND(Calendar.SECOND), 

		/** See {@link Calendar#MILLISECOND} */
		MILLISECOND(Calendar.MILLISECOND),

		/** See {@link Calendar#ZONE_OFFSET} */
		ZONE_OFFSET(Calendar.ZONE_OFFSET),

		/** See {@link Calendar#DST_OFFSET} */
		DST_OFFSET(Calendar.DST_OFFSET);

		private int _calField;

		/**
		 * Creates a {@link CalendarField.Field}.
		 */
		private Field(int calField) {
			_calField = calField;
		}

		/**
		 * The field value for {@link Calendar#get(int)}.
		 */
		public int getCalendarField() {
			return _calField;
		}
	}

	/**
	 * Creates a {@link CalendarField}.
	 */
	protected CalendarField(String name, Field field, SearchExpression self, SearchExpression[] arguments) {
		super(name, self, arguments);
		_field = field;
	}

	@Override
	public GenericMethod copy(SearchExpression self, SearchExpression[] arguments) {
		return new CalendarField(getName(), _field, self, arguments);
	}

	@Override
	public TLType getType(TLType selfType, List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.INTEGER_TYPE);
	}

	@Override
	public Object eval(Object self, Object[] arguments) {
		Calendar date = asCalendar(arguments[0]);
		if (date == null) {
			return null;
		}
		return toNumber(date.get(_field.getCalendarField()));
	}

	@Override
	public Object getId() {
		return _field;
	}

	/**
	 * {@link MethodBuilder} creating a {@link CalendarField}.
	 */
	public static class Builder<C extends Builder.Config<?>> extends AbstractMethodBuilder<C, CalendarField> {

		/**
		 * Configuration options for {@link CalendarField.Builder}.
		 */
		public interface Config<I extends Builder<?>> extends AbstractMethodBuilder.Config<I> {
			/**
			 * The {@link Calendar} field to access.
			 */
			Field getField();
		}

		private Field _field;

		/**
		 * Creates a {@link CalendarField.Builder} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public Builder(InstantiationContext context, C config) {
			super(context, config);
			_field = config.getField();
		}

		@Override
		public CalendarField build(Expr expr, SearchExpression self, SearchExpression[] args)
				throws ConfigurationException {
			checkSingleArg(expr, args);
			return new CalendarField(getName(), _field, self, args);
		}

		@Override
		public Object getId() {
			return _field;
		}

	}
}
