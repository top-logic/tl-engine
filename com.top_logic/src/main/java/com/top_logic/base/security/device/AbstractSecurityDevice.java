/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.device;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.top_logic.base.security.device.interfaces.SecurityDevice;
import com.top_logic.basic.config.InstantiationContext;

/**
 * This is an abstract implementation for a SecurityDevice
 * Eases implementation of new AuthDevices as configuration access and stuff
 * is already taken care of
 * @author    <a href="mailto:tri@top-logic.com">Thomat Trichter</a>
 */
public abstract class AbstractSecurityDevice implements SecurityDevice {
	
	/**
	 * Configuration of an {@link AbstractSecurityDevice}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends SecurityDeviceConfig {

	}

	private Map		mappings;

	private final Config _config;

	/**
	 * Creates a new {@link AbstractSecurityDevice} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link AbstractSecurityDevice}.
	 * 
	 */
	public AbstractSecurityDevice(InstantiationContext context, Config config) {
		_config = config;
		mappings = new HashMap();
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public void startUp() {
		// No action.
	}

	@Override
	public void shutDown() {
		// No action.
	}

	/**
	 * A type specific mapping for this device and the given object class
	 */
	@Override
	public synchronized DeviceMapping getMapping(String objectClass) {
		return this.getMapping(Collections.singletonList(objectClass));
	}

	@Override
	public DeviceMapping getMapping(List<String> objectClasses) {
		return getMappingFor(this.mappings, objectClasses, _config.getMappings());
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
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.top_logic.base.security.device.interfaces.SecurityDevice#getDomainName()
	 */
	@Override
	public String getDomainName() {
		return _config.getDomain();
	}

	/*
	 *  (non-Javadoc)
	 * @see com.top_logic.base.security.device.interfaces.SecurityDevice#getDeviceID()
	 */
	@Override
	public String getDeviceID() {
		return _config.getId();
	}

}