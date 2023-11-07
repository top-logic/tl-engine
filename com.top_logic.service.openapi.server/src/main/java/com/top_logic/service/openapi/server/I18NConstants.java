/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server;

import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en No authentication algorithm for {1}:{0} could be created.
	 */
	public static ResKey2 CREATING_AUTHENTICATOR_FAILED__PATH__METHOD;

	/**
	 * @en The pathes ''{0}'' and ''{1}'' lead to the same result considering the path parameters:
	 *     {2}
	 */
	public static ResKey3 CLASHING_PATH_PARAMETERS__PATH1__PATH2__UNIFIED_COMPLETE_PATH;

	static {
		initConstants(I18NConstants.class);
	}
}
