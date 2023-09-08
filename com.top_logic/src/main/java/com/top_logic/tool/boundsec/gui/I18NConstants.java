/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.gui;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalization constants for this package.
 *
 * @see ResPrefix
 * @see ResKey
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en The object {0} is not a role.
	 */
	public static ResKey1 ERROR_NOT_A_ROLE__ELEMENT;

	public static ResKey ERROR_SYSTEM_ROLE_CANNOT_BE_DETETED = legacyKey("admin.role.edit.deleteRole.disabled.isSystem");

	static {
		initConstants(I18NConstants.class);
	}
}
