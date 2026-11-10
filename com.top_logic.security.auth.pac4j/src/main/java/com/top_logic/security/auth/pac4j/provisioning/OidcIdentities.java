/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.auth.pac4j.provisioning;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.security.auth.pac4j.model.OidcIdentity;
import com.top_logic.security.auth.pac4j.model.TlSecurityPac4jFactory;
import com.top_logic.security.auth.pac4j.model.impl.OidcIdentityBase;

/**
 * Access to the {@link OidcIdentity identities} of the accounts at external logins.
 */
public class OidcIdentities {

	/**
	 * Name of the composite reference of <code>tl.accounts:Person</code> holding the
	 * {@link OidcIdentity identities} of an account.
	 */
	public static final String OIDC_IDENTITIES_ATTR = "oidcIdentities";

	private static final String[] CLIENT_SUBJECT =
		{ OidcIdentity.CLIENT_ATTR, OidcIdentity.SUBJECT_ATTR };

	private OidcIdentities() {
		// Static utility.
	}

	/**
	 * The {@link OidcIdentity identities} of the given account.
	 */
	public static List<OidcIdentity> getIdentities(Person account) {
		@SuppressWarnings("unchecked")
		Collection<OidcIdentity> identities = (Collection<OidcIdentity>) account.tValueByName(OIDC_IDENTITIES_ATTR);
		return identities == null ? Collections.emptyList() : List.copyOf(identities);
	}

	/**
	 * The {@link OidcIdentity identity} of the given account at the given client.
	 *
	 * @return The identity, or <code>null</code> if the account has no identity at the client.
	 */
	public static OidcIdentity getIdentity(Person account, String client) {
		for (OidcIdentity identity : getIdentities(account)) {
			if (client.equals(identity.getClient())) {
				return identity;
			}
		}
		return null;
	}

	/**
	 * The account an {@link OidcIdentity} belongs to.
	 *
	 * @return The account, or <code>null</code> for an identity that is not part of an account.
	 */
	public static Person getOwner(OidcIdentity identity) {
		TLObject container = identity.tContainer();
		return container instanceof Person ? (Person) container : null;
	}

	/**
	 * The stored {@link OidcIdentity} with the given client and subject.
	 *
	 * <p>
	 * One query on the indexed columns {@link OidcIdentity#CLIENT_ATTR} and
	 * {@link OidcIdentity#SUBJECT_ATTR}.
	 * </p>
	 *
	 * @return The identity, or <code>null</code> if none has this client and subject.
	 */
	public static OidcIdentity findIdentity(KnowledgeBase kb, String client, String subject) {
		Iterator<KnowledgeItem> hits =
			kb.getObjectsByAttribute(OidcIdentityBase.TABLE_NAME, CLIENT_SUBJECT, new Object[] { client, subject });
		return hits.hasNext() ? hits.next().getWrapper() : null;
	}

	/**
	 * Creates an {@link OidcIdentity} of the given client and subject as part of the given
	 * account.
	 *
	 * <p>
	 * Must be called in a transaction.
	 * </p>
	 */
	public static OidcIdentity addIdentity(Person account, String client, String subject) {
		OidcIdentity identity = TlSecurityPac4jFactory.getInstance().createOidcIdentity();
		identity.setClient(client);
		identity.setSubject(subject);
		account.tAddByName(OIDC_IDENTITIES_ATTR, identity);
		return identity;
	}

}
