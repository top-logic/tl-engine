/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.credential.azure;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;

import reactor.core.publisher.Mono;

import com.top_logic.basic.credential.CredentialProvider;

/**
 * {@link HttpPipelinePolicy} authenticating the requests of an Azure SDK client with a
 * {@link CredentialProvider}.
 *
 * <p>
 * Each request gets the credentials of the provider at the moment it is sent. A request answered
 * with HTTP status 401 is sent once more after {@link CredentialProvider#invalidate()}, so that a
 * token revoked before its end of life is replaced.
 * </p>
 *
 * <p>
 * With this policy, any {@link CredentialProvider} authenticates at an Azure service, not only the
 * Azure identities of the Azure SDK.
 * </p>
 */
public class CredentialProviderPolicy implements HttpPipelinePolicy {

	/**
	 * HTTP status of a request with missing or rejected credentials.
	 */
	public static final int STATUS_UNAUTHORIZED = 401;

	private final CredentialProvider _credentials;

	/**
	 * Creates a {@link CredentialProviderPolicy}.
	 *
	 * @param credentials
	 *        The provider authenticating the requests.
	 */
	public CredentialProviderPolicy(CredentialProvider credentials) {
		_credentials = credentials;
	}

	@Override
	public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
		HttpPipelineNextPolicy retry = next.clone();
		return Mono.fromRunnable(() -> authenticate(context.getHttpRequest()))
			.then(next.process())
			.flatMap(response -> {
				if (response.getStatusCode() != STATUS_UNAUTHORIZED) {
					return Mono.just(response);
				}
				response.close();
				_credentials.invalidate();
				authenticate(context.getHttpRequest());
				return retry.process();
			});
	}

	@Override
	public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
		HttpPipelineNextSyncPolicy retry = next.clone();
		authenticate(context.getHttpRequest());
		HttpResponse response = next.processSync();
		if (response.getStatusCode() != STATUS_UNAUTHORIZED) {
			return response;
		}
		response.close();
		_credentials.invalidate();
		authenticate(context.getHttpRequest());
		return retry.processSync();
	}

	private void authenticate(HttpRequest request) {
		_credentials.authenticate((name, value) -> request.setHeader(HttpHeaderName.fromString(name), value));
	}

}
