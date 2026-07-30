/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.agent;

import com.top_logic.basic.config.PolymorphicConfiguration;

/**
 * Recognizes the credential a non-browser caller presents to the {@link AgentServlet}.
 *
 * <p>
 * The React layer defines what an admitted caller looks like ({@link AgentCredential}) but not how
 * credentials are stored or issued. An implementation registered with {@link AgentAccess} supplies
 * that — the account access tokens of the view layer, for instance.
 * </p>
 */
public interface AgentAuthenticator {

	/**
	 * Configuration of an {@link AgentAuthenticator}.
	 */
	interface Config extends PolymorphicConfiguration<AgentAuthenticator> {
		// Marker interface.
	}

	/**
	 * Resolves the given credential.
	 *
	 * @param credential
	 *        The credential the caller presented, never empty.
	 * @return The admitted caller, or <code>null</code> if this authenticator does not recognize
	 *         the credential — an expired, revoked or forged one included.
	 */
	AgentCredential authenticate(String credential);

}
