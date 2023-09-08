/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.device;

import com.top_logic.base.security.device.interfaces.PersonDataAccessDevice;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.dob.DataObject;

/**
 * A SecurityDevice that is both, an PersonDataAccessDevice and a SecurityDevice.
 * 
 * This is an abstract implementation for an PersonDataAccessDevice
 * Eases implementation of new AuthDevices as configuration access and stuff
 * is already taken care of
 * 
 * @author    <a href="mailto:tri@top-logic.com">Thomat Trichter</a>
 */
public abstract class AbstractPersonDataAccessDevice extends AbstractSecurityDevice implements PersonDataAccessDevice {
	
	/**
	 * Configuration of an {@link AbstractPersonDataAccessDevice}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractSecurityDevice.Config, PersonDataAccessDeviceConfig {

		// Sum interface.

	}

	/**
	 * Creates a new {@link AbstractPersonDataAccessDevice} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link AbstractPersonDataAccessDevice}.
	 * 
	 */
	public AbstractPersonDataAccessDevice(InstantiationContext context, Config config) {
		super(context, config);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.top_logic.base.security.device.interfaces.PersonDataAccessDevice#createUserEntry(com.top_logic.mig.dataobjects.DataObject)
	 */
	@Override
	public boolean createUserEntry(DataObject aDo) {
		throw new IllegalStateException("Method only supported when isReadOnly() == false");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.top_logic.base.security.device.interfaces.PersonDataAccessDevice#deleteUserData(java.lang.String)
	 */
	@Override
	public boolean deleteUserData(String aName) {
		throw new IllegalStateException("Method only supported when isReadOnly() == false");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.top_logic.base.security.device.interfaces.PersonDataAccessDevice#isReadOnly()
	 */
	@Override
	public boolean isReadOnly() {
		return config().isReadOnly();
	}

	private Config config() {
		return (Config) getConfig();
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.top_logic.base.security.device.interfaces.PersonDataAccessDevice#allowExternalAuthentication()
	 */
	@Override
	public boolean allowExternalAuthentication() {
		return config().isAllowExtAuthentication();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.top_logic.base.security.device.interfaces.PersonDataAccessDevice#renameUserData(java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public boolean renameUserData(String oldID, String newID) {
		throw new IllegalStateException("Method only supported when isReadOnly() == false");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.top_logic.base.security.device.interfaces.PersonDataAccessDevice#updateUserData(com.top_logic.mig.dataobjects.DataObject)
	 */
	@Override
	public boolean updateUserData(DataObject theDo) {
		throw new IllegalStateException("Method only supported when isReadOnly() == false");
	}
	


	/*
	 *  (non-Javadoc)
	 * @see com.top_logic.base.security.device.interfaces.PersonDataAccessDevice#userExists(java.lang.String)
	 */
	@Override
	public boolean userExists(String aName) {
		return this.getUserData(aName) != null;
	}
}