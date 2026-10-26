/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.credential;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en The secret variable "{0}" is neither defined as system property nor as environment
	 *     variable.
	 */
	public static ResKey1 ERROR_SECRET_VARIABLE_UNDEFINED__NAME;

	/**
	 * @en The secret file "{0}" cannot be read.
	 */
	public static ResKey1 ERROR_SECRET_FILE_UNREADABLE__PATH;

	/**
	 * @en The secret file "{0}" is empty.
	 */
	public static ResKey1 ERROR_SECRET_FILE_EMPTY__PATH;

	/**
	 * @en No secret has been configured.
	 */
	public static ResKey ERROR_SECRET_NOT_CONFIGURED;

	/**
	 * @en The secret "{0}" is not available in the secret storage.
	 */
	public static ResKey1 ERROR_SECRET_UNDEFINED__NAME;

	/**
	 * @en The secret "{0}" is empty.
	 */
	public static ResKey1 ERROR_SECRET_EMPTY__NAME;

	/**
	 * @en "{0}" is not a valid name of a secret file.
	 */
	public static ResKey1 ERROR_SECRET_NAME_INVALID__NAME;

	/**
	 * @en The override of the secret "{0}" refers to itself.
	 */
	public static ResKey1 ERROR_SECRET_OVERRIDE_CYCLE__NAME;

	/**
	 * Error reported when a consumer of an external service is configured with both explicit
	 * credentials and an API key.
	 *
	 * @en "{0}" is configured with both credentials and an API key. Configure only one of them.
	 * @de "{0}" ist sowohl mit Zugangsdaten als auch mit einem API-Schlüssel konfiguriert. Nur
	 *     eines von beiden darf angegeben werden.
	 */
	public static ResKey1 ERROR_CREDENTIALS_AND_API_KEY__OWNER;

	static {
		initConstants(I18NConstants.class);
	}
}
