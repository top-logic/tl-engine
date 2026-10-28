/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.credential.oauth;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import net.minidev.json.JSONObject;

import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.nimbusds.oauth2.sdk.ErrorObject;
import com.nimbusds.oauth2.sdk.GeneralException;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.as.AuthorizationServerMetadata;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.oauth2.sdk.token.AccessTokenType;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.util.ResKey;

/**
 * Connection to an OAuth 2.0 authorization server.
 *
 * <p>
 * Locates the endpoints of the server, either from the configured token URL or from the discovery
 * document of the issuer (OIDC Discovery, RFC 8414), and sends the requests of the Nimbus OAuth 2.0
 * SDK to them with the configured timeouts.
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

	/**
	 * Field of the discovery document with the URL of the token endpoint.
	 */
	public static final String TOKEN_ENDPOINT = "token_endpoint";

	/**
	 * Field of the discovery document with the URL of the device authorization endpoint (RFC 8628
	 * §4).
	 */
	public static final String DEVICE_AUTHORIZATION_ENDPOINT = "device_authorization_endpoint";

	/**
	 * Field of a token response with the type of the token.
	 */
	private static final String TOKEN_TYPE = "token_type";

	/**
	 * Field of a token response with the refresh token.
	 */
	private static final String REFRESH_TOKEN = "refresh_token";

	private final String _issuer;

	private final URI _tokenUrl;

	private final int _connectTimeout;

	private final int _readTimeout;

	private final Object _discoveryLock = new Object();

	private volatile AuthorizationServerMetadata _metadata;

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
		String tokenUrl = StringServices.nonEmpty(config.getTokenUrl());
		if (_issuer == null && tokenUrl == null) {
			reportError(context, I18NConstants.ERROR_NO_TOKEN_ENDPOINT);
		} else if (_issuer != null && tokenUrl != null) {
			reportError(context, I18NConstants.ERROR_AMBIGUOUS_TOKEN_ENDPOINT);
		}
		URI tokenUri = null;
		if (tokenUrl != null) {
			try {
				tokenUri = toURI(tokenUrl);
			} catch (I18NRuntimeException ex) {
				context.error(ex.getMessage(), ex);
			}
		}
		_tokenUrl = tokenUri;
		_connectTimeout = toTimeout(config.getConnectTimeout());
		_readTimeout = toTimeout(config.getRequestTimeout());
	}

	private static int toTimeout(long millis) {
		return (int) Math.min(Integer.MAX_VALUE, Math.max(0, millis));
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
	public URI getTokenEndpoint() {
		if (_tokenUrl != null) {
			return _tokenUrl;
		}
		if (_issuer == null) {
			throw new I18NRuntimeException(I18NConstants.ERROR_NO_TOKEN_ENDPOINT);
		}
		return announced(getMetadata().getTokenEndpointURI(), TOKEN_ENDPOINT);
	}

	/**
	 * The URL of the device authorization endpoint announced in the discovery document.
	 *
	 * @throws I18NRuntimeException
	 *         If the discovery document cannot be fetched or does not announce the endpoint.
	 */
	public URI getDeviceAuthorizationEndpoint() {
		return announced(getMetadata().getDeviceAuthorizationEndpointURI(), DEVICE_AUTHORIZATION_ENDPOINT);
	}

	private URI announced(URI endpoint, String field) {
		if (endpoint == null) {
			throw new I18NRuntimeException(I18NConstants.ERROR_ENDPOINT_NOT_ANNOUNCED__ISSUER_FIELD.fill(_issuer, field));
		}
		return endpoint;
	}

	/**
	 * The discovery document of the authorization server.
	 *
	 * @throws I18NRuntimeException
	 *         If no issuer is configured or the discovery document cannot be fetched.
	 */
	public AuthorizationServerMetadata getMetadata() {
		AuthorizationServerMetadata metadata = _metadata;
		if (metadata != null) {
			return metadata;
		}
		synchronized (_discoveryLock) {
			metadata = _metadata;
			if (metadata == null) {
				metadata = fetchMetadata();
				_metadata = metadata;
			}
			return metadata;
		}
	}

	private AuthorizationServerMetadata fetchMetadata() {
		if (_issuer == null) {
			throw new I18NRuntimeException(I18NConstants.ERROR_NO_ISSUER);
		}
		URL url;
		try {
			url = OIDCProviderMetadata.resolveURL(new Issuer(stripTrailingSlash(_issuer)));
		} catch (GeneralException | IllegalArgumentException ex) {
			throw new I18NRuntimeException(I18NConstants.ERROR_INVALID_URL__URL.fill(_issuer), ex);
		}
		HTTPRequest request = new HTTPRequest(HTTPRequest.Method.GET, url);
		HTTPResponse response = send(request);
		if (!response.indicatesSuccess()) {
			throw new I18NRuntimeException(
				I18NConstants.ERROR_DISCOVERY_FAILED__URL_STATUS.fill(url.toString(), response.getStatusCode()));
		}
		AuthorizationServerMetadata metadata;
		try {
			metadata = AuthorizationServerMetadata.parse(response.getBodyAsJSONObject());
		} catch (ParseException ex) {
			throw new I18NRuntimeException(I18NConstants.ERROR_MALFORMED_RESPONSE__URL.fill(url.toString()));
		}
		String issuer = metadata.getIssuer().getValue();
		if (!stripTrailingSlash(issuer).equals(stripTrailingSlash(_issuer))) {
			throw new I18NRuntimeException(I18NConstants.ERROR_ISSUER_MISMATCH__EXPECTED_ACTUAL.fill(_issuer, issuer));
		}
		return metadata;
	}

	/**
	 * Sends the given request with the configured timeouts.
	 *
	 * @param request
	 *        The request, e.g. created by a Nimbus request object.
	 * @return The response, also if it is an error response.
	 *
	 * @throws I18NRuntimeException
	 *         If the server cannot be reached.
	 */
	public HTTPResponse send(HTTPRequest request) {
		request.setConnectTimeout(_connectTimeout);
		request.setReadTimeout(_readTimeout);
		try {
			return request.send();
		} catch (IOException ex) {
			throw new I18NRuntimeException(I18NConstants.ERROR_SERVER_UNREACHABLE__URL.fill(urlOf(request)), ex);
		}
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
	public IssuedToken requestToken(HTTPRequest request) {
		long issuedAt = System.currentTimeMillis();
		JSONObject response = post(request);
		return parseToken(urlOf(request), response, issuedAt);
	}

	/**
	 * Sends the given request and returns the JSON object of a successful response.
	 *
	 * @param request
	 *        The request to send.
	 * @return The JSON object in the response.
	 *
	 * @throws OAuthErrorException
	 *         If the server answered with a status other than 2xx.
	 * @throws I18NRuntimeException
	 *         If the server cannot be reached or its response is malformed.
	 */
	public JSONObject post(HTTPRequest request) {
		String url = urlOf(request);
		HTTPResponse response = send(request);
		if (!response.indicatesSuccess()) {
			throw errorResponse(url, response);
		}
		try {
			return response.getBodyAsJSONObject();
		} catch (ParseException ex) {
			// The cause is not reported, its message could quote the response.
			throw malformed(url);
		}
	}

	private static OAuthErrorException errorResponse(String url, HTTPResponse response) {
		ErrorObject error = ErrorObject.parse(response);
		int status = response.getStatusCode();
		String code = error.getCode();
		if (code == null) {
			return new OAuthErrorException(I18NConstants.ERROR_REQUEST_FAILED__URL_STATUS.fill(url, status), error);
		}
		String description = error.getDescription();
		ResKey message = I18NConstants.ERROR_REQUEST_REJECTED__URL_STATUS_ERROR_DESCRIPTION.fill(url, status, code,
			description != null ? description : "");
		return new OAuthErrorException(message, error);
	}

	/**
	 * Parses a token response (RFC 6749 §5.1) with {@link AccessTokenResponse}.
	 *
	 * <p>
	 * Also usable for token responses of endpoints that are no OAuth 2.0 authorization servers but
	 * answer in the same format, e.g. the managed identity endpoint of a cloud platform. A missing
	 * token type is taken as {@link CredentialProvider#BEARER}, an empty refresh token as none.
	 * </p>
	 *
	 * @param url
	 *        The URL the response came from, for the error message.
	 * @param response
	 *        The JSON object in the response.
	 * @param issuedAt
	 *        Time in milliseconds since the epoch when the request was sent, see
	 *        {@link IssuedToken#getIssuedAt()}.
	 * @return The issued token.
	 *
	 * @throws I18NRuntimeException
	 *         If the response contains no access token or a malformed field.
	 */
	public static IssuedToken parseToken(String url, JSONObject response, long issuedAt) {
		JSONObject json = new JSONObject(response);
		Object type = json.get(TOKEN_TYPE);
		if (!(type instanceof String) || ((String) type).isEmpty()) {
			json.put(TOKEN_TYPE, AccessTokenType.BEARER.getValue());
		}
		if ("".equals(json.get(REFRESH_TOKEN))) {
			json.remove(REFRESH_TOKEN);
		}
		try {
			return IssuedToken.fromTokens(AccessTokenResponse.parse(json).getTokens(), issuedAt);
		} catch (ParseException ex) {
			// The cause is not reported, its message could quote the response.
			throw malformed(url);
		}
	}

	private static I18NRuntimeException malformed(String url) {
		return new I18NRuntimeException(I18NConstants.ERROR_MALFORMED_RESPONSE__URL.fill(url));
	}

	private static String urlOf(HTTPRequest request) {
		return String.valueOf(request.getURL());
	}

	/**
	 * Parses the URL of an endpoint.
	 *
	 * @throws I18NRuntimeException
	 *         If the URL is invalid.
	 */
	static URI toURI(String url) {
		try {
			URI result = new URI(url);
			if (!result.isAbsolute()) {
				throw new URISyntaxException(url, "Not absolute.");
			}
			return result;
		} catch (URISyntaxException ex) {
			throw new I18NRuntimeException(I18NConstants.ERROR_INVALID_URL__URL.fill(url), ex);
		}
	}

	private static String stripTrailingSlash(String url) {
		return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
	}

}
