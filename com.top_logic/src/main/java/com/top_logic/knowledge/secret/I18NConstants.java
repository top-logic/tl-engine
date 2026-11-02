/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.secret;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en The secret "{0}" is not stored in the database.
	 */
	public static ResKey1 ERROR_DATABASE_SECRET_UNDEFINED__NAME;

	/**
	 * @en The secret "{0}" cannot be stored: it is empty.
	 */
	public static ResKey1 ERROR_DATABASE_SECRET_EMPTY__NAME;

	/**
	 * @en The secret "{0}" cannot be stored in the database: it is the key of the database secret
	 *     store.
	 */
	public static ResKey1 ERROR_DATABASE_SECRET_IS_KEY__NAME;

	/**
	 * @en The secret "{0}" stored in the database cannot be decrypted: it was modified or encrypted
	 *     with another key.
	 */
	public static ResKey1 ERROR_DATABASE_SECRET_NOT_AUTHENTIC__NAME;

	/**
	 * @en No secret holding the key of the database secret store is configured.
	 */
	public static ResKey ERROR_DATABASE_SECRET_KEY_NOT_CONFIGURED;

	/**
	 * @en The key of the database secret store is not available: the secret "{0}" is not defined.
	 */
	public static ResKey1 ERROR_DATABASE_SECRET_KEY_MISSING__KEY;

	/**
	 * @en The key of the database secret store is invalid: the secret "{0}" is not the Base64
	 *     encoding of an AES key of 128, 192 or 256 bits.
	 */
	public static ResKey1 ERROR_DATABASE_SECRET_KEY_INVALID__KEY;

	/**
	 * @en Stored secret "{0}".
	 */
	public static ResKey1 STORED_SECRET__NAME;

	/**
	 * @en Removed secret "{0}".
	 */
	public static ResKey1 REMOVED_SECRET__NAME;

	static {
		initConstants(I18NConstants.class);
	}
}
