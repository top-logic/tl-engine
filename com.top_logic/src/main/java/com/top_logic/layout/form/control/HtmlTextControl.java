/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;

import com.top_logic.basic.exception.I18NException;
import com.top_logic.basic.html.SafeHTML;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.form.FormField;

/**
 * {@link TextInputControl} editing the source of an HTML text, and displaying the formatted text
 * while it cannot be edited.
 *
 * <p>
 * The text is displayed formatted only if it passes the application's HTML safety check
 * ({@link SafeHTML}). A text the check rejects is displayed as its source.
 * </p>
 */
public class HtmlTextControl extends TextInputControl {

	/**
	 * Creates a {@link HtmlTextControl}.
	 */
	public HtmlTextControl(FormField model) {
		super(model);
	}

	@Override
	protected void writeMultiLineTextContent(TagWriter out, String value) throws IOException {
		try {
			SafeHTML.getInstance().check(value);
		} catch (I18NException ex) {
			super.writeMultiLineTextContent(out, value);
			return;
		}
		out.writeContent(value);
	}

}
