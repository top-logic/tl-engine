/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import java.text.Format;
import java.text.NumberFormat;

import com.top_logic.basic.format.NumberFormats;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.state.FieldState;
import com.top_logic.layout.react.state.NumberInputState;
import com.top_logic.layout.react.state.NumberInputState.InputMode;

/**
 * A {@link ReactFormFieldControl} for number input fields.
 *
 * <p>
 * The field's {@link Format} decides how the value is written and how a typed one is read. For the
 * usual {@link NumberFormat} that means the digits and separators of the user's locale and the
 * number of digits the attribute or property asks for: a German user sees and types {@code 12,5}
 * where an English user sees and types {@code 12.5}. A format of its own kind writes its own text -
 * a duration in milliseconds reads as {@code 1h 30min} - and the field follows it. The client is
 * handed the formatted text in {@link FieldState#VALUE__PROP} and sends back the text as typed; it
 * does no conversion of its own, and asks for the on-screen keyboard the format's
 * {@link NumberInputState#INPUT_MODE__PROP input mode} names.
 * </p>
 *
 * <p>
 * Typed text is read back through the same format, and the whole text must be a number in it -
 * otherwise {@link I18NConstants#ERROR_INVALID_NUMBER__VALUE} is set on the field. Since the format
 * rewrites what it is given ({@code 12,5} comes back as {@code 12,50} with two decimal places), the
 * field {@link #setSendValueOnBlur(boolean) holds a typed value back} until it is left, so that a
 * mid-edit round-trip cannot re-render the input from the normalized text.
 * </p>
 */
public class ReactNumberInputControl extends ReactFormFieldControl {

	private final Format _format;

	/**
	 * Creates a new {@link ReactNumberInputControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param model
	 *        The field model.
	 * @param format
	 *        The format the value is displayed in and entered in.
	 */
	public ReactNumberInputControl(ReactContext context, FieldModel model, Format format) {
		super(context, model, "TLNumberInput");
		_format = format;
		putState(NumberInputState.INPUT_MODE__PROP, inputMode(format).protocolName());
		// The base constructor seeded the raw number into the value state; re-emit it as the text
		// the user reads and edits.
		putState(FieldState.VALUE__PROP, format(model.getValue()));
		setSendValueOnBlur(true);
	}

	/**
	 * The format the value is displayed in and entered in.
	 */
	public Format getFormat() {
		return _format;
	}

	/**
	 * The on-screen keyboard a value in the given format is typed on.
	 *
	 * <p>
	 * A number the format writes as digits is typed on the numeric keyboard, with the decimal
	 * separator where the format has a fraction. A format that writes words needs the full keyboard.
	 * </p>
	 */
	private static InputMode inputMode(Format format) {
		if (!(format instanceof NumberFormat)) {
			return InputMode.TEXT;
		}
		return NumberFormats.isFractional(format) ? InputMode.DECIMAL : InputMode.NUMERIC;
	}

	@Override
	protected void applyRawClientValue(Object rawValue) {
		FieldModel model = getFieldModel();

		AbstractFieldModel abstractModel =
			model instanceof AbstractFieldModel ? (AbstractFieldModel) model : null;

		if (rawValue == null || rawValue.toString().trim().isEmpty()) {
			if (abstractModel != null) {
				abstractModel.setError(null);
			}
			model.setValue(null);
			return;
		}

		Number parsed = NumberFormats.parse(_format, rawValue.toString());
		if (parsed == null) {
			// Set error on model so it gets displayed in chrome and as red border on input.
			if (abstractModel != null) {
				abstractModel.setError(
					I18NConstants.ERROR_INVALID_NUMBER__VALUE.fill(rawValue.toString()));
			}
			return;
		}
		if (abstractModel != null) {
			abstractModel.setError(null);
		}
		model.setValue(parsed);
	}

	@Override
	protected Object parseClientValue(Object rawValue) {
		if (rawValue instanceof Number) {
			return rawValue;
		}
		if (rawValue == null) {
			return null;
		}
		return NumberFormats.parse(_format, rawValue.toString());
	}

	@Override
	protected void handleModelValueChanged(FieldModel source, Object oldValue, Object newValue) {
		putState(FieldState.VALUE__PROP, format(newValue));
	}

	/**
	 * A typed number is complete when the user presses Enter.
	 */
	@Override
	public boolean hasSubmitGesture() {
		return true;
	}

	private String format(Object value) {
		return value instanceof Number ? _format.format(value) : null;
	}

}
