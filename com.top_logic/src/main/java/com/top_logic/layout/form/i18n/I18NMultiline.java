/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.i18n;

import com.top_logic.layout.form.control.TextInputControl;
import com.top_logic.layout.form.values.MultiLineText;

/**
 * {@link I18NActiveLanguageControlProvider} with multiline input.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class I18NMultiline extends I18NActiveLanguageControlProvider {

	/**
	 * Creates a {@link I18NMultiline}.
	 */
	public I18NMultiline() {
		super(MultiLineText.DEFAULT_ROWS, TextInputControl.NO_COLUMNS);
	}

}
