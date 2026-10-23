/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.credential.azure;

import java.io.IOException;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.util.BinaryData;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.format.MillisFormat;
import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.credential.SecretSource;
import com.top_logic.basic.exception.I18NRuntimeException;

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
 */
public class KeyVaultSecretSource extends AbstractConfiguredInstance<KeyVaultSecretSource.Config>
		implements SecretSource {

	/**
	 * Resource identifier of Azure Key Vault, for which the access token of the default
	 * credentials is requested.
	 */
	public static final String KEY_VAULT_RESOURCE = "https://vault.azure.net";

	/**
	 * Configuration options for {@link KeyVaultSecretSource}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<KeyVaultSecretSource>, AzureRestConfig {

		/**
		 * Tag name of this configuration in a list of {@link SecretSource}s.
		 */
		String TAG_NAME = "azure-key-vault-secret";

		/**
		 * @see #getVaultUrl()
		 */
		String VAULT_URL = "vault-url";

		/**
		 * @see #getSecretName()
		 */
		String SECRET_NAME = "secret-name";

		/**
		 * @see #getSecretVersion()
		 */
		String SECRET_VERSION = "secret-version";

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

	private final SecretClient _client;

	private final String _vaultUrl;

	private final Object _lock = new Object();

	/**
	 * The secret read last, <code>null</code> before the first read.
	 */
	private volatile CachedSecret _cached;

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
		PolymorphicConfiguration<? extends CredentialProvider> credentials = config.getCredentials();
		if (credentials == null) {
			credentials = defaultCredentials();
		}
		CredentialProvider provider = context.getInstance(credentials);
		_vaultUrl = stripTrailingSlash(config.getVaultUrl());
		HttpPipeline pipeline = new HttpPipelineBuilder()
			.httpClient(AzureHttpClients.create(config))
			.policies(new CredentialProviderPolicy(provider))
			.build();
		SecretClientBuilder builder = new SecretClientBuilder().pipeline(pipeline);
		try {
			builder.vaultUrl(_vaultUrl);
		} catch (IllegalArgumentException ex) {
			throw new I18NRuntimeException(I18NConstants.ERROR_INVALID_URL__URL.fill(_vaultUrl), ex);
		}
		_client = builder.buildClient();
	}

	private static ManagedIdentityCredentials.Config defaultCredentials() {
		ManagedIdentityCredentials.Config result =
			TypedConfiguration.newConfigItem(ManagedIdentityCredentials.Config.class);
		result.setImplementationClass(ManagedIdentityCredentials.class);
		result.setResource(KEY_VAULT_RESOURCE);
		return result;
	}

	@Override
	public String getSecret() {
		CachedSecret cached = _cached;
		if (cached != null && !cached.isExpired()) {
			return cached.getValue();
		}
		synchronized (_lock) {
			cached = _cached;
			if (cached != null && !cached.isExpired()) {
				// Read by another thread in the meantime.
				return cached.getValue();
			}
			String value = fetchSecret();
			_cached = new CachedSecret(value, System.currentTimeMillis() + getConfig().getCacheTime());
			return value;
		}
	}

	private String fetchSecret() {
		Config config = getConfig();
		String name = config.getSecretName();
		String version = StringServices.nonNull(config.getSecretVersion());
		KeyVaultSecret secret;
		try {
			secret = _client.getSecret(name, version);
		} catch (HttpResponseException ex) {
			// The cause is not reported, its message quotes the response.
			throw new I18NRuntimeException(I18NConstants.ERROR_SECRET_REQUEST_FAILED__NAME_VAULT_STATUS_DETAIL
				.fill(name, _vaultUrl, ex.getResponse().getStatusCode(), errorText(ex)));
		} catch (I18NRuntimeException ex) {
			// No credentials for the key vault.
			throw ex;
		} catch (RuntimeException ex) {
			IOException failure = ManagedIdentityCredentials.ioCause(ex);
			if (failure != null) {
				throw new I18NRuntimeException(I18NConstants.ERROR_VAULT_UNREACHABLE__URL.fill(_vaultUrl), failure);
			}
			// The cause is not reported, its message could quote the response.
			throw new I18NRuntimeException(I18NConstants.ERROR_MALFORMED_RESPONSE__URL.fill(_vaultUrl));
		}
		String value = secret.getValue();
		if (StringServices.isEmpty(value)) {
			throw new I18NRuntimeException(I18NConstants.ERROR_SECRET_EMPTY__NAME_VAULT.fill(name, _vaultUrl));
		}
		return value;
	}

	private static String errorText(HttpResponseException ex) {
		Object value = ex.getValue();
		if (value instanceof BinaryData) {
			return AzureErrorResponse.describe(value.toString());
		}
		if (value instanceof String) {
			return AzureErrorResponse.describe((String) value);
		}
		return AzureErrorResponse.describeJson(value);
	}

	private static String stripTrailingSlash(String url) {
		return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
	}

	private static final class CachedSecret {

		private final String _value;

		private final long _expires;

		CachedSecret(String value, long expires) {
			_value = value;
			_expires = expires;
		}

		String getValue() {
			return _value;
		}

		boolean isExpired() {
			return System.currentTimeMillis() >= _expires;
		}

	}

}
