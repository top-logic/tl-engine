/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.credential.azure;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.credential.SecretSource;
import com.top_logic.basic.util.ResKey;

/**
 * {@link SecretSource} reading a secret from Azure Key Vault.
 *
 * <p>
 * The application authenticates at the key vault with an access token, by default with its
 * managed identity (see {@link ManagedIdentityCredentials}), so the configuration names only
 * where the secret is kept. The identity needs the role "Key Vault Secrets User" on the vault or
 * the secret.
 * </p>
 *
 * <p>
 * The secret is read with the <code>SecretClient</code> of the Azure Key Vault library. Its
 * requests are authenticated by the configured {@link CredentialProvider} (see
 * {@link CredentialProviderPolicy}), so that any provider works, e.g. also the OAuth client
 * credentials of a service principal.
 * </p>
 *
 * <p>
 * It reads one secret named in its configuration. The named secrets of an installation that keeps
 * all its secrets in one key vault are read through the secret store <code>KeyVaultSecretStore</code>
 * instead.
 * </p>
 *
 * <p>
 * The secret is kept for the configured cache time and read again afterwards, so that a secret
 * rotated in the key vault takes effect without restarting the application.
 * </p>
 *
 * <p>
 * An API key read from the key vault with the system-assigned managed identity:
 * </p>
 *
 * <pre>
 * &lt;secret class="com.top_logic.credential.azure.KeyVaultSecretSource"
 *   vault-url="https://my-vault.vault.azure.net"
 *   secret-name="openai-api-key"
 * /&gt;
 * </pre>
 *
 * <p>
 * With a user-assigned managed identity, the credentials are given explicitly:
 * </p>
 *
 * <pre>
 * &lt;secret class="com.top_logic.credential.azure.KeyVaultSecretSource"
 *   vault-url="https://my-vault.vault.azure.net"
 *   secret-name="openai-api-key"
 * &gt;
 *   &lt;credentials class="com.top_logic.credential.azure.ManagedIdentityCredentials"
 *     resource="https://vault.azure.net"
 *     client-id="00000000-0000-0000-0000-000000000000"
 *   /&gt;
 * &lt;/secret&gt;
 * </pre>
 *
 * @see KeyVaultSecretStore
 */
public class KeyVaultSecretSource extends AbstractConfiguredInstance<KeyVaultSecretSource.Config>
		implements SecretSource {

	/**
	 * Resource identifier of Azure Key Vault, for which the access token of the default
	 * credentials is requested.
	 */
	public static final String KEY_VAULT_RESOURCE = KeyVaultClient.KEY_VAULT_RESOURCE;

	/**
	 * Configuration options for {@link KeyVaultSecretSource}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<KeyVaultSecretSource>, KeyVaultConfig {

		/**
		 * Tag name of this configuration in a list of {@link SecretSource}s.
		 */
		String TAG_NAME = "azure-key-vault-secret";

		/**
		 * @see #getSecretName()
		 */
		String SECRET_NAME = "secret-name";

		/**
		 * @see #getSecretVersion()
		 */
		String SECRET_VERSION = "secret-version";

		/**
		 * Name of the secret in the key vault.
		 */
		@Name(SECRET_NAME)
		@Mandatory
		String getSecretName();

		/**
		 * @see #getSecretName()
		 */
		void setSecretName(String value);

		/**
		 * Version of the secret.
		 *
		 * <p>
		 * If empty, the current version is read, so that a new version takes effect after the
		 * cache time.
		 * </p>
		 */
		@Name(SECRET_VERSION)
		String getSecretVersion();

		/**
		 * @see #getSecretVersion()
		 */
		void setSecretVersion(String value);

	}

	/**
	 * Path segment between the vault URL and the name of a secret in the identifier of a secret.
	 */
	private static final String SECRETS_PATH = "/secrets/";

	private final KeyVaultClient _client;

	/**
	 * Creates a {@link KeyVaultSecretSource} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public KeyVaultSecretSource(InstantiationContext context, Config config) {
		super(context, config);
		_client = new KeyVaultClient(context, config);
	}

	@Override
	public String getSecret() {
		Config config = getConfig();
		return _client.getSecret(config.getSecretName(), StringServices.nonNull(config.getSecretVersion()), false);
	}

	@Override
	public String lookup() {
		Config config = getConfig();
		return _client.getSecret(config.getSecretName(), StringServices.nonNull(config.getSecretVersion()), true);
	}

	/**
	 * The identifier of the secret in the key vault, its URL without the secret.
	 */
	@Override
	public ResKey reference() {
		Config config = getConfig();
		String version = StringServices.nonNull(config.getSecretVersion());
		return ResKey.text(_client.getVaultUrl() + SECRETS_PATH + config.getSecretName()
			+ (version.isEmpty() ? "" : "/" + version));
	}

}
