/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.credential.azure;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.Map;

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
	 * Version of the Key Vault REST API.
	 */
	public static final String API_VERSION = "7.4";

	/**
	 * Path segment of the secrets collection of a key vault.
	 */
	public static final String SECRETS_PATH = "secrets";

	/**
	 * Field of a secret bundle with the value of the secret.
	 */
	public static final String FIELD_VALUE = "value";

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

	private final CredentialProvider _credentials;

	private final AzureRestClient _client;

	private final String _vaultUrl;

	private final String _url;

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
		_credentials = context.getInstance(credentials);
		_client = new AzureRestClient(config);
		_vaultUrl = stripTrailingSlash(config.getVaultUrl());
		StringBuilder url = new StringBuilder(_vaultUrl).append('/').append(SECRETS_PATH)
			.append('/').append(config.getSecretName());
		String version = StringServices.nonEmpty(config.getSecretVersion());
		if (version != null) {
			url.append('/').append(version);
		}
		Map<String, String> query = new LinkedHashMap<>();
		query.put(AzureRestClient.PARAM_API_VERSION, API_VERSION);
		_url = AzureRestClient.withQuery(url.toString(), query);
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
		HttpResponse<String> response = send();
		if (response.statusCode() == 401) {
			// The token may have been revoked before its end of life.
			_credentials.invalidate();
			response = send();
		}
		if (!AzureRestClient.isSuccess(response)) {
			throw new I18NRuntimeException(I18NConstants.ERROR_SECRET_REQUEST_FAILED__NAME_VAULT_STATUS_DETAIL
				.fill(config.getSecretName(), _vaultUrl, response.statusCode(),
					AzureRestClient.errorText(response.body())));
		}
		Map<String, Object> bundle = AzureRestClient.parseObject(response.body());
		if (bundle == null) {
			throw new I18NRuntimeException(I18NConstants.ERROR_MALFORMED_RESPONSE__URL.fill(_vaultUrl));
		}
		Object value = bundle.get(FIELD_VALUE);
		if (!(value instanceof String) || ((String) value).isEmpty()) {
			throw new I18NRuntimeException(
				I18NConstants.ERROR_SECRET_EMPTY__NAME_VAULT.fill(config.getSecretName(), _vaultUrl));
		}
		return (String) value;
	}

	private HttpResponse<String> send() {
		Map<String, String> headers = new LinkedHashMap<>();
		_credentials.authenticate(headers::put);
		try {
			return _client.get(_url, headers);
		} catch (IOException ex) {
			throw new I18NRuntimeException(I18NConstants.ERROR_VAULT_UNREACHABLE__URL.fill(_vaultUrl), ex);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new I18NRuntimeException(I18NConstants.ERROR_VAULT_UNREACHABLE__URL.fill(_vaultUrl), ex);
		}
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
