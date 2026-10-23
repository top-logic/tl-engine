/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.credential.azure;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey4;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en The managed identity endpoint "{0}" cannot be reached. The application does not run on
	 *     Azure, or the endpoint is not accessible from it.
	 */
	public static ResKey1 ERROR_IDENTITY_ENDPOINT_UNREACHABLE__URL;

	/**
	 * @en The managed identity endpoint "{1}" issued no access token for the resource "{0}" (HTTP
	 *     status {2}). {3}
	 */
	public static ResKey4 ERROR_TOKEN_REQUEST_FAILED__RESOURCE_URL_STATUS_DETAIL;

	/**
	 * @en No access token of the managed identity is available for the resource "{0}". {1}
	 */
	public static ResKey2 ERROR_TOKEN_UNAVAILABLE__RESOURCE_DETAIL;

	/**
	 * @en The key vault "{0}" cannot be reached.
	 */
	public static ResKey1 ERROR_VAULT_UNREACHABLE__URL;

	/**
	 * @en Reading the secret "{0}" from the key vault "{1}" failed (HTTP status {2}). {3}
	 */
	public static ResKey4 ERROR_SECRET_REQUEST_FAILED__NAME_VAULT_STATUS_DETAIL;

	/**
	 * @en The secret "{0}" in the key vault "{1}" has no value.
	 */
	public static ResKey2 ERROR_SECRET_EMPTY__NAME_VAULT;

	/**
	 * @en The response of "{0}" is malformed.
	 */
	public static ResKey1 ERROR_MALFORMED_RESPONSE__URL;

	/**
	 * @en The URL "{0}" is invalid.
	 */
	public static ResKey1 ERROR_INVALID_URL__URL;

	static {
		initConstants(I18NConstants.class);
	}
}
