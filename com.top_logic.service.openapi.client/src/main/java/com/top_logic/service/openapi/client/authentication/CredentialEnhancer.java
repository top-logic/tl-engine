/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.authentication;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;

import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.service.openapi.client.registry.impl.call.uri.UriBuilder;

/**
 * {@link SecurityEnhancer} adding the credentials of a {@link CredentialProvider} to each request.
 *
 * <p>
 * The credentials are retrieved for each request, so that a rotated secret or a renewed token is
 * used without restarting the application. When the server rejects a request with HTTP status 401,
 * the provider is {@link CredentialProvider#invalidate() invalidated}, so that the next request
 * fetches fresh credentials.
 * </p>
 */
public class CredentialEnhancer implements SecurityEnhancer {

	private final CredentialProvider _credentials;

	/**
	 * Creates a {@link CredentialEnhancer}.
	 *
	 * @param credentials
	 *        The provider of the credentials to add to each request.
	 */
	public CredentialEnhancer(CredentialProvider credentials) {
		_credentials = credentials;
	}

	@Override
	public void enhanceUrl(UriBuilder urlBuilder) {
		// Credentials are sent in headers.
	}

	@Override
	public void enhanceRequest(HttpClient client, ClassicHttpRequest request) {
		_credentials.authenticate(request::setHeader);
	}

	@Override
	public void handleResponse(HttpResponse response) {
		if (response.getCode() == HttpStatus.SC_UNAUTHORIZED) {
			_credentials.invalidate();
		}
	}

}
