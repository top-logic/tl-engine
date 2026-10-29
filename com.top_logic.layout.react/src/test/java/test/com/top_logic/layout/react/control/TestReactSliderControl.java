/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Format;
import java.util.Locale;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.control.form.ReactSliderControl;
import com.top_logic.layout.react.control.form.SliderRange;
import com.top_logic.layout.react.control.form.SliderValueArguments;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.FieldState;
import com.top_logic.layout.react.state.SliderState;
import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * Tests that a {@link ReactSliderControl} takes its {@link SliderRange} at runtime, writes value
 * and bounds with the range's unit, and stays disabled while the range is not usable.
 */
public class TestReactSliderControl extends TestCase {

	private static final Format GERMAN = new DecimalFormat("#,##0.0", DecimalFormatSymbols.getInstance(Locale.GERMANY));

	/** A non-breaking space, between a number and its unit. */
	private static final String NBSP = " ";

	/** The range reaches the client state, together with the bounds written as text. */
	public void testRangeReachesState() {
		Slider slider = slider(Double.valueOf(12.5), new SliderRange(0.0, 100.0, 0.5, null));

		assertEquals(Double.valueOf(0.0), slider.state(SliderState.MIN__PROP));
		assertEquals(Double.valueOf(100.0), slider.state(SliderState.MAX__PROP));
		assertEquals(Double.valueOf(0.5), slider.state(SliderState.STEP__PROP));
		assertEquals("0,0", slider.state(SliderState.MIN_LABEL__PROP));
		assertEquals("100,0", slider.state(SliderState.MAX_LABEL__PROP));
		assertEquals("12,5", slider.state(SliderState.VALUE_LABEL__PROP));
		assertEquals(Boolean.FALSE, slider.state(FieldState.DISABLED__PROP));
	}

	/** A range without a step moves by whole numbers. */
	public void testDefaultStep() {
		Slider slider = slider(null, new SliderRange(0.0, 10.0, null, null));

		assertEquals(Double.valueOf(1.0), slider.state(SliderState.STEP__PROP));
		assertEquals(1.0, slider.getRange().effectiveStep());
		assertEquals(SliderRange.DEFAULT_STEP, slider.getRange().effectiveStep());
	}

	/** A range set later replaces bounds, step and all texts. */
	public void testRangeIsReplaced() {
		Slider slider = slider(Double.valueOf(5.0), new SliderRange(0.0, 10.0, null, null));

		slider.setRange(new SliderRange(-20.0, 20.0, 2.0, "kg"));

		assertEquals(Double.valueOf(-20.0), slider.state(SliderState.MIN__PROP));
		assertEquals(Double.valueOf(20.0), slider.state(SliderState.MAX__PROP));
		assertEquals(Double.valueOf(2.0), slider.state(SliderState.STEP__PROP));
		assertEquals("-20,0" + NBSP + "kg", slider.state(SliderState.MIN_LABEL__PROP));
		assertEquals("20,0" + NBSP + "kg", slider.state(SliderState.MAX_LABEL__PROP));
		assertEquals("5,0" + NBSP + "kg", slider.state(SliderState.VALUE_LABEL__PROP));
		assertEquals(new SliderRange(-20.0, 20.0, 2.0, "kg"), slider.getRange());
	}

	/** The unit is written after the value and both bounds, and is dropped with a range without unit. */
	public void testUnit() {
		Slider slider = slider(Double.valueOf(12.5), new SliderRange(0.0, 100.0, 0.5, "%"));
		assertEquals("12,5" + NBSP + "%", slider.state(SliderState.VALUE_LABEL__PROP));
		assertEquals("0,0" + NBSP + "%", slider.state(SliderState.MIN_LABEL__PROP));
		assertEquals("100,0" + NBSP + "%", slider.state(SliderState.MAX_LABEL__PROP));

		slider.setRange(new SliderRange(0.0, 100.0, 0.5, null));
		assertEquals("12,5", slider.state(SliderState.VALUE_LABEL__PROP));
		assertEquals("0,0", slider.state(SliderState.MIN_LABEL__PROP));
		assertEquals("100,0", slider.state(SliderState.MAX_LABEL__PROP));
	}

	/** An empty field has no value text, with or without unit. */
	public void testEmptyValueHasNoText() {
		Slider slider = slider(null, new SliderRange(0.0, 100.0, 0.5, "%"));
		assertNull(slider.state(SliderState.VALUE_LABEL__PROP));
	}

	/** The value text follows a value the client sends, with the unit. */
	public void testValueTextFollowsClient() {
		Slider slider = slider(null, new SliderRange(0.0, 100.0, 0.5, "%"));

		slider.slideTo(42.5);

		assertEquals(Double.valueOf(42.5), slider.getFieldModel().getValue());
		assertEquals("42,5" + NBSP + "%", slider.state(SliderState.VALUE_LABEL__PROP));
	}

	/** The value text follows a value set on the model, with the unit. */
	public void testValueTextFollowsModel() {
		Slider slider = slider(null, new SliderRange(0.0, 100.0, 0.5, "%"));

		((AbstractFieldModel) slider.getFieldModel()).setValue(Double.valueOf(7.0));

		assertEquals("7,0" + NBSP + "%", slider.state(SliderState.VALUE_LABEL__PROP));
	}

	/** A range without a bound disables the slider and sends no bounds. */
	public void testMissingBoundDisables() {
		assertUnusable(new SliderRange(null, 10.0, null, "%"));
		assertUnusable(new SliderRange(0.0, null, null, "%"));
		assertUnusable(SliderRange.NONE);
	}

	/** A range whose lower bound is not below the upper one disables the slider. */
	public void testEmptyRangeDisables() {
		assertUnusable(new SliderRange(10.0, 10.0, null, null));
		assertUnusable(new SliderRange(10.0, 0.0, null, null));
	}

	/** A range whose step is not above zero disables the slider. */
	public void testNonPositiveStepDisables() {
		assertUnusable(new SliderRange(0.0, 10.0, 0.0, null));
		assertUnusable(new SliderRange(0.0, 10.0, -1.0, null));
	}

	/** A slider created without a usable range is disabled, and becomes enabled with a usable one. */
	public void testUsableRangeEnables() {
		Slider slider = slider(Double.valueOf(3.0), new SliderRange(null, null, null, "%"));
		assertEquals(Boolean.TRUE, slider.state(FieldState.DISABLED__PROP));
		assertEquals("3,0" + NBSP + "%", slider.state(SliderState.VALUE_LABEL__PROP));

		slider.setRange(new SliderRange(0.0, 10.0, null, "%"));

		assertEquals(Boolean.FALSE, slider.state(FieldState.DISABLED__PROP));
		assertEquals(Double.valueOf(0.0), slider.state(SliderState.MIN__PROP));
		assertEquals("10,0" + NBSP + "%", slider.state(SliderState.MAX_LABEL__PROP));
	}

	/** A slider without a usable range takes no value from the client. */
	public void testUnusableRangeRefusesClientValue() {
		Slider slider = slider(Double.valueOf(3.0), SliderRange.NONE);

		slider.slideTo(5.0);

		assertEquals(Double.valueOf(3.0), slider.getFieldModel().getValue());
	}

	/** A disabled field stays disabled with a usable range, and the range does not override it. */
	public void testDisabledFieldStaysDisabled() {
		AbstractFieldModel model = new AbstractFieldModel(Double.valueOf(1.0));
		model.setDisabled(true);
		Slider slider = new Slider(context(), model, new SliderRange(0.0, 10.0, null, null));
		assertEquals(Boolean.TRUE, slider.state(FieldState.DISABLED__PROP));

		slider.setRange(new SliderRange(0.0, 20.0, null, null));
		assertEquals(Boolean.TRUE, slider.state(FieldState.DISABLED__PROP));

		model.setDisabled(false);
		assertEquals(Boolean.FALSE, slider.state(FieldState.DISABLED__PROP));

		slider.setRange(SliderRange.NONE);
		model.setDisabled(true);
		model.setDisabled(false);
		assertEquals("Re-enabling the field does not enable a slider without a usable range.",
			Boolean.TRUE, slider.state(FieldState.DISABLED__PROP));
	}

	private static void assertUnusable(SliderRange range) {
		assertFalse(range.usable());

		Slider created = slider(Double.valueOf(1.0), range);
		assertCleared(created);

		Slider replaced = slider(Double.valueOf(1.0), new SliderRange(0.0, 10.0, null, null));
		assertEquals(Boolean.FALSE, replaced.state(FieldState.DISABLED__PROP));
		replaced.setRange(range);
		assertCleared(replaced);
	}

	private static void assertCleared(Slider slider) {
		assertEquals(Boolean.TRUE, slider.state(FieldState.DISABLED__PROP));
		assertNull(slider.state(SliderState.MIN__PROP));
		assertNull(slider.state(SliderState.MAX__PROP));
		assertNull(slider.state(SliderState.STEP__PROP));
		assertNull(slider.state(SliderState.MIN_LABEL__PROP));
		assertNull(slider.state(SliderState.MAX_LABEL__PROP));
	}

	private static Slider slider(Object value, SliderRange range) {
		return new Slider(context(), new AbstractFieldModel(value), range);
	}

	private static ReactContext context() {
		return new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
	}

	/**
	 * A {@link ReactSliderControl} exposing its server-side state for assertions.
	 */
	private static final class Slider extends ReactSliderControl {

		Slider(ReactContext context, FieldModel model, SliderRange range) {
			super(context, model, GERMAN, range);
		}

		Object state(String key) {
			return getState(key);
		}

		void slideTo(double value) {
			executeClientCommand(ReactFormFieldControl.CMD_VALUE_CHANGED,
				Map.of(SliderValueArguments.VALUE, Double.valueOf(value)));
		}
	}

	/**
	 * The test suite, started with the resource bundles a rejected input needs for its message.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestReactSliderControl.class, ResourcesModule.Module.INSTANCE));
	}

}
