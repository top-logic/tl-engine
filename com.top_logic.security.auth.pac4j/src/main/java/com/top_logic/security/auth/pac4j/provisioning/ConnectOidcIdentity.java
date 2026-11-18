/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.auth.pac4j.provisioning;

import com.top_logic.base.accesscontrol.IdentityVerificationHandler;
import com.top_logic.base.accesscontrol.VerificationResult;
import com.top_logic.base.accesscontrol.VerifiedIdentity;
import com.top_logic.basic.Logger;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link IdentityVerificationHandler} that connects the identity an external authentication
 * confirms with the account of the session that asked for it.
 *
 * <p>
 * The handler takes the {@link VerifiedIdentity#getClientName() client} and
 * {@link VerifiedIdentity#getSubject() subject} of the confirmed identity and never resolves its
 * {@link VerifiedIdentity#getAccount() account}: the authentication is no login, so it neither
 * looks up nor creates an account through the account mapping of the client. The identity is
 * {@link OidcIdentities#connect(Person, String, String) connected} in a transaction of its own; an
 * identity that cannot be connected is refused with the reason for the user.
 * </p>
 */
public class ConnectOidcIdentity implements IdentityVerificationHandler {

	/**
	 * Singleton {@link ConnectOidcIdentity} instance.
	 */
	public static final ConnectOidcIdentity INSTANCE = new ConnectOidcIdentity();

	private ConnectOidcIdentity() {
		// Singleton constructor.
	}

	@Override
	public VerificationResult handle(Person expected, VerifiedIdentity identity) {
		if (!identity.hasClientIdentity()) {
			return VerificationResult.refused(I18NConstants.ERROR_NO_CLIENT_IDENTITY);
		}
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction(I18NConstants.CONNECT_IDENTITY)) {
			OidcIdentities.connect(expected, identity.getClientName(), identity.getSubject());
			tx.commit();
		} catch (TopLogicException ex) {
			Logger.info("Not connecting " + identity + " with account '" + expected.getName() + "': "
				+ ex.getMessage(), ConnectOidcIdentity.class);
			return VerificationResult.refused(ex.getErrorKey());
		}
		return VerificationResult.verified(I18NConstants.IDENTITY_CONNECTED);
	}

}
