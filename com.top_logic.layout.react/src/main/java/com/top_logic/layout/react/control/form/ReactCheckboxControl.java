/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommandHandler;

/**
 * A {@link ReactFormFieldControl} for checkbox fields.
 */
public class ReactCheckboxControl extends ReactFormFieldControl {

	/**
	 * Creates a new {@link ReactCheckboxControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param model
	 *        The field model.
	 */
	public ReactCheckboxControl(ReactContext context, FieldModel model) {
		super(context, model, "TLCheckbox");
	}

	/**
	 * Handles a checkbox toggle: its value is a {@code boolean}, so it has its own typed arguments
	 * rather than the base field's text value.
	 */
	@ReactCommandHandler(CMD_VALUE_CHANGED)
	void handleChecked(CheckboxValueArguments args) {
		applyClientValue(parseClientValue(args.isChecked()));
	}

	@Override
	protected Object parseClientValue(Object rawValue) {
		return Boolean.TRUE.equals(rawValue);
	}

}
