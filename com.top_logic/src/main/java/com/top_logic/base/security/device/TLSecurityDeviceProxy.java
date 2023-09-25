/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.device;

import java.util.List;

import com.top_logic.base.accesscontrol.LoginCredentials;
import com.top_logic.base.security.device.interfaces.AuthenticationDevice;
import com.top_logic.base.security.device.interfaces.PersonDataAccessDevice;
import com.top_logic.base.security.device.interfaces.SecurityDevice;
import com.top_logic.base.user.UserInterface;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.dob.DataObject;
import com.top_logic.util.license.LicenseTool;

/**
 * This class is a virtual SecurityDevice which is both, AuthenticationDevice and PersonDataAccessDevice.
 * 
 * It is instantiated for a given deviceID and then acts as proxy to the device specified by this deviceID.
 * This way it just makes life easier as it prevents the user of this class from having to aquire the SecurityDevice
 * from the TLSecurityDeviceManager each time it is needed. Aside from this does the same as if one would work on the 
 * Device directly it just shortens the way to do it
 * 
 * @author    <a href="mailto:tri@top-logic.com">Thomas Richter</a>
 */
public class TLSecurityDeviceProxy implements PersonDataAccessDevice, AuthenticationDevice {

	/**
	 * Instance of TLSecurityDeviceManager
	 */
	private TLSecurityDeviceManager sdm;

	private final String _deviceId;
	
	/**
	 * Force usage of getInstance()
	 */
	public TLSecurityDeviceProxy(String aDevID){
		_deviceId = aDevID;
		this.sdm = TLSecurityDeviceManager.getInstance();
	}
	
	@Override
	public void startUp() {
		// Ignore, life cycle of implementation device is controlled separately.
	}

	@Override
	public void shutDown() {
		// Ignore, life cycle of implementation device is controlled separately.
	}

	@Override
	public List<UserInterface> getAllUserData() {
		return accessImpl().getAllUserData();
	}

	@Override
	public UserInterface getUserData(String aName) {
		return accessImpl().getUserData(aName);
	}

	@Override
	public boolean createUserEntry(DataObject aDo) {
		return accessImpl().createUserEntry(aDo);

	}

	@Override
	public boolean updateUserData(DataObject theDo) {
		return accessImpl().updateUserData(theDo);

	}

	@Override
	public boolean deleteUserData(String aName) {
		return accessImpl().deleteUserData(aName);
	}

	@Override
	public boolean userExists(String aName) {
		return accessImpl().userExists(aName);
	}

	@Override
	public DeviceMapping getMapping(String objectClass) {
		return securityImpl().getMapping(objectClass);
	}

	@Override
	public boolean isReadOnly() {
		return accessImpl().isReadOnly();
	}

	@Override
	public boolean renameUserData(String oldID, String newID) {
		return accessImpl().renameUserData(oldID,newID);
		
	}

	@Override
	public boolean authentify(LoginCredentials login) {
		AuthenticationDevice ad = authImpl();
		if (ad != null) {
			return ad.authentify(login);
		}
		return false;
	}

	@Override
	public String getDeviceID() {
		return _deviceId;
	}

	@Override
	public String getDomainName() {
		return securityImpl().getDomainName();
	}

	@Override
	public DeviceMapping getMapping(List<String> objectClasses) {
		return securityImpl().getMapping(objectClasses);
	}

	@Override
	public SecurityDeviceConfig getConfig() {
		return null;
	}

	@Override
	public boolean allowPwdChange() {
		AuthenticationDevice ad = authImpl();
		if (ad != null) {
			return ad.allowPwdChange();
		}
		return false;
	}

	@Override
	public boolean allowExternalAuthentication() {
		if (check()) {
			return false;
		}
		return accessImpl().allowExternalAuthentication();
	}

	@FrameworkInternal
	private boolean check() {
		return !LicenseTool.getInstance().includeFeature("tl.feature.externalAuth");
	}

	private AuthenticationDevice authImpl() {
		return this.sdm.getAuthenticationDevice(getDeviceID());
	}

	private PersonDataAccessDevice accessImpl() {
		return this.sdm.getDataAccessDevice(getDeviceID());
	}

	private SecurityDevice securityImpl() {
		return this.sdm.getSecurityDevice(getDeviceID());
	}

	/**
	 * Prevent implementations outside of this package.
	 */
	public Unimplementable unimplementable() {
		return null;
	}
}
