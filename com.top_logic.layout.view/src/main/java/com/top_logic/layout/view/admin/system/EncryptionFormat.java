/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.system;

import com.top_logic.base.security.util.CryptSupport;
import com.top_logic.base.security.util.CryptSupportSecretSource;
import com.top_logic.basic.ConfigurationEncryption;
import com.top_logic.basic.config.annotation.Encrypted;
import com.top_logic.basic.config.annotation.Label;

/**
 * The forms in which a configuration accepts an encrypted value.
 *
 * @see SecretFunctions#encrypt(String, EncryptionFormat)
 * @see SecretFunctions#decrypt(String, EncryptionFormat)
 */
public enum EncryptionFormat {

	/**
	 * The value of an {@link Encrypted} configuration property, e.g. a secret given in the
	 * configuration itself, and of an entry marked as encrypted in the application properties.
	 *
	 * @see ConfigurationEncryption
	 */
	@Label("Configuration encryption")
	CONFIGURATION,

	/**
	 * A value encoded by {@link CryptSupport}, e.g. the master credential of an LDAP server given
	 * as attribute, or the value of a {@link CryptSupportSecretSource}.
	 */
	@Label("RSA encoding (CryptSupport)")
	CRYPT_SUPPORT;

}
