/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.gui;

import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Theme configuration with ID "{0}" not found.
	 */
	public static ResKey1 ERROR_REFERENCED_THEME_NOT_DEFINED__ID;

	/**
	 * @en Theme with ID "{0}" has a cyclic parent hierarchy.
	 */
	public static ResKey1 ERROR_CYCLIC_THEME_HIERARCHY__ID;

	static {
		initConstants(I18NConstants.class);
	}
}
