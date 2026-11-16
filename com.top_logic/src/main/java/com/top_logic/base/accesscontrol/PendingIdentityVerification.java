/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.accesscontrol;

import java.time.Instant;
import java.util.Objects;

import com.top_logic.knowledge.wrap.person.Person;

/**
 * A request to prove that the person at the keyboard still is the account the session belongs to,
 * waiting for that proof to arrive.
 *
 * <p>
 * Created by {@link IdentityVerifications#register(Person, IdentityVerificationHandler)} and kept in
 * that registry until the external authentication has come back, until the request is
 * {@link IdentityVerifications#cancel(String) cancelled}, or until it {@link #getExpiry() expires}.
 * The authentication itself happens outside the command that is waiting - in another browser window
 * and on another request thread - so what is held here is the account of the session that asked and
 * the {@link IdentityVerificationHandler handler} that decides what the confirmed identity means for
 * it.
 * </p>
 *
 * @see IdentityVerifications
 */
public final class PendingIdentityVerification {

	private final Person _expectedAccount;

	private final IdentityVerificationHandler _handler;

	private final Instant _expiry;

	/**
	 * Creates a {@link PendingIdentityVerification}.
	 *
	 * @param expectedAccount
	 *        See {@link #getExpectedAccount()}.
	 * @param handler
	 *        See {@link #getHandler()}.
	 * @param expiry
	 *        See {@link #getExpiry()}.
	 */
	PendingIdentityVerification(Person expectedAccount, IdentityVerificationHandler handler, Instant expiry) {
		_expectedAccount = Objects.requireNonNull(expectedAccount, "No account to verify.");
		_handler = Objects.requireNonNull(handler, "No handler.");
		_expiry = Objects.requireNonNull(expiry, "No expiry.");
	}

	/**
	 * The account of the session that registered this verification.
	 *
	 * <p>
	 * Handed to the {@link #getHandler() handler} together with the confirmed identity: the account
	 * that has to authenticate again, or the account a confirmed identity is attached to.
	 * </p>
	 */
	public Person getExpectedAccount() {
		return _expectedAccount;
	}

	/**
	 * What to do with the identity the external authentication confirms.
	 */
	public IdentityVerificationHandler getHandler() {
		return _handler;
	}

	/**
	 * Hands the confirmed identity to the {@link #getHandler() handler}.
	 */
	VerificationResult handle(VerifiedIdentity identity) {
		return _handler.handle(_expectedAccount, identity);
	}

	/**
	 * The point in time from which on this verification counts as no longer offered.
	 */
	public Instant getExpiry() {
		return _expiry;
	}

	/**
	 * Whether this verification is no longer offered at the given point in time.
	 */
	public boolean isExpired(Instant now) {
		return !now.isBefore(_expiry);
	}

}
