/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.device;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import com.top_logic.base.security.device.interfaces.SecurityDevice.SecurityDeviceConfig;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;

/**
 * Translate internal attribute names to name used in external Devices.
 *
 * A Device Mapping can translate internal attribute names to their corresponding names as used in an external DataAccessDevice
 *  
 * @author    <a href="mailto:tri@top-logic.com">Thomat Trichter</a>
 */
public class DeviceMapping extends Properties {
	
    /**
	 * The object class to be used to create a generic, objectclass independent mapping
	 */
	public static final String OBJ_CLASS_GENERIC="genericMapping";
	
	public DeviceMapping() {
		super();
	}
	
	/**
	 * @param mappings
	 *        See {@link SecurityDeviceConfig#getMappings()}.
	 * @param objectClass
	 *        the type of object for which this mapping is requested - may not be empty
	 */
	public DeviceMapping(Map<String, String> mappings, String objectClass) {
		String fileName = mappings.get(objectClass);
		
		if(StringServices.isEmpty(fileName)){
			Logger.debug("No mapping configured for object class '" + objectClass + ".", DeviceMapping.class);
		    return;
		}
		
		try {
			InputStream input = FileManager.getInstance().getStream(fileName);
			try {
				this.load(input);
			} finally {
				input.close();
			}
		} catch (Exception ex) {
			throw new IllegalStateException(
				"Failed to load mapping file '" + fileName + "' for object class '" + objectClass + ".", ex);
		}
	}

	/**
	 * Maps Attributes as given in the configured mapping file.
	 * Be advised that this mapping should be distinct in this direction, otherwise 
	 * the first possible match is returned.
     * 
	 * @param attributeName -- attribute name to match, must not be null
	 * @param defaultReturn -- return value if no match is found, may be null
	 */ 
	public String mapAttribute(String attributeName, String defaultReturn) {
		return this.getProperty(attributeName, defaultReturn);
	}

	/**
	 * Merge this DeviceMapping with the given one
	 * 
	 * @param aMapping the other DeviceMapping
	 * @param override if true the other mappings override the current ones
	 */
	public void mergeWith(DeviceMapping aMapping, boolean override) {
		if (aMapping == null) {
			return;
		}
		
		if (override) {
			this.putAll(aMapping);
		}
		else {
			for (Iterator theKeys=aMapping.keySet().iterator(); theKeys.hasNext();) {
				String theKey = (String) theKeys.next();
				if (!this.containsKey(theKey)) {
					this.setProperty(theKey, aMapping.getProperty(theKey));
				}
			}
		}
	}

}
