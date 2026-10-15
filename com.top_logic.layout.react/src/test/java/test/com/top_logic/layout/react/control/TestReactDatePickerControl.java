/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.format.configured.FormatterService;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.form.FieldValueArguments;
import com.top_logic.layout.react.control.form.ReactDatePickerControl;
import com.top_logic.layout.react.control.form.ReactDatePickerControl.Kind;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.FieldState;
import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * Tests that {@link ReactDatePickerControl} exchanges a point in time with the HTML input in its
 * ISO form without losing a part of it.
 */
public class TestReactDatePickerControl extends TestCase {

	/**
	 * A date and time without seconds is written without them, the form the input sends for it.
	 */
	public void testDateTimeWithoutSeconds() {
		assertEquals("2026-10-15T14:30", control(Kind.DATE_TIME, time(2026, 10, 15, 14, 30, 0)).state());
	}

	/**
	 * A date and time with seconds is written with them, so the input shows and returns them.
	 */
	public void testDateTimeWithSeconds() {
		assertEquals("2026-10-15T14:30:45", control(Kind.DATE_TIME, time(2026, 10, 15, 14, 30, 45)).state());
	}

	/** A time of day with seconds is written with them. */
	public void testTimeWithSeconds() {
		assertEquals("14:30:45", control(Kind.TIME, time(1970, 1, 1, 14, 30, 45)).state());
		assertEquals("14:30", control(Kind.TIME, time(1970, 1, 1, 14, 30, 0)).state());
	}

	/** A date and time entered with seconds keeps them. */
	public void testDateTimeEnteredWithSeconds() {
		DateControl field = control(Kind.DATE_TIME, null);

		field.type("2026-10-16T09:45:12");

		assertEquals(time(2026, 10, 16, 9, 45, 12), field.getFieldModel().getValue());
		assertEquals("2026-10-16T09:45:12", field.state());
	}

	/** A date and time entered without seconds is read as well. */
	public void testDateTimeEnteredWithoutSeconds() {
		DateControl field = control(Kind.DATE_TIME, null);

		field.type("2026-10-16T09:45");

		assertEquals(time(2026, 10, 16, 9, 45, 0), field.getFieldModel().getValue());
	}

	/** A time of day entered with seconds keeps them. */
	public void testTimeEnteredWithSeconds() {
		DateControl field = control(Kind.TIME, null);

		field.type("09:45:12");

		Calendar calendar = Calendar.getInstance(ThreadContext.getTimeZone());
		calendar.setTime((Date) field.getFieldModel().getValue());
		assertEquals(9, calendar.get(Calendar.HOUR_OF_DAY));
		assertEquals(45, calendar.get(Calendar.MINUTE));
		assertEquals(12, calendar.get(Calendar.SECOND));
	}

	/** Text following a value no pattern reads entirely is rejected. */
	public void testTrailingTextIsRejected() {
		Date before = time(2026, 1, 1, 0, 0, 0);
		DateControl field = control(Kind.DATE_TIME, before);

		field.type("2026-10-16T09:45:12x");

		assertTrue(((AbstractFieldModel) field.getFieldModel()).hasError());
		assertEquals(before, field.getFieldModel().getValue());
	}

	private static Date time(int year, int month, int day, int hour, int minute, int second) {
		Calendar calendar = Calendar.getInstance(ThreadContext.getTimeZone());
		calendar.clear();
		calendar.set(year, month - 1, day, hour, minute, second);
		return calendar.getTime();
	}

	private static DateControl control(Kind kind, Date value) {
		ReactContext context = new DefaultReactContext("", "test", new SSEUpdateQueue(),
			new ReactWindowRegistry("test"));
		return new DateControl(context, new AbstractFieldModel(value), kind);
	}

	/**
	 * A {@link ReactDatePickerControl} exposing its server-side value state for assertions.
	 */
	private static final class DateControl extends ReactDatePickerControl {

		DateControl(ReactContext context, FieldModel model, Kind kind) {
			super(context, model, kind);
		}

		Object state() {
			return getState(FieldState.VALUE__PROP);
		}

		void type(String text) {
			executeClientCommand(ReactFormFieldControl.CMD_VALUE_CHANGED,
				Map.of(FieldValueArguments.VALUE, text));
		}
	}

	/**
	 * The test suite, started with the formats the display value needs and the resource bundles a
	 * rejected input needs for its message.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestReactDatePickerControl.class, ResourcesModule.Module.INSTANCE,
				FormatterService.Module.INSTANCE));
	}

}
