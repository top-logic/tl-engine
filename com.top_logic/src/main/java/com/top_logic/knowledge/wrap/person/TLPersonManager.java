/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.person;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.TLID;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.basic.col.Provider;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.persist.DataManager;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Revision;
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
import com.top_logic.util.license.TLLicense;

/**
 * Manage Person entities, reference implementation.
 * 
 * Instances of the current PersonManager are to be acquired by a 
 * static call to PersonManager.getInstance().
 * 
 * TODO TRI this class is not MultiKnowledgeBase aware, equals Persons found in
 *      different KBs will kill this class.
 * 
 * @author <a href="mailto:tri@top-logic.com">Thomas Richter</a>
 */
@ServiceDependencies({
	LockService.Module.class,
	TLSecurityDeviceManager.Module.class,
	// Some group operations are dispatched to the corresponding command handlers.
	CommandHandlerFactory.Module.class,
	DataManager.Module.class,
	InitialGroupManager.Module.class,
	ResourcesModule.Module.class,
	})
public class TLPersonManager extends PersonManager {

	public interface Config extends PersonManager.Config {

		/**
		 * @see #getTLSecurityDeviceManagerProvider
		 */
		static final String TL_SECURITY_DEVICE_MANAGER_PROPERTY = "security-device-manager";

		/** @see #getPersonDeletionPolicy() */
		String PERSON_DELETION_POLICY_PROPERTY = "person-deletion-policy";

		/**
		 * {@link TLSecurityDeviceManager}, to which changes, made by this task, will be
		 *         committed to
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
	}


	/**
	 * Timeout of this token, in case this task does not release it itself 10
	 * minutes should be enough here.
	 */
	public static final long	TIMEOUT_INTERVAL_LENGTH	= 1000 * 60 * 10;

	/** Token ID for this task. */
	public static final String	TOKEN_ID				= "RefreshUsers";

    /** Caches all requested person wrappers with user name as key just an internal cache to support getPerson(String aName). */
    private Map<String,Person> allPersons = new Hashtable<>();

	private int					numberAlivePersons		= 0;

	private TLSecurityDeviceManager _tlSecurityDeviceManager;

	private final PersonDeletionPolicy _personDeletionPolicy;

	private final ResourcesModule _resourcesModule;

	/**
	 * Need public Ctor to be created by Introspection
	 */
	@CalledByReflection
	public TLPersonManager(InstantiationContext context, Config config) {
		super(context, config);
		_tlSecurityDeviceManager = config.getTLSecurityDeviceManagerProvider().get();
		_personDeletionPolicy = config.getPersonDeletionPolicy();
		_resourcesModule = ResourcesModule.getInstance();
	}
    
	/**
	 * Return the KO of the person currently using this thread.
	 * 
	 * @return The requested KO of type person or <code>null</code>.
	 */
	@Override
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
	@Override
	public Person getCurrentPerson() {
		TLContext context = TLContext.getContext();
		if (context != null) {
			return context.getCurrentPersonWrapper();
		}
		return null;
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
	 * Get all Persons in KBase (does not care about {@link Person#isAlive()} or such.
	 */
	@Override
	public Collection<Person> getAllPersons() {
		Collection<KnowledgeObject> theKOs     = this.getAllPersonKOs();
		Collection<Person>          theResult  = new ArrayList<>(theKOs.size());

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
	@Override
	public Person getPersonByUser(UserInterface aUser) {
		if (aUser == null) {
			return (null);
		}
		String name = aUser.getUserName();
		return getPersonByName(name);
	}

	@Override
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

	@Override
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
	 * Get the unique instance of Person for this knowledge object.
	 * 
	 * @param aKO
	 *            the object to wrap.
	 * @return The unique wrapper.
	 */
	@Override
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
	@Override
	public Person getPersonByName(String aName) {
		if (StringServices.isEmpty(aName)) {
			return null;
		}
		Person aPerson = this.allPersons.get(aName);

		if (aPerson != null && aPerson.tValid()) {
			return aPerson;
		}
		else {
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
	 * @return The requested wrapper or null if not matching person is found
	 */
	@Override
	public Person getPersonByName(String aName, KnowledgeBase aKBase) {
		if (StringServices.isEmpty(aName)) {
			return null;
		} else if (aKBase == null) {
			aKBase = getKnowledgeBase();
		}
		KnowledgeObject thePerson = (KnowledgeObject) aKBase.getObjectByAttribute(Person.OBJECT_NAME, AbstractWrapper.NAME_ATTRIBUTE, aName);
		if (thePerson != null) {
			return getPersonByKO(thePerson);
		}

		return (null);
	}

	/**
	 * Create a new external Person and User with given Name in given KnowledgeBase and restricted
	 * or not.
	 * 
	 * @return null when a person with the name already exists, the new person otherwise.
	 */
	@Override
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
                throw new IllegalArgumentException("Name '" + aName + "' does not match '" + getPersonNamePattern().pattern() + "'");
            } else {
				throw new IllegalArgumentException("Name '" + aName + "' to long " + len + ">" + max);
            }
		}
		Person thePerson = getPersonByName(aName, aKnowledgeBase);
		PersonDataAccessDevice theDataDevice = SecurityDeviceFactory.getPersonAccessDevice(dataAccessDeviceID);
		DataObject theUserData = theDataDevice.getUserData(aName);
		if (thePerson != null && thePerson.isAlive()) {
			throw new IllegalStateException("Person '" + aName + "' already exists");
		}
		if (theUserData == null) {
			theUserData = UserService.getNewUserDO(aName);
			boolean ok = theDataDevice.createUserEntry(theUserData);
			if (!ok) {
				throw new TopLogicException(I18NConstants.ERROR_NO_MORE_USERS);
			}
		}
		if (thePerson == null) { //creating new KO
			thePerson =
				getOrCreatePersonForUser(DOUser.getInstance(theUserData),
					LicenseTool.moreUsersAllowed(LicenseTool.getInstance().getLicense(), isRestricted));
		} else { //reusing existing KO for new user
			this._renamePerson(thePerson, aName); //make sure personname and username are same (dependingon DB possible upper/lowercase conflicts.)
			thePerson.setDataAccessDeviceID(dataAccessDeviceID); //set datadevice to point to given users device
			setInitialLocale(thePerson);
			thePerson.resetUser(); //reload user
			handleNewPerson(thePerson);
		}
		//set authenticationdevice
		if(thePerson!=null){ //will be null if licence limit reached
			if (!StringServices.isEmpty(authenticationDeviceID)) { //if given set it, use default otherwise
				thePerson.setAuthenticationDeviceID(authenticationDeviceID);
			} else {
				thePerson.setAuthenticationDeviceID(getDefaultAuthenticationDeviceID(dataAccessDeviceID));
			}
		}
		return (thePerson);
	}

	/**
	 * Tries to intialize the locale of the given (newly created) person with the locale of the
	 * current user. Uses configured initial locale as default if that doesn't work.
	 */
	private void setInitialLocale(Person person) {
		Locale locale = findInitialLocale();
		person.setLocale(locale);
	}

	private Locale findInitialLocale() {
		Locale locale = _resourcesModule.getDefaultLocale();
		{ // trying to preset with data from current user
            Person current = getCurrentPerson();
            if (current != null) { // happens when Testing/Import 
				Locale currentLocale = getCurrentPerson().getLocale();
				if (currentLocale != null) {
					locale = currentLocale;
				}
            }
		}
		return locale;
	}

    /**
	 * Create a new PersonKO in given KnowledgeBase. Does NOT check if such a
	 * person already exists! Note: No user object will be created by this
	 * method
	 * 
	 * @param aName
	 *            the person (login) name
	 * @param aKnowledgeBase
	 *            the KBase
	 * @param dataAccessDeviceID
	 *            the security device that contains the userdata
	 * @return the created person
	 */
	private Person createPersonInKBOnly(String aName, KnowledgeBase aKnowledgeBase, String dataAccessDeviceID, String authenticationDeviceID) {
		{
			KnowledgeObject theKO = aKnowledgeBase.createKnowledgeObject(Person.OBJECT_NAME);
			theKO.setAttributeValue(AbstractWrapper.NAME_ATTRIBUTE, aName);
			Person thePerson = getPersonByKO(theKO);
			thePerson.setDataAccessDeviceID(dataAccessDeviceID);
			if (!StringServices.isEmpty(authenticationDeviceID)) { //if there, set it- leave default otherwise
				thePerson.setAuthenticationDeviceID(authenticationDeviceID);
			}
			return (thePerson);
		}
	}

	private String getDefaultAuthenticationDeviceID(String dataAccessDeviceID) {
		String authenticationDeviceID = "";
		if (_tlSecurityDeviceManager.getAuthenticationDevice(dataAccessDeviceID) != null) {
			//if the users data device is also an authentication device, then authenticate the according person against this device by default
			authenticationDeviceID = _tlSecurityDeviceManager.getAuthenticationDevice(dataAccessDeviceID).getDeviceID();
		} //else leave it empty
		return authenticationDeviceID;
	}

	/**
	 * This method provides access to the KnowledgeObject "Person". It'll check,
	 * whether there is a person that refers to the given user in the
	 * KnowledgeBase or not. If not, a new Person will be created for the given
	 * user.
	 * 
	 * Note: If there already is a person with the given name but which refers
	 * to other userdata (i.e. another dataAccessDevice), this method returns
	 * null
	 * 
	 * This method also returns null if no more persons are allowed because of
	 * the given license
	 * 
	 * @param theUser
	 *            for which the person is requested The login name of the
	 *            person.
	 * @return The person or null.
	 */
	public synchronized Person getOrCreatePersonForUser(UserInterface theUser, boolean morePersonsAllowed) {
		return internalGetOrCreatePersonForUser(theUser, morePersonsAllowed);
	}

	private Person internalGetOrCreatePersonForUser(UserInterface theUser, boolean morePersonsAllowed) {
		{
			String theUserName = theUser.getUserName();
			Person existingPerson = getPersonByName(theUserName);
			String dataAccessDeviceID = theUser.getDataAccessDeviceID();
			if (existingPerson != null) {
				if (existingPerson.setUser(theUser)) {
					/* Necessary for dynamic translations see setLocale(...) */
					if (existingPerson.getLocale() == null) {
						setInitialLocale(existingPerson);
					}
					this.handleRefreshPerson(existingPerson);
					return existingPerson;
				} else {
					Logger.info("Unable to create person for " + dataAccessDeviceID + "//" + theUserName
						+ " as there exist already a user " + existingPerson.getDataAccessDeviceID() + "//"
						+ theUserName, TLPersonManager.class);
					return null;
				}
			} else {
				if (personNameAlreadyUsed(theUserName)) {
					Logger.info("Unable to create person for " + dataAccessDeviceID + "//" + theUserName
						+ " as group with given name '" + theUserName + "' may already exist.", TLPersonManager.class);
					return null;
				}
				if (morePersonsAllowed) {
					String authenticationDeviceID = getDefaultAuthenticationDeviceID(dataAccessDeviceID);
					Person createdPerson =
						createPersonInKBOnly(theUserName, getKnowledgeBase(), dataAccessDeviceID,
							authenticationDeviceID);
					setInitialLocale(createdPerson);
					if (!createdPerson.setUser(theUser)) {
						Logger.warn("Unable to connect user dataobject to newly created person. Try to let the person retrieve the data itself.", TLPersonManager.class);
						createdPerson.resetUser();
					}
					handleNewPerson(createdPerson);
					return createdPerson;
				} else {
					Logger.warn("Unable to create person for " + dataAccessDeviceID + "//" + theUserName +
						" as license doesn't allow more than " + LicenseTool.getInstance().getLicense().getUsers()
						+ " full users", TLPersonManager.class);
					return null;
				}

			}
		}
	}

	/**
     * Call _initUserMap() synchronized by a Token and then call commit.
     * 
	 * Will wait for two seconds to get the token and return null if the token
	 * could not be acquired by then make sure the KB get committed and Token
	 * gets released after running this method (not necessary if it doesn't
	 * return a TokenContext)
	 */
	@Override
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
	 * Re-Initializes the usercache upon startup and whenever called thereafter.
     * 
     * This will fetch all know Persons and add Persons when there is a User only.
     * no commit() is executed by this code.
	 */
	@FrameworkInternal
	private synchronized void _initUserMap() {
		// at first getting a copy of the current / previous alive persons
		Set<Person> thePersons = getAllPersonsSet();
		// if no one except the root user is in there we assume this is initial
		// startup
		if (thePersons.size() <= 1) { // no User or only root, KHA: strange check.
			// so we fetch all known persons from KB
			for (KnowledgeObject theKO : getAllPersonKOs()) {
				{
					Person aPerson = getPersonByKO(theKO);
					aPerson.getDataAccessDeviceID(); // just make sure it is
													 // properly initialized on
													 // startup
					aPerson.getAuthenticationDeviceID(); // just make sure it is
														 // properly initialized
														 // on startup
					thePersons.add(aPerson);
				}
			}
		}
		// declaring some variables to avoid having declarations in the loop
		String        theUserName = null;
		Person        thePerson   = null;
		// now we read all users we can find in the security systems
		Set<UserInterface> allUsers =
			new HashSet<>(CollectionUtil.dynamicCastView(UserInterface.class,
				_tlSecurityDeviceManager.getAllAvailableUserData()));
		Logger.info("Synchronizing person objects, and user backups...",this);
		TLLicense licenseObject = LicenseTool.getInstance().getLicense();

		// remove super user from list so it can never be deleted
		String superUserName = PersonManager.getManager().getSuperUserName();
		UserInterface rootUser =
			CollectionUtil.find(allUsers, superUserName, UserInterface::getUserName);
		Person rootPerson = getOrCreatePersonForUser(rootUser, Boolean.FALSE);
		thePersons.remove(rootPerson);

		// First look up for persons from DB. Important for restart of application.
		Set<Person> tmpPersons = new HashSet<>();
		for (Person person : thePersons) {
			// The amount of system users is changed in every cycle, don't move this out!
			boolean allowMoreAccounts = LicenseTool.moreUsersAllowed(licenseObject, false);
			String personName = person.getName();
			Optional<UserInterface> user =
				allUsers.stream().filter(item -> (personName.equals(item.getUserName()))).findFirst();
			if (!user.isPresent()) {
				Logger.warn("Encountered empty username for userentry in userlist - entry will be ignored.", this);
				continue;
			}
			tmpPersons.add(getOrCreatePersonForUser(user.get(), allowMoreAccounts));
			allUsers.remove(user.get());
		}
		// Remove all persons that are found
		thePersons.removeAll(tmpPersons);
		// Import all users left by security devices
		for (UserInterface theUser : allUsers) {
			// The amount of system users is changed in every cycle, don't move this out!
			boolean allowMoreAccounts = LicenseTool.moreUsersAllowed(licenseObject, Boolean.FALSE);
			theUserName = theUser.getUserName();
			if (StringServices.isEmpty(theUserName)) {
				Logger.warn("Encountered empty username for userentry in userlist - entry will be ignored.", this);
				continue;
			}
			thePerson = getOrCreatePersonForUser(theUser, allowMoreAccounts);
			thePersons.remove(thePerson);
		}

		thePersons = _personDeletionPolicy.restrictPersonsToDelete(this, thePersons);

		// what remains in this list are persons for which no user was found in
		// security-
		// either because security system was not available right now or because
		// the user has been deleted
		Iterator<Person> notFoundInSecurity = thePersons.iterator();
		while (notFoundInSecurity.hasNext()) {
			thePerson = notFoundInSecurity.next();
            if (!thePerson.tValid()) {
				// Person has been already deleted from database by another cluster node's knowledge
				// base.
				// Therefore exclude this person from deletion and remove it from cache.
				notFoundInSecurity.remove();
				disconnect(thePerson);
				continue;
            }
			// force those to reload, to see if deleted (not alive) or secdev
			// down (still alive)
			thePerson.resetUser();
			if (thePerson.isAlive()) {
				// again remove all alive
				notFoundInSecurity.remove();
			}
		}
		//just make sure, those persons with changed state make it persistent
		// now
		this.storeChangedAliveStates();
		// therefore rest being handled as deleted
		handleDeletedUsers(thePersons);

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
	 * This method makes sure all person wrappers which have become invalid by
	 * now are removed from the system wide person cache. The same task is also
	 * accomplished by initUserMap() but as this method also checks for changes
	 * in the security systems (i.e. new or deleted users) it is slower than this
	 * method. This method only checks if the wrappers are valid not if the
	 * persons are still alive (i.e. still have a user). The only reason for
	 * this method is that it should not be necessary to call the relatively
	 * expensive initUserMap() if its just the validation of the wrappers that
	 * matters.
	 */
	@Override
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
	 * Hook for subclasses to react on persons which got their user deleted Upon
	 * each sync the persons which are found to have no user in the security
	 * system are given to this method
	 * 
	 * @param personList
	 *            the persons with deleted users
	 */
	private void handleDeletedUsers(Set<Person> personList) {
		Iterator<Person> personsWithDeletedUserData = personList.iterator();
		while(personsWithDeletedUserData.hasNext()){
			Person tmp = personsWithDeletedUserData.next();
			if(!tmp.getIsDeletionHandled()){
				{
					deleteLocalAccount(tmp);
					// delete person
					tmp.tDelete();
				}
				tmp.setIsDeletionHandled(true);
			}
		}
	}

	/**
	 * Deletes the {@link UserInterface} for the {@link Person} and the {@link Person} itself.
	 */
	@Override
	public void deleteUser(Person aPerson) {
		aPerson.deleteUser();
		deleteLocalAccount(aPerson);
		// delete person
		aPerson.tDelete();
	}

	/**
	 * Deletes the locally cached account.
	 * 
	 * @param aPerson
	 *        The {@link Person} representing the local account.
	 */
	private void deleteLocalAccount(Person aPerson) {
		this.postProcessUserDeletion(aPerson);

		// delete representative group
		Group representativeGroup = aPerson.getRepresentativeGroup();
		if (representativeGroup != null) {
			representativeGroup.tDelete();
		}
	}

	@Override
	public void postProcessUserDeletion(Person aPerson) {
        deletePersonalConfiguration(aPerson);
	}

    /** 
     * Delete the {@link PersonalConfigurationWrapper} attached to aPerson.
     */
    protected void deletePersonalConfiguration(Person aPerson) {
        PersonalConfigurationWrapper personalConfiguration = PersonalConfigurationWrapper.getPersonalConfiguration(aPerson);
        if (personalConfiguration != null) try {
            personalConfiguration.tDelete();
        }
        catch (Exception ex) {
        	Logger.warn("Failed to delete personal configuration for person " + aPerson + ".", ex, TLPersonManager.class);
		}
    }

	/**
	 * If a new Person is created this method is supposed to make sure that the
	 * system acts properly, i.e. all default roles are set and stuff
	 */
	protected void handleNewPerson(Person aPerson){
		//for each new person an own personal group is created, with the person as member
	    Person.createRepresentativeGroup(aPerson);
	    try {
			Group defaultGroup = InitialGroupManager.getInstance().getDefaultGroup();
			if (defaultGroup != null) {
				defaultGroup.addMember(aPerson);
	        }
        }
        catch (Exception ex) {
            Logger.error("Failed to add person " + aPerson + " to default group!", ex, this);
        }
	}

	@Override
	public void handleRefreshPerson(Person aPerson) {
		// Ignore.
	}

	/**
	 * a sorted copy of the current AlivePersons - List
	 */
	@Override
	public List<Person> getAllAlivePersons() {
		List<Person> persons = getAllPersonsList();
		sortPersons(persons);
		return persons;
	}

	@Override
	public List<Person> getAllAliveFullPersons() {
		List<Person> thePersons = FilterUtil.filterList(Person.FULL_USER_FILTER, getAllPersonsList());
		sortPersons(thePersons);
		return thePersons;
	}

	@Override
	public List<Person> getAllAliveRestrictedPersons() {
		List<Person> thePersons = FilterUtil.filterList(Person.RESTRICTED_USER_FILTER, getAllPersonsList());
		sortPersons(thePersons);
		return thePersons;
	}

	private void sortPersons(List<Person> persons) {
		try {
			Collections.sort(persons, new PersonComparator());
		} catch (Exception e) {
			Logger.error("Failed to sort persons", e, TLPersonManager.class);
		}
	}
	
	/**
	 * renames a person. Should not be needed in general as renaming is not a
	 * feature accessible through the GUI Note: renaming is not allowed for
	 * alive persons as then their user would have to be renamed, too. Renaming
	 * is only possible on not alive persons and currently being used to solve
	 * upper- / lower case conflicts between a new user and an already
	 * existing KO (Person) of a previously deleted user 
	 */
	@Override
	public void renamePerson(Person aPerson, String aNewName) {
		if (aPerson.isAlive()) {
			throw new IllegalStateException("Attempt to rename a alive person, this is not possible !");
		}
		_renamePerson(aPerson,aNewName);
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