/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import com.top_logic.layout.form.FormField;
import com.top_logic.layout.react.ReactContext;

/**
 * A {@link ReactFormFieldControl} for text input fields.
 */
public class ReactTextInputControl extends ReactFormFieldControl {

	/**
	 * Creates a new {@link ReactTextInputControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param model
	 *        The form field.
	 */
	public ReactTextInputControl(ReactContext context, FormField model) {
		super(context, model, "TLTextInput");
	}

}
