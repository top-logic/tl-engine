/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import com.top_logic.layout.form.FormField;

/**
 * A {@link ReactFormFieldControl} for checkbox fields.
 */
public class ReactCheckboxControl extends ReactFormFieldControl {

	/**
	 * Creates a new {@link ReactCheckboxControl}.
	 *
	 * @param model
	 *        The form field.
	 */
	public ReactCheckboxControl(FormField model) {
		super(model, "TLCheckbox");
	}

}
