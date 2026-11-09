/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.accesscontrol;

import com.top_logic.base.accesscontrol.Login.LoginDeniedException;
import com.top_logic.knowledge.wrap.person.Person;

/**
 * Application code run when an {@link ExternalUserMapping} has resolved the account of an external
 * login.
 *
 * <p>
 * A mapping that supports listeners calls them in the transaction in which it creates or updates
 * the account, after it has brought the account up to date with the identity (e.g. its group
 * memberships). A listener may therefore create objects and link the account to them, e.g. so that
 * role rules give the account its roles right away. An exception of a listener rolls the
 * transaction back and refuses the login; a {@link LoginDeniedException} with a
 * {@link LoginDeniedException#getReason() reason} tells the user why.
 * </p>
 */
public interface ExternalLoginListener {

	/**
	 * Called when a login has created the given account.
	 *
	 * <p>
	 * Called once for an account, before {@link #onLogin(Person, ExternalIdentity)} of the same
	 * login.
	 * </p>
	 *
	 * @param account
	 *        The account created for the identity.
	 * @param identity
	 *        The identity the external authentication system has verified.
	 */
	default void onCreate(Person account, ExternalIdentity identity) {
		// No reaction by default.
	}

	/**
	 * Called at every login of the given account.
	 *
	 * @param account
	 *        The account the identity logs in.
	 * @param identity
	 *        The identity the external authentication system has verified.
	 */
	default void onLogin(Person account, ExternalIdentity identity) {
		// No reaction by default.
	}

}
