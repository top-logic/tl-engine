/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.access;

import java.time.Instant;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.base.accesscontrol.AuthorizationUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.service.openapi.server.authentication.AuthenticationFailure;
import com.top_logic.service.openapi.server.authentication.Authenticator;
import com.top_logic.tl.ai.core.access.model.AccessToken;

/**
 * Resolves {@code Authorization: Bearer <token>} to the person who created the token.
 *
 * <p>
 * Runs in the system interaction the endpoint opens for resolving a credential,
 * so the lookup sees every token and the last-used stamp can be committed.
 * </p>
 *
 * <p>
 * Every refusal is a {@code 401} with a {@code WWW-Authenticate: Bearer} challenge, and none of
 * them - neither the answer nor the log line - contains the presented token. A bearer value that
 * does not have the token {@link AccessTokens#hasFormat(String) format} is refused without a
 * database lookup, which is also what lets an OAuth bearer authentication configured next to this
 * one take a request this one does not recognise.
 * </p>
 */
public class AccessTokenAuthenticator implements Authenticator {

	private static final String BEARER = "Bearer ";

	@Override
	public Person authenticate(HttpServletRequest req, HttpServletResponse resp) throws AuthenticationFailure {
		String header = AuthorizationUtil.getAuthorizationHeader(req);
		if (header == null || !header.regionMatches(true, 0, BEARER, 0, BEARER.length())) {
			throw failure(I18NConstants.ERROR_NO_BEARER_TOKEN, null);
		}
		String presented = header.substring(BEARER.length()).strip();
		if (!AccessTokens.hasFormat(presented)) {
			throw failure(I18NConstants.ERROR_NOT_AN_ACCESS_TOKEN, "invalid_token");
		}

		AccessToken token = AccessTokens.find(presented);
		if (token == null) {
			throw failure(I18NConstants.ERROR_ACCESS_TOKEN_UNKNOWN, "invalid_token");
		}
		Instant now = Instant.now();
		if (AccessTokens.isExpired(token, now)) {
			throw failure(I18NConstants.ERROR_ACCESS_TOKEN_EXPIRED, "invalid_token");
		}

		try {
			AccessTokens.touch(token, now);
		} catch (RuntimeException ex) {
			// A last-used stamp that could not be written - two requests racing on one token, say -
			// is no reason to refuse a valid credential.
			Logger.warn("Could not record the use of personal access token '" + token.getName() + "' of '"
				+ token.getOwner().getName() + "'.", ex, AccessTokenAuthenticator.class);
		}
		return token.getOwner();
	}

	/**
	 * A refusal with a bearer challenge.
	 *
	 * @param error
	 *        The RFC 6750 error code, or <code>null</code> for a request that carried no token.
	 */
	private static AuthenticationFailure failure(ResKey message, String error) {
		AuthenticationFailure result = new AuthenticationFailure(message);
		result.setResponseEnhancer((response, failure, path) -> {
			Logger.info("Refused a personal access token for '" + path + "': " + failure.getMessage(),
				AccessTokenAuthenticator.class);
			AuthorizationUtil.setBearerAuthenticationRequestHeader(response, error, null);
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, failure.getMessage());
		});
		return result;
	}

}
