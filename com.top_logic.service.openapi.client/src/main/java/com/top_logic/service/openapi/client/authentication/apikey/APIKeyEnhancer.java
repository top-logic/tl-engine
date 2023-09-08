/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.authentication.apikey;

import java.util.Objects;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.Header;

import com.top_logic.service.openapi.client.authentication.SecurityEnhancer;
import com.top_logic.service.openapi.client.registry.impl.call.uri.UriBuilder;
import com.top_logic.service.openapi.common.authentication.apikey.APIKeyAuthentication;
import com.top_logic.service.openapi.common.authentication.apikey.APIKeyPosition;

/**
 * {@link SecurityEnhancer} to access an Open API server using API key.
 * 
 * @see APIKeyAuthentication
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class APIKeyEnhancer implements SecurityEnhancer {

	private static final String COOKIE_HEADER_NAME = "Cookie";

	private APIKeyPosition _location;

	private String _parameterName;

	private String _apiKey;

	/**
	 * Creates a new {@link APIKeyEnhancer}.
	 * 
	 * @param location
	 *        Location where the API key must be positioned.
	 * @param parameterName
	 *        Name of the parameter to hold the key.
	 * @param apiKey
	 *        The API key to deliver to Open API server.
	 */
	public APIKeyEnhancer(APIKeyPosition location, String parameterName, String apiKey) {
		_location = location;
		_apiKey = apiKey;
		_parameterName = Objects.requireNonNull(parameterName);
	}

	@Override
	public void enhanceRequest(HttpClient client, ClassicHttpRequest request) {
		switch (_location) {
			case COOKIE:
				Header cookieHeader = request.getFirstHeader(COOKIE_HEADER_NAME);
				String newCookie;
				if (cookieHeader == null) {
					newCookie = _parameterName + "=" + _apiKey;
				} else {
					String oldCookie = cookieHeader.getValue();
					newCookie = oldCookie + "; " + _parameterName + "=" + _apiKey;
					request.removeHeader(cookieHeader);
				}
				request.addHeader(COOKIE_HEADER_NAME, newCookie);
				break;
			case HEADER:
				request.addHeader(_parameterName, _apiKey);
				break;
			case QUERY:
			default:
				break;
		}
	}

	@Override
	public void enhanceUrl(UriBuilder urlBuilder) {
		switch (_location) {
			case QUERY:
				urlBuilder.addParameter(_parameterName, _apiKey);
				break;
			case COOKIE:
			case HEADER:
			default:
				break;
		}
	}

}

