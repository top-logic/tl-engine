/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.task.schedule;

import java.util.Calendar;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.constraint.impl.Positive;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;

/**
 * Base class for {@link SchedulingAlgorithm}s that schedule at a fixed time with a fixed period.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class FixedDatePeriodicalSchedulingAlgorithm<C extends FixedDatePeriodicalSchedulingAlgorithm.Config<?>>
		extends AbstractSchedulingAlgorithm<C> {

	/**
	 * Configuration for a {@link FixedDatePeriodicalSchedulingAlgorithm}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config<S extends FixedDatePeriodicalSchedulingAlgorithm<?>>
			extends AbstractSchedulingAlgorithm.Config<S> {

		/** Property name for {@link #getPeriod()} */
		String PROPERTY_NAME_PERIOD = "period";

		/**
		 * The period to use for scheduling next execution. E.g. when this algorithm schedules the
		 * task daily and the period is 2, then the task is executed every second day.
		 */
		@Name(PROPERTY_NAME_PERIOD)
		@IntDefault(1)
		@Constraint(value = Positive.class)
		int getPeriod();

	}

	/**
	 * The common name prefix for all the {@link FormField}s created by this class.
	 * <p>
	 * Is used to avoid i18n clashes with other {@link SchedulingAlgorithm}s. The {@link FormField}s
	 * created by the superclass and potential subclasses have no or other such prefixes, i.e. they
	 * intentionally don't reuse this.
	 * </p>
	 */
	private static final String NAME_FIELD_PREFIX = FixedDatePeriodicalSchedulingAlgorithm.class.getSimpleName();

	/**
	 * The name of the {@link FormField} presenting {@link Config#getPeriod()}.
	 */
	public static final String NAME_FIELD_PERIOD = NAME_FIELD_PREFIX + "Period";

	/**
	 * Creates a {@link FixedDatePeriodicalSchedulingAlgorithm} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public FixedDatePeriodicalSchedulingAlgorithm(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public long nextSchedule(long notBefore, long lastSchedule) {
		int period = getConfig().getPeriod();
		Calendar result;
		if (lastSchedule != NO_SCHEDULE) {
			result = CalendarUtil.createCalendar(lastSchedule);
			setScheduleTime(result);
			if (result.getTimeInMillis() <= lastSchedule) {
				addPeriod(result, period);
			}
			long nextMillis = result.getTimeInMillis();
			if (nextMillis >= notBefore) {
				return nextMillis;
			}

			result.setTimeInMillis(notBefore);
		} else {
			result = CalendarUtil.createCalendar(notBefore);
		}

		setScheduleTime(result);
		if (result.getTimeInMillis() < notBefore) {
			addPeriod(result, period);
		}
		return result.getTimeInMillis();
	}

	/**
	 * Sets the scheduling time in the given calendar.
	 * 
	 * <p>
	 * Do not care whether this moves the time forward or backwards.
	 * </p>
	 *
	 * @param result
	 *        The {@link Calendar} to modify.
	 */
	protected abstract void setScheduleTime(Calendar result);

	/**
	 * Adds the scheduling period to the given {@link Calendar}.
	 *
	 * @param result
	 *        The {@link Calendar} to modify.
	 * @param period
	 *        The number of periods for the scheduling.
	 */
	protected abstract void addPeriod(Calendar result, int period);

	/**
	 * Adds a field displaying the configured {@link Config#getPeriod()}.
	 * 
	 * @see #fillFormGroup(FormGroup)
	 */
	protected void addPeriodField(FormGroup group) {
		group.addMember(FormFactory.newIntField(
			NAME_FIELD_PERIOD, getConfig().getPeriod(), FormFactory.IMMUTABLE));
	}

}
