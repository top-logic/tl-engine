/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import java.text.Format;
import java.util.Set;

import com.top_logic.basic.format.NumberFormats;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommandHandler;

/**
 * A {@link ReactFormFieldControl} for a number set by dragging a handle along a track.
 *
 * <p>
 * The value travels as a number: the client is handed the number itself in {@link #VALUE} and sends
 * back the number the handle stands on, within the {@link #MIN lower} and {@link #MAX upper} bound
 * and on the grid of the {@link #STEP smallest step}. The text beside the handle is written on the
 * server ({@link #VALUE_LABEL}) with the same {@link Format} the number input uses, so the same
 * value reads the same wherever it is shown - {@code 12,50} for a German user where an English one
 * reads {@code 12.50}.
 * </p>
 *
 * <p>
 * A number typed as text is {@link ReactNumberInputControl} instead. That control exchanges the
 * value as the text the format writes, which a handle position cannot be: the numbers a slider
 * produces are the grid it was given, and reading them back through a locale format would make
 * {@code 12.5} a different value in a German locale than in an English one.
 * </p>
 */
public class ReactSliderControl extends ReactFormFieldControl {

	/** State key for the smallest value the handle can stand on. */
	private static final String MIN = "min";

	/** State key for the largest value the handle can stand on. */
	private static final String MAX = "max";

	/** State key for the distance between two positions the handle can stand on. */
	private static final String STEP = "step";

	/**
	 * State key for the value written in the {@link #getFormat() format of the field}, the text
	 * shown beside the handle.
	 */
	private static final String VALUE_LABEL = "valueLabel";

	private final Format _format;

	/**
	 * Creates a new {@link ReactSliderControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param model
	 *        The field model.
	 * @param format
	 *        The format the value is displayed in.
	 * @param min
	 *        The smallest value that can be set.
	 * @param max
	 *        The largest value that can be set, greater than {@code min}.
	 * @param step
	 *        The distance between two values that can be set, greater than zero.
	 */
	public ReactSliderControl(ReactContext context, FieldModel model, Format format, double min, double max,
			double step) {
		super(context, model, "TLSlider");
		if (min >= max) {
			throw new IllegalArgumentException("A slider needs a range to travel: " + min + " is not below " + max);
		}
		if (step <= 0) {
			throw new IllegalArgumentException("A slider needs a step to move by: " + step + " is not above zero.");
		}
		_format = format;
		putState(MIN, Double.valueOf(min));
		putState(MAX, Double.valueOf(max));
		putState(STEP, Double.valueOf(step));
		putState(VALUE_LABEL, format(model.getValue()));
	}

	/**
	 * The format the value is displayed in.
	 */
	public Format getFormat() {
		return _format;
	}

	/**
	 * Handles the value a dragged handle reports: it is a number, so it has its own typed arguments
	 * rather than the base field's text value.
	 */
	@ReactCommandHandler(CMD_VALUE_CHANGED)
	void handleSliderValue(SliderValueArguments args) {
		clientValueChanged(args.getValue());
	}

	/**
	 * The number the client sends, in the value type the {@link #getFormat() format} works in - a
	 * whole number where it writes no fraction - so that the value a slider writes is of the same
	 * kind as the value the input field writes for the same field.
	 */
	@Override
	protected Object parseClientValue(Object rawValue) {
		if (rawValue == null) {
			return null;
		}
		if (rawValue instanceof Number number) {
			return NumberFormats.normalize(_format, number);
		}
		return NumberFormats.parse(_format, rawValue.toString());
	}

	/**
	 * Writes the text for the value the field now holds.
	 *
	 * @implNote The client already holds the value it sent, so the model's answer to it is recorded
	 *           without an event (see {@link #applyClientValue(Object)}) - and with it the text
	 *           written in {@link #handleModelValueChanged(FieldModel, Object, Object)}. Writing
	 *           the text here, after that answer, sends it as a change of its own, so the text
	 *           follows the handle.
	 */
	@Override
	protected void applyRawClientValue(Object rawValue) {
		super.applyRawClientValue(rawValue);
		putState(VALUE_LABEL, format(getFieldModel().getValue()));
	}

	@Override
	protected void handleModelValueChanged(FieldModel source, Object oldValue, Object newValue) {
		super.handleModelValueChanged(source, oldValue, newValue);
		putState(VALUE_LABEL, format(newValue));
	}

	/**
	 * The range the handle travels and the text beside it are how the value is shown, not what it
	 * is: the value itself is the number the field holds.
	 */
	@Override
	protected Set<String> scriptingPresentationKeys() {
		return presentationKeys(super.scriptingPresentationKeys(), MIN, MAX, STEP, VALUE_LABEL);
	}

	private String format(Object value) {
		return value instanceof Number ? _format.format(value) : null;
	}

}
