/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.encryption;

import java.security.GeneralSecurityException;

/**
 * Signals that a cipher text cannot be decrypted, because it is malformed, was produced with a
 * different key, or was modified after encryption.
 * 
 * <p>
 * The message never contains the rejected cipher text.
 * </p>
 * 
 * @see AuthenticatedEncryption#decrypt(byte[])
 */
public class InvalidCipherTextException extends GeneralSecurityException {

	/**
	 * Creates a {@link InvalidCipherTextException}.
	 *
	 * @param message
	 *        Description of the problem, without the rejected cipher text.
	 */
	public InvalidCipherTextException(String message) {
		super(message);
	}

	/**
	 * Creates a {@link InvalidCipherTextException}.
	 *
	 * @param message
	 *        Description of the problem, without the rejected cipher text.
	 * @param cause
	 *        The failure reported by the cipher.
	 */
	public InvalidCipherTextException(String message, Throwable cause) {
		super(message, cause);
	}

}
