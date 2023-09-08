/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.password.hashing;

/**
 * Algorithm to create and verify a hash value from a given password.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface PasswordHashingAlgorithm {

	/**
	 * Creates a hash value for the given password.
	 * 
	 * @param password
	 *        a password to create a hash value for
	 */
	String createHash(char[] password);

	/**
	 * Checks whether the given password is valid for the given hash value.
	 * 
	 * @param password
	 *        The password to check.
	 * @param hash
	 *        the expected hash value.
	 * @return <code>true</code>, when the given hash password is valid for the given hash.
	 */
	boolean verify(char[] password, String hash);

}

