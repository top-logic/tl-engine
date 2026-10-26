/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.credential;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;

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
	 * @en The secret "{0}" cannot be read: the secret store service is not started. A service
	 *     reading a named secret must declare a dependency on the secret store service.
	 */
	public static ResKey1 ERROR_SECRET_STORE_NOT_STARTED__NAME;

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

	/**
	 * Description of a secret that is kept in the configuration itself.
	 *
	 * @en In the configuration
	 * @de In der Konfiguration
	 */
	public static ResKey SECRET_IN_CONFIGURATION;

	/**
	 * Description of a secret source or store by its kind and the non-secret details identifying
	 * it.
	 *
	 * @en {0} ({1})
	 * @de {0} ({1})
	 */
	public static ResKey2 SECRET_DESCRIPTION__KIND_DETAIL;

	/**
	 * Description of the override in the secret store service delivering a named secret.
	 *
	 * @en Override: {0}
	 * @de Übersteuerung: {0}
	 */
	public static ResKey1 SECRET_RESOLVED_BY_OVERRIDE__SOURCE;

	/**
	 * @en The secret source cannot be created from its configuration.
	 * @de Die Quelle des Geheimnisses kann nicht aus ihrer Konfiguration erzeugt werden.
	 */
	public static ResKey ERROR_SECRET_SOURCE_INVALID;

	/**
	 * @en Checking the secret failed: {0}.
	 * @de Die Prüfung des Geheimnisses ist fehlgeschlagen: {0}.
	 */
	public static ResKey1 ERROR_SECRET_CHECK_FAILED__PROBLEM;

	static {
		initConstants(I18NConstants.class);
	}
}
