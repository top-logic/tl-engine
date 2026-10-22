/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.credential.oauth;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.util.ResKey;

/**
 * Connection to an OAuth 2.0 authorization server.
 *
 * <p>
 * Locates the endpoints of the server, either from the configured token URL or from the discovery
 * document of the issuer (OIDC Discovery, RFC 8414), and sends form-encoded requests
 * to them.
 * </p>
 *
 * <p>
 * The discovery document is fetched once on first use and kept. A failed fetch is not kept, the
 * next use tries again.
 * </p>
 *
 * <p>
 * No exception thrown by this class contains a request parameter or the content of a response,
 * since these may carry secrets or tokens. Error responses are reported with their HTTP status and
 * their error code and description.
 * </p>
 */
@Label("OAuth server")
public class OAuthServer {

	private final String _issuer;

	private final String _tokenUrl;

	private final Duration _requestTimeout;

	private final HttpClient _client;

	private final Object _discoveryLock = new Object();

	private volatile Map<String, Object> _metadata;

	/**
	 * Creates a {@link OAuthServer}.
	 *
	 * @param context
	 *        The context reporting configuration errors.
	 * @param config
	 *        The configuration of the server connection.
	 */
	public OAuthServer(InstantiationContext context, OAuthServerConfig config) {
		_issuer = StringServices.nonEmpty(config.getIssuer());
		_tokenUrl = StringServices.nonEmpty(config.getTokenUrl());
		if (_issuer == null && _tokenUrl == null) {
			reportError(context, I18NConstants.ERROR_NO_TOKEN_ENDPOINT);
		} else if (_issuer != null && _tokenUrl != null) {
			reportError(context, I18NConstants.ERROR_AMBIGUOUS_TOKEN_ENDPOINT);
		}
		_requestTimeout = Duration.ofMillis(config.getRequestTimeout());
		_client = HttpClient.newBuilder()
			.connectTimeout(Duration.ofMillis(config.getConnectTimeout()))
			.followRedirects(HttpClient.Redirect.NORMAL)
			.build();
	}

	private static void reportError(InstantiationContext context, ResKey message) {
		I18NRuntimeException error = new I18NRuntimeException(message);
		context.error(error.getMessage(), error);
	}

	/**
	 * The URL of the token endpoint.
	 *
	 * @throws I18NRuntimeException
	 *         If the endpoint is taken from the discovery document, which cannot be fetched or
	 *         does not announce it.
	 */
	public String getTokenEndpoint() {
		if (_tokenUrl != null) {
			return _tokenUrl;
		}
		if (_issuer == null) {
			throw new I18NRuntimeException(I18NConstants.ERROR_NO_TOKEN_ENDPOINT);
		}
		return getMetadataEndpoint(OAuthConstants.TOKEN_ENDPOINT);
	}

	/**
	 * The URL of an endpoint announced in the discovery document.
	 *
	 * @param field
	 *        The name of the field of the discovery document holding the URL, e.g.
	 *        {@link OAuthConstants#TOKEN_ENDPOINT}.
	 * @return The endpoint URL.
	 *
	 * @throws I18NRuntimeException
	 *         If the discovery document cannot be fetched or does not announce the endpoint.
	 */
	public String getMetadataEndpoint(String field) {
		Object value = getMetadata().get(field);
		if (!(value instanceof String) || ((String) value).isEmpty()) {
			throw new I18NRuntimeException(I18NConstants.ERROR_ENDPOINT_NOT_ANNOUNCED__ISSUER_FIELD.fill(_issuer, field));
		}
		return (String) value;
	}

	/**
	 * The discovery document of the authorization server.
	 *
	 * @return The fields of the discovery document, e.g. {@link OAuthConstants#TOKEN_ENDPOINT}.
	 *
	 * @throws I18NRuntimeException
	 *         If no issuer is configured or the discovery document cannot be fetched.
	 */
	public Map<String, Object> getMetadata() {
		Map<String, Object> metadata = _metadata;
		if (metadata != null) {
			return metadata;
		}
		synchronized (_discoveryLock) {
			metadata = _metadata;
			if (metadata == null) {
				metadata = Collections.unmodifiableMap(fetchMetadata());
				_metadata = metadata;
			}
			return metadata;
		}
	}

	private Map<String, Object> fetchMetadata() {
		if (_issuer == null) {
			throw new I18NRuntimeException(I18NConstants.ERROR_NO_ISSUER);
		}
		String url = stripTrailingSlash(_issuer) + OAuthConstants.WELL_KNOWN_OPENID_CONFIGURATION;
		HttpRequest request = newRequest(url)
			.timeout(_requestTimeout)
			.header(OAuthConstants.ACCEPT, OAuthConstants.APPLICATION_JSON)
			.GET()
			.build();
		HttpResponse<String> response = send(url, request);
		if (!isSuccess(response)) {
			throw new I18NRuntimeException(
				I18NConstants.ERROR_DISCOVERY_FAILED__URL_STATUS.fill(url, response.statusCode()));
		}
		Map<String, Object> metadata = parseObject(url, response.body());
		Object issuer = metadata.get(OAuthConstants.ISSUER);
		if (!(issuer instanceof String) || !stripTrailingSlash((String) issuer).equals(stripTrailingSlash(_issuer))) {
			throw new I18NRuntimeException(
				I18NConstants.ERROR_ISSUER_MISMATCH__EXPECTED_ACTUAL.fill(_issuer, String.valueOf(issuer)));
		}
		return metadata;
	}

	/**
	 * Sends a token request and parses the token response.
	 *
	 * @param request
	 *        The token request, including its client authentication.
	 * @return The issued token.
	 *
	 * @throws OAuthErrorException
	 *         If the authorization server answered with an error.
	 * @throws I18NRuntimeException
	 *         If the server cannot be reached or its response is malformed.
	 */
	public AccessToken requestToken(OAuthRequest request) {
		long issuedAt = System.currentTimeMillis();
		Map<String, Object> response = post(request);
		return parseToken(request.getEndpoint(), response, issuedAt);
	}

	/**
	 * Sends the given request and returns the JSON object of a successful response.
	 *
	 * @param request
	 *        The request to send.
	 * @return The fields of the JSON object in the response.
	 *
	 * @throws OAuthErrorException
	 *         If the server answered with a status other than 2xx.
	 * @throws I18NRuntimeException
	 *         If the server cannot be reached or its response is malformed.
	 */
	public Map<String, Object> post(OAuthRequest request) {
		String url = request.getEndpoint();
		HttpRequest.Builder builder = newRequest(url)
			.timeout(_requestTimeout)
			.header(OAuthConstants.CONTENT_TYPE, OAuthConstants.FORM_URLENCODED)
			.header(OAuthConstants.ACCEPT, OAuthConstants.APPLICATION_JSON)
			.POST(HttpRequest.BodyPublishers.ofString(request.getBody(), StandardCharsets.UTF_8));
		for (Entry<String, String> header : request.getHeaders().entrySet()) {
			builder.setHeader(header.getKey(), header.getValue());
		}
		HttpResponse<String> response = send(url, builder.build());
		if (!isSuccess(response)) {
			throw errorResponse(url, response);
		}
		return parseObject(url, response.body());
	}

	private static OAuthErrorException errorResponse(String url, HttpResponse<String> response) {
		int status = response.statusCode();
		Map<String, Object> error = parseErrorObject(response.body());
		Object code = error.get(OAuthConstants.ERROR);
		if (!(code instanceof String)) {
			return new OAuthErrorException(I18NConstants.ERROR_REQUEST_FAILED__URL_STATUS.fill(url, status),
				status, null);
		}
		Object description = error.get(OAuthConstants.ERROR_DESCRIPTION);
		ResKey message = I18NConstants.ERROR_REQUEST_REJECTED__URL_STATUS_ERROR_DESCRIPTION.fill(url, status, code,
			description instanceof String ? description : "");
		return new OAuthErrorException(message, status, (String) code);
	}

	private static Map<String, Object> parseErrorObject(String body) {
		try {
			Object value = JSON.fromString(body);
			if (value instanceof Map) {
				@SuppressWarnings("unchecked")
				Map<String, Object> result = (Map<String, Object>) value;
				return result;
			}
		} catch (JSON.ParseException | RuntimeException ex) {
			// Not a JSON error object, e.g. an HTML page of a proxy.
		}
		return Collections.emptyMap();
	}

	private static AccessToken parseToken(String url, Map<String, Object> response, long issuedAt) {
		Object value = response.get(OAuthConstants.ACCESS_TOKEN);
		if (!(value instanceof String) || ((String) value).isEmpty()) {
			throw new I18NRuntimeException(I18NConstants.ERROR_MALFORMED_RESPONSE__URL.fill(url));
		}
		Object type = response.get(OAuthConstants.TOKEN_TYPE);
		Object refreshToken = response.get(OAuthConstants.REFRESH_TOKEN);
		return new AccessToken(
			(String) value,
			type instanceof String && !((String) type).isEmpty() ? (String) type : CredentialProvider.BEARER,
			parseSeconds(url, response.get(OAuthConstants.EXPIRES_IN)),
			refreshToken instanceof String ? (String) refreshToken : null,
			issuedAt);
	}

	/**
	 * Parses a number of seconds in a response, e.g. {@link OAuthConstants#EXPIRES_IN}.
	 *
	 * @param url
	 *        The URL the response came from, for the error message.
	 * @param value
	 *        The value of the field in the response.
	 * @return The number, or <code>null</code> if the field is missing.
	 *
	 * @throws I18NRuntimeException
	 *         If the value is not a number.
	 */
	static Long parseSeconds(String url, Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Number) {
			return Long.valueOf(((Number) value).longValue());
		}
		if (value instanceof String) {
			// Some servers send the lifetime as string.
			try {
				return Long.valueOf(Long.parseLong((String) value));
			} catch (NumberFormatException ex) {
				// Reported below.
			}
		}
		throw new I18NRuntimeException(I18NConstants.ERROR_MALFORMED_RESPONSE__URL.fill(url));
	}

	private HttpResponse<String> send(String url, HttpRequest request) {
		try {
			return _client.send(request, BodyHandlers.ofString(StandardCharsets.UTF_8));
		} catch (IOException ex) {
			throw new I18NRuntimeException(I18NConstants.ERROR_SERVER_UNREACHABLE__URL.fill(url), ex);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new I18NRuntimeException(I18NConstants.ERROR_SERVER_UNREACHABLE__URL.fill(url), ex);
		}
	}

	private static boolean isSuccess(HttpResponse<?> response) {
		int status = response.statusCode();
		return status >= 200 && status < 300;
	}

	/**
	 * Parses a JSON object.
	 *
	 * <p>
	 * A parse error is reported without its cause, since the message of the cause quotes the input,
	 * which may contain a token.
	 * </p>
	 */
	private static Map<String, Object> parseObject(String url, String body) {
		Object value;
		try {
			value = JSON.fromString(body);
		} catch (JSON.ParseException | RuntimeException ex) {
			throw new I18NRuntimeException(I18NConstants.ERROR_MALFORMED_RESPONSE__URL.fill(url));
		}
		if (!(value instanceof Map)) {
			throw new I18NRuntimeException(I18NConstants.ERROR_MALFORMED_RESPONSE__URL.fill(url));
		}
		@SuppressWarnings("unchecked")
		Map<String, Object> result = (Map<String, Object>) value;
		return result;
	}

	private static HttpRequest.Builder newRequest(String url) {
		try {
			return HttpRequest.newBuilder(URI.create(url));
		} catch (IllegalArgumentException ex) {
			throw new I18NRuntimeException(I18NConstants.ERROR_INVALID_URL__URL.fill(url), ex);
		}
	}

	private static String stripTrailingSlash(String url) {
		return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
	}

}
