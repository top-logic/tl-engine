/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.task.schedule;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.w3c.dom.Document;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Day;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.time.TimeOfDayAsDateValueProvider;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.util.sched.task.Task;

/**
 * {@link SchedulingAlgorithm} for running a {@link Task} once a week.
 * 
 * <p>
 * Use the {@link SchedulingAlgorithmCombinator} if it should run at multiple days of the week.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@InApp
public class WeeklySchedule<C extends WeeklySchedule.Config<?>>
		extends FixedDatePeriodicalSchedulingAlgorithm<WeeklySchedule.Config<?>> {

	/** {@link TypedConfiguration} of {@link WeeklySchedule}. */
	@TagName("weekly")
	public interface Config<S extends WeeklySchedule<?>> extends AbstractSchedulingAlgorithm.Config<S> {

		/** Property name for {@link #getDayOfWeek()} */
		String PROPERTY_NAME_DAY_OF_WEEK = "day-of-week";

		/** Property name for {@link #getTimeOfDay()} */
		String PROPERTY_NAME_TIME_OF_DAY = "time-of-day";

		/** The {@link Day Day of week} at which the {@link Task} should be scheduled. */
		@Mandatory
		@Name(PROPERTY_NAME_DAY_OF_WEEK)
		Day getDayOfWeek();

		/** The time of day when the {@link Task} should be scheduled. */
		@Mandatory
		@Format(TimeOfDayAsDateValueProvider.class)
		@Name(PROPERTY_NAME_TIME_OF_DAY)
		Date getTimeOfDay();

	}

	/**
	 * The common name prefix for all the {@link FormField}s created by this class.
	 * <p>
	 * Is used to avoid i18n clashes with other {@link SchedulingAlgorithm}s. The {@link FormField}s
	 * created by the superclass and potential subclasses have no or other such prefixes, i.e. they
	 * intentionally don't reuse this.
	 * </p>
	 */
	public static final String NAME_FIELD_PREFIX = WeeklySchedule.class.getSimpleName();

	/**
	 * The name of the {@link FormField} presenting {@link Config#getDayOfWeek()}.
	 */
	public static final String NAME_FIELD_DAY_OF_WEEK = NAME_FIELD_PREFIX + "DayOfWeek";

	/**
	 * The name of the {@link FormField} presenting {@link Config#getTimeOfDay()}.
	 */
	public static final String NAME_FIELD_TIME_OF_DAY = NAME_FIELD_PREFIX + "TimeOfDay";

	private static final Document TEMPLATE = DOMUtil.parseThreadSafe(""
		+ "	<table " + templateRootAttributes() + " >"
		+ templateStandardFields()
		+ "		<tr>"
		+ templateSmallField(NAME_FIELD_DAY_OF_WEEK)
		+ templateSmallField(NAME_FIELD_TIME_OF_DAY)
		+ "		</tr>"
		+ "	</table>"
		);

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link WeeklySchedule}.
	 * 
	 * @param context
	 *        For instantiation of dependent configured objects.
	 * @param config
	 *        The configuration of this {@link WeeklySchedule}.
	 */
	@CalledByReflection
	public WeeklySchedule(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	protected void setScheduleTime(Calendar result) {
		Config<?> config = getConfig();
		config.getDayOfWeek().applyTo(result);
		applyTimeOfDay(config.getTimeOfDay(), result);
	}

	@Override
	protected void addPeriod(Calendar result) {
		result.add(Calendar.WEEK_OF_YEAR, 1);
	}

	@Override
	public Document getFormTemplateDocument() {
		return TEMPLATE;
	}

	@Override
	public void fillFormGroup(FormGroup group) {
		super.fillFormGroup(group);

		List<Day> dayOfWeekOptions = Arrays.asList(Day.values());
		List<Day> dayOfWeekValue = Collections.singletonList(getConfig().getDayOfWeek());
		group.addMember(FormFactory.newSelectField(
			NAME_FIELD_DAY_OF_WEEK, dayOfWeekOptions, !FormFactory.MULTIPLE, dayOfWeekValue, FormFactory.IMMUTABLE));

		DateFormat timeOfDayFormat = HTMLFormatter.getInstance().getShortTimeFormat();
		group.addMember(FormFactory.newComplexField(
			NAME_FIELD_TIME_OF_DAY, timeOfDayFormat, getConfig().getTimeOfDay(), FormFactory.IMMUTABLE));
	}

}
