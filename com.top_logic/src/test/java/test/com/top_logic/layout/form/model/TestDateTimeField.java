/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.model;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.constraints.DateRangeConstraint;
import com.top_logic.layout.form.model.AbstractFormField;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.DateTimeField;
import com.top_logic.layout.form.model.FormFieldInternals;

/**
 * Test for the {@link DateTimeField}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestDateTimeField extends BasicTestCase {

	@Override
	public void inContext() {
		Runnable superCall = () -> super.inContext();
		try {
			/* The given date and time strings are in German format. */
			BasicTestCase.executeInLocale(Locale.GERMAN, new Execution() {

				@Override
				public void run() {
					superCall.run();
				}
			});
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new UnreachableAssertion("No declared exceptions here.", ex);
		}
	}

	/**
	 * Test setting value programmatically.
	 * 
	 * @see DateTimeField#setValue(Object)
	 */
	public void testValue() {
		DateTimeField field = newField();

		field.setValue(null);
		assertNull(field.getValue());
		assertNull(field.getDayField().getValue());
		assertNull(field.getTimeField().getValue());

		field = newField();
		Date date = date(2023, Calendar.AUGUST, 17, 8, 45, 14, 0);
		field.setValue(date);
		assertEquals(date, field.getValue());
		assertEquals(date(2023, Calendar.AUGUST, 17, 0, 0, 0, 0), field.getDayField().getValue());
		assertEquals(date(1970, Calendar.JANUARY, 1, 8, 45, 14, 0), field.getTimeField().getValue());
	}

	/**
	 * Test setting value coming from client.
	 */
	public void testRawValue() {
		DateTimeField field = newField();

		updateField(field.getDayField(), "");
		updateField(field.getTimeField(), "");
		assertNull(field.getValue());
		assertNull(field.getDayField().getValue());
		assertNull(field.getTimeField().getValue());

		field = newField();
		updateField(field.getDayField(), "17.08.2023");
		updateField(field.getTimeField(), "08:45");

		assertEquals(date(2023, Calendar.AUGUST, 17, 8, 45, 0, 0), field.getValue());
		assertEquals(date(2023, Calendar.AUGUST, 17, 0, 0, 0, 0), field.getDayField().getValue());
		assertEquals(date(1970, Calendar.JANUARY, 1, 8, 45, 0, 0), field.getTimeField().getValue());
	}

	/**
	 * Test setting illegal values coming from client.
	 */
	public void testErrorsValue() {
		DateTimeField field = newField();
		ComplexField dayField = field.getDayField();

		updateField(dayField, "Can not parse");

		assertTrue(dayField.hasError());
		assertTrue(field.hasError());
		assertEquals(dayField.getError(), field.getError());

		updateField(dayField, "19.01.1809");

		assertTrue("Date must be at least 01.01.1900", dayField.hasError());
		assertTrue(field.hasError());
		assertEquals(dayField.getError(), field.getError());

		updateField(dayField, "19.01.1978");
		assertFalse(dayField.hasError());
		assertFalse(field.hasError());
	}

	/**
	 * Test changing value such neither error state nor error message changes.
	 */
	public void testSameError() {
		DateTimeField field = newField();
		ComplexField dayField = field.getDayField();
		AbstractFormField proxy = field.getProxy();

		updateField(dayField, "19.01.1809");
		assertTrue("Date must be at least 01.01.1900", dayField.hasError());
		assertTrue(field.hasError());
		assertTrue(proxy.hasError());
		String errorMsg = dayField.getError();
		assertEquals(errorMsg, field.getError());

		updateField(dayField, "23.01.1862");
		assertTrue("Date must still be at least 01.01.1900", dayField.hasError());
		assertTrue(field.hasError());
		assertTrue("Ticket #25907: Field must have an error iff the proxy has one.", proxy.hasError());
		assertEquals("This test checks the behavior when the error message does not change.", errorMsg,
			dayField.getError());
		assertEquals(dayField.getError(), field.getError());
	}

	/**
	 * Tests mandatority when just the day field or the time field but not both are filled.
	 */
	public void testOtherInputMandatory() {
		DateTimeField field = newField();
		ComplexField dayField = field.getDayField();
		ComplexField timeField = field.getTimeField();

		field.setMandatory(false);
		updateField(dayField, "01.01.2000");
		assertTrue("Day is set, so time must be set", timeField.isMandatory());
		updateField(dayField, "");
		assertFalse("Day not set, so time may not be set", timeField.isMandatory());

		updateField(timeField, "12:34");
		assertTrue("Time is set, so day must be set", dayField.isMandatory());
		updateField(timeField, "");
		assertFalse("Time is not set, so day may not be set", dayField.isMandatory());

		field.setMandatory(true);
		assertTrue("DateTime is mandatory so day is mandatory", dayField.isMandatory());
		assertTrue("DateTime is mandatory so time is mandatory", timeField.isMandatory());

		updateField(dayField, "01.01.2000");
		assertTrue(timeField.isMandatory());
		updateField(dayField, "");
		assertTrue("Part remains mandatory because datetime is mandatory", timeField.isMandatory());

		updateField(timeField, "12:34");
		assertTrue(dayField.isMandatory());
		updateField(timeField, "");
		assertTrue("Part remains mandatory because datetime is mandatory", dayField.isMandatory());

		field.setMandatory(false);

		field.setValue(new Date());
		updateField(timeField, "");
		assertTrue("Day is set, so time must be set", timeField.isMandatory());
		updateField(timeField, "3:34");
		assertFalse("Day and time are set", timeField.isMandatory());

		field.setValue(new Date());
		updateField(dayField, "");
		assertTrue("Time is set, so day must be set", dayField.isMandatory());
		updateField(dayField, "20.05.1963");
		assertFalse("Day and time are set", dayField.isMandatory());

		field.setValue(new Date());
		updateField(timeField, "");
		assertTrue("Day is still set, so time must be set", timeField.isMandatory());
		updateField(dayField, "");
		assertFalse("Neither time nor day are set.", timeField.isMandatory());

		field.setValue(new Date());
		updateField(dayField, "");
		assertTrue("Time is still set, so day must be set", dayField.isMandatory());
		updateField(timeField, "");
		assertFalse("Neither time nor day are set.", dayField.isMandatory());
	}

	/**
	 * Tests errors, triggered by non-parsable values and constraints.
	 */
	public void testErrors() {
		DateTimeField field = newField();
		FormField dayField = field.getDayField();
		FormField timeField = field.getTimeField();

		field.addConstraint(new DateRangeConstraint(date(1970, Calendar.JANUARY, 1, 16, 0, 0, 0), null));

		updateField(dayField, "01.02.1979");
		updateField(timeField, "18:45");
		assertFalse(field.hasError());
		assertEquals(date(1979, Calendar.FEBRUARY, 1, 18, 45, 0, 0), field.getValue());

		updateField(dayField, "01.02.1969");
		assertTrue(field.hasError());
		assertEquals(date(1969, Calendar.FEBRUARY, 1, 18, 45, 0, 0), field.getValue());

		updateField(dayField, "01.01.1970");
		updateField(timeField, "8:45");
		assertTrue(field.hasError());
		assertEquals(date(1970, Calendar.JANUARY, 1, 8, 45, 0, 0), field.getValue());

		updateField(timeField, "XXX");
		assertTrue(field.hasError());
		assertFalse(dayField.hasError());
		assertTrue(timeField.hasError());
		assertFalse(field.hasValue());
		assertTrue(dayField.hasValue());
		assertFalse(timeField.hasValue());

		updateField(dayField, "YYYY");
		assertTrue(field.hasError());
		assertTrue(dayField.hasError());
		assertTrue(timeField.hasError());
		assertFalse(field.hasValue());
		assertFalse(dayField.hasValue());
		assertFalse(timeField.hasValue());

		updateField(timeField, "15:45");
		assertTrue(field.hasError());
		assertTrue(dayField.hasError());
		assertFalse(timeField.hasError());
		assertFalse(field.hasValue());
		assertFalse(dayField.hasValue());
		assertTrue(timeField.hasValue());

	}

	private void updateField(FormField field, String value) {
		try {
			FormFieldInternals.updateFieldNoClientUpdate(field, value);
		} catch (VetoException ex) {
			throw new IllegalArgumentException("Field has no veto listeners", ex);
		}
	}

	private static Date date(int year, int month, int day, int hour, int minutes, int seconds, int millis) {
		Calendar cal = CalendarUtil.createCalendar();
		cal.set(year, month, day, hour, minutes, seconds);
		cal.set(Calendar.MILLISECOND, millis);
		return cal.getTime();
	}

	private DateTimeField newField() {
		return new DateTimeField("field", null, false);
	}

	/**
	 * @return a cumulative {@link Test} for all Tests in {@link TestDateTimeField}.
	 */
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestDateTimeField.class);
	}
}
