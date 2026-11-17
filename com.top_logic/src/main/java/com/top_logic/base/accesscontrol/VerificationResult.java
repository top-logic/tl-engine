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

	private final boolean _delivered;

	private VerificationResult(Outcome outcome, ResKey message) {
		this(outcome, message, false);
	}

	private VerificationResult(Outcome outcome, ResKey message, boolean delivered) {
		_outcome = Objects.requireNonNull(outcome, "No outcome.");
		_message = Objects.requireNonNull(message, "No message.");
		_delivered = delivered;
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

	/**
	 * Whether the result has reached the place that asked for the verification.
	 *
	 * <p>
	 * A delivered result is shown, and whatever waited for it continues or gives up, where the
	 * verification was asked for. The browser window that brought the confirmation then has nothing
	 * left to say and closes itself; an undelivered result is shown in that window.
	 * </p>
	 *
	 * @see #delivered()
	 */
	public boolean isDelivered() {
		return _delivered;
	}

	/**
	 * This result, marked as having reached the place that asked for the verification.
	 *
	 * <p>
	 * For a handler that has told the asker the result, see {@link #isDelivered()}.
	 * </p>
	 */
	public VerificationResult delivered() {
		if (_delivered) {
			return this;
		}
		return new VerificationResult(_outcome, _message, true);
	}

	@Override
	public String toString() {
		return _outcome + "(" + _message + (_delivered ? ", delivered" : "") + ")";
	}

}
