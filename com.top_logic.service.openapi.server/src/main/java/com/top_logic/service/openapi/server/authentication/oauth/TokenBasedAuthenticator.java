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
import java.util.concurrent.TimeUnit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.knowledge.wrap.person.Person;
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

	private static final NamedConstant NO_ACCOUNT = new NamedConstant("'No account' replacement");

	private static final TokenStorage<String, Object> TOKENS = new TokenStorage<>();

	@Override
	public Person authenticate(HttpServletRequest req, HttpServletResponse resp)
			throws AuthenticationFailure, IOException {
		AccessToken token = parseAccessToken(req);
		String tokenString = token.getValue();

		Object storedAccount = TOKENS.getValidToken(tokenString);
		if (storedAccount != null) {
			// Valid token available
			if (storedAccount == NO_ACCOUNT) {
				return null;
			}
			return (Person) storedAccount;
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
		long maxExpirationTime = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1);
		if (expirationTime == null) {
			expirationTime = new Date(maxExpirationTime);
		} else {
			if (expirationTime.getTime() < System.currentTimeMillis()) {
				throw failExpiredToken(expirationTime);
			}
			
			if (expirationTime.getTime() > maxExpirationTime) {
				expirationTime = new Date(maxExpirationTime);
			}
			
		}

		Person account;
		String accountName = findAccountName(successResponse, req, resp);
		if (accountName == null) {
			account = null;
		} else {
			account = accountByName(accountName);
			if (account == null) {
				throw new AuthenticationFailure(I18NConstants.NO_SUCH_ACCOUNT__USER.fill(accountName));
			}
		}
		// Cache token for later reuse
		TOKENS.storeToken(tokenString, account != null ? account : NO_ACCOUNT, expirationTime);

		return account;
	}

	private Person accountByName(String accountName) {
		return ThreadContextManager.inSystemInteraction(TokenBasedAuthenticator.class,
			() -> Person.byName(accountName));
	}

	/**
	 * Finds the name of the {@link Person} for which the request was authenticated.
	 * 
	 * <p>
	 * May be <code>null</code> when the request is not executed in user context.
	 * </p>
	 *
	 * @param introspectionResponse
	 *        Response of the OIDC server to the access token introspection request.
	 * @param req
	 *        Request that requires authentication.
	 * @param resp
	 *        Response corresponding to the given request.
	 * @throws AuthenticationFailure
	 *         when authentication is not possible.
	 */
	protected abstract String findAccountName(TokenIntrospectionSuccessResponse introspectionResponse,
			HttpServletRequest req, HttpServletResponse resp) throws AuthenticationFailure, IOException;

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

