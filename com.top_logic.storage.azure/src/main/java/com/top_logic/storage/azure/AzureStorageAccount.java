/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.storage.azure;

import java.net.URI;
import java.net.URISyntaxException;

import com.azure.core.http.HttpClient;
import com.azure.core.http.jdk.httpclient.JdkHttpClientBuilder;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;

/**
 * Access to an Azure storage account configured by an {@link AzureStorageAccountConfig}.
 *
 * <p>
 * The account is accessed through the HTTP client of the JDK ({@link java.net.http.HttpClient}),
 * so that the Netty based default HTTP client of the Azure SDK is not required.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class AzureStorageAccount {

	/**
	 * Pattern of the endpoint of an account in the Azure cloud; the account name is inserted at the
	 * placeholder.
	 */
	public static final String DEFAULT_ENDPOINT_PATTERN = "https://%s.blob.core.windows.net";

	private final BlobServiceClient _client;

	private final StorageSharedKeyCredential _credential;

	private AzureStorageAccount(BlobServiceClient client, StorageSharedKeyCredential credential) {
		_client = client;
		_credential = credential;
	}

	/**
	 * The client of the blob service of the account.
	 */
	public BlobServiceClient getClient() {
		return _client;
	}

	/**
	 * The account key credential, or <code>null</code> if the account is accessed with a shared
	 * access signature.
	 *
	 * <p>
	 * Signing URLs (e.g. for direct downloads) requires the account key.
	 * </p>
	 */
	public StorageSharedKeyCredential getCredential() {
		return _credential;
	}

	/**
	 * The URL of the blob service of the account, e.g.
	 * <code>https://myaccount.blob.core.windows.net</code>.
	 */
	public String getAccountUrl() {
		return _client.getAccountUrl();
	}

	/**
	 * Creates the access to the account given by the configuration.
	 *
	 * <p>
	 * No request is sent to the storage.
	 * </p>
	 *
	 * @param context
	 *        The context for reporting configuration errors.
	 * @param config
	 *        The account settings.
	 * @param owner
	 *        Description of the configured component for error messages.
	 * @return The access to the account, or <code>null</code> if the configuration is invalid; the
	 *         problem is reported to the context.
	 */
	public static AzureStorageAccount create(InstantiationContext context, AzureStorageAccountConfig config,
			String owner) {
		String connectionString = config.getConnectionString();
		String endpoint = config.getEndpoint();
		String accountName = config.getAccountName();
		String accountKey = config.getAccountKey();

		BlobServiceClientBuilder builder = new BlobServiceClientBuilder().httpClient(createHttpClient());
		StorageSharedKeyCredential credential;
		if (!StringServices.isEmpty(connectionString)) {
			if (!StringServices.isEmpty(endpoint) || !StringServices.isEmpty(accountName)
				|| !StringServices.isEmpty(accountKey)) {
				context.error(owner + ": Either a connection string, or account name and account key are given, not"
					+ " both.");
				return null;
			}
			builder.connectionString(connectionString);
			credential = sharedKey(connectionString);
		} else {
			if (StringServices.isEmpty(accountName) || StringServices.isEmpty(accountKey)) {
				context.error(owner + ": A connection string, or account name and account key must be given.");
				return null;
			}
			String url = StringServices.isEmpty(endpoint) ? String.format(DEFAULT_ENDPOINT_PATTERN, accountName)
				: endpoint;
			if (parseUrl(context, owner, AzureStorageAccountConfig.ENDPOINT, url) == null) {
				return null;
			}
			credential = new StorageSharedKeyCredential(accountName, accountKey);
			builder.endpoint(url).credential(credential);
		}

		try {
			return new AzureStorageAccount(builder.buildClient(), credential);
		} catch (RuntimeException ex) {
			context.error(owner + ": Invalid account settings: " + ex.getMessage(), ex);
			return null;
		}
	}

	/**
	 * Creates the HTTP client for accessing the storage.
	 */
	private static HttpClient createHttpClient() {
		return new JdkHttpClientBuilder().build();
	}

	/**
	 * The account key credential contained in the given connection string, or <code>null</code> if
	 * the connection string contains no account key (e.g. a shared access signature).
	 */
	private static StorageSharedKeyCredential sharedKey(String connectionString) {
		try {
			return StorageSharedKeyCredential.fromConnectionString(connectionString);
		} catch (IllegalArgumentException ex) {
			return null;
		}
	}

	/**
	 * Parses an absolute URL from configuration.
	 *
	 * @param context
	 *        The context for reporting an invalid URL.
	 * @param owner
	 *        Description of the configured component for error messages.
	 * @param property
	 *        The name of the configuration property holding the URL.
	 * @param url
	 *        The configured value.
	 * @return The URL, or <code>null</code> if the value is empty or invalid; an invalid value is
	 *         reported to the context.
	 */
	public static URI parseUrl(InstantiationContext context, String owner, String property, String url) {
		if (StringServices.isEmpty(url)) {
			return null;
		}
		try {
			URI result = new URI(url);
			if (result.getScheme() == null || result.getHost() == null) {
				context.error(owner + ": Invalid value of '" + property + "', expected an absolute URL: '" + url + "'.");
				return null;
			}
			return result;
		} catch (URISyntaxException ex) {
			context.error(owner + ": Invalid value of '" + property + "' '" + url + "': " + ex.getMessage(), ex);
			return null;
		}
	}

}
