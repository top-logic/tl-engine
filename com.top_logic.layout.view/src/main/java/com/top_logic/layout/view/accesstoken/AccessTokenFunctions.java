/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.accesstoken;

import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import com.top_logic.basic.AliasManager;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.element.accesstoken.AccessToken;
import com.top_logic.element.accesstoken.AccessTokenPermissions;
import com.top_logic.element.accesstoken.AccessTokenPermissions.PermissionConfig;
import com.top_logic.element.accesstoken.AccessTokens;
import com.top_logic.element.accesstoken.SessionBinding;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.SideEffectFree;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;

/**
 * TL-Script functions behind the views that create and revoke {@link AccessToken access tokens}.
 *
 * <p>
 * They act for the account that runs the script and check it here rather than in the view: a
 * command's input is whatever a channel holds, and these functions are reachable from the script
 * console and a scheduler task as well. Every account may create tokens with every permission the
 * application offers: it hands on its own rights only.
 * </p>
 *
 * <p>
 * A permission that {@link PermissionConfig#getSessionBinding() binds to the session} acts only in
 * the session the token is created in: {@link #create(String, Integer, Collection)} registers that
 * session through the {@link PermissionConfig#getSessionBinding() session binding} the module
 * offering the permission contributes, and {@link #invitation(String)} names the endpoint to send
 * such a token to.
 * </p>
 */
@ScriptPrefix("accessToken")
public class AccessTokenFunctions extends TLScriptFunctions {

	/**
	 * The permissions the application offers for an access token, to choose from when creating
	 * one.
	 *
	 * <p>
	 * Example: <code>accessTokenPermissions().isEmpty()</code> - whether tokens are of any use in
	 * this application.
	 * </p>
	 *
	 * @return The permissions, shown by their labels. A permission is passed to
	 *         <code>accessTokenCreate()</code> as it is, or by its name.
	 */
	@Label("Offered access token permissions")
	@SideEffectFree
	public static List<PermissionConfig> permissions() {
		return new ArrayList<>(AccessTokenPermissions.getInstance().getPermissions());
	}

	/**
	 * Creates an access token for the account running the script, and returns the token.
	 *
	 * <p>
	 * The returned token is the only copy: what is stored is its hash, so show it to its owner once
	 * and do not keep it anywhere.
	 * </p>
	 *
	 * <p>
	 * A token with a permission that binds to the session acts in the session the script runs in
	 * only, so such a token can only be created in a session of the user interface.
	 * </p>
	 *
	 * <p>
	 * Example: <code>accessTokenCreate("Claude Code laptop", 90, ["agent-endpoints"])</code>
	 * </p>
	 *
	 * @param name
	 *        What the owner calls the token, typically the client it is meant for.
	 * @param validForDays
	 *        How many days from now the token is accepted. Empty for a token that does not expire.
	 * @param permissions
	 *        The permissions the token grants, by their names or as offered by
	 *        <code>accessTokenPermissions()</code>.
	 * @return The token, starting with <code>tlpat_</code>.
	 */
	@Label("Create access token")
	public static String create(String name, Integer validForDays, @Mandatory Collection<?> permissions) {
		Person owner = TLContext.currentUser();
		if (owner == null) {
			throw new TopLogicException(I18NConstants.ERROR_NO_ACCOUNT);
		}
		List<String> names = names(permissions);
		AccessTokenPermissions offered = AccessTokenPermissions.getInstance();
		if (!offered.isBindingSession(names)) {
			return AccessTokens.create(owner, name, validForDays, names, null).plain();
		}

		HttpSession session = currentSession();
		if (session == null) {
			throw new TopLogicException(I18NConstants.ERROR_NO_SESSION);
		}
		String sessionKey = UUID.randomUUID().toString();
		String plain = AccessTokens.create(owner, name, validForDays, names, sessionKey).plain();
		for (SessionBinding binding : sessionBindings(names)) {
			binding.bind(sessionKey, session);
		}
		return plain;
	}

	/**
	 * The invitation a person hands to the client of a token with a permission that binds to the
	 * session: the address of each endpoint the token is meant for, followed by the token, one line
	 * per endpoint.
	 *
	 * <p>
	 * A client of such a token needs both, so both are copied in one go. The address is the public
	 * one of the application, because the address seen inside a request may be a load balancer's.
	 * </p>
	 *
	 * <p>
	 * Example: <code>accessTokenInvitation(accessTokenCreate("Agent", 1, ["ui-act"]))</code>
	 * </p>
	 *
	 * @param token
	 *        A token just created by <code>accessTokenCreate()</code>.
	 * @return The invitation, or nothing if the token has no permission that binds to the session.
	 */
	@Label("Invitation for an access token")
	@SideEffectFree
	public static String invitation(@Mandatory String token) {
		AccessToken stored = AccessTokens.find(token);
		if (stored == null) {
			throw new TopLogicException(I18NConstants.ERROR_NOT_A_TOKEN);
		}
		Set<String> endpoints = new LinkedHashSet<>();
		String applicationURL = null;
		for (SessionBinding binding : sessionBindings(AccessTokens.permissions(stored))) {
			if (applicationURL == null) {
				applicationURL = applicationURL();
			}
			endpoints.add(binding.endpoint(applicationURL));
		}
		if (endpoints.isEmpty()) {
			return null;
		}
		StringBuilder result = new StringBuilder();
		for (String endpoint : endpoints) {
			if (result.length() > 0) {
				result.append('\n');
			}
			result.append(endpoint).append(' ').append(token);
		}
		return result.toString();
	}

	/**
	 * Revokes an access token: the next request presenting it is refused.
	 *
	 * <p>
	 * The owner revokes their own tokens, an administrator any token.
	 * </p>
	 *
	 * @param token
	 *        The <code>tl.accounts:AccessToken</code> to revoke.
	 * @return Always <code>true</code>; a refusal is an error, not a <code>false</code>.
	 */
	@Label("Revoke access token")
	public static boolean revoke(@Mandatory Object token) {
		AccessTokens.revoke(revocable(token));
		return true;
	}

	private static List<String> names(Collection<?> permissions) {
		List<String> result = new ArrayList<>();
		for (Object permission : permissions) {
			if (permission instanceof PermissionConfig config) {
				result.add(config.getName());
			} else if (permission != null) {
				result.add(permission.toString());
			}
		}
		return result;
	}

	private static Set<SessionBinding> sessionBindings(Collection<String> names) {
		AccessTokenPermissions offered = AccessTokenPermissions.getInstance();
		Set<SessionBinding> result = new LinkedHashSet<>();
		for (String name : names) {
			SessionBinding binding = offered.getSessionBinding(name);
			if (binding != null) {
				result.add(binding);
			}
		}
		return result;
	}

	private static HttpSession currentSession() {
		if (!DefaultDisplayContext.hasDisplayContext()) {
			return null;
		}
		HttpServletRequest request = DefaultDisplayContext.getDisplayContext().asRequest();
		return request == null ? null : request.getSession(false);
	}

	private static String applicationURL() {
		DisplayContext context = DefaultDisplayContext.getDisplayContext();
		StringBuilder result = new StringBuilder();
		try {
			LayoutUtils.appendHostURL(context, result);
		} catch (IOException ex) {
			throw new IOError(ex);
		}
		result.append(AliasManager.getInstance().getAlias(AliasManager.APP_CONTEXT));
		return result.toString();
	}

	private static AccessToken revocable(Object token) {
		AccessToken resolved = token instanceof AccessToken found ? found : null;
		if (resolved == null && token instanceof Collection<?> many && many.size() == 1) {
			resolved = many.iterator().next() instanceof AccessToken found ? found : null;
		}
		if (resolved == null) {
			throw new TopLogicException(I18NConstants.ERROR_NOT_A_TOKEN);
		}
		Person actor = TLContext.currentUser();
		if (actor == null || (!actor.equals(resolved.getOwner()) && !actor.isAdmin())) {
			throw new TopLogicException(I18NConstants.ERROR_TOKEN_NOT_OWNED);
		}
		return resolved;
	}

}
