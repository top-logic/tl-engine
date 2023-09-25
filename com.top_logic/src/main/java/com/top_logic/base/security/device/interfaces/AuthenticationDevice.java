/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.device.interfaces;

import com.top_logic.base.accesscontrol.LoginCredentials;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.dob.DataObject;

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
	public interface AuthenticationDeviceConfig extends SecurityDeviceConfig {

		/** Name of the configuration option {@link AuthenticationDeviceConfig#isAllowPwdChange}. */
		String ALLOW_PWD_CHANGE_NAME = "allow-pwd-change";

		/**
		 * Whether this device enables the user to change his password within this system.
		 */
		@Name(ALLOW_PWD_CHANGE_NAME)
		boolean isAllowPwdChange();

	}

	/**
	 * @param login
	 *        The credentials holding the user and password.
	 * @return true if authentication succeeded, false otherwise
	 */
	public boolean authentify(LoginCredentials login);

	/**
	 * @see AuthenticationDeviceConfig#isAllowPwdChange()
	 */
	public boolean allowPwdChange();	

	/**
	 * Stores the given user data, used to change the password in the authentication device
	 */
	public boolean updateUserData(DataObject theDo);
}
