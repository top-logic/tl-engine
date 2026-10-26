/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.credential.azure;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.util.BinaryData;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.exception.I18NRuntimeException;

/**
 * Reads secrets from one Azure Key Vault and keeps them for the configured cache time.
 *
 * <p>
 * The secrets are read with the <code>SecretClient</code> of the Azure Key Vault library. Its
 * requests are authenticated by the configured {@link CredentialProvider} (see
 * {@link CredentialProviderPolicy}), by default the managed identity of the machine or app for the
 * resource {@link #KEY_VAULT_RESOURCE}. A secret is kept for the cache time and read again
 * afterwards, so that a secret rotated in the key vault takes effect without restarting the
 * application. A failed read is not kept.
 * </p>
 *
 * @see KeyVaultSecretSource
 * @see KeyVaultSecretStore
 */
public final class KeyVaultClient {

	/**
	 * Resource identifier of Azure Key Vault, for which the access token of the default
	 * credentials is requested.
	 */
	public static final String KEY_VAULT_RESOURCE = "https://vault.azure.net";

	/**
	 * HTTP status of a request for a secret that does not exist.
	 */
	public static final int STATUS_NOT_FOUND = 404;

	private final SecretClient _client;

	private final String _vaultUrl;

	private final long _cacheTime;

	private final ConcurrentMap<String, CacheEntry> _cache = new ConcurrentHashMap<>();

	/**
	 * Creates a {@link KeyVaultClient}.
	 *
	 * @param context
	 *        The context for instantiating the credentials.
	 * @param config
	 *        The access to the key vault.
	 */
	public KeyVaultClient(InstantiationContext context, KeyVaultConfig config) {
		PolymorphicConfiguration<? extends CredentialProvider> credentials = config.getCredentials();
		if (credentials == null) {
			credentials = defaultCredentials();
		}
		CredentialProvider provider = context.getInstance(credentials);
		_vaultUrl = stripTrailingSlash(config.getVaultUrl());
		_cacheTime = config.getCacheTime();
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

	/**
	 * The URL of the key vault, without trailing slash.
	 */
	public String getVaultUrl() {
		return _vaultUrl;
	}

	/**
	 * The secret with the given name.
	 *
	 * @param name
	 *        Name of the secret in the key vault.
	 * @param version
	 *        Version of the secret, empty for the current version.
	 * @param optional
	 *        Whether a missing secret is reported as <code>null</code> instead of as error. The
	 *        absence is kept for the cache time like a secret.
	 * @return The secret, never empty; <code>null</code> only if the secret does not exist and
	 *         the request is optional.
	 *
	 * @throws I18NRuntimeException
	 *         If the secret cannot be read. The message names the secret and the vault but never
	 *         contains the secret.
	 */
	public String getSecret(String name, String version, boolean optional) {
		String key = version.isEmpty() ? name : name + '/' + version;
		CacheEntry entry = _cache.computeIfAbsent(key, x -> new CacheEntry());
		return entry.get(() -> fetch(name, version, optional));
	}

	private String fetch(String name, String version, boolean optional) {
		KeyVaultSecret secret;
		try {
			secret = _client.getSecret(name, version);
		} catch (HttpResponseException ex) {
			int status = ex.getResponse().getStatusCode();
			if (optional && status == STATUS_NOT_FOUND) {
				return null;
			}
			// The cause is not reported, its message quotes the response.
			throw new I18NRuntimeException(I18NConstants.ERROR_SECRET_REQUEST_FAILED__NAME_VAULT_STATUS_DETAIL
				.fill(name, _vaultUrl, status, errorText(ex)));
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

	/**
	 * The cached state of one secret.
	 */
	private final class CacheEntry {

		/**
		 * The result read last, <code>null</code> before the first read.
		 */
		private volatile Snapshot _snapshot;

		String get(Supplier<String> fetch) {
			Snapshot snapshot = _snapshot;
			if (snapshot != null && !snapshot.isExpired()) {
				return snapshot._value;
			}
			synchronized (this) {
				snapshot = _snapshot;
				if (snapshot != null && !snapshot.isExpired()) {
					// Read by another thread in the meantime.
					return snapshot._value;
				}
				String value = fetch.get();
				_snapshot = new Snapshot(value, System.currentTimeMillis() + _cacheTime);
				return value;
			}
		}

	}

	/**
	 * A secret (or its absence) read from the key vault with its end of validity.
	 */
	private static final class Snapshot {

		final String _value;

		private final long _expires;

		Snapshot(String value, long expires) {
			_value = value;
			_expires = expires;
		}

		boolean isExpired() {
			return System.currentTimeMillis() >= _expires;
		}

	}

}
