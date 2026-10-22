/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.credential.oauth;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.credential.RequestAuthentication;

/**
 * A form-encoded POST request to an endpoint of an OAuth 2.0 authorization server, e.g. a token
 * request.
 *
 * <p>
 * The request collects the form parameters and headers. A {@link ClientAuthentication} adds the
 * client credentials to it, {@link OAuthServer#post(OAuthRequest)} sends it.
 * </p>
 */
public class OAuthRequest implements RequestAuthentication {

	private final String _endpoint;

	private final String _audience;

	private final Map<String, String> _parameters = new LinkedHashMap<>();

	private final Map<String, String> _headers = new LinkedHashMap<>();

	/**
	 * Creates a {@link OAuthRequest}.
	 *
	 * @param endpoint
	 *        See {@link #getEndpoint()}.
	 * @param audience
	 *        See {@link #getAudience()}.
	 */
	public OAuthRequest(String endpoint, String audience) {
		_endpoint = endpoint;
		_audience = audience;
	}

	/**
	 * URL of the endpoint the request is sent to.
	 */
	public String getEndpoint() {
		return _endpoint;
	}

	/**
	 * Identifier of the authorization server as audience of a client assertion, the URL of its
	 * token endpoint (RFC 7523 §3).
	 */
	public String getAudience() {
		return _audience;
	}

	/**
	 * Sets a form parameter of the request body, replacing a value set before.
	 *
	 * @param name
	 *        The parameter name.
	 * @param value
	 *        The parameter value.
	 */
	public void setParameter(String name, String value) {
		_parameters.put(name, value);
	}

	/**
	 * The form parameters of the request body.
	 */
	public Map<String, String> getParameters() {
		return Collections.unmodifiableMap(_parameters);
	}

	@Override
	public void setHeader(String name, String value) {
		_headers.put(name, value);
	}

	/**
	 * The headers of the request.
	 */
	public Map<String, String> getHeaders() {
		return Collections.unmodifiableMap(_headers);
	}

	/**
	 * The request body in the format {@link OAuthConstants#FORM_URLENCODED}.
	 */
	public String getBody() {
		StringBuilder result = new StringBuilder();
		for (Entry<String, String> parameter : _parameters.entrySet()) {
			if (result.length() > 0) {
				result.append('&');
			}
			result.append(formEncode(parameter.getKey()));
			result.append('=');
			result.append(formEncode(parameter.getValue()));
		}
		return result.toString();
	}

	/**
	 * Encodes the given value in the format {@link OAuthConstants#FORM_URLENCODED}.
	 */
	public static String formEncode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}

}
