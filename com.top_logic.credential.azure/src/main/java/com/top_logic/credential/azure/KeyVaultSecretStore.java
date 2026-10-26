/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.credential.azure;

import java.util.regex.Pattern;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.credential.AbstractSecretStore;
import com.top_logic.basic.credential.NamedSecretSource;
import com.top_logic.basic.credential.SecretStore;
import com.top_logic.basic.credential.SecretStores;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.util.ResKey;

/**
 * {@link SecretStore} keeping all named secrets of the installation in one Azure Key Vault.
 *
 * <p>
 * The names of the secrets are written in snake case (<code>db_password</code>), whereas Azure Key
 * Vault allows only letters, digits and dashes. The store therefore looks up the name with the
 * configured prefix and each underscore replaced by a dash: the secret
 * <code>&lt;api-key name="tl_openai_api_key"/&gt;</code> is read from the vault secret
 * <code>tl-openai-api-key</code>. A secret that does not exist in the vault is not held by the
 * store, so that a chain of stores asks its next store.
 * </p>
 *
 * <p>
 * The application authenticates at the key vault with an access token, by default with its
 * managed identity (see {@link ManagedIdentityCredentials}); the identity needs the role "Key
 * Vault Secrets User" on the vault. A secret (and the absence of a secret) is kept for the cache
 * time and read again afterwards, so that a secret rotated in the vault takes effect without
 * restarting the application.
 * </p>
 *
 * <p>
 * The store of the installation is configured in the global configuration section
 * <code>SecretStores$Config</code>:
 * </p>
 *
 * <pre>
 * &lt;config config:interface="com.top_logic.basic.credential.SecretStores$Config"&gt;
 *   &lt;store class="com.top_logic.credential.azure.KeyVaultSecretStore"
 *     vault-url="https://my-vault.vault.azure.net"
 *   /&gt;
 * &lt;/config&gt;
 * </pre>
 *
 * <p>
 * With a user-assigned managed identity and secrets of several installations in one vault,
 * distinguished by a prefix (<code>prod-db-password</code> for the secret
 * <code>db_password</code>):
 * </p>
 *
 * <pre>
 * &lt;config config:interface="com.top_logic.basic.credential.SecretStores$Config"&gt;
 *   &lt;store class="com.top_logic.credential.azure.KeyVaultSecretStore"
 *     vault-url="https://my-vault.vault.azure.net"
 *     prefix="prod_"
 *   &gt;
 *     &lt;credentials class="com.top_logic.credential.azure.ManagedIdentityCredentials"
 *       resource="https://vault.azure.net"
 *       client-id="00000000-0000-0000-0000-000000000000"
 *     /&gt;
 *   &lt;/store&gt;
 * &lt;/config&gt;
 * </pre>
 *
 * @see NamedSecretSource
 * @see SecretStores
 * @see com.top_logic.basic.credential.ChainSecretStore
 * @see KeyVaultSecretSource
 */
@Label("Azure Key Vault")
public class KeyVaultSecretStore extends AbstractSecretStore<KeyVaultSecretStore.Config> {

	/**
	 * Configuration options for {@link KeyVaultSecretStore}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends AbstractSecretStore.Config<KeyVaultSecretStore>, KeyVaultConfig {

		/**
		 * Tag name of this configuration in a list of {@link SecretStore}s.
		 */
		String TAG_NAME = "azure-key-vault-store";

	}

	/**
	 * Names of secrets allowed by Azure Key Vault.
	 */
	private static final Pattern VAULT_NAME = Pattern.compile("[0-9a-zA-Z-]{1,127}");

	private final KeyVaultClient _client;

	/**
	 * Creates a {@link KeyVaultSecretStore} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public KeyVaultSecretStore(InstantiationContext context, Config config) {
		super(context, config);
		_client = new KeyVaultClient(context, config);
	}

	@Override
	protected String lookupKey(String key) {
		return _client.getSecret(vaultName(key), "", true);
	}

	@Override
	protected ResKey missing(String key) {
		return I18NConstants.ERROR_SECRET_NOT_FOUND__NAME_VAULT.fill(vaultName(key), _client.getVaultUrl());
	}

	/**
	 * The name of the secret with the given key in the key vault.
	 *
	 * @param key
	 *        The name of the secret with the configured prefix.
	 * @return The key with each underscore replaced by a dash.
	 *
	 * @throws I18NRuntimeException
	 *         If the result is not a valid name in Azure Key Vault.
	 */
	public static String vaultName(String key) {
		String result = key.replace('_', '-');
		if (!VAULT_NAME.matcher(result).matches()) {
			throw new I18NRuntimeException(I18NConstants.ERROR_INVALID_SECRET_NAME__NAME.fill(key));
		}
		return result;
	}

}
