/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.password;

import com.top_logic.base.security.password.PasswordValidator.ValidationResult;
import com.top_logic.knowledge.wrap.person.Person;

/**
 * Strategy for checking passwords that users try to set for their accounts.
 */
public interface PasswordValidation {

	/**
	 * Validates a password for an account.
	 * 
	 * @return returns an int indicating the state of the pwd (refer constants of this class)
	 */
	ValidationResult validatePassword(Person account, char[] password) throws IllegalArgumentException;

	/**
	 * Whether the given account should be excluded from password policy requirements.
	 *
	 * @param account
	 *        The account to check.
	 * @return Whether the given user is excluded from password validation.
	 */
	boolean isExcluded(Person account);

	/**
	 * Number of days a password stays valid.
	 * 
	 * <p>
	 * A password will expire after the given number of days. After that period, a user has to
	 * change his password. A value of zero means that a password never expires.
	 * </p>
	 */
	int getExpiryPeriod();

}
