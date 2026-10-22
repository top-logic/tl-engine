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
 * The range is a {@link SliderRange} and can be exchanged at any time ({@link #setRange(SliderRange)}).
 * A range that is not {@link SliderRange#usable() usable} - a bound missing, the lower bound not
 * below the upper one, or a step not above zero - leaves the slider disabled without bounds until a
 * usable range is set. The {@link SliderRange#unit() unit} of the range is written after the value
 * and after both bounds, separated by a non-breaking space ({@code 12,5 %}).
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

	/** Separator between a number and its unit: a non-breaking space keeps them on one line. */
	private static final char UNIT_SEPARATOR = '\u00A0';

	private final Format _format;

	private SliderRange _range;

	/**
	 * Creates a new {@link ReactSliderControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param model
	 *        The field model.
	 * @param format
	 *        The format the value is displayed in.
	 * @param range
	 *        The range the handle travels, see {@link #setRange(SliderRange)}.
	 */
	public ReactSliderControl(ReactContext context, FieldModel model, Format format, SliderRange range) {
		super(context, model, REACT_MODULE);
		_format = format;
		setRange(range);
	}

	/**
	 * The range the handle currently travels.
	 */
	public SliderRange getRange() {
		return _range;
	}

	/**
	 * Sets the range the handle travels and writes the value and the bounds as text with the
	 * range's unit.
	 *
	 * <p>
	 * A range that is not {@link SliderRange#usable() usable} sends no bounds and disables the
	 * slider until a usable range is set.
	 * </p>
	 *
	 * @param range
	 *        The range to travel, {@link SliderRange#NONE} if there is none.
	 */
	public void setRange(SliderRange range) {
		_range = range == null ? SliderRange.NONE : range;
		if (_range.usable()) {
			putState(SliderState.MIN__PROP, _range.min());
			putState(SliderState.MAX__PROP, _range.max());
			putState(SliderState.STEP__PROP, Double.valueOf(_range.effectiveStep()));
			putState(SliderState.MIN_LABEL__PROP, format(_range.min()));
			putState(SliderState.MAX_LABEL__PROP, format(_range.max()));
		} else {
			putState(SliderState.MIN__PROP, null);
			putState(SliderState.MAX__PROP, null);
			putState(SliderState.STEP__PROP, null);
			putState(SliderState.MIN_LABEL__PROP, null);
			putState(SliderState.MAX_LABEL__PROP, null);
		}
		putState(SliderState.VALUE_LABEL__PROP, format(getFieldModel().getValue()));
		setDisabled(getFieldModel().isDisabled());
	}

	/**
	 * The slider is disabled when its field is, and while its {@link #getRange() range} is not
	 * {@link SliderRange#usable() usable}.
	 */
	@Override
	protected void setDisabled(boolean disabled) {
		super.setDisabled(disabled || !hasUsableRange());
	}

	/**
	 * A slider without a {@link SliderRange#usable() usable} range cannot be moved, so it takes no
	 * value from the client.
	 */
	@Override
	protected boolean acceptsClientValue() {
		return super.acceptsClientValue() && hasUsableRange();
	}

	private boolean hasUsableRange() {
		// The range is not yet set while the base class initializes the field state.
		return _range != null && _range.usable();
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
		if (!(value instanceof Number)) {
			return null;
		}
		String text = _format.format(value);
		String unit = _range == null ? null : _range.unit();
		return unit == null || unit.isEmpty() ? text : text + UNIT_SEPARATOR + unit;
	}

}
