/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.task.schedule;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import org.w3c.dom.Document;

import com.top_logic.basic.CalledByReflection;
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
 * {@link SchedulingAlgorithm} for running a {@link Task} once every day at a given time.
 * 
 * <p>
 * Use either {@link DailyPeriodicallySchedule} or {@link SchedulingAlgorithmCombinator} if the the
 * task should run multiple times per day.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@InApp
public class DailySchedule<C extends DailySchedule.Config<?>>
		extends FixedDatePeriodicalSchedulingAlgorithm<DailySchedule.Config<?>> {

	/** {@link TypedConfiguration} of {@link DailySchedule}. */
	@TagName("daily")
	public interface Config<S extends DailySchedule<?>> extends AbstractSchedulingAlgorithm.Config<S> {

		/** Property name for {@link #getTimeOfDay()} */
		String PROPERTY_NAME_TIME_OF_DAY = "time-of-day";

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
	public static final String NAME_FIELD_PREFIX = DailySchedule.class.getSimpleName();

	/**
	 * The name of the {@link FormField} presenting {@link Config#getTimeOfDay()}.
	 */
	public static final String NAME_FIELD_TIME_OF_DAY = NAME_FIELD_PREFIX + "TimeOfDay";

	private static final Document TEMPLATE = DOMUtil.parseThreadSafe(""
		+ "	<table " + templateRootAttributes() + " >"
		+ templateStandardFields()
		+ "		<tr>"
		+ templateLargeField(NAME_FIELD_TIME_OF_DAY)
		+ "		</tr>"
		+ "	</table>"
		);

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link DailySchedule}.
	 * 
	 * @param context
	 *        For instantiation of dependent configured objects.
	 * @param config
	 *        The configuration of this {@link DailySchedule}.
	 */
	@CalledByReflection
	public DailySchedule(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	protected void setScheduleTime(Calendar result) {
		applyTimeOfDay(getConfig().getTimeOfDay(), result);
	}

	@Override
	protected void addPeriod(Calendar result) {
		result.add(Calendar.DAY_OF_YEAR, 1);
	}

	@Override
	public Document getFormTemplateDocument() {
		return TEMPLATE;
	}

	@Override
	public void fillFormGroup(FormGroup group) {
		super.fillFormGroup(group);

		DateFormat format = HTMLFormatter.getInstance().getShortTimeFormat();
		Object value = getConfig().getTimeOfDay();
		group.addMember(FormFactory.newComplexField(NAME_FIELD_TIME_OF_DAY, format, value, FormFactory.IMMUTABLE));
	}

}
