/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import java.util.Map;

import com.top_logic.layout.form.FormField;

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
	 * @param model
	 *        The form field.
	 * @param decimalPlaces
	 *        The number of decimal places. When greater than zero, the React component uses decimal
	 *        step mode.
	 */
	public ReactNumberInputControl(FormField model, int decimalPlaces) {
		super(model, "TLNumberInput");
		if (decimalPlaces > 0) {
			putState(CONFIG, Map.of("decimal", Boolean.TRUE));
		}
	}

}
