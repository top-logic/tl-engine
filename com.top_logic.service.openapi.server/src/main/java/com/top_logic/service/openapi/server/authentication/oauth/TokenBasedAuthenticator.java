/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.authentication.oauth;

import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nimbusds.oauth2.sdk.ErrorObject;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.Request;
import com.nimbusds.oauth2.sdk.TokenIntrospectionErrorResponse;
import com.nimbusds.oauth2.sdk.TokenIntrospectionRequest;
import com.nimbusds.oauth2.sdk.TokenIntrospectionResponse;
import com.nimbusds.oauth2.sdk.TokenIntrospectionSuccessResponse;
import com.nimbusds.oauth2.sdk.auth.ClientSecretPost;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.oauth2.sdk.util.URLUtils;

import com.top_logic.base.accesscontrol.AuthorizationUtil;
import com.top_logic.basic.Logger;
import com.top_logic.service.openapi.common.authentication.oauth.TokenStorage;
import com.top_logic.service.openapi.server.authentication.AuthenticationFailure;
import com.top_logic.service.openapi.server.authentication.Authenticator;
import com.top_logic.service.openapi.server.authentication.I18NConstants;

/**
 * {@link Authenticator} using a token and checks its validity by delegating to a validation
 * endpoint.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class TokenBasedAuthenticator implements Authenticator {

	private static final TokenStorage<String, String> TOKENS = new TokenStorage<>();

	@Override
	public void authenticate(HttpServletRequest req, HttpServletResponse resp)
			throws AuthenticationFailure, IOException {
		AccessToken token = parseAccessToken(req);
		String tokenString = token.getValue();

		String validToken = TOKENS.getValidToken(tokenString);
		if (validToken != null) {
			// Valid token available
			return;
		}
		HTTPResponse response = sendInspectRequest(token);
		TokenIntrospectionResponse tokenResponse = parseInspectResponse(response);
		if (!tokenResponse.indicatesSuccess()) {
			throw handleIntrospectionError((TokenIntrospectionErrorResponse) tokenResponse);
		}
		TokenIntrospectionSuccessResponse successResponse = (TokenIntrospectionSuccessResponse) tokenResponse;
		if (!successResponse.isActive()) {
			throw failInactiveToken();
		}
		Date expirationTime = successResponse.getExpirationTime();
		if (expirationTime == null || expirationTime.before(new Date())) {
			throw failExpiredToken(expirationTime);
		}
		// Cache token for later reuse
		TOKENS.storeToken(tokenString, tokenString, expirationTime);
	}

	private AuthenticationFailure failExpiredToken(Date expirationTime) {
		AuthenticationFailure ex =
			new AuthenticationFailure(I18NConstants.AUTH_FAILED_TOKEN_EXPIRED__DATE.fill(expirationTime));
		ex.setResponseEnhancer((response, failure, path) -> {
			AuthorizationUtil.setBearerAuthenticationRequestHeader(response, "invalid_token",
				"The access token expired.");
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		});
		return ex;
	}

	private AuthenticationFailure failInactiveToken() throws AuthenticationFailure {
		AuthenticationFailure ex =
			new AuthenticationFailure(I18NConstants.AUTH_FAILED_TOKEN_INACTIVE);
		ex.setResponseEnhancer((response, failure, path) -> {
			AuthorizationUtil.setBearerAuthenticationRequestHeader(response, "invalid_token",
				"The access token is inactive.");
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		});
		throw ex;
	}

	private TokenIntrospectionResponse parseInspectResponse(HTTPResponse response) throws AuthenticationFailure {
		try {
			return TokenIntrospectionResponse.parse(response);
		} catch (ParseException ex) {
			Logger.error("Unable to parse response", ex, ClientCredentialsAuthenticator.class);
			throw new AuthenticationFailure(
				I18NConstants.AUTH_FAILED_ILLEGAL_TOKEN_VALIDATION_RESPONSE__MSG.fill(ex.getMessage()));
		}
	}

	private HTTPResponse sendInspectRequest(AccessToken token) throws IOException {
		Request request = new TokenIntrospectionRequest(getIntrospectionURI(), getClientAuth(), token);
		return request.toHTTPRequest().send();
	}

	private AuthenticationFailure handleIntrospectionError(TokenIntrospectionErrorResponse errorResponse)
			throws AuthenticationFailure {
		ErrorObject errorObject = errorResponse.getErrorObject();
		int httpStatus = errorObject.getHTTPStatusCode();
		String code = String.valueOf(errorObject.getCode());
		String description = String.valueOf(errorObject.getDescription());
		throw new AuthenticationFailure(I18NConstants.AUTH_FAILED_TOKEN_DECLINED__STATUS_CODE__CODE__DESCRIPTION
			.fill(httpStatus, code, description));
	}

	private BearerAccessToken parseAccessToken(HttpServletRequest req) throws AuthenticationFailure {
		try {
			String authHeader = AuthorizationUtil.getAuthorizationHeader(req);
			if (authHeader != null) {
				return BearerAccessToken.parse(authHeader);
			}
			Map<String, List<String>> params = URLUtils.parseParameters(req.getQueryString());
			return BearerAccessToken.parse(params);
		} catch (ParseException ex) {
			throw failNoToken();
		}
	}

	private AuthenticationFailure failNoToken() {
		AuthenticationFailure ex = new AuthenticationFailure(I18NConstants.AUTH_FAILED_NO_TOKEN);
		ex.setResponseEnhancer((response, failure, path) -> {
			AuthorizationUtil.setBearerAuthenticationRequestHeader(response, null, null);
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No authentication for '" + path + "'.");
		});
		return ex;
	}

	/**
	 * The {@link URI} of the token introspection endpoint.
	 */
	protected abstract URI getIntrospectionURI();

	/**
	 * The client authentication for the introspection endpoint.
	 * 
	 * @return May be <code>null</code> when no authentication is necessary.
	 */
	protected abstract ClientSecretPost getClientAuth();

}

