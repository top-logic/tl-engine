/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.device.interfaces;

import java.util.List;
import java.util.Map;

import com.top_logic.base.security.device.DeviceMapping;
import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.MapBinding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;

/**
 * Generic super-interface for for Security devices. 
 * 
 * A SecurityDevice describes a subsystem either used to authenticate a person against 
 * or to retrieve data according to the PersonAttributes from
 *  
 * @author    <a href="mailto:tri@top-logic.com">Tomate Richter</a>
 */
public interface SecurityDevice extends ConfiguredInstance<SecurityDevice.SecurityDeviceConfig> {
	
	/**
	 * Configuration of a {@link SecurityDevice}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface SecurityDeviceConfig extends PolymorphicConfiguration<SecurityDevice> {

		/** Name of configuration option {@link SecurityDeviceConfig#getId()}. */
		String ID_ATTRIBUTE = "id";

		/** Name of configuration option {@link SecurityDeviceConfig#getDomain()}. */
		String DOMAIN_ATTRIBUTE = "domain";

		/**
		 * Domain name of the configured {@link SecurityDevice}.
		 * 
		 * @see SecurityDevice#getDomainName()
		 */
		@Name(DOMAIN_ATTRIBUTE)
		@Nullable
		String getDomain();

		/**
		 * Id of the configured {@link SecurityDevice}.
		 * 
		 * @see SecurityDevice#getDeviceID()
		 */
		@Name(ID_ATTRIBUTE)
		@Mandatory
		String getId();

		/**
		 * {@link DeviceType} of the configured {@link SecurityDevice}.
		 */
		@Mandatory
		DeviceType getType();

		/**
		 * Property mappings.
		 */
		@MapBinding()
		Map<String, String> getMappings();

		/**
		 * Whether the configured {@link SecurityDevice} is not active in the application.
		 */
		boolean isDisabled();

	}

	/**
	 * Kind of authentication device.
	 * 
	 * @see SecurityDeviceConfig#getType()
	 */
	public enum DeviceType implements ExternallyNamed {
		/**
		 * A {@link SecurityDevice} for authentication only.
		 */
		AUTH("auth", true, false),

		/**
		 * A {@link SecurityDevice} providing user data only.
		 */
		DATA("data", false, true),

		/**
		 * A {@link SecurityDevice} for authentication and user data.
		 */
		AUTH_WITH_DATA("auth-with-data", true, true),

		/**
		 * A disabled {@link SecurityDevice}.
		 */
		DISABLED("disabled", false, false);

		private String _name;

		private boolean _supportsAuth;

		private boolean _supportsData;

		/**
		 * Creates a {@link SecurityDevice.DeviceType}.
		 */
		private DeviceType(String name, boolean supportsAuth, boolean supportsData) {
			_name = name;
			_supportsAuth = supportsAuth;
			_supportsData = supportsData;
		}

		@Override
		public String getExternalName() {
			return _name;
		}

		/**
		 * Whether the {@link SecurityDevice} supports authentication.
		 */
		public boolean supportsAuth() {
			return _supportsAuth;
		}

		/**
		 * Whether the {@link SecurityDevice} supports user dataS.
		 */
		public boolean supportsData() {
			return _supportsData;
		}
	}

	/**
	 * the ID of this device
	 */
	public String getDeviceID();
	
	/**
	 * the Domain name of this device; maybe null if not supported
	 */
	public String getDomainName();

	/**
	 * Start this {@link SecurityDevice}.
	 * 
	 * <p>
	 * Called during application startup, when {@link TLSecurityDeviceManager} is starting up.
	 * </p>
	 */
	public void startUp();

	/**
	 * Stops this {@link SecurityDevice}.
	 * 
	 * <p>
	 * Called during application shutdown, when {@link TLSecurityDeviceManager} is shutting down.
	 * </p>
	 */
	public void shutDown();

	/**
	 * @param objectClass  the type of object for which a mapping is requested. DeviceMapping::OBJ_CLASS_GENERIC for generic mapping. May not be empty
	 * @return the Mapping
	 */
	public DeviceMapping getMapping(String objectClass);

	/**
	 * @param objectClasses  the types of object for which a mapping is requested. DeviceMapping::OBJ_CLASS_GENERIC for generic mapping. May not be empty
	 * @return the Mapping
	 */
	public DeviceMapping getMapping(List<String> objectClasses);

}
