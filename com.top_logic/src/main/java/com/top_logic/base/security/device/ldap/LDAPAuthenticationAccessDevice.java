/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.device.ldap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.AuthenticationException;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import com.top_logic.base.accesscontrol.LoginCredentials;
import com.top_logic.base.dsa.ldap.LDAPAccessService;
import com.top_logic.base.dsa.ldap.ServiceProviderInfo;
import com.top_logic.base.security.device.DeviceMapping;
import com.top_logic.base.security.device.interfaces.AuthenticationDevice;
import com.top_logic.base.security.device.interfaces.PersonDataAccessDevice;
import com.top_logic.base.security.device.interfaces.SecurityDevice;
import com.top_logic.base.security.password.PasswordValidator;
import com.top_logic.base.user.UserInterface;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.MapBinding;
import com.top_logic.basic.config.annotation.Name;
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
public class LDAPAuthenticationAccessDevice extends AbstractConfiguredInstance<SecurityDevice.Config<?>>
		implements PersonDataAccessDevice, AuthenticationDevice {

	/**
	 * Configuration of the {@link LDAPAuthenticationAccessDevice}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PersonDataAccessDevice.Config, AuthenticationDevice.Config {
		
		/** Name of configuration option {@link Config#getAccessService()}. */
		String ACCESS_SERVICE_NAME = "access-service";

		/**
		 * Configuration of the {@link LDAPAccessService} to delegate authentication check to.
		 */
		@Mandatory
		@Name(ACCESS_SERVICE_NAME)
		LDAPAccessService.Config getAccessService();

		/**
		 * Property mappings.
		 */
		@MapBinding()
		Map<String, String> getMappings();
	}

	/**
	 * Instance of the LDAP access service, used to actually access the external system
	 */
	protected LDAPAccessService	las;

	private Map mappings;

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
		mappings = new HashMap();
	}

	/**
	 * Set the correct LAS internally
	 */
	protected void initLAS(String deviceID, Config config) {
		las = new LDAPAccessService(deviceID, config.getAccessService(), this);
	}

	@Override
	public String getAuthenticationDeviceID() {
		return getDeviceID();
	}

	/**
	 * @param objectClass
	 *        the type of object for which a mapping is requested. DeviceMapping::OBJ_CLASS_GENERIC
	 *        for generic mapping. May not be empty
	 * @return the Mapping
	 */
	public DeviceMapping getMapping(String objectClass) {
		return this.getMapping(Collections.singletonList(objectClass));
	}

	/**
	 * @param objectClasses
	 *        the types of object for which a mapping is requested. DeviceMapping::OBJ_CLASS_GENERIC
	 *        for generic mapping. May not be empty
	 * @return the Mapping
	 */
	public DeviceMapping getMapping(List<String> objectClasses) {
		return LDAPAuthenticationAccessDevice.getMappingFor(this.mappings, objectClasses, ((Config) getConfig()).getMappings());
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

	/**
	 * A type specific mapping for this device and the given object class
	 */
	public static synchronized DeviceMapping getMappingFor(Map<String, DeviceMapping> aMappingCache,
			List<String> objectClasses, Map<String, String> mappings) {
		DeviceMapping theMapping = new DeviceMapping();
	
		for (Iterator<String> theClasses = objectClasses.iterator(); theClasses.hasNext();) {
			String theObjectClass = theClasses.next();
			DeviceMapping theInnerMapping = null;
			if (aMappingCache != null) {
				theInnerMapping = aMappingCache.get(theObjectClass);
			}
	
			if (theInnerMapping == null) {
				theInnerMapping = new DeviceMapping(mappings, theObjectClass);
				if (aMappingCache != null) {
					aMappingCache.put(theObjectClass, theInnerMapping);
				}
			}
			theMapping.mergeWith(theInnerMapping, true);
		}
	
		return theMapping;
	}

}