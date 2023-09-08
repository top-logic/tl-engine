/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.export.ui;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en New module configuration file created and entered into the application configuration. The
	 *     configuration must be reloaded and all sessions must be terminated.
	 * @tooltip New module configuration stored.
	 */
	public static ResKey NEW_MODULE_CONFIGURATION;

	static {
		initConstants(I18NConstants.class);
	}
}
