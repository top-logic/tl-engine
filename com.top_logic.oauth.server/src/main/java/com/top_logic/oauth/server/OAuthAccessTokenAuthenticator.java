/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.oauth.server;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.nimbusds.oauth2.sdk.ErrorObject;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.oauth2.sdk.token.BearerTokenError;

import com.top_logic.base.accesscontrol.AuthorizationUtil;
import com.top_logic.base.discovery.DiscoveryContext;
import com.top_logic.basic.Logger;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.version.Version;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.oauth.server.model.OAuthGrant;
import com.top_logic.service.openapi.server.authentication.AuthenticationFailure;
import com.top_logic.service.openapi.server.authentication.Authenticator;

/**
 * Resolves <code>Authorization: Bearer &lt;access token&gt;</code> to the person whose grant the
 * access token belongs to, if the token was issued for this endpoint's resource.
 *
 * <p>
 * Must run in an interaction that may read every grant, e.g. the system interaction an endpoint
 * opens for resolving a credential.
 * </p>
 *
 * <p>
 * Every refusal is a <code>401</code> with a <code>WWW-Authenticate: Bearer</code> challenge naming
 * the protected resource metadata of the endpoint (RFC 9728), see
 * {@link #challenge(HttpServletRequest, BearerTokenError)}; none of them, neither the answer nor
 * the log line, contains the presented token. A bearer value that does not have the format of an
 * access token of this server is refused without a database lookup, so that another bearer scheme
 * configured beside this one can take the request.
 * </p>
 */
public class OAuthAccessTokenAuthenticator implements Authenticator {

	/**
	 * The parameter of the <code>WWW-Authenticate</code> challenge naming the URL of the protected
	 * resource metadata (RFC 9728, section 5.1).
	 */
	public static final String RESOURCE_METADATA_PARAMETER = "resource_metadata";

	private final String _resource;

	/**
	 * Creates a {@link OAuthAccessTokenAuthenticator}.
	 *
	 * @param resource
	 *        See {@link OAuthAccessTokenAuthentication.Config#getResource()}.
	 */
	public OAuthAccessTokenAuthenticator(String resource) {
		_resource = resource;
	}

	@Override
	public Person authenticate(HttpServletRequest req, HttpServletResponse resp) throws AuthenticationFailure {
		String header = AuthorizationUtil.getAuthorizationHeader(req);
		if (header == null) {
			throw failure(req, I18NConstants.ERROR_NO_BEARER_TOKEN, BearerTokenError.MISSING_TOKEN);
		}
		BearerAccessToken token;
		try {
			token = BearerAccessToken.parse(header);
		} catch (ParseException ex) {
			throw failure(req, I18NConstants.ERROR_NO_BEARER_TOKEN, BearerTokenError.MISSING_TOKEN);
		}

		OAuthAuthorizationServer server = OAuthAuthorizationServer.getInstance();
		if (server == null) {
			throw failure(req, I18NConstants.ERROR_AUTHORIZATION_SERVER_INACTIVE, BearerTokenError.INVALID_TOKEN);
		}
		try {
			OAuthGrant grant = server.authenticate(token.getValue(), resource(req));
			return grant.getOwner();
		} catch (OAuthException ex) {
			ErrorObject error = ex.getError();
			throw failure(req, ex.getErrorKey(),
				error instanceof BearerTokenError bearerError ? bearerError : BearerTokenError.INVALID_TOKEN);
		}
	}

	/**
	 * The resource URI of the endpoint, as the given request reaches it.
	 */
	public String resource(HttpServletRequest req) {
		return ProtectedResources.resourceUri(_resource, DiscoveryContext.fromRequest(req));
	}

	/**
	 * The value of the header <code>WWW-Authenticate</code> refusing {@code req} with
	 * {@code error}.
	 *
	 * <p>
	 * The challenge names the absolute URL of the protected resource metadata of the endpoint in
	 * the parameter {@link #RESOURCE_METADATA_PARAMETER} (RFC 9728, section 5.1), from which a
	 * client learns the authorization server to sign in with.
	 * </p>
	 */
	protected String challenge(HttpServletRequest req, BearerTokenError error) {
		String challenge = error.setRealm(Version.getApplicationName()).toWWWAuthenticateHeader();
		String metadataUrl = DiscoveryContext.fromRequest(req).url(ProtectedResources.metadataPath(_resource));
		return challenge + ", " + RESOURCE_METADATA_PARAMETER + "=\"" + metadataUrl + '"';
	}

	private AuthenticationFailure failure(HttpServletRequest req, ResKey message, BearerTokenError error) {
		String challenge = challenge(req, error);
		AuthenticationFailure result = new AuthenticationFailure(message);
		result.setResponseEnhancer((response, failure, path) -> {
			Logger.info("Refused an OAuth access token for '" + path + "': " + failure.getMessage(),
				OAuthAccessTokenAuthenticator.class);
			response.setHeader(AuthorizationUtil.AUTHENTICATE_RESPONSE_HEADER_NAME, challenge);
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, failure.getMessage());
		});
		return result;
	}

}
