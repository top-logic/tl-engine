/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.accesscontrol;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.top_logic.knowledge.wrap.person.Person;

/**
 * The identity of a user as an external authentication system has verified it.
 *
 * <p>
 * Handed to {@link ExternalUserMapping#findAccountForIdentity(ExternalIdentity)}, which resolves it
 * to the {@link Person} to log in. Besides the {@link #getName() name} that the login configuration
 * extracts, it carries the stable identifier the authentication system knows the user by
 * ({@link #getSubject()}) and everything else the system has said about the user
 * ({@link #getClaims()}), so that a mapping can find an account by something other than its login
 * name.
 * </p>
 *
 * <p>
 * An immutable value: two identities are equal if all their parts are equal.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class ExternalIdentity {

	private final String _clientName;

	private final String _subject;

	private final String _name;

	private final Map<String, Object> _claims;

	/**
	 * Creates an {@link ExternalIdentity}.
	 *
	 * @param clientName
	 *        See {@link #getClientName()}.
	 * @param subject
	 *        See {@link #getSubject()}.
	 * @param name
	 *        See {@link #getName()}.
	 * @param claims
	 *        See {@link #getClaims()}. The map is copied, later changes to it are not seen by the
	 *        created identity. <code>null</code> is the same as an empty map.
	 */
	public ExternalIdentity(String clientName, String subject, String name, Map<String, ?> claims) {
		_clientName = clientName;
		_subject = subject;
		_name = name;
		_claims = claims == null || claims.isEmpty() ? Collections.emptyMap()
			: Collections.unmodifiableMap(new LinkedHashMap<>(claims));
	}

	/**
	 * Name of the configured client (login method) through which the user has authenticated, e.g.
	 * the name of a pac4j client.
	 *
	 * <p>
	 * A mapping serving several clients tells by it which authentication system the
	 * {@link #getSubject() subject} belongs to.
	 * </p>
	 */
	public String getClientName() {
		return _clientName;
	}

	/**
	 * The identifier the authentication system knows the user by, e.g. the claim <code>sub</code>
	 * of OIDC.
	 *
	 * <p>
	 * The subject is unique and stable within one {@link #getClientName() client}, in contrast to
	 * the {@link #getName() name}, which a user may be able to change at the authentication system.
	 * </p>
	 */
	public String getSubject() {
		return _subject;
	}

	/**
	 * The login name the configuration of the client extracts from the authenticated identity.
	 *
	 * <p>
	 * The name that {@link ExternalUserMapping#findAccountForExternalName(String)} receives.
	 * </p>
	 */
	public String getName() {
		return _name;
	}

	/**
	 * All attributes the authentication system has delivered about the user, by name, e.g. the
	 * claims <code>email</code> and <code>email_verified</code> of OIDC.
	 *
	 * <p>
	 * The map cannot be modified. Its values are the ones the authentication system has delivered
	 * (strings, booleans, numbers, lists, ...). Which attributes exist depends on the
	 * authentication system and its configuration (e.g. the requested scopes).
	 * </p>
	 */
	public Map<String, Object> getClaims() {
		return _claims;
	}

	/**
	 * The value of the {@link #getClaims() claim} with the given name, or <code>null</code> if
	 * there is no such claim.
	 */
	public Object getClaim(String claimName) {
		return _claims.get(claimName);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ExternalIdentity)) {
			return false;
		}
		ExternalIdentity other = (ExternalIdentity) obj;
		return Objects.equals(_clientName, other._clientName)
			&& Objects.equals(_subject, other._subject)
			&& Objects.equals(_name, other._name)
			&& _claims.equals(other._claims);
	}

	@Override
	public int hashCode() {
		return Objects.hash(_clientName, _subject, _name, _claims);
	}

	/**
	 * Names the client, subject, name and the names of the claims, but no claim values: those may
	 * be personal data or tokens that must not end up in a log.
	 */
	@Override
	public String toString() {
		return "ExternalIdentity(client=" + _clientName + ", subject=" + _subject + ", name=" + _name
			+ ", claims=" + _claims.keySet() + ")";
	}

}
