/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.wrap.person;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.top_logic.base.accesscontrol.LoginCredentials;
import com.top_logic.base.security.device.DeviceMapping;
import com.top_logic.base.security.device.interfaces.AuthenticationDevice;
import com.top_logic.base.security.device.interfaces.PersonDataAccessDevice;
import com.top_logic.base.security.password.PasswordValidator;
import com.top_logic.base.user.UserInterface;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.dob.DataObject;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;

/**
 * Mock object for {@link PersonDataAccessDevice}
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class MockPersonDataAccessDevice implements PersonDataAccessDevice, AuthenticationDevice {

	/**
	 * Configuration for a {@link MockPersonDataAccessDevice}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AuthenticationDeviceConfig, PersonDataAccessDeviceConfig {

		@Override
		@BooleanDefault(true)
		boolean isReadOnly();

	}

	private List<UserInterface> availableUsers;

	private final UserInterface _rootUser;

	private final Config _config;

	/**
	 * Creates a new {@link MockPersonDataAccessDevice} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link MockPersonDataAccessDevice}.
	 * 
	 * @throws ConfigurationException
	 *         iff configuration is invalid.
	 */
	public MockPersonDataAccessDevice(InstantiationContext context, Config config)
			throws ConfigurationException {
		_config = config;
		this.availableUsers = new ArrayList<>();
		_rootUser = MockUserInterface.newUserInterface(PersonManager.Config.DEFAULT_SUPER_USER_NAME, getDeviceID());
		addUserData(_rootUser);
	}

	@Override
	public SecurityDeviceConfig getConfig() {
		return _config;
	}

	@Override
	public String getAuthenticationDeviceID() {
		return getDeviceID();
	}

	@Override
	public void startUp() {
		// Ignore.
	}

	@Override
	public void shutDown() {
		// Ignore.
	}

	@Override
	public String getDeviceID() {
		return _config.getId();
	}

	@Override
	public String getDomainName() {
		return _config.getDomain();
	}

	@Override
	public DeviceMapping getMapping(String objectClass) {
		if (true)
			throw new UnsupportedOperationException("Method is not implemented");
		return null;
	}

	@Override
	public DeviceMapping getMapping(List objectClasses) {
		if (true)
			throw new UnsupportedOperationException("Method is not implemented");
		return null;
	}

	@Override
	public List<UserInterface> getAllUserData() {
		return availableUsers;
	}

	public void setAllUserData(List<UserInterface> availableUsers) {
		this.availableUsers = availableUsers;
	}

	public void addUserData(UserInterface availableUser) {
		availableUsers.add(availableUser);
	}

	public UserInterface getRootUser() {
		return _rootUser;
	}

	@Override
	public UserInterface getUserData(String name) {
		for (UserInterface user : availableUsers) {
			if (user.getUserName().equals(name)) {
				return user;
			}
		}
		return null;
	}

	@Override
	public boolean createUserEntry(DataObject aDo) {
		if (true)
			throw new UnsupportedOperationException("Method is not implemented");
		return false;
	}

	@Override
	public boolean updateUserData(DataObject theDo) {
		if (true)
			throw new UnsupportedOperationException("Method is not implemented");
		return false;
	}

	@Override
	public boolean renameUserData(String oldID, String newID) {
		if (true)
			throw new UnsupportedOperationException("Method is not implemented");
		return false;
	}

	@Override
	public boolean deleteUserData(String aName) {
		Iterator<UserInterface> allUsers = availableUsers.iterator();
		while (allUsers.hasNext()) {
			UserInterface user = allUsers.next();
			if (StringServices.equals(user.getUserName(), aName)) {
				allUsers.remove();
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean userExists(String aName) {
		Iterator<UserInterface> allUsers = availableUsers.iterator();
		while (allUsers.hasNext()) {
			UserInterface user = allUsers.next();
			if (StringServices.equals(user.getUserName(), aName)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isReadOnly() {
		return _config.isReadOnly();
	}

	@Override
	public boolean allowExternalAuthentication() {
		return _config.isAllowExtAuthentication();
	}

	@Override
	public boolean authentify(LoginCredentials login) {
		throw new UnsupportedOperationException("Method is not implemented");
	}

	@Override
	public boolean allowPwdChange() {
		return false;
	}

	@Override
	public void setPassword(Person account, char[] password) {
		throw new UnsupportedOperationException("Method is not implemented");
	}

	@Override
	public void expirePassword(Person account) {
		throw new UnsupportedOperationException("Method is not implemented");
	}

	@Override
	public PasswordValidator getPasswordValidator() {
		throw new UnsupportedOperationException("Method is not implemented");
	}

	@Override
	public boolean isPasswordChangeRequested(Person account, char[] password) {
		return false;
	}
}
