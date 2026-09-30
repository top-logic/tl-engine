/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dev.tools.model;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for the model-editor developer tool.
 *
 * @see ModelEditAction
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Please enter a name.
	 */
	public static ResKey ERROR_MISSING_NAME;

	/**
	 * @en No target selected. Select the module or type to create the element in first.
	 */
	public static ResKey ERROR_NO_TARGET;

	/**
	 * @en Please select the value type of the new attribute.
	 */
	public static ResKey ERROR_MISSING_TYPE;

	static {
		initConstants(I18NConstants.class);
	}
}
