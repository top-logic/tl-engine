/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactDatePickerControl;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link ReactFieldControlProvider} for date attributes.
 */
public class DatePickerControlProvider implements ReactFieldControlProvider {

	@Override
	public ReactControl createControl(ReactContext context, TLStructuredTypePart part, FieldModel model) {
		return new ReactDatePickerControl(context, model);
	}

}
