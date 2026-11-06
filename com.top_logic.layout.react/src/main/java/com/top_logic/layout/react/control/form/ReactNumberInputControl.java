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
 *
 * <p>
 * An optional unit is shown after the value, in {@link NumberInputState#UNIT__PROP}. It is display
 * only: the user does not type it, it is not part of the format, and a typed text is read without
 * it. The unit is the {@link #setFieldUnit(String) unit of the field}, until a
 * {@link #setUnit(String) unit of the control's own} is set.
 * </p>
 */
public class ReactNumberInputControl extends ReactFormFieldControl implements FieldUnitDisplay {

	private final Format _format;

	private String _unit;

	/**
	 * Whether {@link #_unit} is a unit of this control's own, which the unit of the field does not
	 * replace.
	 */
	private boolean _ownUnit;

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
		this(context, model, format, null);
	}

	/**
	 * Creates a new {@link ReactNumberInputControl} showing a unit after its value.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param model
	 *        The field model.
	 * @param format
	 *        The format the value is displayed in and entered in.
	 * @param unit
	 *        The unit of the field, see {@link #setFieldUnit(String)}.
	 */
	public ReactNumberInputControl(ReactContext context, FieldModel model, Format format, String unit) {
		super(context, model, "TLNumberInput");
		_format = format;
		_unit = normalizeUnit(unit);
		putState(NumberInputState.UNIT__PROP, _unit);
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
	 * The unit shown after the value, or {@code null} for none.
	 */
	public String getUnit() {
		return _unit;
	}

	/**
	 * Sets a unit of this control's own, shown after the value (e.g. {@code min} for
	 * {@code 120 min}) instead of the {@link #setFieldUnit(String) unit of the field}.
	 *
	 * @param unit
	 *        The text of the unit; {@code null} or an empty text shows no unit, not even the unit
	 *        of the field.
	 */
	public void setUnit(String unit) {
		_ownUnit = true;
		showUnit(unit);
	}

	/**
	 * Shows the given unit of the field, unless a {@link #setUnit(String) unit of this control's
	 * own} is set.
	 */
	@Override
	public void setFieldUnit(String unit) {
		if (!_ownUnit) {
			showUnit(unit);
		}
	}

	private void showUnit(String unit) {
		_unit = normalizeUnit(unit);
		putState(NumberInputState.UNIT__PROP, _unit);
	}

	private static String normalizeUnit(String unit) {
		return unit == null || unit.isEmpty() ? null : unit;
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

	@Override
	protected String formatElement(Object value) {
		return format(value);
	}

	/**
	 * A typed number is complete when the user presses Enter.
	 */
	@Override
	public boolean hasSubmitGesture() {
		return true;
	}

	/**
	 * A typed number is committed when the field is left or Enter is pressed.
	 */
	@Override
	public boolean commitsEdits() {
		return true;
	}

	private String format(Object value) {
		return value instanceof Number ? _format.format(value) : null;
	}

}
