/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.person;

import java.util.regex.Pattern;

import com.top_logic.base.locking.LockService;
import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.base.security.device.interfaces.AuthenticationDevice;
import com.top_logic.base.services.InitialGroupManager;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Environment;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.format.RegExpValueProvider;
import com.top_logic.basic.encryption.SecureRandomService;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.util.AbstractStartStopListener;
import com.top_logic.util.TLContext;
import com.top_logic.util.license.LicenseTool;

/**
 * TopLogic account management.
 *
 * @author <a href="mailto:tri@top-logic.com">Thomas Richter </a>
 */
@ServiceDependencies({
	PersistencyLayer.Module.class, 
	ApplicationConfig.Module.class,
	LockService.Module.class,
	TLSecurityDeviceManager.Module.class,
	InitialGroupManager.Module.class,
	ResourcesModule.Module.class,
})
public class PersonManager extends ManagedClass {

	public interface Config extends ServiceConfiguration<PersonManager> {

		/**
		 * @see #getUserNamePattern()
		 */
		static final String XML_KEY_PATTERN = "person-name-pattern";

		/**
		 * default of @see #getUserNamePattern()
		 */
		static final String DEFAULT_PATTERN = "[a-zA-Z]\\w*";

		/**
		 * @see #getPersonNameMaxLength()
		 */
		static final String XML_KEY_LENGTH = "person-name-max-length";

		/**
		 * default of @see #getPersonNameMaxLength()
		 */
		static final int DEFAULT_MAX_PERSON_NAME_LENGTH = 128;

		/**
		 * @see #getSuperUserName()
		 */
		static final String XML_KEY_SUPERUSERNAME = "super-user-name";

		/**
		 * default of @see #getSuperUserName()
		 */
		static final String DEFAULT_SUPER_USER_NAME = "root";

		/**
		 * pattern, which user names must follow
		 */
		@FormattedDefault(DEFAULT_PATTERN)
		@Format(RegExpValueProvider.class)
		@Name(XML_KEY_PATTERN)
		Pattern getUserNamePattern();

		/**
		 * maximum length of person name
		 */
		@IntDefault(DEFAULT_MAX_PERSON_NAME_LENGTH)
		@Name(XML_KEY_LENGTH)
		int getPersonNameMaxLength();

		/**
		 * name of the super user
		 */
		@StringDefault(DEFAULT_SUPER_USER_NAME)
		@Name(XML_KEY_SUPERUSERNAME)
		String getSuperUserName();
	}

	private final Pattern userNamePattern;

	private final int maxPersonNameLength;

	private final String superUserName;
	
	/**
	 * Creates a {@link PersonManager} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public PersonManager(InstantiationContext context, Config config) {
		userNamePattern = config.getUserNamePattern();
		maxPersonNameLength = config.getPersonNameMaxLength();
		superUserName = config.getSuperUserName();
	}

	/**
	 * The system root. Should always be there so should never return null.
	 */
	public Person getRoot() {
		return Person.byName(superUserName);
	}

	/**
	 * The name of the system root. Should always be there so should never return null.
	 */
	public String getSuperUserName() {
		return superUserName;
	}

	/**
	 * true if the given name is valid for usage as TL person name according to the configured pattern
	 */
	public boolean validatePersonName(String aPersonName) {
		if (!StringServices.isEmpty(aPersonName) && aPersonName.length() < this.maxPersonNameLength) {
			try {
				return getPersonNamePattern().matcher(aPersonName).matches();
			} catch (Exception ex) {
				Logger.error("Could not validate person name: " + aPersonName, ex, this);
				return false;
			}
		} else {
			return false;
		}
	}

	public Pattern getPersonNamePattern() {
		return this.userNamePattern;
	}

	public int getPersonNameMaxLength() {
		return this.maxPersonNameLength;
	}
	
	/**
	 * Note - a Person KO without user is considered !alive. That state isn't really used anymore as
	 * person KOs will be physically deleted when the user is removed. However, this strategy is
	 * still supported, so a username used by a person that is !alive must not be considered as
	 * used. In this case also a representative with the given name group would exist, still this
	 * method would return false, as it would be ok to reuse the username in this scenario.
	 * 
	 * @param name
	 *        The name of the person to check.
	 * @return true if either an alive person with given name or a group with given name exist
	 */
	public boolean personNameAlreadyUsed(String name) {
		{
			Person p = Person.byName(name);
			Group g = Group.getGroupByName(name);
			if (p == null && g != null) {
				return true;
			} else if (p != null && p.isAlive()) {
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * Initializes the {@link Person#getUser()} property with a new user info object, if the current
	 * system configuration supports user information (the contact module is linked to the
	 * application).
	 * 
	 * @param account
	 *        The account to initialize.
	 */
	public void initUser(Person account) {
		// No user information available
	}

	/**
	 * singleton instance of this manager
	 */
	public static final PersonManager getManager() {
		return Module.INSTANCE.getImplementationInstance();
	}

	@Override
	protected void startUp() {
		super.startUp();
		internalStartUp();
		ensureRootAccount();
	}

	private void ensureRootAccount() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();

		String loginName = getSuperUserName();
		Person existingAccount = Person.byName(loginName);
		if (existingAccount == null) {
			try (Transaction tx = kb.beginTransaction(I18NConstants.CREATED_ROOT_ACCOUNT)) {
				AuthenticationDevice device = TLSecurityDeviceManager.getInstance().getDefaultAuthenticationDevice();
				String deviceID = device.getDeviceID();
				Person root = Person.create(kb, loginName, deviceID);
				root.setAdmin(true);

				setupRootPassword(root, loginName);

				tx.commit();
			}
		} else {
			boolean passwordReset =
				Environment.getSystemPropertyOrEnvironmentVariable("tl_reset_password", null) != null;
			if (passwordReset) {
				try (Transaction tx = kb.beginTransaction(I18NConstants.RESETTING_ROOT_PASSWORD)) {
					setupRootPassword(existingAccount, loginName);
					tx.commit();
				}
			}
		}

	}

	private void setupRootPassword(Person root, String loginName) {
		AuthenticationDevice device = root.getAuthenticationDevice();
		if (!device.allowPwdChange()) {
			Logger.info("No password setting for root user allowed.", PersonManager.class);
			return;
		}
		String initialPassword =
			Environment.getSystemPropertyOrEnvironmentVariable("tl_initial_password", null);

		String message;
		if (initialPassword == null) {
			initialPassword = SecureRandomService.getInstance().getRandomString();
			message = "Initial password for '" + loginName + "': " + initialPassword;
			Logger.info(message, PersonManager.class);
		} else {
			message = "Initial password for '" + loginName + "' set up from environment variable.";
			Logger.info(message, PersonManager.class);
		}
		TLContext.getContext().set(AbstractStartStopListener.PASSWORD_INITIALIZATION_MESSAGE, message);

		device.setPassword(root, initialPassword.toCharArray());
	}

	private void internalStartUp() {
		LicenseTool.init();
	}

	public static final class Module extends TypedRuntimeModule<PersonManager> {

		public static final Module INSTANCE = new Module();

		@Override
		public Class<PersonManager> getImplementation() {
			return PersonManager.class;
		}
	}

}
