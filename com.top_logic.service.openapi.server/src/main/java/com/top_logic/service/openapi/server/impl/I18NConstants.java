/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.impl;

import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Error when compiling operation for path "{0}" with parameters "{1}".
	 */
	public static ResKey2 ERROR_COMPILING_EXPRESSION__PATH_PARAMETERS;

	/**
	 * @en Invalid content type "{0}": {1}
	 */
	public static ResKey2 ERROR_INVALID_CONTENET_TYPE__VALUE_MSG;

	static {
		initConstants(I18NConstants.class);
	}
}
