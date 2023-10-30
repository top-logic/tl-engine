/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.template;

import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en The property {1} is not defined on {0}. Available properties are: {2}
	 */
	public static ResKey3 ERROR_NO_SUCH_PROPERTY__SELF_NAME_AVAILABLE;

	/**
	 * @en The property {1} is not defined on {0}.
	 */
	public static ResKey2 ERROR_NO_SUCH_PROPERTY__SELF_NAME;

	static {
		initConstants(I18NConstants.class);
	}
}
