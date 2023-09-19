/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.person;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.top_logic.base.locking.Lock;
import com.top_logic.base.security.device.SecurityDeviceFactory;
import com.top_logic.base.user.UserInterface;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.TLID;
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
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.tool.boundsec.wrap.Group;
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
@ServiceDependencies({SecurityDeviceFactory.Module.class,
	PersistencyLayer.Module.class, 
	ApplicationConfig.Module.class })
public abstract class PersonManager extends ManagedClass {

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
     * Caches all users with the appropriate (alive) person as key.
     * 
     * There can be Persons with invalid/unaccessible Users that will not
     * be found here.
     */
	private Map<Person, UserInterface> allUsers = new HashMap<>();

	private final KnowledgeBase knowledgeBase;

	private final Pattern userNamePattern;

	private final int maxPersonNameLength;

    /** Set of Persons where {@link Person#isAlive()} was changed since last call to {@link #storeChangedAliveStates()}  */
	private Set<Person> aliveStateChanged = new HashSet<>();

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
	}

    /**
     * Call storeChangedAliveState()) for all Persons in aliveStateChanged Set.
     *  
     * TODO TRI should this be synchronized ?
     */
	protected void storeChangedAliveStates() {
	    synchronized (aliveStateChanged) {
    		Iterator it = this.aliveStateChanged.iterator();
    		while (it.hasNext()) {
    			String thePersonStr = "";
    			try {
    				Person tmp = (Person) it.next();
                    if (!tmp.tValid()) {
                        Logger.info("storeChangedAliveStates(): Skipping Person" + tmp, this);
                        continue; // better skip these
                    }
                    thePersonStr = tmp.getDataAccessDeviceID() + "\\" + tmp.getName();
    				tmp.storeChangedAliveState();
    			} catch (Exception e) {
    				Logger.error("Unable to store last known alive state from person: " + thePersonStr, e, this);
    			} finally {
    				it.remove();
    			}
    		}
	    }
	}

	/**
	 * Callback from person.
	 */
	void aliveStateChanged(Person aPerson) {
	    synchronized (aliveStateChanged) {
	        this.aliveStateChanged.add(aPerson);
	    }
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
	public abstract KnowledgeObject getCurrentPersonKO();

	/**
	 * Return the person currently using this thread.
	 * 
	 * @return The requested Person or <code>null</code>.
	 */
	public abstract Person getCurrentPerson();

	/**
	 * Get all Persons that are known in the System.
	 */
	public abstract Collection<Person> getAllPersons();

	/**
	 * Get the single wrapper for a given data object representing a user. if no
	 * matching person is found, null is returned
	 * 
	 * @param aUser
	 *            a UserInterface representing a DOUser.
	 * @return The wrapper or <code>null</code>, if no matching person found.
	 */
	public abstract Person getPersonByUser(UserInterface aUser);

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
		return getPersonByIdentifier(anID, aKB, Revision.CURRENT_REV);
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
	public abstract Person getPersonByIdentifier(TLID anID, KnowledgeBase aKB, long historyContext);

	/**
	 * Get the unique instance of Person for this knowledge object.
	 * 
	 * @param aKO
	 *            the object to wrap.
	 * @return The unique wrapper.
	 */
	public abstract Person getPersonByKO(KnowledgeObject aKO);

	/**
	 * Get the single Person wrapper for a given user name representing a user.
	 * 
	 * @param aName -
	 *            A login name of a person.
	 * @return The requested wrapper.
	 */
	// TODO: Should be made final, but the implementation in TLPersonManager does not seem to be equivalent to this (persons are only cached, if looked up through this method).
	public Person getPersonByName(String aName) {
		return getPersonByName(aName, getKnowledgeBase());
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
	public abstract Person getPersonByName(String aName, KnowledgeBase aKBase);

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
	public abstract Person createPerson(String aName, KnowledgeBase aKnowledgeBase, String dataAccessDeviceID,
			String authenticationDeviceID, Boolean isRestricted);

	/**
	 * Disable a Person by deleting its user, i.e. the Person will not be able
	 * to login in again.
	 * 
	 * @param aPerson
	 *            the Person
	 */
	public abstract void deleteUser(Person aPerson);

	/**
	 * Re-Initializes the user cache upon startup and whenever called thereafter. Returns the
	 * {@link Lock} that was used to synch this call or <code>null</code> if the token was not
	 * available at the time of the call. if <code>null</code> is returned, this method does
	 * nothing. If the {@link Lock} is returned, the refresh was performed successfully and the
	 * caller now is responsible of releasing the {@link Lock}.
	 * 
	 * @return the {@link Lock} used for initializing
	 */
	public abstract Lock initUserMap();
	
	/**
	 * If a Person is updated this method is supposed to make sure that the
	 * system acts properly, e.g. set attributes in other objects
	 */
	public abstract void handleRefreshPerson(Person aPerson);

	/**
	 * A sorted copy of the current AlivePersons - List
	 */
	public abstract List<Person> getAllAlivePersons();

	/**
	 * A sorted copy of the current AlivePersons which are NOT marked as restricted - List
	 */
	public abstract List<Person> getAllAliveFullPersons();
    
	/**
	 * a sorted list of all current AlivePersons which are marked as restricted user.
	 */
	public abstract List<Person> getAllAliveRestrictedPersons();
	
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
	public abstract void validatePersons();

	/**
	 * Sometimes, if the user of a person has been deleted from security, some
	 * postprocessing of the Person object is necessary - such as removing
	 * assignments/roles or to delete previously saved user images. Thats what
	 * this method is for. Note, this method has to work even if called AFTER
	 * the user of the person already had been deleted from security.
	 */
	public abstract void postProcessUserDeletion(Person aPerson);

	/**
	 * renames a person. Should not be needed in general as renaming is not a
	 * feature accessible through the GUI
	 */
	public abstract void renamePerson(Person aPerson, String aNewName);

	
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
	    return allUsers.containsKey(aPerson);
	}

    /**
     * Connect the given Person to the given user.
     */
    synchronized UserInterface connect(Person aPerson, UserInterface user) {
        return allUsers.put(aPerson, user);
    }
    
    /**
     * Disconnect given Person from its User.
     */
    synchronized UserInterface disconnect(Person aPerson) {
        return allUsers.remove(aPerson);
    }

    /**
     * Return the User mapped to aPerson.
     */
    synchronized UserInterface getUser(Person aPerson) {
        return allUsers.get(aPerson);
    }
    
    /**
     * a List containing all Users belonging to the currently known
     *         alive persons
     */
    public synchronized List<UserInterface> getAllAliveUsers() {
        return new ArrayList<>(allUsers.values());
    }
    
    /**
     * a Copy of the currently know alive Persons.
     */
    public synchronized Set<Person> getAllPersonsSet() {
        return new HashSet<>(allUsers.keySet());
    }
    
    /**
     * a Copy of the currently know alive Persons.
     */
    public synchronized List<Person> getAllPersonsList() {
        return new ArrayList<>(allUsers.keySet());
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
	 * Prevent implementations outside of this package.
	 */
	public Unimplementable unimplementable() {
		return null;
	}

	/**
	 * Whether the given user is non-<code>null</code> and marked as administrator.
	 */
	public static boolean isAdmin(Person theUser) {
		return theUser != null && theUser.isAdmin();
	}

}
