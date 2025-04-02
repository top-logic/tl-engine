/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.authentication.oauth;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en No username available for token.
	 */
	public static ResKey NO_USERNAME_IN_INTROSPECTION_RESPONSE;

	/**
	 * @en Multiple Client Credential configurations for domain {0}.
	 */
	public static ResKey1 ERROR_MULTIPLE_CLIENT_CREDENTIAL_SECRETS__DOMAIN;

	/**
	 * @en "No token introspection endpoint for domain {0}.
	 */
	public static ResKey1 ERROR_NO_INTROSPECTION_ENDPOINT__DOMAIN;

	static {
		initConstants(I18NConstants.class);
	}
}
