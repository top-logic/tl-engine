/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import java.util.Calendar;
import java.util.Date;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.layout.form.control.CalendarControl;
import com.top_logic.layout.form.control.CalendarMarker;
import com.top_logic.layout.form.control.DefaultCalendarMarker;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay.ItemDisplayType;
import com.top_logic.model.TLObject;
import com.top_logic.model.annotate.ui.MarkerFactory;
import com.top_logic.model.annotate.ui.MarkerFactoryAnnotation;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * Configurable {@link MarkerFactory} using TL-Script expressions.
 * 
 * @see MarkerFactoryAnnotation
 * 
 * @author <a href="mailto:Diana.Pankratz@top-logic.com">Diana Pankratz</a>
 */
public class MarkerFactoryByExpression<C extends MarkerFactoryByExpression.Config<?>>
		extends AbstractConfiguredInstance<C> implements MarkerFactory{

	/**
	 * Configuration options for {@link MarkerFactoryByExpression}.
	 */
	@DisplayOrder({ Config.LOWER_BOUND, Config.UPPER_BOUND, Config.DISABLED, Config.OVERLAP, Config.IN_RANGE,
		Config.OUT_OF_RANGE })
	public interface Config<I extends MarkerFactoryByExpression<?>> extends PolymorphicConfiguration<I> {

		/**
		 * Lower bound of the time range.
		 */
		public static final String LOWER_BOUND = "lower-bound";

		/**
		 * Upper bound of the time range.
		 */
		public static final String UPPER_BOUND = "upper-bound";

		/**
		 * Disables {@link CalendarControl} fields.
		 */
		public static final String DISABLED = "disabled";

		/**
		 * Accepts overlapping time ranges as in range.
		 */
		public static final String OVERLAP = "overlap";

		/**
		 * CSS-Class for in range {@link CalendarControl} fields.
		 */
		public static final String IN_RANGE = "in-range-css";

		/**
		 * CSS-Class for out of range {@link CalendarControl} fields.
		 */
		public static final String OUT_OF_RANGE = "out-of-range-css";

		/**
		 * The getter function retrieving the lower bound date value.
		 * 
		 * <p>
		 * The expression is expected to be a one-argument function receiving the object with the
		 * date attribute.
		 * </p>
		 */
		@ItemDisplay(ItemDisplayType.VALUE)
		@Name(LOWER_BOUND)
		Expr getLowerBound();

		/**
		 * The getter function retrieving the upper bound date value.
		 * 
		 * <p>
		 * The expression is expected to be a one-argument function receiving the object with the
		 * date attribute.
		 * </p>
		 */
		@ItemDisplay(ItemDisplayType.VALUE)
		@Name(UPPER_BOUND)
		Expr getUpperBound();



		/**
		 * The getter function retrieving the value of the boolean "disabled"
		 * 
		 * <p>
		 * If disabled is true all {@link CalendarControl} fields out of range will be disabled a
		 * cannot be selected
		 * </p>
		 */
		@Name(DISABLED)
		boolean getDisabled();

		/**
		 * The getter function retrieving the value of the CSS-Class for {@link CalendarControl}
		 * fields in time range.
		 */
		@Name(IN_RANGE)
		String getInRangeCss();

		/**
		 * The getter function retrieving the value of the CSS-Class for {@link CalendarControl}
		 * fields out of time range.
		 */
		@Name(OUT_OF_RANGE)
		String getOutOfRangeCss();

		/**
		 * The getter function retrieving the value of the boolean "overlap".
		 * 
		 * <p>
		 * When overlap is true given {@link CalendarControl} fields don't have to be completely in
		 * the range of upperBound and lowerBound to be "in range".
		 * </p>
		 */
		@Name(OVERLAP)
		boolean getOverlap();
	}

	private final QueryExecutor _upperBound;

	private final QueryExecutor _lowerBound;

	/**
	 * Creates a {@link MarkerFactoryByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public MarkerFactoryByExpression(InstantiationContext context, C config) {
		super(context, config);

		_lowerBound = QueryExecutor.compile(config.getLowerBound());
		_upperBound = QueryExecutor.compile(config.getUpperBound());
	}

	/**
	 * The getter function retrieving the date of {@link #_lowerBound} from TL-Script.
	 * 
	 * @param object
	 *        Object with date attribute.
	 * @return Date of the lower bound.
	 */

	public Date getLowerBoundValue(Object object) {
		return (Date) _lowerBound.execute(object);
	}


	/**
	 * The getter function retrieving the date of {@link #_upperBound} from TL-Script.
	 * 
	 * @param object
	 *        Object with date attribute.
	 * @return Date of the upper Bound.
	 */
	public Date getUpperBoundValue(Object object) {
		return (Date) _upperBound.execute(object);
	}

	/**
	 * Creates and returns a {@link CalendarMarker} with the given Parameters.
	 * @param object
	 *        The object containing the date attribute.
	 * @param field
	 *        The date field.
	 * 
	 * @return The configured {@link CalendarMarker}.
	 * 
	 * @see MarkerFactory#getCalendarMarker(TLObject, ComplexField)
	 */
	@Override
	public CalendarMarker getCalendarMarker(TLObject object, ComplexField field) {
		Date lowerBoundDate = getLowerBoundValue(object);
		Date upperBoundDate = getUpperBoundValue(object);
		Calendar lowerBoundCalendar = null;
		Calendar upperBoundCalendar = null;
		if (lowerBoundDate != null) {
			lowerBoundCalendar = CalendarUtil.createCalendar(lowerBoundDate);
		}
		if (upperBoundDate != null) {
			upperBoundCalendar = CalendarUtil.createCalendar(upperBoundDate);
		}
		boolean disabled = getConfig().getDisabled();
		String inRangeCss = getConfig().getInRangeCss();
		String outOfRangeCss = getConfig().getOutOfRangeCss();
		boolean overlap = getConfig().getOverlap();
		return new DefaultCalendarMarker(lowerBoundCalendar, upperBoundCalendar, disabled, inRangeCss, outOfRangeCss,
			overlap);
	}

}
