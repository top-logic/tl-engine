/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values;

import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.control.HtmlTextControl;
import com.top_logic.layout.form.control.TextInputControl;

/**
 * {@link MultiLineText} editing the source of an HTML text, and displaying the formatted text
 * while it cannot be edited.
 *
 * @see HtmlTextControl
 */
public class HtmlText extends MultiLineText {

	/**
	 * Creates a {@link HtmlText}.
	 *
	 * @param rows
	 *        The number of rows to display in the text area.
	 */
	public HtmlText(int rows) {
		super(rows);
	}

	@Override
	protected TextInputControl createControl(FormField field) {
		return new HtmlTextControl(field);
	}

}
