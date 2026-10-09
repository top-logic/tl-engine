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
import com.top_logic.layout.react.state.FieldState;
import com.top_logic.layout.react.state.SliderState;

/**
 * A {@link ReactFormFieldControl} for a number set by dragging a handle along a track.
 *
 * <p>
 * The value travels as a number: the client is handed the number itself in {@link FieldState#VALUE__PROP} and sends
 * back the number the handle stands on, within the {@link SliderState#MIN__PROP lower} and
 * {@link SliderState#MAX__PROP upper} bound and on the grid of the {@link SliderState#STEP__PROP
 * smallest step}. The text beside the handle is written on the server
 * ({@link SliderState#VALUE_LABEL__PROP}) with the same {@link Format} the number input uses, so the same
 * value reads the same wherever it is shown - {@code 12,50} for a German user where an English one
 * reads {@code 12.50}.
 * </p>
 *
 * <p>
 * The bounds are written in the same format as well ({@link SliderState#MIN_LABEL__PROP},
 * {@link SliderState#MAX_LABEL__PROP}): the client reserves the width of the wider of the two for the
 * text, so the track keeps its length while the text changes.
 * </p>
 *
 * <p>
 * The state is described by {@link SliderState}.
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

	private static final String REACT_MODULE = "TLSlider";

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
		super(context, model, REACT_MODULE);
		if (min >= max) {
			throw new IllegalArgumentException("A slider needs a range to travel: " + min + " is not below " + max);
		}
		if (step <= 0) {
			throw new IllegalArgumentException("A slider needs a step to move by: " + step + " is not above zero.");
		}
		_format = format;
		putState(SliderState.MIN__PROP, Double.valueOf(min));
		putState(SliderState.MAX__PROP, Double.valueOf(max));
		putState(SliderState.STEP__PROP, Double.valueOf(step));
		putState(SliderState.MIN_LABEL__PROP, format(Double.valueOf(min)));
		putState(SliderState.MAX_LABEL__PROP, format(Double.valueOf(max)));
		putState(SliderState.VALUE_LABEL__PROP, format(model.getValue()));
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
		putState(SliderState.VALUE_LABEL__PROP, format(getFieldModel().getValue()));
	}

	@Override
	protected void handleModelValueChanged(FieldModel source, Object oldValue, Object newValue) {
		super.handleModelValueChanged(source, oldValue, newValue);
		putState(SliderState.VALUE_LABEL__PROP, format(newValue));
	}

	/**
	 * The range the handle travels, the text beside it and the bounds written as text are how the
	 * value is shown, not what it is: the value itself is the number the field holds.
	 */
	@Override
	protected Set<String> scriptingPresentationKeys() {
		return presentationKeys(super.scriptingPresentationKeys(), SliderState.MIN__PROP,
			SliderState.MAX__PROP, SliderState.STEP__PROP, SliderState.VALUE_LABEL__PROP,
			SliderState.MIN_LABEL__PROP, SliderState.MAX_LABEL__PROP);
	}

	private String format(Object value) {
		return value instanceof Number ? _format.format(value) : null;
	}

}
