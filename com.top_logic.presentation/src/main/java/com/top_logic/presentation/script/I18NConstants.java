/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.script;

import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en The font size must be between {1}pt and {2}pt, but is {0}.
	 */
	public static ResKey3 ERROR_INVALID_FONT_SIZE__SIZE_MIN_MAX;

	/**
	 * @en The file "{0}" is not a PowerPoint presentation or template.
	 */
	public static ResKey1 ERROR_NOT_A_PRESENTATION__NAME;

	/**
	 * @en The width available for the text must be a positive number of points, but is {0}.
	 */
	public static ResKey1 ERROR_INVALID_TEXT_WIDTH__WIDTH;

	static {
		initConstants(I18NConstants.class);
	}
}
