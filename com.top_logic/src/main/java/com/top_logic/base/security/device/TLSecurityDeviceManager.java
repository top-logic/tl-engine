/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.device;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.base.accesscontrol.Login;
import com.top_logic.base.security.device.interfaces.AuthenticationDevice;
import com.top_logic.base.security.device.interfaces.PersonDataAccessDevice;
import com.top_logic.base.security.device.interfaces.SecurityDevice;
import com.top_logic.base.security.device.interfaces.SecurityDevice.SecurityDeviceConfig;
import com.top_logic.basic.Log;
import com.top_logic.basic.Logger;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.encryption.SecureRandomService;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.util.license.LicenseTool;

/**
 * Provides access to all configured SecurityDevices without knowledge of device IDs.
 * 
 * The TLSecurityDeviceManager provides all methods to access the configured SecurityDevices and to
 * perform requests over all devices without explicitly knowing a specific device ID.
 * 
 * @author <a href="mailto:tri@top-logic.com">Thomas Richter</a>
 */
@ServiceDependencies({
	SecureRandomService.Module.class,
	Login.Module.class,
})
public class TLSecurityDeviceManager extends ManagedClass {
	
	private static final String TL_FEATURE_LDAP = "tl.feature.ldap";

	private static final String DB_SECURITY = "dbSecurity";

	/**
	 * Configuration of the {@link TLSecurityDeviceManager}. 
	 * 
	 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends ManagedClass.ServiceConfiguration<TLSecurityDeviceManager> {
		
		/** Name of the configuration option {@link Config#getSecurityDevices()}. */
		String SECURITY_DEVICES_NAME = "security-devices";

		/** Name of the configuration option {@link Config#getDefaultDataAccessDevice()}. */
		String DEFAULT_DATA_ACCESS_DEVICE_NAME = "default-data-access";

		/** Name of the configuration option {@link Config#getDefaultAuthenticationDevice()}. */
		String DEFAULT_AUTHENTICATION_DEVICE_NAME = "default-authentication";

		/**
		 * Configuration of all {@link SecurityDevice}s.
		 */
		@Key(SecurityDevice.SecurityDeviceConfig.ID_ATTRIBUTE)
		@Name(SECURITY_DEVICES_NAME)
		Map<String, SecurityDeviceConfig> getSecurityDevices();

		/**
		 * Name of the default data access device.
		 * 
		 * <p>
		 * Must either be a key of {@link #getSecurityDevices()}, or empty in case there is exactly
		 * one data access device configured.
		 * </p>
		 */
		@Name(DEFAULT_DATA_ACCESS_DEVICE_NAME)
		String getDefaultDataAccessDevice();

		/**
		 * Name of the default authentication device.
		 * 
		 * <p>
		 * Must either be a key of {@link #getSecurityDevices()}, or empty in case there is exactly
		 * one authentication device configured.
		 * </p>
		 */
		@Name(DEFAULT_AUTHENTICATION_DEVICE_NAME)
		@StringDefault(DB_SECURITY)
		String getDefaultAuthenticationDevice();

	}

	/**
	 * Map to hold instances of the data access devices with their deviceID as key
	 */
	private Map<String, SecurityDevice> _dataAccessDevices;

	private SecurityDevice _defaultDataAccessDevice;

	/**
	 * Map to hold instances of the authentication devices with their deviceID as key
	 */
	private Map<String, AuthenticationDevice> _authenticationDevices;

	private AuthenticationDevice _defaultAuthenticationDevice;

	private Map<String, SecurityDevice> _configuredDevices;

	/**
	 * Creates a new {@link TLSecurityDeviceManager} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link TLSecurityDeviceManager}.
	 */
	public TLSecurityDeviceManager(InstantiationContext context, Config config) {
		_configuredDevices = TypedConfiguration.getInstanceMap(context, config.getSecurityDevices());
		checkSecurityDevices(_configuredDevices);
		_dataAccessDevices = getDataAccessDevices(context, _configuredDevices);
		_defaultDataAccessDevice = getDefaultAccessDevice(context, config, _dataAccessDevices);
		_authenticationDevices = getAuthenticationDevices(context, _configuredDevices);
		_defaultAuthenticationDevice = getDefaultAuthenticationDevice(context, config, _authenticationDevices);
	}

	@FrameworkInternal
	private void checkSecurityDevices(Map<String, SecurityDevice> devices) {
		if (!LicenseTool.getInstance().includeFeature(TL_FEATURE_LDAP)) {
			devices.entrySet().removeIf(e -> !e.getKey().equals(DB_SECURITY));
		}
	}

	private Map<String, AuthenticationDevice> getAuthenticationDevices(Log log,
			Map<String, SecurityDevice> allDevices) {
		Map<String, AuthenticationDevice> result = new HashMap<>();
		for (Entry<String, SecurityDevice> entry : allDevices.entrySet()) {
			String id = entry.getKey();
			SecurityDevice device = entry.getValue();

			SecurityDeviceConfig deviceConfig = device.getConfig();
			if (deviceConfig.isDisabled()) {
				continue;
			}

			if (!deviceConfig.getType().supportsAuth()) {
				continue;
			}

			if (!(device instanceof AuthenticationDevice)) {
				StringBuilder noAuthentication = new StringBuilder();
				noAuthentication.append("Configured device '");
				noAuthentication.append(id);
				noAuthentication.append("' is not an AuthenticationDevice. Class: ");
				noAuthentication.append(device.getClass());
				log.error(noAuthentication.toString());
				continue;
			}

			result.put(id, (AuthenticationDevice) device);
		}
		if (result.isEmpty()) {
			log.error("No authentication device configured.");
		}
		return result;
	}

	private AuthenticationDevice getDefaultAuthenticationDevice(Log log, Config config,
			Map<String, AuthenticationDevice> authenticationDevices) {
		AuthenticationDevice defaultAuthenticationDevice;
		String defaultAuthenticationId;
		if (LicenseTool.getInstance().includeFeature(TL_FEATURE_LDAP)) {
			defaultAuthenticationId = config.getDefaultAuthenticationDevice();
		} else {
			defaultAuthenticationId = DB_SECURITY;
		}
		if (defaultAuthenticationId.isEmpty()) {
			if (authenticationDevices.size() != 1) {
				StringBuilder noDefaultAuthentication = new StringBuilder();
				noDefaultAuthentication.append("There are more than one authentication configured '");
				noDefaultAuthentication.append(authenticationDevices.keySet());
				noDefaultAuthentication.append("' but no default authentication device.");
				log.error(noDefaultAuthentication.toString());
				defaultAuthenticationDevice = null;
			} else {
				defaultAuthenticationDevice = authenticationDevices.values().iterator().next();
			}
		} else {
			defaultAuthenticationDevice = authenticationDevices.get(defaultAuthenticationId);
			if (defaultAuthenticationDevice == null) {
				StringBuilder unknownDefaultDevice = new StringBuilder();
				unknownDefaultDevice.append("There is not authentication device with id '");
				unknownDefaultDevice.append(defaultAuthenticationId);
				unknownDefaultDevice.append("' to use as default authentication device.");
				log.error(unknownDefaultDevice.toString());
			}
		}
		return defaultAuthenticationDevice;
	}

	private Map<String, SecurityDevice> getDataAccessDevices(Log log, Map<String, SecurityDevice> allDevices) {
		checkSecurityDevices(allDevices);
		Map<String, SecurityDevice> result = new HashMap<>();
		for (Entry<String, SecurityDevice> entry : allDevices.entrySet()) {
			String id = entry.getKey();
			SecurityDevice device = entry.getValue();

			SecurityDeviceConfig deviceConfig = device.getConfig();
			if (deviceConfig.isDisabled()) {
				continue;
			}

			if (!deviceConfig.getType().supportsData()) {
				continue;
			}

			result.put(id, device);
		}
		if (result.isEmpty()) {
			log.error("No data access device configured.");
		}
		return result;
	}

	private SecurityDevice getDefaultAccessDevice(Log log, Config config,
			Map<String, SecurityDevice> dataAccessDevices) {
		SecurityDevice defaultDataAccessDevice;
		String defaultAccessDeviceId = config.getDefaultDataAccessDevice();
		if (defaultAccessDeviceId.isEmpty()) {
			if (dataAccessDevices.size() != 1) {
				StringBuilder noDefaultAccessDevice = new StringBuilder();
				noDefaultAccessDevice.append("There are more than one data access devices configured '");
				noDefaultAccessDevice.append(dataAccessDevices.keySet());
				noDefaultAccessDevice.append("' but no default data access device.");
				log.error(noDefaultAccessDevice.toString());
				defaultDataAccessDevice = null;
			} else {
				defaultDataAccessDevice = dataAccessDevices.values().iterator().next();
			}
		} else {
			defaultDataAccessDevice = dataAccessDevices.get(defaultAccessDeviceId);
			if (defaultDataAccessDevice == null) {
				StringBuilder unknownDefaultDevice = new StringBuilder();
				unknownDefaultDevice.append("There is not data access device with id '");
				unknownDefaultDevice.append(defaultAccessDeviceId);
				unknownDefaultDevice.append("' to use as default access device.");
				log.error(unknownDefaultDevice.toString());
			}
		}
		return defaultDataAccessDevice;
	}

	@Override
	protected void startUp() {
		super.startUp();

		for (SecurityDevice device : _configuredDevices.values()) {
			device.startUp();
		}
	}

	@Override
	protected void shutDown() {
		for (SecurityDevice device : _configuredDevices.values()) {
			device.shutDown();
		}

		super.shutDown();
	}

	 /**
	  * the singleton instance of this manager
	  */
	public static synchronized TLSecurityDeviceManager getInstance(){
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * a Set of the configured PersonDataAccessDevice id's (Strings)
	 */
	public Set<String> getConfiguredDataAccessDeviceIDs(){
		Set<String> result = new HashSet<>(_dataAccessDevices.keySet());
		if(result.isEmpty()){
			Logger.warn("No User data access device configured!",this);
		}
		return result;
	}

	/**
	 * a Set of the configured AuthenticationDevice id's (Strings)
	 */
	public Set<String>  getConfiguredAuthenticationDeviceIDs(){
		Set<String> result = new HashSet<>(_authenticationDevices.keySet());
		if(result.isEmpty()){
			Logger.warn("No User authentication device configured!",this);
		}
		return result;
	}

	/**
	 * the security device with the given ID, no matter whether it is registered as authentication-
	 * or data access device
	 * 
	 * @return the requested security device or null
	 */
	public SecurityDevice getSecurityDevice(String deviceID){
		SecurityDevice aDev = getDataAccessDevice(deviceID);
		if(aDev==null){
			aDev = getAuthenticationDevice(deviceID);
		}
		return aDev;
	}
	/**
	 * the PersonDataAccessDevice with the given ID or null
	 * if no such device exists
	 */
	public PersonDataAccessDevice getDataAccessDevice(String deviceID){
		try{
			return (PersonDataAccessDevice) _dataAccessDevices.get(deviceID);
		}catch(ClassCastException e){
			Logger.warn("DataAccessDevice "+deviceID+" is configured with a class that does not implement PersonDataAccessDevice",e,this);
			// no person data access device with that ID
			return null;
		}
	}

	/**
	 * the AuthenticationDevice with the given ID or null
	 * if no such device exists
	 */
	public AuthenticationDevice getAuthenticationDevice(String deviceID){
		return _authenticationDevices.get(deviceID);
	}
	
	/**
	 * Used to initially assign a device to persons who dont have one (migration from older versions)
	 * @return the configured DefaultDataAccessDevice
	 */
	public PersonDataAccessDevice getDefaultDataAccessDevice(){
		return (PersonDataAccessDevice) _defaultDataAccessDevice;
	}

	/**
	 * Used to initially assign a device to persons who dont have one (migration from older versions)
	 * @return the configured DefaultAuthenticationDevice
	 */
	public AuthenticationDevice getDefaultAuthenticationDevice(){
		return _defaultAuthenticationDevice;
	}

	/**
	 * a Set of all configured PersonDataAccessDevices which indicate to support write access
	 */
	public Set<String> getWritableSecurityDeviceIDs() {
		Iterator<?> it = getConfiguredDataAccessDeviceIDs().iterator();
		Set<String> result = new HashSet<>();
		while(it.hasNext()){
			String devID = (String)it.next();
			PersonDataAccessDevice pd = this.getDataAccessDevice(devID);
			if(!pd.isReadOnly()){
				result.add(devID);
			}
		}
		return result;
	}
	
	/**
	 * {@link BasicRuntimeModule} for {@link TLSecurityDeviceManager}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static final class Module extends TypedRuntimeModule<TLSecurityDeviceManager> {

		/**
		 * Singleton {@link Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<TLSecurityDeviceManager> getImplementation() {
			return TLSecurityDeviceManager.class;
		}

	}

	/**
	 * Prevent implementations outside of this package.
	 */
	public Unimplementable unimplementable() {
		return null;
	}
}
