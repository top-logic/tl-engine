/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

/**
 * Signals that a configuration value is no valid encrypted value, e.g. because it is a plain text,
 * was modified, or was encrypted with another key.
 * 
 * <p>
 * Neither the message nor the cause of this exception contains the rejected value or parts of it,
 * since a rejected value is in most cases a secret configured in plain text.
 * </p>
 * 
 * <p>
 * This is an unchecked exception, since an invalid configuration value is a configuration error
 * that callers report, not a condition they recover from.
 * </p>
 * 
 * @see ConfigurationEncryption#decrypt(String)
 */
public class InvalidEncryptedValueException extends IllegalArgumentException {

	/**
	 * Creates a {@link InvalidEncryptedValueException}.
	 *
	 * @param message
	 *        Description of the problem, without the rejected value.
	 */
	public InvalidEncryptedValueException(String message) {
		super(message);
	}

	/**
	 * Creates a {@link InvalidEncryptedValueException}.
	 *
	 * @param message
	 *        Description of the problem, without the rejected value.
	 * @param cause
	 *        The failure of the decryption, which does not contain the rejected value.
	 */
	public InvalidEncryptedValueException(String message, Throwable cause) {
		super(message, cause);
	}

}
