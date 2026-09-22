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
 * Created by {@link IdentityVerifications#register(Person, Runnable)} and kept in that registry
 * until the expected account has authenticated afresh, until the request is
 * {@link IdentityVerifications#cancel(String) cancelled}, or until it {@link #getExpiry() expires}.
 * The authentication itself happens outside the command that is waiting - in another browser window
 * and on another request thread - so what is held here is the account that must come back and the
 * continuation to run when it does.
 * </p>
 *
 * @see IdentityVerifications
 */
public final class PendingIdentityVerification {

	private final Person _expectedAccount;

	private final Runnable _onVerified;

	private final Instant _expiry;

	/**
	 * Creates a {@link PendingIdentityVerification}.
	 *
	 * @param expectedAccount
	 *        See {@link #getExpectedAccount()}.
	 * @param onVerified
	 *        See {@link #getOnVerified()}.
	 * @param expiry
	 *        See {@link #getExpiry()}.
	 */
	PendingIdentityVerification(Person expectedAccount, Runnable onVerified, Instant expiry) {
		_expectedAccount = Objects.requireNonNull(expectedAccount, "No account to verify.");
		_onVerified = Objects.requireNonNull(onVerified, "No continuation to run.");
		_expiry = Objects.requireNonNull(expiry, "No expiry.");
	}

	/**
	 * The account that has to authenticate again for this verification to succeed.
	 *
	 * <p>
	 * An authentication that produces any other account is a mismatch: the proof then belongs to
	 * someone else and says nothing about the session's own user.
	 * </p>
	 */
	public Person getExpectedAccount() {
		return _expectedAccount;
	}

	/**
	 * What to run once {@link #getExpectedAccount()} has authenticated again.
	 */
	public Runnable getOnVerified() {
		return _onVerified;
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

	/**
	 * Whether the given account is the one this verification waits for.
	 */
	public boolean matches(Person account) {
		return _expectedAccount.equals(account);
	}

}
