/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.credential.azure;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.format.MillisFormat;
import com.top_logic.basic.credential.CredentialProvider;

/**
 * Configuration of the access to an Azure Key Vault.
 *
 * @see KeyVaultClient
 */
@Abstract
public interface KeyVaultConfig extends AzureRestConfig {

	/**
	 * @see #getVaultUrl()
	 */
	String VAULT_URL = "vault-url";

	/**
	 * @see #getCredentials()
	 */
	String CREDENTIALS = "credentials";

	/**
	 * @see #getCacheTime()
	 */
	String CACHE_TIME = "cache-time";

	/**
	 * URL of the key vault, e.g. <code>https://my-vault.vault.azure.net</code>.
	 */
	@Name(VAULT_URL)
	@Mandatory
	String getVaultUrl();

	/**
	 * @see #getVaultUrl()
	 */
	void setVaultUrl(String value);

	/**
	 * Credentials for the requests to the key vault.
	 *
	 * <p>
	 * If not given, the managed identity of the machine or app is used, see
	 * {@link ManagedIdentityCredentials}.
	 * </p>
	 */
	@Name(CREDENTIALS)
	PolymorphicConfiguration<? extends CredentialProvider> getCredentials();

	/**
	 * @see #getCredentials()
	 */
	void setCredentials(PolymorphicConfiguration<? extends CredentialProvider> value);

	/**
	 * Time a secret read from the key vault is used before it is read again.
	 *
	 * <p>
	 * A secret rotated in the key vault takes effect after this time at the latest.
	 * </p>
	 */
	@Name(CACHE_TIME)
	@Format(MillisFormat.class)
	@FormattedDefault("5min")
	long getCacheTime();

	/**
	 * @see #getCacheTime()
	 */
	void setCacheTime(long value);

}
