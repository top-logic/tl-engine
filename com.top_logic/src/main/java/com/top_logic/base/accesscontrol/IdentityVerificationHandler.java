/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.accesscontrol;

import java.util.Objects;

import com.top_logic.knowledge.wrap.person.Person;

/**
 * What a {@link PendingIdentityVerification} does with the identity the external authentication
 * has confirmed.
 *
 * <p>
 * Run once, on the thread of the request that brings the confirmation, after the entry has been
 * taken from the registry - a handler never sees the same token twice.
 * </p>
 *
 * @see IdentityVerifications#register(Person, IdentityVerificationHandler)
 */
@FunctionalInterface
public interface IdentityVerificationHandler {

	/**
	 * Handles the confirmed identity.
	 *
	 * @param expected
	 *        The account of the session that registered the verification. Taken from that session,
	 *        never from the request that returns.
	 * @param identity
	 *        What the external authentication has just confirmed.
	 * @return What became of the verification, with the message for the user. A handler that has
	 *         told the place that asked for the verification returns the result
	 *         {@link VerificationResult#delivered() delivered}.
	 */
	VerificationResult handle(Person expected, VerifiedIdentity identity);

	/**
	 * The handler of a fresh authentication of the session's own user.
	 *
	 * <p>
	 * Accepts the identity only if its {@link VerifiedIdentity#getAccount() account} is the expected
	 * one, and then runs the given continuation. Any other account, and none, is a
	 * {@link VerificationResult#mismatch() mismatch}: the proof then belongs to someone else and
	 * says nothing about the session's own user.
	 * </p>
	 *
	 * <p>
	 * An accepted identity is {@link VerificationResult#delivered() delivered}: the continuation is
	 * what carries it to the place that asked.
	 * </p>
	 *
	 * @param onVerified
	 *        Run once the expected account has authenticated again, carrying the confirmation to the
	 *        place that asked for it.
	 */
	static IdentityVerificationHandler reauthentication(Runnable onVerified) {
		Objects.requireNonNull(onVerified, "No continuation to run.");
		return (expected, identity) -> {
			if (!expected.equals(identity.getAccount())) {
				return VerificationResult.mismatch();
			}
			onVerified.run();
			return VerificationResult.verified(I18NConstants.IDENTITY_VERIFIED).delivered();
		};
	}

}
