/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.device.ldap;

import java.util.List;

import javax.naming.AuthenticationException;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import com.top_logic.base.accesscontrol.LoginCredentials;
import com.top_logic.base.dsa.ldap.LDAPAccessService;
import com.top_logic.base.dsa.ldap.ServiceProviderInfo;
import com.top_logic.base.security.device.AbstractSecurityDevice;
import com.top_logic.base.security.device.interfaces.AuthenticationDevice;
import com.top_logic.base.security.device.interfaces.PersonDataAccessDevice;
import com.top_logic.base.security.password.PasswordValidator;
import com.top_logic.base.user.UserInterface;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.dob.DataObject;
import com.top_logic.knowledge.wrap.person.Person;

/**
 * AuthenticationDevice and PersonDataAccessDevice against LDAP.
 * 
 * This implementation represents as well an AuthenticationDevice as an PersonDataAccessDevice
 * for and external UserRepository that can be accessed by the LDAP protocol
 * Typically this class will be instantiated by the TLSecurityDeviceManager, which also
 * has control of the number of created instances.
 * 
 * NOTE: this device provides only read only access!
 * 
 * @author    <a href="mailto:tri@top-logic.com">Thomas Richter</a>
 */
public class LDAPAuthenticationAccessDevice extends AbstractSecurityDevice
		implements PersonDataAccessDevice, AuthenticationDevice {

	/**
	 * Configuration of the {@link LDAPAuthenticationAccessDevice}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractSecurityDevice.Config, PersonDataAccessDevice.PersonDataAccessDeviceConfig,
			AuthenticationDevice.AuthenticationDeviceConfig {
		
		/** Name of configuration option {@link Config#getAccessService()}. */
		String ACCESS_SERVICE_NAME = "access-service";

		/**
		 * Configuration of the {@link LDAPAccessService} to delegate authentication check to.
		 */
		@Mandatory
		@Name(ACCESS_SERVICE_NAME)
		LDAPAccessService.Config getAccessService();

	}
	/**
	 * Instance of the LDAP access service, used to actually access the external system
	 */
	protected LDAPAccessService	las;

	/**
	 * Creates a new {@link LDAPAuthenticationAccessDevice} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link LDAPAuthenticationAccessDevice}.
	 * 
	 */
	public LDAPAuthenticationAccessDevice(InstantiationContext context, Config config) {
		super(context, config);
		initLAS(getDeviceID(), config);
	}

	/**
	 * Set the correct LAS internally
	 */
	protected void initLAS(String deviceID, Config config) {
		las = new LDAPAccessService(deviceID, config.getAccessService(), this);
	}

	@Override
	public boolean authentify(LoginCredentials login) {
		String aName = login.getUsername();
		char[] aPassword = login.getPassword();
		if (aPassword == null || StringServices.isEmpty(aName)) {
			return false;
		}
		ServiceProviderInfo theEnv = this.las.getCopyOfSPI();
		String fullUserDN = this.las.getFullUserDN(aName);
		if(StringServices.isEmpty(fullUserDN)){
			Logger.warn("No DN found for user with name "+aName+". Not a group member. Authentication denied.",this);
			return false; //username unknown - not in group
		}
		theEnv.put(ServiceProviderInfo.KEY_CREDENTIALS, new String(aPassword));
		theEnv.put(ServiceProviderInfo.KEY_PRINCIPAL, fullUserDN);
		DirContext theContext = null;
		try {
			theContext = LDAPAccessService.createContext(theEnv);
		} catch (AuthenticationException ae) {
			Logger.debug("Cannot authenticate " + aName + " (Wrong username or pwd):", ae, this);
		} catch (NamingException ex) {
			Logger.error("Unable to authenticate the user \"" + aName + "\"", ex, this);
		} finally {
			if (theContext != null) {
				try {
					theContext.close();
				} catch (NamingException ex) {
					Logger.error("Unable to close context used to authenticate user \"" + aName + "\"", ex,
						LDAPAuthenticationAccessDevice.class);
				}
			}
		}
		return (theContext != null);
	}

	@Override
	public boolean allowPwdChange() {
		return false;
	}

	@Override
	public void setPassword(Person account, char[] password) {
		throw new UnsupportedOperationException("Updating LDAP passwords not suppported.");
	}

	@Override
	public boolean isPasswordChangeRequested(Person account, char[] password) {
		return false;
	}

	@Override
	public void expirePassword(Person account) {
		// Ignore.
	}

	@Override
	public PasswordValidator getPasswordValidator() {
		throw new UnsupportedOperationException("LDAP passwords are not validated.");
	}

	@Override
	public List<UserInterface> getAllUserData() {
		return this.las.getAllUserData();
	}

	@Override
	public UserInterface getUserData(String aName) {
		return this.las.getUserData(aName);
	}

	@Override
	public boolean updateUserData(DataObject theDo) {
		throw new UnsupportedOperationException("Updating LDAP user data not suppported.");
	}

	@Override
	public boolean createUserEntry(DataObject aDo) {
		throw new UnsupportedOperationException("Creating LDAP user entries not suppported.");
	}

	@Override
	public boolean renameUserData(String oldID, String newID) {
		throw new UnsupportedOperationException("Renaming LDAP users entries not suppported.");
	}

	@Override
	public boolean deleteUserData(String aName) {
		throw new UnsupportedOperationException("LDAP users cannot be deleted.");
	}

}