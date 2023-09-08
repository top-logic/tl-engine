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
import com.top_logic.basic.config.format.MillisFormatInt;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.time.TimeOfDayAsDateValueProvider;
import com.top_logic.basic.time.TimeUtil;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.util.sched.task.Task;

/**
 * {@link AbstractSchedulingAlgorithm} for running a {@link Task} multiple times every day.
 * <p>
 * The first schedule of a day is at {@link Config#getStartTime()}. The following schedules are at:
 * <code>startTime + n*interval</code> The schedule is not affected by manual runs of the task: If
 * the task should run for for the first time at 8:00 and then every hour but it is run manually at
 * 10:30, for example, the next run after that will be 11:00. Therefore, the task sticks to its
 * original schedule. This is similar to weekly tasks that should run every Sunday, but are not
 * affected if they are run manually at a Wednesday, for example.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@InApp
public class DailyPeriodicallySchedule<C extends DailyPeriodicallySchedule.Config<?>> extends AbstractSchedulingAlgorithm<DailyPeriodicallySchedule.Config<?>> {

	/** {@link TypedConfiguration} of {@link DailyPeriodicallySchedule}. */
	@TagName("periodically")
	public interface Config<S extends DailyPeriodicallySchedule<?>> extends AbstractSchedulingAlgorithm.Config<S> {

		/** Property name for {@link #getStartTime()} */
		String PROPERTY_NAME_START_TIME = "start-time";

		/** Property name for {@link #getStopTime()} */
		String PROPERTY_NAME_STOP_TIME = "stop-time";

		/** Property name for {@link #getInterval()} */
		String PROPERTY_NAME_INTERVAL = "interval";

		/** The default value for {@link #getStartTime()}: 0:0 */
		Date DEFAULT_START_TIME = createDate(0, 0);

		/** The default value for {@link #getStopTime()}: 23:59 */
		Date DEFAULT_STOP_TIME = createDate(23, 59);

		/**
		 * The time of day when the {@link Task} should start to be scheduled.
		 * <p>
		 * If it is not set, {@link #DEFAULT_START_TIME 00:00} is used. Has to be earlier than
		 * {@link #getStopTime()}.
		 * </p>
		 */
		@Format(TimeOfDayAsDateValueProvider.class)
		@Name(PROPERTY_NAME_START_TIME)
		Date getStartTime();

		/**
		 * The time of day when the {@link Task} should stop to be scheduled.
		 * <p>
		 * If it is not set, {@link #DEFAULT_STOP_TIME 23:59} is used. Has to be later than
		 * {@link #getStartTime()}.
		 * </p>
		 */
		@Format(TimeOfDayAsDateValueProvider.class)
		@Name(PROPERTY_NAME_STOP_TIME)
		Date getStopTime();

		/**
		 * The time between task runs.
		 * 
		 * @implNote Value in milliseconds.
		 */
		@Mandatory
		@Name(PROPERTY_NAME_INTERVAL)
		@Format(MillisFormatInt.class)
		int getInterval();

	}

	/**
	 * The common name prefix for all the {@link FormField}s created by this class.
	 * <p>
	 * Is used to avoid i18n clashes with other {@link SchedulingAlgorithm}s. The {@link FormField}s
	 * created by the superclass and potential subclasses have no or other such prefixes, i.e. they
	 * intentionally don't reuse this.
	 * </p>
	 */
	public static final String NAME_FIELD_PREFIX = DailyPeriodicallySchedule.class.getSimpleName();

	/**
	 * The name of the {@link FormField} presenting {@link Config#getStartTime()}.
	 */
	public static final String NAME_FIELD_START_TIME = NAME_FIELD_PREFIX + "StartTime";

	/**
	 * The name of the {@link FormField} presenting {@link Config#getStopTime()}.
	 */
	public static final String NAME_FIELD_STOP_TIME = NAME_FIELD_PREFIX + "StopTime";

	/**
	 * The name of the {@link FormField} presenting {@link Config#getInterval()}.
	 */
	public static final String NAME_FIELD_INTERVAL = NAME_FIELD_PREFIX + "Interval";

	private static final Document TEMPLATE = DOMUtil.parseThreadSafe(""
		+ "	<table " + templateRootAttributes() + " >"
		+ templateStandardFields()
		+ "		<tr>"
		+ templateSmallField(NAME_FIELD_START_TIME)
		+ templateSmallField(NAME_FIELD_STOP_TIME)
		+ "		</tr>"
		+ "		<tr>"
		+ templateLargeField(NAME_FIELD_INTERVAL)
		+ "		</tr>"
		+ "	</table>"
		);

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link DailyPeriodicallySchedule}.
	 * 
	 * @param context
	 *        For instantiation of dependent configured objects.
	 * @param config
	 *        The configuration of this {@link DailyPeriodicallySchedule}.
	 */
	@CalledByReflection
	public DailyPeriodicallySchedule(InstantiationContext context, C config) {
		super(context, config);
		checkConfig(context, config);
	}

	private static void checkConfig(InstantiationContext context, Config<?> config) {
		if (config.getInterval() <= 0) {
			context.error("The interval has to be > 0.");
		}

		Date startTime = config.getStartTime();
		if (startTime == null) {
			startTime = Config.DEFAULT_START_TIME;
		}
		Date stopTime = config.getStopTime();
		if (stopTime == null) {
			stopTime = Config.DEFAULT_STOP_TIME;
		}
		if (startTime.compareTo(stopTime) >= 0) {
			context.error("The start time has to be earlier than the stop time."
				+ " Start time: " + startTime + "; Stop time: " + stopTime);
		}
	}

	@Override
	public long nextSchedule(long notBefore, long lastSchedule) {
		Calendar now = CalendarUtil.createCalendar(notBefore);
		if (hasPassed(getStopTime(), now)) {
			Calendar calendar = calcStartTimeTomorrow(now);
			return calendar.getTimeInMillis();
		}
		if (!hasPassed(getStartTime(), now)) {
			Calendar startTime = calcStartTimeToday(now);
			return startTime.getTimeInMillis();
		}
		Calendar nextInterval = calcNextInterval(now);
		if (nextInterval.after(calcStopTimeToday(now))) {
			Calendar calendar = calcStartTimeTomorrow(now);
			return calendar.getTimeInMillis();
		}
		return nextInterval.getTimeInMillis();
	}

	/** Getter for {@link Config#getStartTime()}, which respects {@link Config#DEFAULT_START_TIME}. */
	public Date getStartTime() {
		Date startTime = getConfig().getStartTime();
		if (startTime == null) {
			return Config.DEFAULT_START_TIME;
		}
		return startTime;
	}

	/** Getter for {@link Config#getStopTime()}, which respects {@link Config#DEFAULT_STOP_TIME}. */
	public Date getStopTime() {
		Date stopTime = getConfig().getStopTime();
		if (stopTime == null) {
			return Config.DEFAULT_STOP_TIME;
		}
		return stopTime;
	}

	private Calendar calcNextInterval(Calendar now) {
		return CalendarUtil.createCalendar(calcNextIntervalLongs(now));
	}

	/** Must not be used if now < calcStartTimeToday(now). */
	private long calcNextIntervalLongs(Calendar now) {
		long nowMillis = now.getTimeInMillis();
		long startTimeMillis = calcStartTimeToday(now).getTimeInMillis();
		long diffInMillis = nowMillis - startTimeMillis;
		long residual = diffInMillis % getConfig().getInterval();
		if (residual == 0) {
			return nowMillis;
		}
		return (nowMillis - residual) + getConfig().getInterval();
	}

	private Calendar calcStartTimeTomorrow(Calendar now) {
		Calendar calendar = calcStartTimeToday(now);
		calendar.add(Calendar.DAY_OF_YEAR, 1);
		return calendar;
	}

	private Calendar calcStartTimeToday(Calendar now) {
		Calendar result = CalendarUtil.clone(now);
		applyTimeOfDay(getStartTime(), result);
		return result;
	}

	private Calendar calcStopTimeToday(Calendar now) {
		Calendar result = CalendarUtil.clone(now);
		applyTimeOfDay(getStopTime(), result);
		return result;
	}

	private boolean hasPassed(Date timeOfDay, Calendar now) {
		Calendar timeAsCalendar = CalendarUtil.clone(now);
		applyTimeOfDay(timeOfDay, timeAsCalendar);
		return timeAsCalendar.before(now);
	}

	/**
	 * <b>Private!</b>.
	 * <p>
	 * Made non-private to remove warning "Access to enclosing method createDate(int, int) from the
	 * type DailyPeriodicallySchedule is emulated by a synthetic accessor method".
	 * </p>
	 */
	static Date createDate(int hour, int minute) {
		Date result = new Date(0);
		result.setHours(hour);
		result.setMinutes(minute);
		return result;
	}

	@Override
	public Document getFormTemplateDocument() {
		return TEMPLATE;
	}

	@Override
	public void fillFormGroup(FormGroup group) {
		super.fillFormGroup(group);

		DateFormat timeFormat = HTMLFormatter.getInstance().getShortTimeFormat();
		group.addMember(FormFactory.newComplexField(
			NAME_FIELD_START_TIME, timeFormat, getStartTime(), FormFactory.IMMUTABLE));
		group.addMember(FormFactory.newComplexField(
			NAME_FIELD_STOP_TIME, timeFormat, getStopTime(), FormFactory.IMMUTABLE));
		group.addMember(FormFactory.newStringField(
			NAME_FIELD_INTERVAL, TimeUtil.formatMillisAsTime(getConfig().getInterval()), FormFactory.IMMUTABLE));
	}

}
