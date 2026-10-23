/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.credential.azure;

import java.time.Duration;

import com.azure.core.http.HttpClient;
import com.azure.core.util.HttpClientOptions;

/**
 * Factory for the {@link HttpClient}s of the Azure SDK clients of this module.
 *
 * <p>
 * The client comes from the {@link com.azure.core.http.HttpClientProvider} on the class path,
 * which is the JDK HTTP client of <code>azure-core-http-jdk-httpclient</code> unless the
 * application adds another one; the Azure configuration property
 * <code>AZURE_HTTP_CLIENT_IMPLEMENTATION</code> selects one of several.
 * </p>
 */
public final class AzureHttpClients {

	private AzureHttpClients() {
		// Static utility.
	}

	/**
	 * Creates the {@link HttpClient} for the given connection options.
	 *
	 * @param config
	 *        The connection options.
	 * @return A new client.
	 */
	public static HttpClient create(AzureRestConfig config) {
		HttpClientOptions options = new HttpClientOptions()
			.setConnectTimeout(Duration.ofMillis(config.getConnectTimeout()))
			.setResponseTimeout(Duration.ofMillis(config.getRequestTimeout()))
			.setReadTimeout(Duration.ofMillis(config.getRequestTimeout()));
		return HttpClient.createDefault(options);
	}

}
