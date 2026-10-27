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
import com.top_logic.basic.util.ResKey;

/**
 * Reads secrets from one Azure Key Vault and keeps them for the configured cache time.
 *
 * <p>
 * The secrets are read with the <code>SecretClient</code> of the Azure Key Vault library. Its
 * requests are authenticated by the configured {@link CredentialProvider} (see
 * {@link CredentialProviderPolicy}), by default the managed identity of the machine or app for the
 * resource {@link #KEY_VAULT_RESOURCE}. A secret is kept for the cache time and read again
 * afterwards, so that a secret rotated in the key vault takes effect without restarting the
 * application. The absence of a secret is kept like a secret, a failed read is not kept.
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
	 *        absence is kept for the cache time like a secret, for optional and non-optional
	 *        requests alike: a non-optional request reports a cached absence as error.
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
		Snapshot snapshot = entry.get(() -> fetch(name, version));
		if (snapshot.isAbsent()) {
			if (optional) {
				return null;
			}
			throw new I18NRuntimeException(snapshot._notFound);
		}
		return snapshot._value;
	}

	/**
	 * Reads the secret from the key vault.
	 *
	 * @return The secret, or the absence of the secret with the error reporting it.
	 */
	private Snapshot fetch(String name, String version) {
		KeyVaultSecret secret;
		try {
			secret = _client.getSecret(name, version);
		} catch (HttpResponseException ex) {
			int status = ex.getResponse().getStatusCode();
			if (status == STATUS_NOT_FOUND) {
				return Snapshot.absent(I18NConstants.ERROR_SECRET_REQUEST_FAILED__NAME_VAULT_STATUS_DETAIL
					.fill(name, _vaultUrl, status, errorText(ex)));
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
		return Snapshot.secret(value);
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

		Snapshot get(Supplier<Snapshot> fetch) {
			Snapshot snapshot = _snapshot;
			if (snapshot != null && !snapshot.isExpired()) {
				return snapshot;
			}
			synchronized (this) {
				snapshot = _snapshot;
				if (snapshot != null && !snapshot.isExpired()) {
					// Read by another thread in the meantime.
					return snapshot;
				}
				snapshot = fetch.get().expiringAt(System.currentTimeMillis() + _cacheTime);
				_snapshot = snapshot;
				return snapshot;
			}
		}

	}

	/**
	 * A secret (or its absence) read from the key vault with its end of validity.
	 */
	private static final class Snapshot {

		/**
		 * The secret, <code>null</code> if it does not exist.
		 */
		final String _value;

		/**
		 * The error reporting the absence of the secret, <code>null</code> if it exists.
		 */
		final ResKey _notFound;

		private final long _expires;

		private Snapshot(String value, ResKey notFound, long expires) {
			_value = value;
			_notFound = notFound;
			_expires = expires;
		}

		/**
		 * A snapshot of an existing secret.
		 */
		static Snapshot secret(String value) {
			return new Snapshot(value, null, 0);
		}

		/**
		 * A snapshot of a secret that does not exist, reported by the given error when it is
		 * required.
		 */
		static Snapshot absent(ResKey notFound) {
			return new Snapshot(null, notFound, 0);
		}

		/**
		 * This snapshot, valid until the given time.
		 */
		Snapshot expiringAt(long expires) {
			return new Snapshot(_value, _notFound, expires);
		}

		boolean isAbsent() {
			return _value == null;
		}

		boolean isExpired() {
			return System.currentTimeMillis() >= _expires;
		}

	}

}
