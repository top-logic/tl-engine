/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.task.schedule;

import java.text.DateFormat;
import java.util.Calendar;

import org.w3c.dom.Document;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.schedule.DailySchedule.TimeOfDayConfig;

/**
 * {@link SchedulingAlgorithm} for running a {@link Task} once a month.
 * 
 * <p>
 * Use the {@link SchedulingAlgorithmCombinator} if it should run at multiple days of the month.
 * </p>
 */
@InApp
public class MonthlySchedule<C extends MonthlySchedule.Config<?>>
		extends FixedDatePeriodicalSchedulingAlgorithm<MonthlySchedule.Config<?>> {

	/** {@link TypedConfiguration} of {@link MonthlySchedule}. */
	@TagName("monthly")
	@DisplayOrder({
		Config.PROPERTY_NAME_DAY_OF_MONTH,
		Config.PROPERTY_NAME_TIME_OF_DAY,
	})
	public interface Config<S extends MonthlySchedule<?>>
			extends AbstractSchedulingAlgorithm.Config<S>, TimeOfDayConfig {

		/** Property name for {@link #getDayOfMonth()} */
		String PROPERTY_NAME_DAY_OF_MONTH = "day-of-month";

		/**
		 * The day of the month on which the task should be scheduled. If the day falls outside the
		 * range of the actual month, it is adjusted so that it falls within it.
		 */
		@Mandatory
		@Name(PROPERTY_NAME_DAY_OF_MONTH)
		int getDayOfMonth();

	}

	/**
	 * The common name prefix for all the {@link FormField}s created by this class.
	 * <p>
	 * Is used to avoid i18n clashes with other {@link SchedulingAlgorithm}s. The {@link FormField}s
	 * created by the superclass and potential subclasses have no or other such prefixes, i.e. they
	 * intentionally don't reuse this.
	 * </p>
	 */
	public static final String NAME_FIELD_PREFIX = MonthlySchedule.class.getSimpleName();

	/**
	 * The name of the {@link FormField} presenting {@link Config#getDayOfMonth()}.
	 */
	public static final String NAME_FIELD_DAY_OF_MONTH = NAME_FIELD_PREFIX + "DayOfMonth";

	/**
	 * The name of the {@link FormField} presenting {@link Config#getTimeOfDay()}.
	 */
	public static final String NAME_FIELD_TIME_OF_DAY = NAME_FIELD_PREFIX + "TimeOfDay";

	private static final Document TEMPLATE = DOMUtil.parseThreadSafe(""
		+ "	<table " + templateRootAttributes() + " >"
		+ templateStandardFields()
		+ "		<tr>"
			+ templateSmallField(NAME_FIELD_DAY_OF_MONTH)
		+ templateSmallField(NAME_FIELD_TIME_OF_DAY)
		+ "		</tr>"
		+ "	</table>"
		);

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link MonthlySchedule}.
	 * 
	 * @param context
	 *        For instantiation of dependent configured objects.
	 * @param config
	 *        The configuration of this {@link MonthlySchedule}.
	 */
	@CalledByReflection
	public MonthlySchedule(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	protected void setScheduleTime(Calendar result) {
		setCorrectDayOfMonth(result);
		applyTimeOfDay(getConfig().getTimeOfDay(), result);
	}

	private void setCorrectDayOfMonth(Calendar result) {
		int dayOfMonth = Math.max(
			Math.min(
				getConfig().getDayOfMonth(),
				result.getActualMaximum(Calendar.DAY_OF_MONTH)),
			result.getActualMinimum(Calendar.DAY_OF_MONTH));
		result.set(Calendar.DAY_OF_MONTH, dayOfMonth);
	}

	@Override
	protected void addPeriod(Calendar result) {
		// Ensure that adding month does not roll calendar to large,e.g. 30.01. should not roll to
		// 02.03.
		result.set(Calendar.DAY_OF_MONTH, result.getGreatestMinimum(Calendar.DAY_OF_MONTH));
		result.add(Calendar.MONTH, 1);
		setCorrectDayOfMonth(result);
		
	}

	@Override
	public Document getFormTemplateDocument() {
		return TEMPLATE;
	}

	@Override
	public void fillFormGroup(FormGroup group) {
		super.fillFormGroup(group);

		group.addMember(FormFactory.newIntField(
			NAME_FIELD_DAY_OF_MONTH, getConfig().getDayOfMonth(), FormFactory.IMMUTABLE));

		DateFormat timeOfDayFormat = HTMLFormatter.getInstance().getShortTimeFormat();
		group.addMember(FormFactory.newComplexField(
			NAME_FIELD_TIME_OF_DAY, timeOfDayFormat, getConfig().getTimeOfDay(), FormFactory.IMMUTABLE));
	}

}
