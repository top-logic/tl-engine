/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.accesscontrol.loginmethod;

import com.top_logic.base.accesscontrol.ExternalAuthenticationServlet;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.ThemeImage;

/**
 * An external login method that augments the built-in account login (e.g. "Login with Google").
 *
 * <p>
 * A login method is presented in a login UI as an additional action besides the built-in
 * username/password form. Activating it is a full browser redirect to {@link #getInitiationUrl(String)
 * an initiation URL}, which typically hands off to an external identity provider (OAuth/OIDC/SAML)
 * and, after authentication, returns the browser to the application.
 * </p>
 *
 * <p>
 * Login methods are contributed by modules through a {@link LoginMethodProvider} and consumed by any
 * login UI. The contract is intentionally UI-agnostic so that both the legacy login dialog and the
 * React-based view layer can render the same methods.
 * </p>
 *
 * @see LoginMethodProvider
 */
public interface LoginMethod {

	/**
	 * Stable identifier of this login method (e.g. the underlying client name {@code "pac4j-google"}).
	 *
	 * <p>
	 * Used to distinguish methods on the client and for diagnostics; not displayed to the user.
	 * </p>
	 */
	String getId();

	/**
	 * The user-visible label of the action (e.g. "Login with Google").
	 */
	ResKey getLabel();

	/**
	 * An optional icon for the action, or {@code null} for no icon.
	 */
	ThemeImage getIcon();

	/**
	 * Builds the absolute (context-relative) URL the browser must navigate to in order to start this
	 * login method.
	 *
	 * @param returnToUrl
	 *        The application URL the user should be returned to after a successful login (e.g.
	 *        {@code /<context>/view/<windowName>/}). Implementations encode it into the initiation
	 *        URL so the external authentication round-trip lands back on it.
	 * @return The URL to redirect the browser to.
	 */
	String getInitiationUrl(String returnToUrl);

	/**
	 * Builds the URL a browser is sent to so that the identity provider authenticates the user of
	 * the running session afresh and returns to the application carrying the given token.
	 *
	 * <p>
	 * Where a login method establishes a session, this is how it proves, later on, that the person
	 * at the keyboard still is the account holder: the browser walks through the provider's
	 * authentication once more and comes back to the application's authentication servlet, which
	 * recognises the token and completes the identity verification the token stands for. The
	 * request that returns stays in the same session - it confirms an identity, it does not
	 * establish one.
	 * </p>
	 *
	 * @param token
	 *        The token of the awaited verification, to be carried back in the request parameter
	 *        {@link ExternalAuthenticationServlet#VERIFICATION_PARAM}.
	 * @return The URL to open, or <code>null</code> when this method cannot authenticate the user
	 *         of an established session again. A login method that only knows how to start a fresh
	 *         login answers <code>null</code>.
	 */
	default String getReauthenticationUrl(String token) {
		return null;
	}

}
