/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.accesscontrol;

import java.util.Objects;
import java.util.function.Supplier;

import com.top_logic.knowledge.wrap.person.Person;

/**
 * The identity an external authentication has just confirmed, as it reaches an
 * {@link IdentityVerificationHandler}.
 *
 * <p>
 * Where the authentication names it, the identity is the pair of the {@link #getClientName()
 * client} the user authenticated at and the {@link #getSubject() subject} that client knows the
 * user by. The {@link #getAccount() account} of the identity is resolved only when a handler asks
 * for it: resolving it goes through the account mapping of a login, which may link or create
 * accounts, and a handler that only needs the pair must not cause that.
 * </p>
 *
 * @see ExternalAuthenticationServlet#retrieveVerifiedIdentity(jakarta.servlet.http.HttpServletRequest,
 *      jakarta.servlet.http.HttpServletResponse)
 */
public final class VerifiedIdentity {

	private final String _clientName;

	private final String _subject;

	private Supplier<Person> _accountResolver;

	private Person _account;

	/**
	 * Creates a {@link VerifiedIdentity}.
	 *
	 * @param clientName
	 *        See {@link #getClientName()}.
	 * @param subject
	 *        See {@link #getSubject()}.
	 * @param accountResolver
	 *        Computes the {@link #getAccount() account} when it is first asked for. Called at most
	 *        once.
	 */
	public VerifiedIdentity(String clientName, String subject, Supplier<Person> accountResolver) {
		_clientName = clientName;
		_subject = subject;
		_accountResolver = Objects.requireNonNull(accountResolver, "No account resolver.");
	}

	/**
	 * A {@link VerifiedIdentity} known only by its account.
	 *
	 * <p>
	 * For an authentication that establishes the account but names no client and subject.
	 * </p>
	 *
	 * @param account
	 *        The confirmed account, may be <code>null</code> for none.
	 */
	public static VerifiedIdentity ofAccount(Person account) {
		return new VerifiedIdentity(null, null, () -> account);
	}

	/**
	 * The name of the login client the user authenticated at, or <code>null</code> if the
	 * authentication does not name one.
	 *
	 * <p>
	 * The name of the client as it is configured, also when the authentication used a client
	 * derived from it (e.g. one that demands a fresh authentication).
	 * </p>
	 */
	public String getClientName() {
		return _clientName;
	}

	/**
	 * The identifier of the user at the {@link #getClientName() client}, or <code>null</code> if the
	 * authentication does not name one.
	 */
	public String getSubject() {
		return _subject;
	}

	/**
	 * Whether both {@link #getClientName()} and {@link #getSubject()} are known.
	 */
	public boolean hasClientIdentity() {
		return _clientName != null && !_clientName.isEmpty() && _subject != null && !_subject.isEmpty();
	}

	/**
	 * The account the confirmed identity belongs to, resolved on first access.
	 *
	 * <p>
	 * Resolving follows the account mapping of a login with the same identity, and fails like that
	 * login does.
	 * </p>
	 *
	 * @return The account, or <code>null</code> if the authentication established none.
	 */
	public synchronized Person getAccount() {
		if (_accountResolver != null) {
			Supplier<Person> resolver = _accountResolver;
			_accountResolver = null;
			_account = resolver.get();
		}
		return _account;
	}

	@Override
	public String toString() {
		return "VerifiedIdentity(client=" + _clientName + ", subject=" + _subject + ")";
	}

}
