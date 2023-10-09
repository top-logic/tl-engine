/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.device.interfaces;

import com.top_logic.base.accesscontrol.LoginCredentials;
import com.top_logic.base.security.password.PasswordValidator;
import com.top_logic.knowledge.wrap.person.Person;

/**
 * An authentication device is used to autheticate a person against.
 * 
 * @author    <a href="mailto:tri@top-logic.com">Thomas Richter</a>
 */
public interface AuthenticationDevice extends SecurityDevice {
	
	/**
	 * Configuration interface for {@link AuthenticationDevice}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends SecurityDevice.Config {
		// Pure marker interface.
	}

	/**
	 * @param login
	 *        The credentials holding the user and password.
	 * @return true if authentication succeeded, false otherwise
	 */
	public boolean authentify(LoginCredentials login);

	/**
	 * Whether this device supports password change.
	 * 
	 * @see #setPassword(Person, char[])
	 */
	boolean allowPwdChange();

	/**
	 * Updates the password for the given account.
	 * 
	 * <p>
	 * Must only be called, if {@link #allowPwdChange()} returns <code>true</code>.
	 * </p>
	 */
	public void setPassword(Person account, char[] password);

	/**
	 * The password validation strategy for passwords.
	 * 
	 * <p>
	 * Must only be called, if {@link #allowPwdChange()} returns <code>true</code>.
	 * </p>
	 */
	public PasswordValidator getPasswordValidator();

	/**
	 * Requests a password change when the user logs in next time.
	 * 
	 * <p>
	 * Must only be called, if {@link #allowPwdChange()} returns <code>true</code>.
	 * </p>
	 */
	public void expirePassword(Person account);

	/**
	 * Whether a user should be requested to change his password.
	 *
	 * <p>
	 * Must only be called, if {@link #allowPwdChange()} returns <code>true</code>.
	 * </p>
	 */
	public boolean isPasswordChangeRequested(Person account, char[] password);
}
