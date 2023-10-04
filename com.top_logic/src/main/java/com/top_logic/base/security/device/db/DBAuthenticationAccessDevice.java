/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.device.db;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.top_logic.base.accesscontrol.LoginCredentials;
import com.top_logic.base.security.device.AbstractSecurityDevice;
import com.top_logic.base.security.device.interfaces.AuthenticationDevice;
import com.top_logic.base.security.device.interfaces.PersonDataAccessDevice;
import com.top_logic.base.security.password.hashing.PasswordHashingService;
import com.top_logic.base.user.UserInterface;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.XMLProperties;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.core.workspace.Environment;
import com.top_logic.basic.db.schema.properties.DBProperties;
import com.top_logic.basic.encryption.SecureRandomService;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.meta.MOStructureImpl;
import com.top_logic.dob.simple.ExampleDataObject;
import com.top_logic.knowledge.service.CommitHandler;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.util.AbstractStartStopListener;
import com.top_logic.util.TLContext;

/**
 * AuthenticationDevice and PersonDataAccessDevice for the DBUserRepository.
 * 
 * This implementation represents as well an AuthenticationDevice as an PersonDataAccessDevice
 * for the <i>TopLogic</i> internal DBUserRepository
 * Typically this class will be instantiated by the TLSecurityDeviceManager, which also
 * has control of the number of created instances
 * 
 * @author    <a href="mailto:tri@top-logic.com">Thomas Richter</a>
 */
public class DBAuthenticationAccessDevice extends AbstractSecurityDevice implements AuthenticationDevice {

	private static final String INITIALIZED_VALUE = "true";
	private static final String INITIALIZED_PROPERTY = "DBAuthenticationAccessDevice.initialized";

	/**
	 * Configuration options for {@link DBAuthenticationAccessDevice}.
	 */
	public interface Config extends AbstractSecurityDevice.Config, PersonDataAccessDevice.PersonDataAccessDeviceConfig,
			AuthenticationDevice.AuthenticationDeviceConfig {

		/**
		 * Name of the resource with initial users to load.
		 */
		@ListBinding(attribute = "name")
		List<String> getInitialUserResources();
	}

	/**
	 * The own instance of the DBUserRepository, used to access the Database
	 */
	private DBUserRepository _repository;

	private final KnowledgeBase _kb;

	private MOStructureImpl _userMO;

	/**
	 * Creates a new {@link DBAuthenticationAccessDevice} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link DBAuthenticationAccessDevice}.
	 */
	public DBAuthenticationAccessDevice(InstantiationContext context, Config config) {
		super(context, config);
		_kb = PersistencyLayer.getKnowledgeBase();
		try {
			_userMO = DBUserRepository.getUserMetaObject(_kb);
			_repository = new DBUserRepository(_userMO);
		} catch (UnknownTypeException ex) {
			context.error("Unable to get DBUserRepository.", ex);
		}
	}

	@Override
	public void startUp() {
		super.startUp();

		try {
			ConnectionPool connectionPool = KBUtils.getConnectionPool(_kb);

			PooledConnection con = connectionPool.borrowWriteConnection();
			try {
				String initialized = DBProperties.getProperty(con, DBProperties.GLOBAL_PROPERTY, INITIALIZED_PROPERTY);
				boolean passwordReset =
					Environment.getSystemPropertyOrEnvironmentVariable("tl_reset_password", null) != null;

				if (INITIALIZED_VALUE.equals(initialized) && !passwordReset) {
					return;
				}

				String superUserName = lookupSuperUserName();
				if (passwordReset) {
					DataObject root = _repository.searchUser(connectionPool, superUserName);
					if (root != null) {
						setupPassword(root);
						_repository.updateUser(connectionPool, root);
					}
					return;
				}

				for (String resourceConf : ((Config) getConfig()).getInitialUserResources()) {
					String input = resourceConf;
					if (StringServices.isEmpty(input)) {
						continue;
					}
					try {
						// read user from specified file
						List<? extends ExampleDataObject> theListOfAllUsers = loadUserData(input);
						Iterator<? extends ExampleDataObject> iter = theListOfAllUsers.iterator();
						while (iter.hasNext()) {
							ExampleDataObject user = iter.next();

							initPassword(user, superUserName);

							try {
								_repository.createUser(con, user);
							} catch (SQLException | DataObjectException ex) {
								Logger.error(
									"Creation of user '" + user.getMap() + "' failed (loaded from '" + input + "').",
									ex, DBAuthenticationAccessDevice.class);
							}
						}
					} catch (IOException ex) {
						Logger.error("Unable access users file : " + input, ex, DBAuthenticationAccessDevice.class);
					}
				}

				DBProperties.setProperty(con, DBProperties.GLOBAL_PROPERTY, INITIALIZED_PROPERTY, INITIALIZED_VALUE);
				con.commit();
			} finally {
				connectionPool.releaseWriteConnection(con);
			}
		} catch (SQLException ex) {
			Logger.error("Database access failed.", ex, DBAuthenticationAccessDevice.class);
		}
	}

	private void initPassword(DataObject user, String superUserName) {
		if (superUserName.equals(user.getAttributeValue(PersonAttributes.USER_NAME))) {
			setupPassword(user);
		} else {
			// Drop insecure password.
			if (user.getAttributeValue(PersonAttributes.PASSWORD) != null) {
				Logger.info(
					"Dropped insecure static password for user '"
						+ user.getAttributeValue(PersonAttributes.USER_NAME) + "'.",
					DBAuthenticationAccessDevice.class);
			}

			user.setAttributeValue(PersonAttributes.PASSWORD, DBUserRepository.NO_PASSWORD);
		}
	}

	private void setupPassword(DataObject user) {
		String initialPassword =
			Environment.getSystemPropertyOrEnvironmentVariable("tl_initial_password", null);

		String message;
		Object userName = user.getAttributeValue(PersonAttributes.USER_NAME);
		if (initialPassword == null) {
			initialPassword = SecureRandomService.getInstance().getRandomString();
			message = "Initial password for '" + userName + "': " + initialPassword;
			Logger.info(message, DBAuthenticationAccessDevice.class);
		} else {
			message = "Initial password for '" + userName + "' set up from environment variable.";
			Logger.info(message, DBAuthenticationAccessDevice.class);
		}
		TLContext.getContext()
			.set(AbstractStartStopListener.PASSWORD_INITIALIZATION_MESSAGE, message);

		user.setAttributeValue(PersonAttributes.PASSWORD,
			PasswordHashingService.getInstance().createHash(initialPassword.toCharArray()));
	}

	private String lookupSuperUserName() {
		String superUserName;
		try {
			PersonManager.Config accountConfig =
				(PersonManager.Config) ApplicationConfig.getInstance().getServiceConfiguration(PersonManager.class);
			superUserName = accountConfig.getSuperUserName();
		} catch (ConfigurationException ex) {
			Logger.error("Failed reading configuration.", ex, DBAuthenticationAccessDevice.class);
			superUserName = PersonManager.Config.DEFAULT_SUPER_USER_NAME;
		}
		return superUserName;
	}

	/**
	 * Loads user data from a file.
	 * 
	 * @param resourceName
	 *        The name of the file to be read.
	 */
	private List<? extends ExampleDataObject> loadUserData(final String resourceName) throws IOException {
		List<ExampleDataObject> result = new ArrayList<>();

		BinaryData theFile = FileManager.getInstance().getData(resourceName);
		Logger.info("Loading initial users from " + theFile.getName(), DBAuthenticationAccessDevice.class);
		XMLProperties fileProperties = XMLProperties.createXMLProperties(theFile);
		List<?> theServices = fileProperties.getServiceNames();

		for (Object name : theServices) {
			Properties theInfo = fileProperties.getProperties((String) name);
			if (theInfo != null) {
				ExampleDataObject theObject = new ExampleDataObject((Map) theInfo);
				result.add(theObject);
			}
		}
		return (result);
	}

	@Override
	public boolean updateUserData(DataObject anObject) {
		String aName = "";
		try {
			aName = (String) anObject
					.getAttributeValue(UserInterface.USER_NAME);
			anObject.setAttributeValue(Person.DATA_ACCESS_DEVICE_ID,this.getDeviceID()); //just make sure, objects deviceId reflects this device
			if (_kb instanceof CommitHandler) {
				CommitableDBUserRepository.getInstance((CommitHandler) _kb, _repository).updateUser(anObject);
            } else {
				_repository.updateUser(KBUtils.getConnectionPool(_kb), anObject);
            }
			return true;
        } catch (DataObjectException dox) {
            Logger.error("Unable to update entry \"" + aName + "\"", dox, this);
		} catch (SQLException sqx) {
			Logger.error("Unable to update entry \"" + aName + "\"", sqx, this);
		}
		return false;
	}

	@Override
	public boolean authentify(LoginCredentials login) {
		try {
			return this._repository.checkPassword(_kb, login);
		} catch (SQLException ex) {
			Logger.error("Could not authentify against DB", ex, this);
		}
		return false;
	}
}