/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Format;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.format.DurationFormat;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.form.FieldValueArguments;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.control.form.ReactNumberInputControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;

/**
 * Tests that {@link ReactNumberInputControl} writes and reads its value through the field's number
 * format, so that a value is displayed and entered in the user's locale.
 */
public class TestReactNumberInputControl extends TestCase {

	private static final NumberFormat GERMAN = NumberFormat.getInstance(Locale.GERMANY);

	private static final NumberFormat ENGLISH = NumberFormat.getInstance(Locale.UK);

	private static final NumberFormat GERMAN_INTEGER = NumberFormat.getIntegerInstance(Locale.GERMANY);

	/** Half an hour and an hour, in milliseconds. */
	private static final long NINETY_MINUTES = 5400000L;

	/**
	 * A value is written in the field's format, so the same number reads differently for a German
	 * and an English user.
	 */
	public void testValueIsFormatted() {
		assertEquals("12,5", control(GERMAN, Double.valueOf(12.5)).state());
		assertEquals("12.5", control(ENGLISH, Double.valueOf(12.5)).state());
	}

	/**
	 * A format asking for a fixed number of decimal places writes them, exactly as the value is
	 * displayed elsewhere.
	 */
	public void testValueKeepsTheFormatsDigits() {
		DecimalFormat format = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.GERMANY));
		assertEquals("12,50", control(format, Double.valueOf(12.5)).state());
	}

	/**
	 * A field without a value shows nothing.
	 */
	public void testEmptyValue() {
		assertNull(control(GERMAN, null).state());
	}

	/**
	 * Typed text is read in the field's format: a German user enters a decimal fraction with a
	 * comma.
	 */
	public void testTypedValueIsParsed() {
		NumberControl field = control(GERMAN, null);

		field.type("12,5");

		assertEquals(Double.valueOf(12.5), field.getFieldModel().getValue());
		assertFalse(model(field).hasError());
	}

	/**
	 * A grouping separator is part of the number, not a stray character.
	 */
	public void testGroupedValueIsParsed() {
		NumberControl field = control(GERMAN, null);

		field.type("1.234,5");

		assertEquals(Double.valueOf(1234.5), field.getFieldModel().getValue());
	}

	/**
	 * The separators mean what they mean in the field's format: the same text is a different number
	 * for a German and an English user.
	 */
	public void testSeparatorsFollowTheFormat() {
		NumberControl german = control(GERMAN, null);
		german.type("12.5");
		assertEquals("A dot groups digits for a German user.", Long.valueOf(125), german.getFieldModel().getValue());

		NumberControl english = control(ENGLISH, null);
		english.type("12.5");
		assertEquals("A dot separates the fraction for an English user.",
			Double.valueOf(12.5), english.getFieldModel().getValue());
	}

	/**
	 * Text that is no number at all is rejected.
	 */
	public void testNonNumberIsRejected() {
		NumberControl field = control(GERMAN, Double.valueOf(1.0));

		field.type("abc");

		assertTrue(model(field).hasError());
		assertEquals(Double.valueOf(1.0), field.getFieldModel().getValue());
	}

	/**
	 * A number followed by anything else is rejected: the whole text must be the number.
	 */
	public void testTrailingTextIsRejected() {
		NumberControl field = control(GERMAN, Double.valueOf(1.0));

		field.type("12,5 kg");

		assertTrue(model(field).hasError());
		assertEquals(Double.valueOf(1.0), field.getFieldModel().getValue());
	}

	/**
	 * Clearing the field clears the value and any error the previous input left.
	 */
	public void testClearedValue() {
		NumberControl field = control(GERMAN, Double.valueOf(1.0));
		field.type("abc");
		assertTrue(model(field).hasError());

		field.type("");

		assertNull(field.getFieldModel().getValue());
		assertFalse(model(field).hasError());
	}

	/**
	 * A whole-number format never produces a fractional value: a typed fraction is not a number in
	 * it.
	 */
	public void testWholeNumberFormat() {
		NumberControl field = control(GERMAN_INTEGER, null);

		field.type("1.234");
		assertEquals(Long.valueOf(1234), field.getFieldModel().getValue());

		field.type("12,5");
		assertTrue(model(field).hasError());
		assertEquals(Long.valueOf(1234), field.getFieldModel().getValue());
	}

	/**
	 * The applied value is echoed back in the format the field writes, so the input shows the
	 * number the way it is stored.
	 */
	public void testAppliedValueIsEchoedFormatted() {
		NumberControl field = control(GERMAN, null);

		field.type("12,5");

		assertEquals("12,5", field.state());
	}

	/**
	 * A format that writes words rather than digits governs the field just the same: a duration is a
	 * number of milliseconds, displayed and entered as the text its format reads.
	 */
	public void testFormatWritingWords() {
		NumberControl field = control(DurationFormat.INSTANCE, Long.valueOf(NINETY_MINUTES));
		assertEquals("1h 30min", field.state());

		field.type("45min");
		assertEquals(Long.valueOf(2700000L), field.getFieldModel().getValue());
		assertFalse(model(field).hasError());

		field.type("abc");
		assertTrue("Text the format does not read is not a value.", model(field).hasError());
		assertEquals(Long.valueOf(2700000L), field.getFieldModel().getValue());
	}

	/**
	 * The on-screen keyboard the client asks for follows the field's format.
	 */
	public void testInputMode() {
		assertEquals(ReactNumberInputControl.INPUT_MODE_DECIMAL, control(GERMAN, null).inputMode());
		assertEquals(ReactNumberInputControl.INPUT_MODE_NUMERIC, control(GERMAN_INTEGER, null).inputMode());
		assertEquals("A duration is typed as words, so the full keyboard is needed.",
			ReactNumberInputControl.INPUT_MODE_TEXT, control(DurationFormat.INSTANCE, null).inputMode());
	}

	private static AbstractFieldModel model(NumberControl field) {
		return (AbstractFieldModel) field.getFieldModel();
	}

	private static NumberControl control(Format format, Object value) {
		ReactContext context = new DefaultReactContext("", "test", new SSEUpdateQueue());
		return new NumberControl(context, new AbstractFieldModel(value), format);
	}

	/**
	 * A {@link ReactNumberInputControl} exposing its server-side value state for assertions.
	 */
	private static final class NumberControl extends ReactNumberInputControl {

		NumberControl(ReactContext context, FieldModel model, Format format) {
			super(context, model, format);
		}

		Object state() {
			return getState(VALUE);
		}

		Object inputMode() {
			return getState(INPUT_MODE);
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
			ServiceTestSetup.createSetup(TestReactNumberInputControl.class, ResourcesModule.Module.INSTANCE));
	}

}
