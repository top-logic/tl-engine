/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.accesscontrol;

import java.util.Objects;

import com.top_logic.base.accesscontrol.IdentityVerifications.Outcome;
import com.top_logic.basic.util.ResKey;

/**
 * What became of the {@link IdentityVerifications#complete(String, VerifiedIdentity) completion}
 * of an identity verification, with the message the user is shown about it.
 */
public final class VerificationResult {

	private static final VerificationResult UNKNOWN =
		new VerificationResult(Outcome.UNKNOWN, I18NConstants.ERROR_IDENTITY_VERIFICATION_UNKNOWN);

	private static final VerificationResult MISMATCH =
		new VerificationResult(Outcome.MISMATCH, I18NConstants.ERROR_IDENTITY_MISMATCH);

	private final Outcome _outcome;

	private final ResKey _message;

	private VerificationResult(Outcome outcome, ResKey message) {
		_outcome = Objects.requireNonNull(outcome, "No outcome.");
		_message = Objects.requireNonNull(message, "No message.");
	}

	/**
	 * The handler of the verification has accepted the identity.
	 *
	 * @param message
	 *        What the user is told.
	 */
	public static VerificationResult verified(ResKey message) {
		return new VerificationResult(Outcome.VERIFIED, message);
	}

	/**
	 * The handler of the verification has refused the identity for the given reason.
	 *
	 * @param reason
	 *        What the user is told.
	 */
	public static VerificationResult refused(ResKey reason) {
		return new VerificationResult(Outcome.REFUSED, reason);
	}

	/**
	 * An account authenticated that is not the one the verification waits for.
	 */
	public static VerificationResult mismatch() {
		return MISMATCH;
	}

	/**
	 * No verification is held under the token.
	 */
	public static VerificationResult unknown() {
		return UNKNOWN;
	}

	/**
	 * The kind of result.
	 */
	public Outcome getOutcome() {
		return _outcome;
	}

	/**
	 * Whether the identity has been accepted.
	 */
	public boolean isVerified() {
		return _outcome == Outcome.VERIFIED;
	}

	/**
	 * What the user is told about the result.
	 */
	public ResKey getMessage() {
		return _message;
	}

	@Override
	public String toString() {
		return _outcome + "(" + _message + ")";
	}

}
