/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.password.hashing;

import java.util.Objects;

/**
 * Verification result of a password check.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface VerificationResult {

	/** Signals that the verification was successful. */
	VerificationResult SUCCESS = new VerificationResult() {

		@Override
		public boolean success() {
			return true;
		}

	};

	/** Signals that the verification failed. */
	VerificationResult FAILED = new VerificationResult() {

		@Override
		public boolean success() {
			return false;
		}

	};

	/**
	 * Creates a {@link VerificationResult} for a successful verification, but the given hash was
	 * outdated, i.e. a new hash representing the same password should be used instead.
	 * 
	 * @param newHash
	 *        The new hash value for the given password.
	 */
	static VerificationResult withRehash(String newHash) {
		Objects.requireNonNull(newHash);
		return new VerificationResult() {

			@Override
			public boolean success() {
				return true;
			}

			@Override
			public String newHash() {
				return newHash;
			}
		};
	}

	/**
	 * Whether verification of the password against the given hash was successful or not.
	 */
	boolean success();

	/**
	 * A new hash to use for the given password when verification against the old hash value was
	 * successful.
	 * 
	 * <p>
	 * Must not be used unless {@link #hasUpdatedHash()} returns <code>true</code>.
	 * </p>
	 * 
	 * @return The potential new hash to use.
	 * 
	 * @see #hasUpdatedHash()
	 */
	default String newHash() {
		return null;
	}

	/**
	 * Whether a new hash value for the given password should be used.
	 * 
	 * @return Whether {@link #newHash()} should be used as new hash value for the given password.
	 * 
	 * @see #newHash()
	 */
	default boolean hasUpdatedHash() {
		return success() && (newHash() != null);
	}

}

