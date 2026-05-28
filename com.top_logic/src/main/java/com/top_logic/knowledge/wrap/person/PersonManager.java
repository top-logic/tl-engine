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
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.knowledge.service.KBBasedManagedClass;
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
	ApplicationConfig.Module.class,
	LockService.Module.class,
	TLSecurityDeviceManager.Module.class,
	InitialGroupManager.Module.class,
	ResourcesModule.Module.class,
})
public class PersonManager extends KBBasedManagedClass<PersonManager.Config> {

	/**
	 * Configuration for the account manager.
	 */
	public interface Config extends KBBasedManagedClass.Config<PersonManager> {

		/**
		 * Configuration name for {@link #getUserNamePattern()}.
		 */
		String XML_KEY_PATTERN = "person-name-pattern";

		/**
		 * Default value for {@link #getUserNamePattern()}.
		 */
		String DEFAULT_PATTERN = "[a-zA-Z]\\w*";

		/**
		 * Configuration name for {@link #getPersonNameMaxLength()}.
		 */
		String XML_KEY_LENGTH = "person-name-max-length";

		/**
		 * Default value for {@link #getPersonNameMaxLength()}.
		 */
		int DEFAULT_MAX_PERSON_NAME_LENGTH = 128;

		/**
		 * Configuration name for {@link #getSuperUserName()}.
		 */
		String XML_KEY_SUPERUSERNAME = "super-user-name";

		/**
		 * Default value for {@link #getSuperUserName()}.
		 */
		String DEFAULT_SUPER_USER_NAME = "root";

		/**
		 * Configuration name for {@link #getAnonymousUserName()}.
		 */
		String ANONYMOUS_USER_NAME = "anonymous-user-name";

		/**
		 * Default value for {@link #getAnonymousUserName()}.
		 */
		String DEFAULT_ANONYMOUS_USER_NAME = "anonymous";

		/**
		 * Pattern, which user names must follow.
		 */
		@FormattedDefault(DEFAULT_PATTERN)
		@Format(RegExpValueProvider.class)
		@Name(XML_KEY_PATTERN)
		Pattern getUserNamePattern();

		/**
		 * The maximum length of a person name.
		 */
		@IntDefault(DEFAULT_MAX_PERSON_NAME_LENGTH)
		@Name(XML_KEY_LENGTH)
		int getPersonNameMaxLength();

		/**
		 * The name of the super user.
		 */
		@StringDefault(DEFAULT_SUPER_USER_NAME)
		@Name(XML_KEY_SUPERUSERNAME)
		String getSuperUserName();

		/**
		 * The name of the anonymous user.
		 */
		@StringDefault(DEFAULT_ANONYMOUS_USER_NAME)
		@Name(ANONYMOUS_USER_NAME)
		String getAnonymousUserName();
	}

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
		super(context, config);
	}

	/**
	 * The system root. Should always be there so should never return null.
	 */
	public Person getRoot() {
		return Person.byName(kb(), getSuperUserName());
	}

	/**
	 * The name of the system root. Should always be there so should never return null.
	 */
	public String getSuperUserName() {
		return getConfig().getSuperUserName();
	}

	/**
	 * The anonymous user. This user is used when navigating the application without login.
	 */
	public Person getAnonymous() {
		return Person.byName(kb(), getAnonymousUserName());
	}

	private String getAnonymousUserName() {
		return getConfig().getAnonymousUserName();
	}

	/**
	 * true if the given name is valid for usage as TL person name according to the configured pattern
	 */
	public boolean validatePersonName(String aPersonName) {
		if (!StringServices.isEmpty(aPersonName) && aPersonName.length() < this.getPersonNameMaxLength()) {
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
		return this.getConfig().getUserNamePattern();
	}

	public int getPersonNameMaxLength() {
		return this.getConfig().getPersonNameMaxLength();
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
			Person p = Person.byName(kb(), name);
			Group g = Group.getGroupByName(kb(), name);
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
		ensureAnonymousAccount();
	}

	private void ensureAnonymousAccount() {
		String loginName = getAnonymousUserName();
		Person existingAccount = Person.byName(loginName);
		if (existingAccount == null) {
			try (Transaction tx = kb().beginTransaction(I18NConstants.CREATED_ANONYMOUS_ACCOUNT)) {
				// No login for anonymous user.
				AuthenticationDevice device = null;
				Person anonymous = Person.create(kb(), loginName, device);
				anonymous.setRestrictedUser(true);

				tx.commit();
			}
		}
	}

	private void ensureRootAccount() {
		String loginName = getSuperUserName();
		Person existingAccount = Person.byName(kb(), loginName);
		if (existingAccount == null) {
			try (Transaction tx = kb().beginTransaction(I18NConstants.CREATED_ROOT_ACCOUNT)) {
				AuthenticationDevice device = TLSecurityDeviceManager.getInstance().getDefaultAuthenticationDevice();
				Person root = Person.create(kb(), loginName, device);
				root.setAdmin(true);

				setupRootPassword(root, loginName);

				tx.commit();
			}
		} else {
			boolean passwordReset =
				Environment.getSystemPropertyOrEnvironmentVariable("tl_reset_password", null) != null;
			if (passwordReset) {
				try (Transaction tx = kb().beginTransaction(I18NConstants.RESETTING_ROOT_PASSWORD)) {
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
