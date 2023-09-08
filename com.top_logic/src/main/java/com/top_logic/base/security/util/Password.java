/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.util;

import com.top_logic.basic.config.annotation.Inspectable;

/**
 * Wrapper object for a password value that prevents that the value is rendered to the UI.
 */
public final class Password {

	/**
	 * The string to render for a non-empty password value.
	 */
	public static final String PASSWORD_REPLACEMENT = "***";

	@Inspectable(false)
	private final String _cryptedValue;

	/**
	 * Constructs a new {@link Password} from the secret plain-text value.
	 */
	public static Password fromPlainText(String plainText) {
		return new Password(CryptSupport.getInstance().encodeString(plainText));
	}

	/**
	 * Creates a {@link Password}.
	 * 
	 * @param cryptedValue
	 *        The encrypted password value, see {@link CryptSupport#encodeString(String)}.
	 * 
	 * @see #fromPlainText(String)
	 */
	public Password(String cryptedValue) {
		_cryptedValue = cryptedValue;
	}

	/**
	 * The hidden password.
	 */
	public String getCryptedValue() {
		return _cryptedValue;
	}

	/**
	 * The decoded plain-text version of the password.
	 */
	public String decrypt() {
		return CryptSupport.getInstance().decodeString(getCryptedValue());
	}

	@Override
	public String toString() {
		return PASSWORD_REPLACEMENT;
	}

}
