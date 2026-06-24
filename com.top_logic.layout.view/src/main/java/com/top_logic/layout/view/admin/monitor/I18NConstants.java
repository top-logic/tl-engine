/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.monitor;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for the {@link com.top_logic.layout.view.admin.monitor} package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Section
	 */
	public static ResKey SYSTEM_ENVIRONMENT_SECTION_COLUMN;

	/**
	 * @en Name
	 */
	public static ResKey SYSTEM_ENVIRONMENT_NAME_COLUMN;

	/**
	 * @en Value
	 */
	public static ResKey SYSTEM_ENVIRONMENT_VALUE_COLUMN;

	static {
		initConstants(I18NConstants.class);
	}
}
