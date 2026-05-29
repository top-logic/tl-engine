/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.accesscontrol.login;

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

}
