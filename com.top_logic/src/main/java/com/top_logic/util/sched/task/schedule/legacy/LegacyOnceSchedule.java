/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.task.schedule.legacy;

import static com.top_logic.util.sched.task.schedule.legacy.LegacySchedulesCommon.*;

import java.util.Calendar;

import org.w3c.dom.Document;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.ConfigurationErrorProtocol;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.util.sched.task.TaskCommon;
import com.top_logic.util.sched.task.impl.TaskImpl;
import com.top_logic.util.sched.task.schedule.AbstractSchedulingAlgorithm;
import com.top_logic.util.sched.task.schedule.SchedulingAlgorithm;

/**
 * The old code of the <code>TaskImpl.calcOnce(Calendar)</code> method, before it was rewritten as a
 * {@link SchedulingAlgorithm}.
 * <p>
 * Extracted to this class, in case someone still needs it. Use this class only if switching to the
 * new code is too dangerous and the old code should be used because it is already in use for many
 * years and its behavior is well known. <br/>
 * <b>It is unclear what this code is intended to do exactly, and what it actually does. But it
 * probably has some bugs.</b><br/>
 * It allows to schedule a task once after each server startup.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@Deprecated
public class LegacyOnceSchedule extends AbstractSchedulingAlgorithm<LegacyOnceSchedule.Config> {

	/** {@link TypedConfiguration} for the {@link LegacyOnceSchedule}. */
	public interface Config extends AbstractSchedulingAlgorithm.Config<LegacyOnceSchedule> {

		/**
		 * @see #getHour()
		 */
		String HOUR_PROPERTY = "hour";

		/**
		 * @see #getMinute()
		 */
		String MINUTE_PROPERTY = "minute";

		/**
		 * At what hour (0..23) shall the task run (or start)
		 */
		@Name(HOUR_PROPERTY)
		int getHour();

		/**
		 * At what minute (0..59) shall the task run (or start)
		 */
		@Name(MINUTE_PROPERTY)
		int getMinute();

	}

	/**
	 * The name of the {@link FormField} presenting either {@link Config#getHour()} and
	 * {@link Config#getMinute()}.
	 */
	public static final String NAME_FIELD_TIME = NAME_FIELD_PREFIX + "Time";

	private static final Document TEMPLATE = DOMUtil.parseThreadSafe(""
		+ "	<table " + templateRootAttributes() + " >"
		+ templateStandardFields()
		+ "		<tr>"
		+ templateLargeField(NAME_FIELD_TIME) // 'large', as it is the only field in this row.
		+ "		</tr>"
		+ "	</table>"
		);

	// Fields are only 'protected' and not 'private', because thats how they were in TaskImpl.
	// This allows subclass of TaskImpl that use these fields to easily migrate by
	// subclassing the corresponding LegacyFooSchedule class and using these fields.

	/** at what hour (0..23) shall the task run (or start) */
	protected int hour;

	/** at what minute (0..59) shall the task run (or start) */
	protected int minute;

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link LegacyOnceSchedule}.
	 * 
	 * @param context
	 *        For instantiation of dependent configured objects.
	 * @param config
	 *        The configuration of this {@link LegacyOnceSchedule}.
	 */
	@CalledByReflection
	public LegacyOnceSchedule(InstantiationContext context, Config config) {
		super(context, config);
		hour = config.getHour();
		minute = config.getMinute();
	}

	/**
	 * Needed by the legacy {@link TaskImpl} constructors.
	 * <p>
	 * Use {@link #LegacyOnceSchedule(InstantiationContext, Config)} instead.
	 * </p>
	 */
	@Deprecated
	public LegacyOnceSchedule(int hour, int minute) {
		super(createInstantiationContext(), createConfig());
		this.hour = hour;
		this.minute = minute;
	}

	private static InstantiationContext createInstantiationContext() {
		return new SimpleInstantiationContext(ConfigurationErrorProtocol.INSTANCE);
	}

	private static Config createConfig() {
		return TypedConfiguration.newConfigItem(Config.class);
	}

	@Override
	protected Maybe<Calendar> nextScheduleImpl(Calendar now, Maybe<Calendar> lastSchedule) {
		long nextShed = SchedulingAlgorithm.NO_SCHEDULE;
		long lastSched = TaskCommon.lastScheduleAsLong(lastSchedule);
		if (lastSched == SchedulingAlgorithm.NO_SCHEDULE) {
			Calendar calc = CalendarUtil.clone(now);
			long lnow = now.getTimeInMillis();
			calcStartTime(calc, hour, minute);

			// Is this time of day in the past ?
			nextShed = calc.getTimeInMillis();
			if (lnow > nextShed) {
				// do it next day
				calc.add(Calendar.DAY_OF_YEAR, 1);
				nextShed = calc.getTimeInMillis();
			}
		}
		return Maybe.some(CalendarUtil.createCalendar(nextShed));
	}

	@Override
	public Document getFormTemplateDocument() {
		return TEMPLATE;
	}

	@Override
	public void fillFormGroup(FormGroup group) {
		super.fillFormGroup(group);

		group.addMember(FormFactory.newStringField(
			NAME_FIELD_TIME, formatTime(hour, minute), FormFactory.IMMUTABLE));
	}

}
