/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.device.interfaces;

import java.util.List;

import com.top_logic.base.user.UserInterface;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.dob.DataObject;

/**
 * Used to access a persons userdata according to PersonAttributes.
 * 
 * @author    <a href="mailto:tri@top-logic.com">Thomas Richter</a>
 */
public interface PersonDataAccessDevice extends SecurityDevice {
	
	/**
	 * Configuration for a {@link PersonDataAccessDevice}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config<I extends PersonDataAccessDevice> extends SecurityDevice.Config<I> {

		/** @see #getDomain() */
		String DOMAIN_ATTRIBUTE = "domain";

		/**
		 * Configuration name for option
		 * {@link Config#isAllowExtAuthentication}.
		 */
		String ALLOW_EXT_AUTHENTICATION_PROPERTY = "allow-ext-authentication";

		/** Configuration name for option {@link Config#isReadOnly()}. */
		String READ_ONLY_PROPERTY = "read-only";

		/**
		 * Domain name of the device.
		 * 
		 * @see PersonDataAccessDevice#getDomainName()
		 */
		@Name(DOMAIN_ATTRIBUTE)
		@Nullable
		String getDomain();

		/**
		 * Whether this {@link PersonDataAccessDevice} is read-only.
		 * 
		 * <p>
		 * Data can only be updated by the system, if the device is not read-only.
		 * </p>
		 * 
		 * @see PersonDataAccessDevice#isReadOnly()
		 */
		@Name(READ_ONLY_PROPERTY)
		boolean isReadOnly();

		/**
		 * Whether users of this device may be granted access to the system trusting an external
		 * authentication system (such as authentication done by the servlet container).
		 */
		@Name(ALLOW_EXT_AUTHENTICATION_PROPERTY)
		boolean isAllowExtAuthentication();

	}

	/**
	 * The domain name of this device.
	 * 
	 * <p>
	 * A device associates its domain name to users managed by the device. This may be used to
	 * restrict authentication methods to users associated with certain domains.
	 * </p>
	 * 
	 * <p>
	 * A value of <code>null</code> means to no associate any domain to managed users.
	 * </p>
	 */
	default String getDomainName() {
		return ((Config<?>) getConfig()).getDomain();
	}

	/**
	 * Return the data for all persons that are represented by this device
	 * 
	 * @return a List of DOs where each DO represents the data of a person
	 */
	public List<UserInterface> getAllUserData();
	
	/**
	 * @param aName - the internal unique username
	 * @return the data for the given person as DataObject or null if not found
	 */
	public UserInterface getUserData(String aName);
	
	/**
	 * Creates a new user entry with the given data if not already exists
	 */
	public boolean createUserEntry(DataObject aDo);
	
	/**
	 * Stores the given user data, if a corresponding user is found in this device
	 */
	public boolean updateUserData(DataObject theDo);
	
	/**
	 * Rename an entry so from now on it can be found using the new identifier
     * 
	 * @param oldID the old identifier 
	 * @param newID the new identifier
	 */
	public boolean renameUserData(String oldID, String newID);

	/**
	 * @param aName - the internal unique username
	 * @return true if data for this name was deleted from this device, false otherwise (i.e. if no such name was found)
	 */	
	public boolean deleteUserData(String aName);
	/**
	 * @param aName - the internal unique username
	 * @return true if data for this name is available from this device, false otherwise
	 */
	default boolean userExists(String aName) {
		return this.getUserData(aName) != null;
	}
	
	/**
	 * @see Config#isReadOnly()
	 */
	default boolean isReadOnly() {
		return ((Config<?>) getConfig()).isReadOnly();
	}

	/**
	 * @see Config#isAllowExtAuthentication()
	 */
	default boolean allowExternalAuthentication() {
		return ((Config<?>) getConfig()).isAllowExtAuthentication();
	}

	/**
	 * The authentication device to use for accounts created by this device.
	 */
	public String getAuthenticationDeviceID();
	
}
