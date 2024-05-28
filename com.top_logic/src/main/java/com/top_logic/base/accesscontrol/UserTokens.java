/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.accesscontrol;

import java.util.Date;

/**
 * Tokens of the currently logged in via OIDC.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface UserTokens {

	/**
	 * The login access token of the current user.
	 */
	String getAccessToken();

	/**
	 * Expiration date of {@link #getAccessToken()}.
	 */
	Date getExpiration();

	/**
	 * Whether the {@link #getAccessToken()} is expired.
	 */
	boolean isExpired();

	/**
	 * The ID token of the user.
	 *
	 * <p>
	 * The ID token contains informations about the currently logged in user, like username, name,
	 * birthday, etc.
	 * </p>
	 */
	String getIdToken();

	/**
	 * Tries to refresh all tokens.
	 * 
	 * <p>
	 * This method must be called when the user tokens are expired to either get new tokens or
	 * update is expiration date.
	 * </p>
	 * 
	 * @return Whether the tokens could be refreshed.
	 */
	boolean refreshTokens();

	/**
	 * The CSRF token.
	 */
	String getCSFRToken();

}
