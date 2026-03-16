/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import java.util.Map;

import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommand;

/**
 * A {@link ReactFormFieldControl} for number input fields.
 *
 * <p>
 * The {@code decimalPlaces} constructor parameter controls whether the React component uses decimal
 * or integer step mode. When {@code decimalPlaces > 0}, a {@code config.decimal} flag is set so the
 * client renders with {@code step='0.01'} instead of {@code step='1'}.
 * </p>
 */
public class ReactNumberInputControl extends ReactFormFieldControl {

	private static final String CONFIG = "config";

	/**
	 * Creates a new {@link ReactNumberInputControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param model
	 *        The field model.
	 * @param decimalPlaces
	 *        The number of decimal places. When greater than zero, the React component uses decimal
	 *        step mode.
	 */
	public ReactNumberInputControl(ReactContext context, FieldModel model, int decimalPlaces) {
		super(context, model, "TLNumberInput");
		if (decimalPlaces > 0) {
			putState(CONFIG, Map.of("decimal", Boolean.TRUE));
		}
	}

	@Override
	@ReactCommand("valueChanged")
	void handleValueChanged(Map<String, Object> arguments) {
		Object rawValue = arguments.get(VALUE);
		FieldModel model = getFieldModel();

		AbstractFieldModel abstractModel =
			model instanceof AbstractFieldModel ? (AbstractFieldModel) model : null;

		if (rawValue == null || "".equals(rawValue.toString().trim())) {
			if (abstractModel != null) {
				abstractModel.setError(null);
			}
			model.setValue(null);
			return;
		}

		try {
			double parsed = Double.parseDouble(rawValue.toString());
			if (abstractModel != null) {
				abstractModel.setError(null);
			}
			model.setValue(parsed);
		} catch (NumberFormatException ex) {
			// Set error on model so it gets displayed in chrome and as red border on input.
			if (abstractModel != null) {
				abstractModel.setError(
					I18NConstants.ERROR_INVALID_NUMBER__VALUE.fill(rawValue.toString()));
			}
		}
	}

	@Override
	protected Object parseClientValue(Object rawValue) {
		if (rawValue instanceof Number) {
			return rawValue;
		}
		if (rawValue != null) {
			try {
				return Double.parseDouble(rawValue.toString());
			} catch (NumberFormatException ex) {
				return null;
			}
		}
		return null;
	}

}
