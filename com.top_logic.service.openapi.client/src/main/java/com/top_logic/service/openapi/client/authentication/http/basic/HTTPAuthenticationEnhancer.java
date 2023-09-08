/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.authentication.http.basic;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.core5.http.ClassicHttpRequest;

import com.top_logic.base.accesscontrol.AuthorizationUtil;
import com.top_logic.service.openapi.client.authentication.SecurityEnhancer;
import com.top_logic.service.openapi.client.registry.impl.call.uri.UriBuilder;
import com.top_logic.service.openapi.common.authentication.http.LoginCredentials;

/**
 * {@link SecurityEnhancer} using HTTP authentication.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class HTTPAuthenticationEnhancer implements SecurityEnhancer {

	private final LoginCredentials _credentials;

	/**
	 * Creates a new {@link HTTPAuthenticationEnhancer}.
	 */
	public HTTPAuthenticationEnhancer(LoginCredentials credentials) {
		_credentials = credentials;
	}

	@Override
	public void enhanceUrl(UriBuilder urlBuilder) {
		// No enhancement
	}

	@Override
	public void enhanceRequest(HttpClient client, ClassicHttpRequest request) {
		request.addHeader(AuthorizationUtil.AUTHORIZATION_HEADER_NAME,
			AuthorizationUtil.createBasicAuthAuthorizationHeader(_credentials.getUser(), _credentials.getPassword()));
	}

}

