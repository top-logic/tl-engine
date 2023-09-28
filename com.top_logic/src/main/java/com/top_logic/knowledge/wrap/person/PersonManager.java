/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.person;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.base.locking.Lock;
import com.top_logic.base.locking.LockService;
import com.top_logic.base.security.device.SecurityDeviceFactory;
import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.base.security.device.interfaces.PersonDataAccessDevice;
import com.top_logic.base.services.InitialGroupManager;
import com.top_logic.base.user.UserInterface;
import com.top_logic.base.user.UserService;
import com.top_logic.base.user.douser.DOUser;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.TLID;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.basic.col.Provider;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
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
import com.top_logic.dob.DataObject;
import com.top_logic.dob.persist.DataManager;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.knowledge.wrap.exceptions.InvalidWrapperException;
import com.top_logic.knowledge.wrap.person.deletion.AllPersonDeletionPolicy;
import com.top_logic.knowledge.wrap.person.deletion.PersonDeletionPolicy;
import com.top_logic.knowledge.wrap.util.PersonComparator;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.util.TLContext;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.license.LicenseTool;

/**
 * Superclass for person managers which also should create the various instances.
 * 
 * Usage of different PersonManagers can be done by subclasses of this class,
 * which are registered in the configuration in the section defined by the constants
 * below. Subclasses have to provide an empty constructor which needs to be
 * public if they are not placed in the same package as this class (which is not
 * recommended).
 * 
 * However, going this way, a call to getInstance() will always deliver the
 * correct implementation of PersonManager
 * 
 * TODO KHA/BHU/TRI we badly need a Call-back/Listener mechanism in case of
 *      Person creation/deletion, see the derived classes ....
 *
 * @author <a href="mailto:tri@top-logic.com">Thomas Richter </a>
 *  
 */
@ServiceDependencies({
	SecurityDeviceFactory.Module.class,
	PersistencyLayer.Module.class, 
	ApplicationConfig.Module.class,
	LockService.Module.class,
	TLSecurityDeviceManager.Module.class,
	// Some group operations are dispatched to the corresponding command handlers.
	CommandHandlerFactory.Module.class,
	DataManager.Module.class,
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
		 * @see #isRepresentativeGroupsShown()
		 */
		static final String XML_KEY_SHOW_REP_GRPS = "show-representative-groups";

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

		/**
		 * @see #getKnowledgeBaseProvider()
		 */
		static final String KNOWLEDGE_BASE_PROPERTY = "knowledge-base";

		/**
		 * @see #getTLSecurityDeviceManagerProvider
		 */
		static final String TL_SECURITY_DEVICE_MANAGER_PROPERTY = "security-device-manager";

		/** @see #getPersonDeletionPolicy() */
		String PERSON_DELETION_POLICY_PROPERTY = "person-deletion-policy";

		/**
		 * {@link TLSecurityDeviceManager}, to which changes, made by this task, will be committed
		 * to
		 */
		@InstanceDefault(DefaultTLSecurityDeviceManagerProvider.class)
		@InstanceFormat
		@Name(TL_SECURITY_DEVICE_MANAGER_PROPERTY)
		Provider<TLSecurityDeviceManager> getTLSecurityDeviceManagerProvider();

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
		 * a config flag indicating whether the persons representative groups should show up
		 *         in the system or not
		 */
		@BooleanDefault(false)
		@Name(XML_KEY_SHOW_REP_GRPS)
		boolean isRepresentativeGroupsShown();

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

		/**
		 * {@link KnowledgeBase}, to which changes, made by this task, will be committed to
		 */
		@InstanceDefault(DefaultKnowledgeBaseProvider.class)
		@InstanceFormat
		@Name(KNOWLEDGE_BASE_PROPERTY)
		Provider<KnowledgeBase> getKnowledgeBaseProvider();
	}

	/**
	 * Timeout of this token, in case this task does not release it itself 10 minutes should be
	 * enough here.
	 */
	public static final long TIMEOUT_INTERVAL_LENGTH = 1000 * 60 * 10;

	/** Token ID for this task. */
	public static final String TOKEN_ID = "RefreshUsers";

	/**
	 * Caches all requested person wrappers with user name as key just an internal cache to support
	 * getPerson(String aName).
	 */
	private Map<String, Person> allPersons = new Hashtable<>();

	private int numberAlivePersons = 0;

	private TLSecurityDeviceManager _tlSecurityDeviceManager;

	private final PersonDeletionPolicy _personDeletionPolicy;

	private final ResourcesModule _resourcesModule;

    /**
     * Caches all users with the appropriate (alive) person as key.
     * 
     * There can be Persons with invalid/unaccessible Users that will not
     * be found here.
     */
	private Map<String, Person> allUsers = new HashMap<>();

	private final KnowledgeBase knowledgeBase;

	private final Pattern userNamePattern;

	private final int maxPersonNameLength;

	private final boolean showRepresentativeGroups;
	
	private final String superUserName;
	
	private final int tokenTimeout;

	/**
	 * <p>
	 * <b>The {@link PersonManager} is build via reflection so no other
	 * constructor is allowed.</b>
	 * </p>
	 * 
	 */
	@CalledByReflection
	public PersonManager(InstantiationContext context, Config config) {
		userNamePattern = config.getUserNamePattern();
		showRepresentativeGroups = config.isRepresentativeGroupsShown();
		maxPersonNameLength = config.getPersonNameMaxLength();
		superUserName = config.getSuperUserName();
		tokenTimeout = config.getTokenTimeout();
		knowledgeBase = config.getKnowledgeBaseProvider().get();
		_tlSecurityDeviceManager = config.getTLSecurityDeviceManagerProvider().get();
		_personDeletionPolicy = config.getPersonDeletionPolicy();
		_resourcesModule = ResourcesModule.getInstance();
	}

	/**
	 * The system root. Should always be there so should never return null.
	 */
	public Person getRoot() {
		return getPersonByName(superUserName);
	}

	/**
	 * The name of the system root. Should always be there so should never return null.
	 */
	public String getSuperUserName() {
		return superUserName;
	}

	/**
	 * A config flag indicating whether the persons representative groups should show up in the
	 * system or not.
	 */
	public boolean returnRepresentativeGroups(){
		return showRepresentativeGroups;
	}
	
	/**
	 * Return the KO of the person currently using this thread.
	 * 
	 * @return The requested KO of type person or <code>null</code>.
	 */
	public KnowledgeObject getCurrentPersonKO() {
		TLSubSessionContext session = TLContextManager.getSubSession();
		if (session == null) {
			return null;
		}
		Person currentPerson = session.getPerson();
		if (currentPerson != null) {
			return currentPerson.tHandle();
		}
		return null;
	}

	/**
	 * Return the person currently using this thread.
	 * 
	 * @return The requested Person or <code>null</code>.
	 */
	public Person getCurrentPerson() {
		TLContext context = TLContext.getContext();
		if (context != null) {
			return context.getCurrentPersonWrapper();
		}
		return null;
	}

	/**
	 * Get all Persons that are known in the System.
	 */
	public Collection<Person> getAllPersons() {
		Collection<KnowledgeObject> theKOs = this.getAllPersonKOs();
		Collection<Person> theResult = new ArrayList<>(theKOs.size());

		for (KnowledgeObject theKO : theKOs) {
			{
				Person person = getPersonByKO(theKO);
				theResult.add(person);
			}
		}

		return (theResult);
	}

	/**
	 * Get the single wrapper for a given data object representing a user. if no
	 * matching person is found, null is returned
	 * 
	 * @param aUser
	 *            a UserInterface representing a DOUser.
	 * @return The wrapper or <code>null</code>, if no matching person found.
	 */
	public Person getPersonByUser(UserInterface aUser) {
		if (aUser == null) {
			return (null);
		}
		String name = aUser.getUserName();
		return getPersonByName(name);
	}

	/**
	 * Return the instance of person for the given identifier, if this person has not been deleted.
	 * 
	 * @param anID
	 *            The Knowledge Identifier of the person KO.
	 * @return The requested person, which is guaranteed to be a current object.
	 */
	public final Person getPersonByIdentifierCurrent(TLID anID) {
		return getPersonByIdentifierCurrent(anID, getKnowledgeBase());
	}
    
	/**
	 * Return the instance of person for the given identifier.
	 * 
	 * @param anID
	 *            The Knowledge Identifier of the person KO.
	 * @param aKB
	 *            the knowledge base
	 * @return The requested person, which is guaranteed to be a current object.
	 */
	public Person getPersonByIdentifierCurrent(TLID anID, KnowledgeBase aKB) {
		Person currentPerson = (Person) WrapperFactory.getWrapper(anID, Person.OBJECT_NAME, aKB);
		if (currentPerson != null) {
			currentPerson.init(this, currentPerson);
			return currentPerson;
		} else {
			return null;
		}
	}
	
	/**
	 * Return the instance of person for the given identifier.
	 * 
	 * @param anID
	 *            The Knowledge Identifier of the person KO.
	 * @param historyContext
	 *        The revision number in which the requested {@link Person} was known to be alive.
	 * @return The requested person in the given history context.
	 */
	public final Person getPersonByIdentifier(TLID anID, Long historyContext) {
		return getPersonByIdentifier(anID, getKnowledgeBase(), historyContext);
	}

	/**
	 * Return the instance of person for the given identifier.
	 * 
	 * @param anID
	 *            The Knowledge Identifier of the person KO.
	 * @param aKB
	 *            the knowledge base
	 * @param historyContext
	 *        The revision number in which the requested {@link Person} was known to be alive.
	 * @return The requested person in the given history context.
	 */
	public Person getPersonByIdentifier(TLID anID, KnowledgeBase aKB, long historyContext) {
		if (historyContext != Revision.CURRENT_REV) {
			// Fall back: Find historic version of a deleted person.
			HistoryManager historyManager = HistoryUtils.getHistoryManager(aKB);
			if (historyManager.hasHistory()) {
				Revision revision = historyManager.getRevision(historyContext);
				Person historicPerson =
					(Person) WrapperFactory.getWrapper(historyManager.getTrunk(), revision, anID, Person.OBJECT_NAME);
				if (historicPerson == null) {
					/* Note: Even a historic reference may not point to a valid person due to
					 * several reasons:
					 * 
					 * - The commit context id is not necessarily a person identifier (e.g. system
					 * context).
					 * 
					 * - The data might have been migrated from non-versioned storage and the person
					 * has been deleted before the time the repository was switched to versioned
					 * storage. */
					return null;
				}

				Person currentPerson = getPersonByIdentifierCurrent(anID, aKB);
				historicPerson.init(this, currentPerson);
				return historicPerson;
			}
		}

		return getPersonByIdentifierCurrent(anID, aKB);
	}

	/**
	 * Get the unique instance of Person for this knowledge object.
	 * 
	 * @param aKO
	 *            the object to wrap.
	 * @return The unique wrapper.
	 */
	public Person getPersonByKO(KnowledgeObject aKO) {
		AbstractWrapper.verifyType(aKO, Person.OBJECT_NAME);
		Person person = (Person) WrapperFactory.getWrapper(aKO);
		if (aKO.getHistoryContext() == Revision.CURRENT_REV) {
			person.init(this, person);
		} else {
			person.init(this, getPersonByKO((KnowledgeObject) HistoryUtils.getKnowledgeItem(Revision.CURRENT, aKO)));
		}
		return person;
	}

	/**
	 * Get the single Person wrapper for a given user name representing a user.
	 * 
	 * @param aName -
	 *            A login name of a person.
	 * @return The requested wrapper.
	 */
	public Person getPersonByName(String aName) {
		if (StringServices.isEmpty(aName)) {
			return null;
		}
		Person aPerson = this.allPersons.get(aName);

		if (aPerson != null && aPerson.tValid()) {
			return aPerson;
		} else {
			aPerson = getPersonByName(aName, null);
			if (aPerson != null && aPerson.tValid()) {
				allPersons.put(aName, aPerson);
				return aPerson;
			} else {
				allPersons.remove(aName);
				return null;
			}
		}
	}

	/**
	 * Get the single Person wrapper for a given user name representing a user.
	 * 
	 * @param aName -
	 *            A login name of a person.
	 * @param aKBase -
	 *            The KB to look for the person
	 * 
	 * @return The requested wrapper or null if not matching person is found
	 */
	public Person getPersonByName(String aName, KnowledgeBase aKBase) {
		if (StringServices.isEmpty(aName)) {
			return null;
		} else if (aKBase == null) {
			aKBase = getKnowledgeBase();
		}
		KnowledgeObject thePerson =
			(KnowledgeObject) aKBase.getObjectByAttribute(Person.OBJECT_NAME, AbstractWrapper.NAME_ATTRIBUTE, aName);
		if (thePerson != null) {
			return getPersonByKO(thePerson);
		}

		return (null);
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
	public final Person createPerson(String aName, String dataAccessDeviceID, String authenticationDeviceID,
			Boolean isRestricted) {
		return createPerson(aName, getKnowledgeBase(), dataAccessDeviceID, authenticationDeviceID, isRestricted);
	}

	/**
	 * Create a new external Person and User with given Name in given
	 * KnowledgeBase.
	 * 
	 * @return null when a person with the name already exists, the new person
	 *         otherwise.
	 */
	public synchronized Person createPerson(String aName, KnowledgeBase aKnowledgeBase, String dataAccessDeviceID,
			String authenticationDeviceID, Boolean isRestricted) {
		return internalCreatePerson(aName, aKnowledgeBase, dataAccessDeviceID, authenticationDeviceID, isRestricted);
	}

	private Person internalCreatePerson(String aName, KnowledgeBase aKnowledgeBase, String dataAccessDeviceID,
			String authenticationDeviceID, Boolean isRestricted) {
		if (StringServices.isEmpty(dataAccessDeviceID)) {
			throw new IllegalArgumentException("Given dataAccessDeviceID must not be empty !");
		}
		if (!validatePersonName(aName)) {
			int len = aName.length();
			int max = this.getPersonNameMaxLength();
			if (len < max) {
				throw new IllegalArgumentException(
					"Name '" + aName + "' does not match '" + getPersonNamePattern().pattern() + "'");
			} else {
				throw new IllegalArgumentException("Name '" + aName + "' to long " + len + ">" + max);
			}
		}
		Person thePerson = getPersonByName(aName, aKnowledgeBase);
		PersonDataAccessDevice theDataDevice = SecurityDeviceFactory.getPersonAccessDevice(dataAccessDeviceID);
		UserInterface user = theDataDevice.getUserData(aName);
		if (thePerson != null && thePerson.isAlive()) {
			throw new IllegalStateException("Person '" + aName + "' already exists");
		}
		if (user == null) {
			DataObject newUserData = UserService.getNewUserDO(aName);
			user = DOUser.getInstance(newUserData);
			boolean ok = theDataDevice.createUserEntry(newUserData);
			if (!ok) {
				throw new TopLogicException(I18NConstants.ERROR_NO_MORE_USERS);
			}
		}
		if (thePerson == null) { // creating new KO
			thePerson =
				getOrCreatePersonForUser(dataAccessDeviceID, user,
					LicenseTool.moreUsersAllowed(LicenseTool.getInstance().getLicense(), isRestricted));
		} else { // reusing existing KO for new user
			this._renamePerson(thePerson, aName); // make sure personname and username are same
													// (dependingon DB possible upper/lowercase
													// conflicts.)
			thePerson.setDataAccessDeviceID(dataAccessDeviceID); // set datadevice to point to given
																	// users device
			ensureLocale(thePerson);
			thePerson.resetUser(); // reload user
			handleNewPerson(thePerson);
		}
		// set authenticationdevice
		if (thePerson != null) { // will be null if licence limit reached
			if (!StringServices.isEmpty(authenticationDeviceID)) { // if given set it, use default
																	// otherwise
				thePerson.setAuthenticationDeviceID(authenticationDeviceID);
			} else {
				thePerson.setAuthenticationDeviceID(getDefaultAuthenticationDeviceID(dataAccessDeviceID));
			}
		}
		return (thePerson);
	}

	/**
	 * Disable a Person by deleting its user, i.e. the Person will not be able
	 * to login in again.
	 * 
	 * @param aPerson
	 *            the Person
	 */
	public void deleteUser(Person aPerson) {
		aPerson.deleteUser();
		aPerson.tDelete();
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
			int tmp = getUsersSize();
			if (tmp != numberAlivePersons) {
				numberAlivePersons = tmp;
				Logger.info("Currently working on " + numberAlivePersons + " alive persons", this);
			}
		} catch (Exception e) {
			Logger.error("Error reiniting usercache", e, this);
		}
		return lock;
	}

	/**
	 * A sorted copy of the current AlivePersons - List
	 */
	public List<Person> getAllAlivePersons() {
		List<Person> persons = getAllPersonsList();
		sortPersons(persons);
		return persons;
	}

	/**
	 * A sorted copy of the current AlivePersons which are NOT marked as restricted - List
	 */
	public List<Person> getAllAliveFullPersons() {
		List<Person> thePersons = FilterUtil.filterList(Person.FULL_USER_FILTER, getAllPersonsList());
		sortPersons(thePersons);
		return thePersons;
	}
    
	/**
	 * a sorted list of all current AlivePersons which are marked as restricted user.
	 */
	public List<Person> getAllAliveRestrictedPersons() {
		List<Person> thePersons = FilterUtil.filterList(Person.RESTRICTED_USER_FILTER, getAllPersonsList());
		sortPersons(thePersons);
		return thePersons;
	}
	
	/**
	 * This method makes sure all person wrappers which have become invalid by
	 * now are removed from the system wide person cache. The same task is also
	 * accomplished by initUserMap() but as this method also checks for changes
	 * in the security systems (i.e. new or deleted users) it is slower than this
	 * method.
	 * 
	 * This method only checks if the wrappers are valid not if the persons are
	 * still alive (i.e. still have a user). The only reason for this method is
	 * that it should not be necessary to call the relatively expensive
	 * initUserMap() if its just the validation of the wrappers that matters.
	 */
	public void validatePersons() {
		Iterator<Person> it = getAllPersons().iterator();
		while (it.hasNext()) {
			Person aPerson = it.next();
			try {
				aPerson.checkInvalid();
			} catch (InvalidWrapperException ignored) {
				// this is okay here do nothing. Check invalid already
				// handled this exception
			}
		}
	}

	/**
	 * renames a person. Should not be needed in general as renaming is not a
	 * feature accessible through the GUI
	 */
	public void renamePerson(Person aPerson, String aNewName) {
		if (aPerson.isAlive()) {
			throw new IllegalStateException("Attempt to rename a alive person, this is not possible !");
		}
		_renamePerson(aPerson, aNewName);
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
			Person p = getPersonByName(name);
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
	 * True when we have a known Mapping for this Person to a User.
	 */
	synchronized boolean isKnown(Person aPerson) {
		return aPerson == allUsers.get(aPerson.getName());
	}

    /**
     * Connect the given Person to the given user.
     */
	synchronized void enter(Person aPerson) {
		allUsers.put(aPerson.getName(), aPerson);
    }
    
    /**
     * Disconnect given Person from its User.
     */
	synchronized void disconnect(Person aPerson) {
		allUsers.remove(aPerson.getName());
    }
    
    /**
     * a Copy of the currently know alive Persons.
     */
    public synchronized Set<Person> getAllPersonsSet() {
		return new HashSet<>(allUsers.values());
    }
    
    /**
     * a Copy of the currently know alive Persons.
     */
    public synchronized List<Person> getAllPersonsList() {
		return new ArrayList<>(allUsers.values());
    }

    /**
	 * The number of active Persons/Users.
	 */
	protected synchronized int getUsersSize() {
        return allUsers.size();
    }

    /**
	 * singleton instance of this manager
	 */
	public static final PersonManager getManager() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * default {@link KnowledgeBase}, defined by the {@link PersistencyLayer}
	 */
	protected final KnowledgeBase getKnowledgeBase() {
		return knowledgeBase;
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
		Transaction tx = getKnowledgeBase().beginTransaction(commitMessage);
		try {
			Lock lock = initUserMap();
			if (lock != null) {
				lock.unlock();
			}
			tx.commit();
		} catch (KnowledgeBaseException ex) {
			String msg = "Unable to commit transaction starting PersonManager.";
			throw new KnowledgeBaseRuntimeException(msg, ex);
		} finally {
			tx.rollback();
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
	 * Return the list of all persons. Meant are all Persons known to knowledge base, whether alive
	 * or not
	 * 
	 * @return The list of KnowledgeObjects (!)
	 */
	private Collection<KnowledgeObject> getAllPersonKOs() {
		return getKnowledgeBase().getAllKnowledgeObjects(Person.OBJECT_NAME);
	}

	/**
	 * Create a new PersonKO in given KnowledgeBase. Does NOT check if such a person already exists!
	 * Note: No user object will be created by this method
	 * 
	 * @param aName
	 *        the person (login) name
	 * @param aKnowledgeBase
	 *        the KBase
	 * @param dataAccessDeviceID
	 *        the security device that contains the userdata
	 * @return the created person
	 */
	private Person createPersonInKBOnly(String aName, KnowledgeBase aKnowledgeBase, String dataAccessDeviceID,
			String authenticationDeviceID) {
		{
			KnowledgeObject theKO = aKnowledgeBase.createKnowledgeObject(Person.OBJECT_NAME);
			theKO.setAttributeValue(AbstractWrapper.NAME_ATTRIBUTE, aName);
			Person thePerson = getPersonByKO(theKO);
			thePerson.setDataAccessDeviceID(dataAccessDeviceID);
			if (!StringServices.isEmpty(authenticationDeviceID)) { // if there, set it- leave
																	// default otherwise
				thePerson.setAuthenticationDeviceID(authenticationDeviceID);
			}
			return (thePerson);
		}
	}

	private String getDefaultAuthenticationDeviceID(String dataAccessDeviceID) {
		String authenticationDeviceID = "";
		if (_tlSecurityDeviceManager.getAuthenticationDevice(dataAccessDeviceID) != null) {
			// if the users data device is also an authentication device, then authenticate the
			// according person against this device by default
			authenticationDeviceID = _tlSecurityDeviceManager.getAuthenticationDevice(dataAccessDeviceID).getDeviceID();
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
	 * @param theUser
	 *        for which the person is requested The login name of the person.
	 * @return The person or null.
	 */
	public synchronized Person getOrCreatePersonForUser(String dataAccessDeviceID, UserInterface theUser,
			boolean morePersonsAllowed) {
		return internalGetOrCreatePersonForUser(dataAccessDeviceID, theUser, morePersonsAllowed);
	}

	private Person internalGetOrCreatePersonForUser(String dataAccessDeviceID, UserInterface user,
			boolean morePersonsAllowed) {
		{
			String theUserName = user.getUserName();
			Person existingAccount = getPersonByName(theUserName);
			if (existingAccount != null) {
				existingAccount.setUser(user);
				enter(existingAccount);
				ensureLocale(existingAccount);
				return existingAccount;
			} else {
				if (personNameAlreadyUsed(theUserName)) {
					Logger.info("Unable to create person for " + dataAccessDeviceID + "//" + theUserName
						+ " as group with given name '" + theUserName + "' may already exist.", PersonManager.class);
					return null;
				}
				if (morePersonsAllowed) {
					String authenticationDeviceID = getDefaultAuthenticationDeviceID(dataAccessDeviceID);
					Person createdPerson =
						createPersonInKBOnly(theUserName, getKnowledgeBase(), dataAccessDeviceID,
							authenticationDeviceID);
					ensureLocale(createdPerson);
					createdPerson.setUser(user);
					handleNewPerson(createdPerson);
					return createdPerson;
				} else {
					Logger.warn("Unable to create person for " + dataAccessDeviceID + "//" + theUserName +
						" as license doesn't allow more than " + LicenseTool.getInstance().getLicense().getUsers()
						+ " full users", PersonManager.class);
					return null;
				}

			}
		}
	}

	private void ensureLocale(Person sccount) {
		if (sccount.getLocale() == null) {
			sccount.setLocale(_resourcesModule.getDefaultLocale());
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
		TLSecurityDeviceManager deviceManager = _tlSecurityDeviceManager;

		Set<Person> accountsToCheck = getAllPersonsSet();

		for (String deviceId : deviceManager.getConfiguredDataAccessDeviceIDs()) {
			PersonDataAccessDevice device = deviceManager.getDataAccessDevice(deviceId);

			for (UserInterface user : device.getAllUserData()) {
				String userName = user.getUserName();
				if (StringServices.isEmpty(userName)) {
					Logger.warn("Encountered empty username in '" + deviceId + "' - entry ignored.", this);
					continue;
				}

				Person account = getOrCreatePersonForUser(deviceId, user, true);
				accountsToCheck.remove(account);
			}
		}

		accountsToCheck = _personDeletionPolicy.restrictPersonsToDelete(this, accountsToCheck);

		// what remains in this list are persons for which no user was found in any security device,
		// either because security system was not available right now or because
		// the user has been deleted.
		for (Person account : accountsToCheck) {
			if (!account.tValid()) {
				// Person has been already deleted from database by another cluster node's knowledge
				// base.
				// Therefore exclude this person from deletion and remove it from cache.
				disconnect(account);
			} else {
				deleteUser(account);
			}

		}

		for (Person person : getAllPersons()) {
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

	private void sortPersons(List<Person> persons) {
		try {
			Collections.sort(persons, PersonComparator.getInstance());
		} catch (Exception e) {
			Logger.error("Failed to sort persons", e, PersonManager.class);
		}
	}

	/**
	 * The actual renaming code
	 */
	private void _renamePerson(Person aPerson, String aNewName) {
		String oldName = "";
		try {
			oldName = aPerson.getName();
			allPersons.remove(oldName);
			aPerson.setValue(Person.NAME_ATTRIBUTE, aNewName);
		} catch (Exception e) {
			Logger.error("Failed to rename person " + aPerson + "with name " + oldName + " to " + aNewName, e, this);
		}
	}
}
