/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.device;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.top_logic.base.security.device.interfaces.AuthenticationDevice;
import com.top_logic.base.security.device.interfaces.PersonDataAccessDevice;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ModuleException;

/**
 * Factory for {@link AuthenticationDevice} and {@link PersonDataAccessDevice}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class SecurityDeviceFactory extends ManagedClass {

	/**
	 * Only one instance per device (deviceID is key)
	 */
	private Map<String, TLSecurityDeviceProxy> proxis;

	SecurityDeviceFactory() {
		final TLSecurityDeviceManager tlsdMgr = TLSecurityDeviceManager.getInstance();
		Set<String> allDevices = tlsdMgr.getConfiguredAuthenticationDeviceIDs();
		allDevices.addAll(tlsdMgr.getConfiguredDataAccessDeviceIDs());
		Iterator<String> it = allDevices.iterator();
		proxis = new HashMap<>();
		while (it.hasNext()) {
			String anID = it.next();
			proxis.put(anID, new TLSecurityDeviceProxy(anID));
		}
	}

	/**
	 * @param deviceID
	 *        As configured in section security/SecurityDevices of your
	 *        configuration.
	 * 
	 * @return an Instance of this proxy for the given device, null in case the
	 *         device does not exist.
	 */
	public static TLSecurityDeviceProxy getInstance(String deviceID) {
		return Module.INSTANCE.getImplementationInstance().proxis.get(deviceID);
	}

	public static PersonDataAccessDevice getPersonAccessDevice(String deviceID) {
		return getInstance(deviceID);
	}

	public static AuthenticationDevice getAuthenticationDevice(String deviceID) {
		return getInstance(deviceID);
	}

	public static final class Module extends BasicRuntimeModule<SecurityDeviceFactory> {

		public static final Module INSTANCE = new Module();

		private static final Collection<? extends Class<? extends BasicRuntimeModule<?>>> DEPENDENCIES = Collections
				.singletonList(TLSecurityDeviceManager.Module.class);

		@Override
		public Collection<? extends Class<? extends BasicRuntimeModule<?>>> getDependencies() {
			return DEPENDENCIES;
		}

		@Override
		public Class<SecurityDeviceFactory> getImplementation() {
			return SecurityDeviceFactory.class;
		}

		@Override
		protected SecurityDeviceFactory newImplementationInstance() throws ModuleException {
			return new SecurityDeviceFactory();
		}
	}

}
