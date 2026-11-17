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
	 * Reports that the verification expired before the external authentication came back.
	 *
	 * <p>
	 * Run instead of {@link #handle(Person, VerifiedIdentity)}, once, on the thread of the request
	 * that brings the late answer: the side that waits learns that no answer will come, as it would
	 * learn of a refusal. A verification that is never answered at all is not reported.
	 * </p>
	 *
	 * @param expected
	 *        The account of the session that registered the verification.
	 * @return Whether the expiry has reached the place that asked for the verification, see
	 *         {@link VerificationResult#isDelivered()}.
	 */
	default boolean expired(Person expected) {
		// Nothing waits by default.
		return false;
	}

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
		IdentityVerificationHandler check = reauthentication();
		return (expected, identity) -> {
			VerificationResult result = check.handle(expected, identity);
			if (!result.isVerified()) {
				return result;
			}
			onVerified.run();
			return result.delivered();
		};
	}

	/**
	 * The check of a fresh authentication of the session's own user, for a caller that acts on
	 * the result itself.
	 *
	 * <p>
	 * Accepts the identity only if its {@link VerifiedIdentity#getAccount() account} is the expected
	 * one, see {@link #reauthentication(Runnable)}.
	 * </p>
	 */
	static IdentityVerificationHandler reauthentication() {
		return (expected, identity) -> {
			if (!expected.equals(identity.getAccount())) {
				return VerificationResult.mismatch();
			}
			return VerificationResult.verified(I18NConstants.IDENTITY_VERIFIED);
		};
	}

}
