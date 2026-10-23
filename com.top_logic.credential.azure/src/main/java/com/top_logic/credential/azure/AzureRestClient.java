/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.credential.azure;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.json.JSON;

/**
 * Sends GET requests to an Azure REST endpoint and reads its JSON responses.
 *
 * <p>
 * Nothing this class reports contains the content of a response, since a response may carry a
 * secret or token. Only the error code and description of an error response are extracted, see
 * {@link #errorText(String)}.
 * </p>
 */
final class AzureRestClient {

	/**
	 * Query parameter selecting the version of an Azure REST API.
	 */
	static final String PARAM_API_VERSION = "api-version";

	/**
	 * Field of an Azure error object (<code>{"error": {"code": ..., "message": ...}}</code>) with the
	 * error code.
	 */
	static final String ERROR_CODE = "code";

	/**
	 * Field of an Azure error object with the error message.
	 */
	static final String ERROR_MESSAGE = "message";

	/**
	 * Field of an OAuth 2.0 error response (RFC 6749 §5.2) with the error code.
	 */
	private static final String OAUTH_ERROR = "error";

	/**
	 * Field of an OAuth 2.0 error response with the error description.
	 */
	private static final String OAUTH_ERROR_DESCRIPTION = "error_description";

	private static final String ACCEPT = "Accept";

	private static final String APPLICATION_JSON = "application/json";

	private final HttpClient _client;

	private final Duration _requestTimeout;

	/**
	 * Creates a {@link AzureRestClient}.
	 *
	 * @param config
	 *        The connection options.
	 */
	AzureRestClient(AzureRestConfig config) {
		_requestTimeout = Duration.ofMillis(config.getRequestTimeout());
		_client = HttpClient.newBuilder()
			.connectTimeout(Duration.ofMillis(config.getConnectTimeout()))
			.followRedirects(HttpClient.Redirect.NORMAL)
			.build();
	}

	/**
	 * Sends a GET request accepting a JSON response.
	 *
	 * @param url
	 *        The URL including its query.
	 * @param headers
	 *        Additional request headers.
	 * @return The response.
	 *
	 * @throws I18NRuntimeException
	 *         If the URL is invalid.
	 */
	HttpResponse<String> get(String url, Map<String, String> headers) throws IOException, InterruptedException {
		HttpRequest.Builder builder;
		try {
			builder = HttpRequest.newBuilder(URI.create(url));
		} catch (IllegalArgumentException ex) {
			throw new I18NRuntimeException(I18NConstants.ERROR_INVALID_URL__URL.fill(url), ex);
		}
		builder
			.timeout(_requestTimeout)
			.header(ACCEPT, APPLICATION_JSON)
			.GET();
		for (Entry<String, String> header : headers.entrySet()) {
			builder.setHeader(header.getKey(), header.getValue());
		}
		return _client.send(builder.build(), BodyHandlers.ofString(StandardCharsets.UTF_8));
	}

	/**
	 * Appends query parameters to a URL.
	 *
	 * @param url
	 *        The URL without query.
	 * @param parameters
	 *        The parameters in their order of appearance. Names and values are URL-encoded.
	 * @return The URL with query.
	 */
	static String withQuery(String url, Map<String, String> parameters) {
		StringBuilder result = new StringBuilder(url);
		char separator = '?';
		for (Entry<String, String> parameter : parameters.entrySet()) {
			result.append(separator);
			result.append(URLEncoder.encode(parameter.getKey(), StandardCharsets.UTF_8));
			result.append('=');
			result.append(URLEncoder.encode(parameter.getValue(), StandardCharsets.UTF_8));
			separator = '&';
		}
		return result.toString();
	}

	/**
	 * Whether the response has a success status (2xx).
	 */
	static boolean isSuccess(HttpResponse<?> response) {
		int status = response.statusCode();
		return status >= 200 && status < 300;
	}

	/**
	 * Parses a JSON object.
	 *
	 * @param body
	 *        The response body.
	 * @return The fields of the object, or <code>null</code> if the body is no JSON object. A parse
	 *         error is not passed on, since its message quotes the input.
	 */
	static Map<String, Object> parseObject(String body) {
		Object value;
		try {
			value = JSON.fromString(body);
		} catch (JSON.ParseException | RuntimeException ex) {
			return null;
		}
		if (!(value instanceof Map)) {
			return null;
		}
		@SuppressWarnings("unchecked")
		Map<String, Object> result = (Map<String, Object>) value;
		return result;
	}

	/**
	 * Description of an error response for an error message.
	 *
	 * <p>
	 * Understands the OAuth error format (<code>{"error": "...", "error_description": "..."}</code>)
	 * of the managed identity endpoints and the error object of the Azure resource APIs
	 * (<code>{"error": {"code": "...", "message": "..."}}</code>). Other content of the response is
	 * never part of the result.
	 * </p>
	 *
	 * @param body
	 *        The body of the error response.
	 * @return Error code and description, or the empty string if the response has none.
	 */
	static String errorText(String body) {
		Map<String, Object> response = parseObject(body);
		if (response == null) {
			return "";
		}
		Object error = response.get(OAUTH_ERROR);
		if (error instanceof String) {
			return join(error, response.get(OAUTH_ERROR_DESCRIPTION));
		}
		if (error instanceof Map) {
			Map<?, ?> errorObject = (Map<?, ?>) error;
			return join(errorObject.get(ERROR_CODE), errorObject.get(ERROR_MESSAGE));
		}
		return "";
	}

	private static String join(Object code, Object description) {
		String codeText = code instanceof String ? (String) code : "";
		String descriptionText = description instanceof String ? (String) description : "";
		if (codeText.isEmpty()) {
			return descriptionText;
		}
		if (descriptionText.isEmpty()) {
			return codeText;
		}
		return codeText + ": " + descriptionText;
	}

}
