/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.Calendar;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.CalendarField.Field;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractMethodBuilder;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link GenericMethod} updating a {@link Calendar}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CalendarUpdate extends GenericMethod {

	private Field _field;

	private Op _op;

	/**
	 * Creates a {@link CalendarUpdate}.
	 */
	protected CalendarUpdate(String name, Field field, Op op, SearchExpression self, SearchExpression[] arguments) {
		super(name, self, arguments);
		_field = field;
		_op = op;
	}

	@Override
	public GenericMethod copy(SearchExpression self, SearchExpression[] arguments) {
		return new CalendarUpdate(getName(), _field, _op, self, arguments);
	}

	@Override
	public TLType getType(TLType selfType, List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.DATE_TIME_TYPE);
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		if (arguments[0] == null) {
			return null;
		}
		int amount = asInt(arguments[1]);

		Calendar calendar = asCalendar(arguments[0]);
		Calendar result = (Calendar) calendar.clone();
		_op.update(result, _field, amount);
		return result;
	}

	@Override
	public Object getId() {
		return new Pair<>(_field, _op);
	}

	/**
	 * {@link Calendar} update operation.
	 */
	public enum Op {
		/**
		 * Sets the {@link Calendar} field to the new value.
		 */
		SET {
			@Override
			public void update(Calendar target, Field field, int amount) {
				target.set(field.getCalendarField(), amount);
			}
		},

		/**
		 * Adds the given value to the given {@link Calendar} field.
		 */
		ADD {
			@Override
			public void update(Calendar target, Field field, int amount) {
				target.add(field.getCalendarField(), amount);
			}
		};

		/**
		 * Updates the given {@link Calendar}.
		 *
		 * @param target
		 *        The {@link Calendar} to modify.
		 * @param field
		 *        The field to update.
		 * @param amount
		 *        The update amount.
		 */
		public abstract void update(Calendar target, Field field, int amount);
	}

	/**
	 * {@link MethodBuilder} creating a {@link CalendarUpdate}.
	 */
	public static class Builder<C extends Builder.Config<?>> extends AbstractMethodBuilder<C, CalendarUpdate> {

		/**
		 * Configuration options for {@link CalendarUpdate.Builder}.
		 */
		public interface Config<I extends Builder<?>> extends AbstractMethodBuilder.Config<I> {
			/**
			 * The {@link Calendar} field to update.
			 */
			@Mandatory
			Field getField();

			/**
			 * The operation to perform.
			 */
			@Mandatory
			Op getOp();
		}

		private Field _field;

		private Op _op;

		/**
		 * Creates a {@link CalendarUpdate.Builder} from configuration.
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
			_op = config.getOp();
		}

		@Override
		public CalendarUpdate build(Expr expr, SearchExpression self, SearchExpression[] args)
				throws ConfigurationException {
			checkTwoArgs(expr, args);
			return new CalendarUpdate(getName(), _field, _op, self, args);
		}

		@Override
		public Object getId() {
			return new Pair<>(_field, _op);
		}

	}
}
