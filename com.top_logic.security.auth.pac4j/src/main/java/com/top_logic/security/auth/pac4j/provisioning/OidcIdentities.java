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
import java.util.Objects;

import com.top_logic.base.accesscontrol.IdentityVerificationHandler;
import com.top_logic.base.accesscontrol.VerificationResult;
import com.top_logic.base.accesscontrol.VerifiedIdentity;
import com.top_logic.base.security.device.interfaces.AuthenticationDevice;
import com.top_logic.basic.Logger;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.security.auth.pac4j.model.OidcIdentity;
import com.top_logic.security.auth.pac4j.model.TlSecurityPac4jFactory;
import com.top_logic.security.auth.pac4j.model.impl.OidcIdentityBase;
import com.top_logic.util.error.TopLogicException;

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

	/**
	 * Connects the identity of the given client and subject with the given account.
	 *
	 * <p>
	 * An identity is never moved between accounts: one that is part of another account is refused,
	 * and so is a second identity of a client the account already has an identity of. An identity
	 * that is already part of the account is no error. No group memberships are granted here; the
	 * identity's groups are synchronized on its next login.
	 * </p>
	 *
	 * <p>
	 * Must be called in a transaction.
	 * </p>
	 *
	 * @return The identity, as part of the given account.
	 * @throws TopLogicException
	 *         If the identity cannot be connected, with the reason for the user.
	 */
	public static OidcIdentity connect(Person account, String client, String subject) throws TopLogicException {
		Objects.requireNonNull(account, "No account.");
		OidcIdentity existing = findIdentity(account.getKnowledgeBase(), client, subject);
		if (existing != null) {
			if (account.equals(getOwner(existing))) {
				return existing;
			}
			throw new TopLogicException(I18NConstants.ERROR_IDENTITY_OF_OTHER_ACCOUNT);
		}
		if (getIdentity(account, client) != null) {
			throw new TopLogicException(I18NConstants.ERROR_OTHER_IDENTITY__NAME.fill(account.getName()));
		}
		return addIdentity(account, client, subject);
	}

	/**
	 * The {@link IdentityVerificationHandler} that connects the identity an external authentication
	 * confirms with the account of the session that asked for it.
	 *
	 * <p>
	 * The handler takes the {@link VerifiedIdentity#getClientName() client} and
	 * {@link VerifiedIdentity#getSubject() subject} of the confirmed identity and never resolves its
	 * {@link VerifiedIdentity#getAccount() account}: the authentication is no login, so it neither
	 * looks up nor creates an account through the account mapping of the client. The identity is
	 * {@link #connect(Person, String, String) connected} in a transaction of its own.
	 * </p>
	 *
	 * @param onConnected
	 *        Run after the identity has been connected and committed, e.g. to show the connected
	 *        identity in the window that asked.
	 */
	public static IdentityVerificationHandler connectHandler(Runnable onConnected) {
		Objects.requireNonNull(onConnected, "No continuation.");
		return (expected, identity) -> {
			if (!identity.hasClientIdentity()) {
				return VerificationResult.refused(I18NConstants.ERROR_NO_CLIENT_IDENTITY);
			}
			KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
			try (Transaction tx = kb.beginTransaction(I18NConstants.CONNECT_IDENTITY)) {
				connect(expected, identity.getClientName(), identity.getSubject());
				tx.commit();
			} catch (TopLogicException ex) {
				Logger.info("Not connecting " + identity + " with account '" + expected.getName() + "': "
					+ ex.getMessage(), OidcIdentities.class);
				return VerificationResult.refused(ex.getErrorKey());
			}
			onConnected.run();
			return VerificationResult.verified(I18NConstants.IDENTITY_CONNECTED);
		};
	}

	/**
	 * Removes the given identity from the given account.
	 *
	 * <p>
	 * Refused when the account would have no way left to sign in, see
	 * {@link #canRemove(Person, OidcIdentity)}. The group memberships that only this identity has
	 * granted are taken back with it.
	 * </p>
	 *
	 * <p>
	 * Must be called in a transaction.
	 * </p>
	 *
	 * @throws TopLogicException
	 *         If the identity cannot be removed, with the reason for the user.
	 */
	public static void removeIdentity(Person account, OidcIdentity identity) throws TopLogicException {
		if (!account.equals(getOwner(identity))) {
			throw new TopLogicException(I18NConstants.ERROR_NOT_CONNECTED);
		}
		if (!canRemove(account, identity)) {
			throw new TopLogicException(I18NConstants.ERROR_LAST_LOGIN);
		}
		GroupSynchronization.revokeAll(account, identity);
		identity.tDelete();
	}

	/**
	 * Whether the given account can still sign in after the given identity has been removed from
	 * it.
	 *
	 * <p>
	 * That is the case if the account has another identity, or {@link #hasPasswordLogin(Person) a
	 * password}.
	 * </p>
	 */
	public static boolean canRemove(Person account, OidcIdentity identity) {
		for (OidcIdentity other : getIdentities(account)) {
			if (!other.equals(identity)) {
				return true;
			}
		}
		return hasPasswordLogin(account);
	}

	/**
	 * Whether the given account can sign in with a password.
	 *
	 * @see #hasPasswordLogin(AuthenticationDevice, Person)
	 */
	public static boolean hasPasswordLogin(Person account) {
		return hasPasswordLogin(account.getAuthenticationDevice(), account);
	}

	/**
	 * Whether the given account can sign in with a password at the given device.
	 *
	 * <p>
	 * An account without a device has no password. A device that stores the passwords itself must
	 * {@link AuthenticationDevice#hasPassword(Person) hold one for the account}; one that checks the
	 * password elsewhere (e.g. in an LDAP directory) and therefore does not
	 * {@link AuthenticationDevice#allowPwdChange() let the password be changed} counts as a
	 * password, since this application cannot tell whether the directory knows the account.
	 * </p>
	 *
	 * @param device
	 *        The authentication device of the account, <code>null</code> for none.
	 */
	public static boolean hasPasswordLogin(AuthenticationDevice device, Person account) {
		if (device == null) {
			return false;
		}
		return device.hasPassword(account) || !device.allowPwdChange();
	}

}
