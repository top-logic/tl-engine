/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.authentication.apikey;

import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en No cookie with name ''{0}'' found.
	 */
	public static ResKey1 AUTH_FAILED_NO_COOKIE__PARAMETER;

	/**
	 * @en No query parameter with name ''{0}'' found.
	 */
	public static ResKey1 AUTH_FAILED_NO_QUERY_PARAM__PARAMETER;

	/**
	 * @en No header with name ''{0}'' found.
	 */
	public static ResKey1 AUTH_FAILED_NO_HEADER__PARAMETER;

	/**
	 * @en Invalid API key found in parameter ''{0}''.
	 */
	public static ResKey1 AUTH_FAILED_INVALID_API_KEY__PARAMETER;

	static {
		initConstants(I18NConstants.class);
	}
}
