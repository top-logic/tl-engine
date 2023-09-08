/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.encryption.pbe;

/**
 * Result of an encryption using {@link PasswordBasedEncryption}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class EncryptionResult {

	private final byte[] _salt;

	private final byte[] _cipherText;

	/**
	 * Creates a {@link EncryptionResult}.
	 * 
	 * @param salt
	 *        See {@link #getSalt()}.
	 * @param cipherText
	 *        See {@link #getCipherText()}.
	 */
	public EncryptionResult(byte[] salt, byte[] cipherText) {
		_salt = salt;
		_cipherText = cipherText;
	}

	/**
	 * The salt bytes to prevent known plain text attacks.
	 */
	public byte[] getSalt() {
		return _salt;
	}

	/**
	 * The cipher text.
	 */
	public byte[] getCipherText() {
		return _cipherText;
	}

}