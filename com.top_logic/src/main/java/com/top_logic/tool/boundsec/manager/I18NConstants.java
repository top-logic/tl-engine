/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.manager;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalization constants for this package.
 *
 * @see ResPrefix
 * @see ResKey
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Restart access manager and reload role rules
	 */
	public static ResKey RESTART_AND_RELOAD_ACCESS_MANAGER;

	static {
		initConstants(I18NConstants.class);
	}

}
