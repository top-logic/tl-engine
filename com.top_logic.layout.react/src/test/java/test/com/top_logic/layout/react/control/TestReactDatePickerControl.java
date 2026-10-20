/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

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
import com.top_logic.layout.react.state.DatePickerState;
import com.top_logic.layout.react.state.DatePickerState.Precision;
import com.top_logic.layout.react.state.FieldState;
import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * Tests that {@link ReactDatePickerControl} edits a time of day with the precision its display
 * format writes: a field whose display format writes seconds edits seconds.
 */
public class TestReactDatePickerControl extends TestCase {

	/**
	 * 2026-06-01 14:30:15 in the user's time zone.
	 */
	private static Date moment() {
		Calendar calendar = Calendar.getInstance(ThreadContext.getTimeZone(), Locale.ROOT);
		calendar.clear();
		calendar.set(2026, Calendar.JUNE, 1, 14, 30, 15);
		return calendar.getTime();
	}

	/**
	 * A time field whose display format writes seconds edits seconds and exchanges them with the
	 * client.
	 */
	public void testTimeWithSeconds() {
		DateControl field = control(Kind.TIME, format("HH:mm:ss"), moment());

		assertEquals(Precision.SECONDS.protocolName(), field.precision());
		assertEquals("14:30:15", field.value());
		assertEquals("14:30:15", field.displayValue());
	}

	/**
	 * A time field whose display format leaves out the seconds edits minutes only.
	 */
	public void testTimeWithoutSeconds() {
		DateControl field = control(Kind.TIME, format("HH:mm"), moment());

		assertEquals(Precision.MINUTES.protocolName(), field.precision());
		assertEquals("14:30", field.value());
	}

	/**
	 * A date-time field whose display format writes seconds edits seconds and exchanges them with
	 * the client.
	 */
	public void testDateTimeWithSeconds() {
		DateControl field = control(Kind.DATE_TIME, format("dd.MM.yyyy HH:mm:ss"), moment());

		assertEquals(Precision.SECONDS.protocolName(), field.precision());
		assertEquals("2026-06-01T14:30:15", field.value());
	}

	/**
	 * A date-time field whose display format leaves out the seconds edits minutes only.
	 */
	public void testDateTimeWithoutSeconds() {
		DateControl field = control(Kind.DATE_TIME, format("dd.MM.yyyy HH:mm"), moment());

		assertEquals(Precision.MINUTES.protocolName(), field.precision());
		assertEquals("2026-06-01T14:30", field.value());
	}

	/**
	 * A date field has no time of day, so it edits no seconds whatever its display format writes.
	 */
	public void testDateHasNoSeconds() {
		DateControl field = control(Kind.DATE, format("dd.MM.yyyy HH:mm:ss"), moment());

		assertEquals(Precision.MINUTES.protocolName(), field.precision());
		assertEquals("2026-06-01", field.value());
	}

	/**
	 * A time typed with seconds reaches the model with its seconds, and a time without seconds is
	 * accepted as well, whatever the precision of the field.
	 */
	public void testClientValueWithAndWithoutSeconds() {
		for (String pattern : new String[] { "HH:mm:ss", "HH:mm" }) {
			DateControl field = control(Kind.TIME, format(pattern), null);

			field.type("14:30:15");
			assertFalse(pattern, model(field).hasError());
			assertEquals(pattern, "14:30:15", format("HH:mm:ss").format((Date) field.getFieldModel().getValue()));

			field.type("09:05");
			assertFalse(pattern, model(field).hasError());
			assertEquals(pattern, "09:05:00", format("HH:mm:ss").format((Date) field.getFieldModel().getValue()));
		}
	}

	/**
	 * The value applied by the client is echoed back in the ISO form of the field's precision.
	 */
	public void testAppliedValueIsEchoedWithPrecision() {
		DateControl seconds = control(Kind.TIME, format("HH:mm:ss"), null);
		seconds.type("14:30:15");
		assertEquals("14:30:15", seconds.value());

		DateControl minutes = control(Kind.TIME, format("HH:mm"), null);
		minutes.type("14:30");
		assertEquals("14:30", minutes.value());
	}

	/**
	 * A value that is no time at all is rejected and leaves the model value unchanged.
	 */
	public void testUnparsableValueIsRejected() {
		Date before = moment();
		DateControl field = control(Kind.TIME, format("HH:mm:ss"), before);

		field.type("14:3x");

		assertTrue(model(field).hasError());
		assertEquals(before, field.getFieldModel().getValue());
	}

	/**
	 * The precision is derived from what a format writes, not from a pattern, so it works for a
	 * format not given by a pattern as well.
	 */
	public void testPrecisionOfLocaleFormats() {
		assertEquals(Precision.MINUTES,
			ReactDatePickerControl.precision(Kind.TIME, DateFormat.getTimeInstance(DateFormat.SHORT, Locale.GERMANY)));
		assertEquals(Precision.SECONDS,
			ReactDatePickerControl.precision(Kind.TIME, DateFormat.getTimeInstance(DateFormat.MEDIUM, Locale.GERMANY)));
		assertEquals(Precision.SECONDS, ReactDatePickerControl.precision(Kind.DATE_TIME,
			DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.GERMANY)));
		assertEquals(Precision.MINUTES, ReactDatePickerControl.precision(Kind.DATE,
			DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.GERMANY)));
	}

	private static SimpleDateFormat format(String pattern) {
		SimpleDateFormat result = new SimpleDateFormat(pattern, Locale.ROOT);
		result.setTimeZone(ThreadContext.getTimeZone());
		return result;
	}

	private static AbstractFieldModel model(DateControl field) {
		return (AbstractFieldModel) field.getFieldModel();
	}

	private static DateControl control(Kind kind, DateFormat displayFormat, Date value) {
		ReactContext context = new DefaultReactContext("", "test", new SSEUpdateQueue(),
			new ReactWindowRegistry("test"));
		return new DateControl(context, new AbstractFieldModel(value), kind, displayFormat);
	}

	/**
	 * A {@link ReactDatePickerControl} exposing its server-side state for assertions.
	 */
	private static final class DateControl extends ReactDatePickerControl {

		DateControl(ReactContext context, FieldModel model, Kind kind, DateFormat displayFormat) {
			super(context, model, kind, displayFormat);
		}

		Object value() {
			return getState(FieldState.VALUE__PROP);
		}

		Object displayValue() {
			return getState(DatePickerState.DISPLAY_VALUE__PROP);
		}

		Object precision() {
			return getState(DatePickerState.PRECISION__PROP);
		}

		void type(String text) {
			executeClientCommand(ReactFormFieldControl.CMD_VALUE_CHANGED,
				Map.of(FieldValueArguments.VALUE, text));
		}
	}

	/**
	 * The test suite, started with the resource bundles a rejected input needs for its message.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestReactDatePickerControl.class, ResourcesModule.Module.INSTANCE));
	}

}
