/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.oauth.server;

import java.net.URI;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.SideEffectFree;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;
import com.top_logic.oauth.server.model.OAuthGrant;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;

/**
 * TL-Script functions of the consent page of the {@link OAuthAuthorizationServer}, on which a
 * person decides about the authorization request of a client, and of the list of the authorizations
 * a person gave ({@link #revoke(Object)}).
 *
 * <p>
 * Every function of the consent page takes the identifier of the pending authorization, which the
 * authorization endpoint passes to the consent page in the parameter
 * {@link OAuthAuthorizationServer#PENDING_PARAMETER}. The identifier is the proof: the lookup does
 * not depend on access rights.
 * </p>
 *
 * @implNote Every public static method here is a TL-Script function named by the
 *           {@link ScriptPrefix} of this class followed by the capitalized method name, so
 *           {@link #approve(String)} is called <code>oauthApprove</code>.
 */
@ScriptPrefix(OAuthFunctions.PREFIX)
public class OAuthFunctions extends TLScriptFunctions {

	/** Prefix all function names of this class start with. */
	public static final String PREFIX = "oauth";

	/** Key of the {@link #request(String) request description} naming the client. */
	public static final String CLIENT_KEY = "client";

	/** Key of the {@link #request(String) request description} with the client ID. */
	public static final String CLIENT_ID_KEY = "clientId";

	/**
	 * Key of the {@link #request(String) request description} with the host publishing the client
	 * ID metadata document, <code>null</code> for a dynamically registered client.
	 */
	public static final String CLIENT_HOST_KEY = "clientHost";

	/** Key of the {@link #request(String) request description} with the redirect URI. */
	public static final String REDIRECT_URI_KEY = "redirectUri";

	/** Key of the {@link #request(String) request description} with the host of the redirect URI. */
	public static final String REDIRECT_HOST_KEY = "redirectHost";

	/** Key of the {@link #request(String) request description} with the resource (endpoint). */
	public static final String RESOURCE_KEY = "resource";

	/** Key of the {@link #request(String) request description} with the requested scope. */
	public static final String SCOPE_KEY = "scope";

	/**
	 * The reason why the person cannot decide about a pending authorization.
	 *
	 * @param pending
	 *        The identifier of the pending authorization.
	 * @return The message telling that there is no such request - it never existed or has been
	 *         answered - or that it has expired; <code>null</code> if the person can decide.
	 *
	 * @implNote Asks {@link OAuthAuthorizationServer#pendingAuthorization(String)}.
	 */
	@Label("Refusal of an OAuth authorization request")
	@SideEffectFree
	public static ResKey refusal(String pending) {
		try {
			server().pendingAuthorization(pending);
			return null;
		} catch (OAuthException ex) {
			return ex.getErrorKey();
		}
	}

	/**
	 * What a pending authorization asks for, for display on the consent page.
	 *
	 * @param pending
	 *        The identifier of the pending authorization.
	 * @return A dictionary with the name of the client ({@value #CLIENT_KEY}), its client ID
	 *         ({@value #CLIENT_ID_KEY}), the host of its client ID metadata document
	 *         ({@value #CLIENT_HOST_KEY}, empty for a registered client), the redirect URI
	 *         ({@value #REDIRECT_URI_KEY}) and its host ({@value #REDIRECT_HOST_KEY}), the endpoint
	 *         the client wants to call ({@value #RESOURCE_KEY}) and the requested scope
	 *         ({@value #SCOPE_KEY}); <code>null</code> if the person cannot decide about it, see
	 *         {@link #refusal(String)}.
	 */
	@Label("OAuth authorization request")
	@SideEffectFree
	public static Map<String, Object> request(String pending) {
		ValidatedAuthorization authorization;
		try {
			authorization = server().pendingAuthorization(pending);
		} catch (OAuthException ex) {
			return null;
		}
		OAuthClientInfo client = authorization.client();
		Map<String, Object> result = new LinkedHashMap<>();
		result.put(CLIENT_KEY, client.displayName());
		result.put(CLIENT_ID_KEY, client.clientId());
		result.put(CLIENT_HOST_KEY,
			OAuthAuthorizationServer.isMetadataDocumentClientId(client.clientId())
				? URI.create(client.clientId()).getHost()
				: null);
		result.put(REDIRECT_URI_KEY, authorization.redirectUri().toString());
		result.put(REDIRECT_HOST_KEY, authorization.redirectUri().getHost());
		result.put(RESOURCE_KEY, authorization.resource().toString());
		result.put(SCOPE_KEY, authorization.scope() == null ? null : authorization.scope().toString());
		return result;
	}

	/**
	 * The current user agrees to a pending authorization.
	 *
	 * <p>
	 * Issues the authorization code for the client and ends the pending authorization; a second
	 * decision about it is refused.
	 * </p>
	 *
	 * @param pending
	 *        The identifier of the pending authorization.
	 * @return The address to send the browser to: the redirect URI of the client with the code and
	 *         the state of the client.
	 *
	 * @implNote Calls {@link OAuthAuthorizationServer#approve(String, com.top_logic.knowledge.wrap.person.Person)}
	 *           with the current user, who must be logged in.
	 */
	@Label("Allow OAuth authorization request")
	public static String approve(@Mandatory String pending) {
		try {
			return server().approve(pending, TLContext.currentUser()).toString();
		} catch (OAuthException ex) {
			throw new TopLogicException(ex.getErrorKey(), ex);
		}
	}

	/**
	 * The current user refuses a pending authorization.
	 *
	 * @param pending
	 *        The identifier of the pending authorization.
	 * @return The address to send the browser to: the redirect URI of the client with the error
	 *         <code>access_denied</code> and the state of the client.
	 *
	 * @implNote Calls {@link OAuthAuthorizationServer#deny(String)}.
	 */
	@Label("Deny OAuth authorization request")
	public static String deny(@Mandatory String pending) {
		try {
			return server().deny(pending).toString();
		} catch (OAuthException ex) {
			throw new TopLogicException(ex.getErrorKey(), ex);
		}
	}

	/**
	 * Revokes the authorization a person gave a client: its access and refresh tokens are refused
	 * from now on.
	 *
	 * @param grant
	 *        The authorization ({@link OAuthGrant}), or a collection holding exactly one.
	 * @return Always <code>true</code>.
	 * @throws TopLogicException
	 *         If the current user is neither the owner of the authorization nor an administrator.
	 *
	 * @implNote Calls {@link OAuthAuthorizationServer#revoke(OAuthGrant)}.
	 */
	@Label("Revoke OAuth authorization")
	public static boolean revoke(@Mandatory Object grant) {
		server().revoke(revocable(grant));
		return true;
	}

	private static OAuthGrant revocable(Object grant) {
		Object single = grant instanceof Collection<?> many && many.size() == 1 ? many.iterator().next() : grant;
		if (!(single instanceof OAuthGrant resolved)) {
			throw new TopLogicException(I18NConstants.ERROR_NOT_A_GRANT);
		}
		Person actor = TLContext.currentUser();
		if (actor == null || (!actor.equals(resolved.getOwner()) && !actor.isAdmin())) {
			throw new TopLogicException(I18NConstants.ERROR_GRANT_NOT_OWNED);
		}
		return resolved;
	}

	private static OAuthAuthorizationServer server() {
		OAuthAuthorizationServer server = OAuthAuthorizationServer.getInstance();
		if (server == null) {
			throw new TopLogicException(I18NConstants.ERROR_AUTHORIZATION_SERVER_INACTIVE);
		}
		return server;
	}

}
