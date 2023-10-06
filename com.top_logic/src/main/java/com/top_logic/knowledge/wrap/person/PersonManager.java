/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.person;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import com.top_logic.base.locking.Lock;
import com.top_logic.base.locking.LockService;
import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.base.security.device.interfaces.PersonDataAccessDevice;
import com.top_logic.base.services.InitialGroupManager;
import com.top_logic.base.user.UserInterface;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.format.RegExpValueProvider;
import com.top_logic.basic.message.Message;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.person.deletion.AllPersonDeletionPolicy;
import com.top_logic.knowledge.wrap.person.deletion.PersonDeletionPolicy;
import com.top_logic.tool.boundsec.wrap.Group;
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
		 * @see #getTokenTimeout()
		 */
		static final String XML_KEY_TOKEN_TIMEOUT = "token-timeout";

		/**
		 * default of @see for #getTokenTimeout()
		 */
		static final int DEFAULT_TOKEN_TIMEOUT = 100000;

		/** @see #getPersonDeletionPolicy() */
		String PERSON_DELETION_POLICY_PROPERTY = "person-deletion-policy";

		/**
		 * Policy describing which persons without user should be deleted.
		 */
		@InstanceDefault(AllPersonDeletionPolicy.class)
		@InstanceFormat
		@Name(PERSON_DELETION_POLICY_PROPERTY)
		PersonDeletionPolicy getPersonDeletionPolicy();

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

		/**
		 * token timeout
		 */
		@IntDefault(DEFAULT_TOKEN_TIMEOUT)
		@Name(XML_KEY_TOKEN_TIMEOUT)
		int getTokenTimeout();
	}

	/**
	 * Timeout of this token, in case this task does not release it itself 10 minutes should be
	 * enough here.
	 */
	public static final long TIMEOUT_INTERVAL_LENGTH = 1000 * 60 * 10;

	/** Token ID for this task. */
	public static final String TOKEN_ID = "RefreshUsers";

	private final PersonDeletionPolicy _personDeletionPolicy;

	private final Pattern userNamePattern;

	private final int maxPersonNameLength;

	private final String superUserName;
	
	private final int tokenTimeout;

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
		tokenTimeout = config.getTokenTimeout();
		_personDeletionPolicy = config.getPersonDeletionPolicy();
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
	 * Create a new Person and User with given Name in default KnowledgeBase.
	 * 
	 * @param securityDeviceName
	 *            when null the default security will be assumed.
	 * @return null when a person with the name already exists, the new person
	 *         otherwise.
	 */
	/**
	 * Create a new Person and User with given Name in default KnowledgeBase.
	 * 
	 * @return null when a person with the name already exists, the new person
	 *         otherwise.
	 */
	public Person createPerson(String aName, String authenticationDeviceID, Boolean isRestricted) {
		return createPerson(aName, PersistencyLayer.getKnowledgeBase(), authenticationDeviceID, isRestricted);
	}

	/**
	 * Create a new external Person and User with given Name in given
	 * KnowledgeBase.
	 * 
	 * @return null when a person with the name already exists, the new person
	 *         otherwise.
	 */
	public Person createPerson(String aName, KnowledgeBase aKnowledgeBase, String authenticationDeviceID,
			Boolean isRestricted) {
		return internalCreatePerson(aName, aKnowledgeBase, authenticationDeviceID, isRestricted);
	}

	private Person internalCreatePerson(String aName, KnowledgeBase aKnowledgeBase, String authenticationDeviceID,
			Boolean isRestricted) {
		if (!validatePersonName(aName)) {
			int len = aName.length();
			int max = getPersonNameMaxLength();
			if (len < max) {
				throw new IllegalArgumentException(
					"Name '" + aName + "' does not match '" + getPersonNamePattern().pattern() + "'");
			} else {
				throw new IllegalArgumentException("Name '" + aName + "' to long " + len + ">" + max);
			}
		}
		Person existing = Person.byName(aKnowledgeBase, aName);
		if (existing != null) {
			throw new IllegalStateException("Person '" + aName + "' already exists");
		}

		return
			getOrCreatePersonForUser(aName,
				LicenseTool.moreUsersAllowed(LicenseTool.getInstance().getLicense(), isRestricted),
				authenticationDeviceID);
	}

	/**
	 * Re-Initializes the user cache upon startup and whenever called thereafter. Returns the
	 * {@link Lock} that was used to synch this call or <code>null</code> if the token was not
	 * available at the time of the call. if <code>null</code> is returned, this method does
	 * nothing. If the {@link Lock} is returned, the refresh was performed successfully and the
	 * caller now is responsible of releasing the {@link Lock}.
	 * 
	 * @return the {@link Lock} used for initializing
	 */
	public Lock initUserMap() {
		Lock lock = null;
		try {
			lock = LockService.getInstance().createLock("initUsers", (Object) null);
			if (lock != null) {
				// try to lock for them
				if (!lock.tryLock(getTokenTimeout())) {
					// throw new
					// TopLogicException(TokenManagedFormComponent.class,
					// "alreadylocked", null);
					return null;
				}
			}
		} catch (Exception ex) {
			Logger.error("TokenContext not available", ex, this);
		}
		if (lock == null) {
			return null;
		}
		try {
			_initUserMap();
		} catch (Exception e) {
			Logger.error("Error reiniting usercache", e, this);
		}
		return lock;
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
	 * token timeout
	 */
	protected final int getTokenTimeout() {
		return tokenTimeout;
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
	}

	private void internalStartUp() {
		Message commitMessage = Messages.STARTUP_PERSON_MANAGER.fill();
		updatePersons(commitMessage);

		LicenseTool.init();
	}

	/**
	 * Update all persons
	 */
	public void updatePersons(Message commitMessage) {
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction(commitMessage)) {
			Lock lock = initUserMap();
			if (lock != null) {
				lock.unlock();
			}
			tx.commit();
		} catch (KnowledgeBaseException ex) {
			String msg = "Unable to commit transaction starting PersonManager.";
			throw new KnowledgeBaseRuntimeException(msg, ex);
		}
	}

	public static final class Module extends TypedRuntimeModule<PersonManager> {

		public static final Module INSTANCE = new Module();

		@Override
		public Class<PersonManager> getImplementation() {
			return PersonManager.class;
		}
	}

	/**
	 * Create a new PersonKO in given KnowledgeBase. Does NOT check if such a person already exists!
	 * Note: No user object will be created by this method
	 * 
	 * @param aName
	 *        the person (login) name
	 * @param aKnowledgeBase
	 *        the KBase
	 * @return the created person
	 */
	private Person createPersonInKBOnly(String aName, KnowledgeBase aKnowledgeBase, String authenticationDeviceID) {
		KnowledgeObject theKO = aKnowledgeBase.createKnowledgeObject(Person.OBJECT_NAME);
		theKO.setAttributeValue(AbstractWrapper.NAME_ATTRIBUTE, aName);
		Person result = theKO.getWrapper();
		result.setAuthenticationDeviceID(authenticationDeviceID);
		return result;
	}

	private String getDefaultAuthenticationDeviceID(String dataAccessDeviceID) {
		String authenticationDeviceID = "";
		TLSecurityDeviceManager securityDevices = TLSecurityDeviceManager.getInstance();
		if (securityDevices.getAuthenticationDevice(dataAccessDeviceID) != null) {
			// if the users data device is also an authentication device, then authenticate the
			// according person against this device by default
			authenticationDeviceID = securityDevices.getAuthenticationDevice(dataAccessDeviceID).getDeviceID();
		} // else leave it empty
		return authenticationDeviceID;
	}

	/**
	 * This method provides access to the KnowledgeObject "Person". It'll check, whether there is a
	 * person that refers to the given user in the KnowledgeBase or not. If not, a new Person will
	 * be created for the given user.
	 * 
	 * Note: If there already is a person with the given name but which refers to other userdata
	 * (i.e. another dataAccessDevice), this method returns null
	 * 
	 * This method also returns null if no more persons are allowed because of the given license
	 * 
	 * @param userName
	 *        The user ID to create an account for.
	 * @param authenticationDeviceID
	 *        See {@link Person#getAuthenticationDeviceID()}.
	 * 
	 * @return The person or null.
	 */
	public synchronized Person getOrCreatePersonForUser(String userName, boolean morePersonsAllowed, String authenticationDeviceID) {
		{
			Person existingAccount = Person.byName(userName);
			if (existingAccount != null) {
				ensureLocale(existingAccount);
				return existingAccount;
			} else {
				if (personNameAlreadyUsed(userName)) {
					Logger.info("Unable to create person " + userName
						+ " as group with given name '" + userName + "' may already exist.", PersonManager.class);
					return null;
				}
				if (morePersonsAllowed) {
					Person createdPerson =
						createPersonInKBOnly(userName, PersistencyLayer.getKnowledgeBase(), authenticationDeviceID);
					ensureLocale(createdPerson);
					handleNewPerson(createdPerson);
					return createdPerson;
				} else {
					Logger.warn("Unable to create person " + userName +
						" as license doesn't allow more than " + LicenseTool.getInstance().getLicense().getUsers()
						+ " full users", PersonManager.class);
					return null;
				}
			}
		}
	}

	private void ensureLocale(Person sccount) {
		if (sccount.getLocale() == null) {
			sccount.setLocale(ResourcesModule.getInstance().getDefaultLocale());
		}
	}

	/**
	 * Re-Initializes the user cache upon startup and whenever called thereafter.
	 * 
	 * This will fetch all know Persons and add Persons when there is a User only. no commit() is
	 * executed by this code.
	 */
	@FrameworkInternal
	private synchronized void _initUserMap() {
		TLSecurityDeviceManager deviceManager = TLSecurityDeviceManager.getInstance();

		Set<Person> accountsToCheck = new HashSet<>(Person.all());

		for (String deviceId : deviceManager.getConfiguredDataAccessDeviceIDs()) {
			PersonDataAccessDevice device = deviceManager.getDataAccessDevice(deviceId);
			String authenticationDeviceID = getDefaultAuthenticationDeviceID(deviceId);

			for (UserInterface user : device.getAllUserData()) {
				String userName = user.getUserName();
				if (StringServices.isEmpty(userName)) {
					Logger.warn("Encountered empty username in '" + deviceId + "' - entry ignored.", this);
					continue;
				}

				Person account = getOrCreatePersonForUser(userName, true, authenticationDeviceID);
				accountsToCheck.remove(account);
			}
		}

		accountsToCheck = _personDeletionPolicy.restrictPersonsToDelete(this, accountsToCheck);

		// what remains in this list are persons for which no user was found in any security device,
		// either because security system was not available right now or because
		// the user has been deleted.
		for (Person account : accountsToCheck) {
			if (account.tValid()) {
				account.tDelete();
			}

		}

		for (Person person : Person.all()) {
			ensureRepresentativeGroup(person);
		}
	}

	/**
	 * Ensures that the given person has a representative group.
	 */
	private void ensureRepresentativeGroup(Person person) {
		if (person.getRepresentativeGroup() == null) {
			Person.createRepresentativeGroup(person);
		}
	}

	/**
	 * If a new Person is created this method is supposed to make sure that the system acts
	 * properly, i.e. all default roles are set and stuff
	 */
	protected void handleNewPerson(Person aPerson) {
		// for each new person an own personal group is created, with the person as member
		Person.createRepresentativeGroup(aPerson);
		try {
			Group defaultGroup = InitialGroupManager.getInstance().getDefaultGroup();
			if (defaultGroup != null) {
				defaultGroup.addMember(aPerson);
			}
		} catch (Exception ex) {
			Logger.error("Failed to add person " + aPerson + " to default group!", ex, this);
		}
	}

}
