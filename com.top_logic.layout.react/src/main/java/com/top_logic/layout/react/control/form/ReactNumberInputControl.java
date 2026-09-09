/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import java.text.NumberFormat;
import java.util.Map;

import com.top_logic.basic.format.NumberFormats;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactContext;

/**
 * A {@link ReactFormFieldControl} for number input fields.
 *
 * <p>
 * The field's {@link NumberFormat} decides how the value is written and how a typed one is read, so
 * a number is shown and entered in the user's locale and with the number of digits the attribute or
 * property asks for: a German user sees and types {@code 12,5} where an English user sees and types
 * {@code 12.5}. The client is handed the formatted text in {@link #VALUE} and sends back the text as
 * typed; it does no number conversion of its own.
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

	/** State key holding the client-side configuration of the input. */
	private static final String CONFIG = "config";

	/**
	 * Key within {@link #CONFIG} telling the client that the value carries a fraction, which
	 * decides the on-screen keyboard the input asks for.
	 */
	private static final String DECIMAL = "decimal";

	private final NumberFormat _format;

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
	public ReactNumberInputControl(ReactContext context, FieldModel model, NumberFormat format) {
		super(context, model, "TLNumberInput");
		_format = format;
		putState(CONFIG, Map.of(DECIMAL, Boolean.valueOf(NumberFormats.isFractional(format))));
		// The base constructor seeded the raw number into the value state; re-emit it as the text
		// the user reads and edits.
		putState(VALUE, format(model.getValue()));
		setSendValueOnBlur(true);
	}

	/**
	 * The format the value is displayed in and entered in.
	 */
	public NumberFormat getFormat() {
		return _format;
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
		putState(VALUE, format(newValue));
	}

	private String format(Object value) {
		return value instanceof Number ? _format.format(value) : null;
	}

}
