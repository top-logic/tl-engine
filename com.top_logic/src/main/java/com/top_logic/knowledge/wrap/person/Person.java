/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.person;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.top_logic.base.security.attributes.PersonAttributes;
import com.top_logic.base.security.device.SecurityDeviceFactory;
import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.base.user.UserDataObject;
import com.top_logic.base.user.UserInterface;
import com.top_logic.base.user.UserService;
import com.top_logic.base.user.douser.DOUser;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.time.TimeZones;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.NamedValues;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.persist.DataManager;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.AssociationQuery;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.db2.AssociationSetQuery;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.knowledge.wrap.exceptions.InvalidWrapperException;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLType;
import com.top_logic.model.core.Author;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.wrap.AbstractBoundWrapper;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.util.Utils;
import com.top_logic.util.license.LicenseTool;

/**
 * Wrapper for {@link com.top_logic.knowledge.objects.KnowledgeObject KnowledgeObjects} of
 * type Person.
 *
 * @author <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 */
@Label("account")
public class Person extends AbstractBoundWrapper implements Author {

    /**
     * Type of KO as defined by KBMeta.xml
     */
    public static final String OBJECT_NAME = "Person";

	/** Full qualified name of the {@link TLType} of a {@link Person}. */
	public static final String PERSON_TYPE = "tl.accounts:Person";

	/**
	 * Resolves {@link #PERSON_TYPE}.
	 * 
	 * @implNote Casts result of {@link TLModelUtil#resolveQualifiedName(String)} to
	 *           {@link TLStructuredType}. Potential {@link ConfigurationException} are wrapped into
	 *           {@link ConfigurationError}.
	 * 
	 * @return The {@link TLStructuredType} representing the {@link Person}s.
	 * 
	 * @throws ConfigurationError
	 *         iff {@link #PERSON_TYPE} could not be resolved.
	 */
	public static TLStructuredType getPersonType() throws ConfigurationError {
		return (TLStructuredType) TLModelUtil.resolveQualifiedName(PERSON_TYPE);
	}

    /** Attribute indicating whether the person has been notified about his unused account. */
    public static final String ATTR_UNUSED_NOTIFIED = "unusedNotified";

	/**
	 * Name of the {@link #getLastPasswordChange()} attribute.
	 */
	public static final String LAST_PWD_CHANGE = "lastPwdChange";

    protected static final String GLOBAL_ROLE_KA = "hasGlobalRole";
    
    /**
     * Placeholder for actual data device as should be used for persons initially in KB
     * will be replaced with default DataAccessDevice automatically
     */
    protected static final String PLACE_HOLDER_DEFAULT_DATA_ACCESS_ID = "defaultDataAccessID";

    /**
     * Placeholder for actual AuthenticationDevice as should be used for persons initially in KB
     * will be replaced with default AuthenticationDevice automatically
     */    
    protected static final String PLACE_HOLDER_DEFAULT_DATA_AUTH_ID   = "defaultAuthenticationID";
    
	private static final AssociationSetQuery<KnowledgeAssociation> GLOBAL_ROLES_ATTR = AssociationQuery
		.createOutgoingQuery("globalRoles",
		Person.GLOBAL_ROLE_KA);

    /**
     * Set to true when this person was known as alive - that means its user data was found in the security system
     * It is set to false, when the security system is available, but no user data for this person was found, which means it has been deleted from this security system
     * 
     * This info is used to determine the state of a person (alive or externally deleted) when the security system is not available, i.e. due to a network problem
     * 
     * For newly created persons, this attribute is initialized automatically (with true, otherwise, the creation would fail)
     * If running this version on an old database, this attribute can be null for persons which didn't have this attribute before.
     * Null means not alive (same as externally deleted). It will be set automatically as soon as the security system, the person refers to can be reached and the persons state can be determined 
     */
    private static final String WAS_ALIVE = "wasAlive";

    /**
     * cache of the KOAttribute WAS_ALIVE
     */
    private Boolean wasAlive = null;
    
    /**
     * Transient flag to indicate if the deletion of this persons user already
     * has been handled or not. Avoids executing this handling every
     * refresh cycle if this persons user is found to have been deleted
     */
    private boolean userDeletionHandled = false;

    /**
     * Reference to a backed up user object which is used/returned in case of the
     * actual user being deleted from security or the security system currently
     * not being available (due to network problems or alike)
     */
    private UserInterface userBackup = null;
    
    /**
     * Caches the representative group for this person.
     * 
     * The representative group of a person may never change,
     * therefore it can be cached here.
     */
    private Group representativeGroup = null;
    
	private PersonManager personManager;
    
	/**
	 * A filter which accepts exactly the persons, which are declared as restricted user.
	 * 
	 * @see #isRestrictedUser()
	 */
    public static final Filter<Person> RESTRICTED_USER_FILTER = new Filter<>() {

		@Override
		public boolean accept(Person aPerson) {
			return aPerson != null && aPerson.isRestrictedUser().booleanValue();
		}
	};

	/**
	 * A filter which accepts exactly the persons, which are NOT declared as restricted user.
	 * 
	 * @see #isRestrictedUser()
	 */
	public static final Filter<Person> FULL_USER_FILTER = new Filter<>() {

		@Override
		public boolean accept(Person aPerson) {
			return aPerson != null && !aPerson.isRestrictedUser().booleanValue();
		}
	};

	private Person current;

    /**
     * Ctor to be used ONLY by WrapperFactory.
     * 
     * @param ko   a KnowledgeObject of type Ticket
     */
    public Person(KnowledgeObject ko) {
        super(ko);
    }
    
    /* (non-Javadoc)
     * @see com.top_logic.knowledge.wrap.Wrapper#getDAP()
     */
    @Override
	public DataAccessProxy getDAP() {
        return null; //Persons have no DAP
    }
    /* (non-Javadoc)
     * @see com.top_logic.knowledge.wrap.Wrapper#getDSN()
     */
    @Override
	public String getDSN() {
        return null; //Persons have no DSN
    }

    /**
	 * Get the {@link TimeZone} of the person.
	 * 
	 * @return the {@link TimeZone} of the person.
	 */
	public TimeZone getTimeZone() {
		String timeZoneData = tGetDataString("timezone");
		if (timeZoneData == null) {
			return TimeZones.defaultUserTimeZone();
		}
		return Utils.parseTimeZone(timeZoneData);
	}

	/**
	 * This method sets the {@link TimeZone} of the person.
	 * 
	 * @param timeZone
	 *        The new {@link TimeZone}
	 */
	public void setTimeZone(TimeZone timeZone) {
		if (this != current) {
			current.setTimeZone(timeZone);
			return;
		}

		if (timeZone == null) {
			tSetData("timezone", null);
			return;
		}
		tSetData("timezone", Utils.formatTimeZone(timeZone));
	}

	/**
     * Get the locale of the person.
     * 
     * @return the locale of the person.
     */
	public Locale getLocale() {
		return Utils.parseLocale(tGetDataString(PersonAttributes.LOCALE));
    }
    
    /**
	 * Sets the locale of the person.
	 * 
	 * @param locale
	 *        The new {@link Locale}.
	 */
	public void setLocale(Locale locale) {
    	if (this != current) {
			current.setLocale(locale);
    		return;
    	}
    	
		if (locale == null) {
			tSetData(PersonAttributes.LOCALE, null);
		} else {
			tSetData(PersonAttributes.LOCALE, Utils.formatLocale(locale));
		}
    }

    /**
     * returns a PersonalConfiguration for this Person
     * 
     * @return the PersonalConfiguration of this Person or <code>null</code> if none exist
     * 
     * @deprecated use TLContext directly.
     */
    @Deprecated
	public PersonalConfiguration getPersonalConfiguration(){
        return PersonalConfiguration.getPersonalConfiguration();
    }
    
    /**
     * the ID of the PersonDataAccessDevice, this person retrieves its data from
     */
	public String getDataAccessDeviceID() {
        String theName = this.getName();
        String theDeviceID = (String)getValue(PersonAttributes.DATA_ACCESS_DEVICE_ID);
        if(StringServices.isEmpty(theDeviceID) || PLACE_HOLDER_DEFAULT_DATA_ACCESS_ID.equals(theDeviceID)){ //happens only during the initial startup call
            theDeviceID = TLSecurityDeviceManager.getInstance().getDefaultDataAccessDevice().getDeviceID();
            Logger.info("Person "+theName+" has no data access device, using '" + theDeviceID + "'. This is normal on first startup for system persons who initially are in the knowledgebase.",this);
            this.setDataAccessDeviceID(theDeviceID);
        }
        return theDeviceID;
    }

    /**
     * the ID of the AuthenticationDevice, this person can be authenticated against
     */
	public String getAuthenticationDeviceID() {
        String theName = this.getName();
        String theDeviceID = (String)getValue(PersonAttributes.AUTHENTICATION_DEVICE_ID);
        if(PLACE_HOLDER_DEFAULT_DATA_AUTH_ID.equals(theDeviceID)){ //happens only during the initial startup call
            theDeviceID = TLSecurityDeviceManager.getInstance().getDefaultAuthenticationDevice().getDeviceID();
            Logger.info("Person "+theName+" has no authentication device, using '" + theDeviceID + "'. This is normal on first startup for system persons who initially are in the knowledgebase.",this);
            this.setAuthenticationDeviceID(theDeviceID);
        }
        return theDeviceID;
    }

    /**
     * Check if deletion has already been handled by the system.
     * 
     * @return true if the user deletion has definitely been handled
     */
    public boolean getIsDeletionHandled() {
        return userDeletionHandled;
    }

    /**
     * Set if the user has already deletion has already been handled by the
     * system.
     * 
     * @param isDeleted if true the user deletion has definitely been handled
     */
    public void setIsDeletionHandled(boolean isDeleted) {
    	if (this != current) {
    		current.setIsDeletionHandled(isDeleted);
    		return;
    	}
    	
    	userDeletionHandled = isDeleted;
    }

    /**
     * a backup of the actual user of this person, which is directly
     *         read from the DB. Returns null, if this person has no backup, yet
     */
	public UserInterface getUserBackup() {
        if (this.userBackup == null) {
            DataManager theDM = DataManager.getInstance();
			DataObject theDO;
			try {
				theDO = theDM.load(UserDataObject.META_OBJECT_NAME, KBUtils.getWrappedObjectName(this));
			} catch (SQLException ex) {
				Logger.error("Unable to load user interface for person " + KBUtils.getWrappedObjectName(this), ex,
					Person.class);
				theDO = null;
			}
            if (theDO != null) {
                // Fill in attributes that were null and so were not written by
                // the DataManager and so are not reconstructed in the DataObject and so access
                // to these attributes throws a NoSuchAttributeException
                MetaObject theMO = theDO.tTable();
                for (int i = 0; i < PersonAttributes.PERSON_INFO.length; i++) {
                    String theAttName = PersonAttributes.PERSON_INFO[i];
                    if (!MetaObjectUtils.hasAttribute(theMO, theAttName)) {
                        try {
                            theDO.setAttributeValue(theAttName, "");
                        } catch (Exception ex) {
                            // 
                        }
                    }
                }
                this.userBackup = DOUser.getInstance(theDO);
            }
        }

        return this.userBackup;
    }

    /**
     * Copies the data of the given user into the backup user. If no backup user
     * exists, yet, it creates one for this person
     */
	void updateUserBackup(UserInterface actualUser) {
    	if (this != current) {
    		current.updateUserBackup(actualUser);
    		return;
    	}
    	
        UserInterface currentUserBackup = getUserBackup();
        boolean deleteBackup = actualUser == null && currentUserBackup != null;
        boolean updateBackup = actualUser != null && currentUserBackup != null;
        boolean createBackup = actualUser != null && currentUserBackup == null;
        if (deleteBackup) {         //else both is null, so do nothing
            try {
                boolean success = DataManager.getInstance().delete(currentUserBackup);
                this.userBackup = null;
                if (!success) {
                    Logger.error("updateUserBackup(): Deletion of UserBackup failed for unknown reasons", this);
                }
            } catch (Exception e) {
                Logger.error("problem deleting user backup", e, this);
            }
            return; //backup deleted, return
        }
        if (createBackup) { //create empty user object and set identifier
            currentUserBackup = UserService.getEmptyUser(this.getName());
            currentUserBackup.setIdentifier(KBUtils.getWrappedObjectName(this));
        }
        if (updateBackup || createBackup) {
            // copy actual data to backup
            if (copyAnyThingExceptID(actualUser, currentUserBackup)) {
                try {
                    DataManager.getInstance().store(currentUserBackup); //save
                    this.userBackup = currentUserBackup;
                } catch (Exception e) {
                    Logger.error("problem storing user backup", e, this);
                }
            }
        }
    }

    /**
     * The method which actual copies the data from one user object to another
     * 
     * @return true if at least one difference between copied source and target
     *         attributes was found, false if source and target were equal
     */
	public static boolean copyAnyThingExceptID(NamedValues source, NamedValues target) {
        String[] attrNamesSrc = source.getAttributeNames();
        String[] attrNamesTgt = target.getAttributeNames();
        Object tgtAttribValue = null;
        Object srcAttrbValue = null;
        boolean result = false;
        for (int idx = 0; idx < attrNamesSrc.length; idx++) {
            String attrName = attrNamesSrc[idx];
            if (!attrName.equals(PersonAttributes.USER_NAME)) {
                try {
                    if (StringServices.contains(attrNamesTgt, attrName)) {
                        tgtAttribValue = target.getAttributeValue(attrName);
                        srcAttrbValue = source.getAttributeValue(attrName);
                        boolean changed = !((tgtAttribValue != null && tgtAttribValue.equals(srcAttrbValue)) || (tgtAttribValue == srcAttrbValue));
                        result = result || changed;
                        if (changed) {
                            target.setAttributeValue(attrName, srcAttrbValue);
                        }
                    }
                } catch (Exception e) {
                    Logger.error("Unexpected Error when copying attribute from current user to backup.", e, Person.class);
                }

            }
        }
        return result;
    }

    /**
     * True when getUser() would only return a (stale) backup copy of the actual user.
     * 
     * That is if the person's security device cannot be accessed momentarily and
     * calls to getUser() therefore cannot return the actual user object, but a
     * previously saved copy. Same is true for persons which users have been
     * deleted. These persons also return the backed up user to allow access to
     * the last known user data. However- the latter are not alive while the former are.
     * 
     * @return true when you can only get the backup user.
     */
	public boolean isBackupMode() {
		UserInterface user = getUser();
        return user != null && KBUtils.getWrappedObjectName(this).equals(user.getIdentifier());
    }

    /**
     * This is true if this person was alive when the according security system
     * was asked last time and false otherwise if this attribute has not been
     * set before, false is returned
     * 
     * @return the last know alive state.
     */
    public boolean wasAlive() {
        if (wasAlive == null) {
			this.wasAlive = tGetDataBoolean(WAS_ALIVE);
        }
		return wasAlive != null && wasAlive.booleanValue();
    }

    /**
     * Sets the alive state of this person to what was found when trying the
     * fetch this persons user from security system. That is true if the user was
     * found and false if the user was not found even though the device was
     * available
     * 
     * @param flag
     *            will be ignores if same as current state
     */
    private void setLastKnownAliveState(boolean flag) {
    	if (this != current) {
    		if (current == null) {
    			// Already known to be dead.
    			return;
    		}
    		current.setLastKnownAliveState(flag);
    		return;
    	}
    	
    	if (flag != wasAlive() || this.wasAlive == null) {
    		Boolean bFlag = Boolean.valueOf(flag);
    		this.wasAlive = bFlag;
    		getPersonManager().aliveStateChanged(current);
    	}
    }
    
    /**
     * Get the representative group for this person. 
     * 
     * @return the representative group if it exists, null otherwise
     */
    public Group getRepresentativeGroup() {
        if (this.representativeGroup != null) {
            return this.representativeGroup;
        } else {
            try{
                Collection<Group> definedGroups = Group.getDefinedGroups(this);
                switch (definedGroups.size()) {
                	case 0: 
                	// Once all Persons are supposed to have a rep group, this warning should be enabled.
                	// at the moment there are exceptions such as root, refer the manual
                	// if (theGroup == null){
                	//Logger.warn("Person "+getName()+" found to have no representative group",this);
                	// }
                		break;
					case 1:
						this.representativeGroup = definedGroups.iterator().next();
						break;
					default:
						this.representativeGroup = definedGroups.iterator().next();
						Logger.error("More than one group defined by Person " + this + ": " + definedGroups,
							Person.class);
						break;
                }
                
                return this.representativeGroup;
            } catch (Exception e) {
				Logger.error("Failed to get representative Group for person", e, Person.class);
                return null;
            }
        }
    }

    /**
     * Callback from manager
     */
    void storeChangedAliveState(){
    	if (this != current) {
    		current.storeChangedAliveState();
    		return;
    	}
    	
		this.tSetData(WAS_ALIVE, this.wasAlive);
    }
    /**
     * Called if the user of this person was found in security and is going to
     * be updated in the cache. Last known alive state of this person is set to
     * true by this call
     */
    private void putInAliveCache(UserInterface user) {

		// The current is null if this is a historic person which was deleted and the
		// system cannot access the DataAccessDevice now.
		// @see #getUser() and the catch-block in there
		if (current == null) {
			return;
		}
        getPersonManager().connect(current, user);
        setLastKnownAliveState(true);
        this.setIsDeletionHandled(false);
    }

	/**
	 * Null-safe account information retrieval for the given account.
	 */
	public static UserInterface getUser(Person person) {
		if (person == null) {
			return null;
		}
		return person.getUser();
	}

	/**
	 * Get the DOUser for the Person.
	 *
	 * @return the User of this person, or the backup if the secDevice could not be reached or this
	 *         persons user has been deleted
	 */
	protected UserInterface getUser() {
        this.checkInvalid();

		// User has been deleted before, use data from backup user.
		if (this.current == null) {
			return this.getUserBackup();
		}

        return internalGetUser();
    }

	private UserInterface internalGetUser() {
		// check if person has a user being cached
        PersonManager pm = getPersonManager();
		UserInterface ui = pm.getUser(current);
		if (ui == null) {
            try {
				{
					UserInterface tmp = getUserBackup();
					if (tmp.getUserName().equals(PersonManager.getManager().getSuperUserName())
						|| LicenseTool.moreUsersAllowed(LicenseTool.getInstance().getLicense(), current.isRestrictedUser())) {
						// user not found in security (user has been
						// deleted) note: person is not put into alive
						// user map, as this person is not alive
						setLastKnownAliveState(false);
						ui = tmp;
					}
				}
				return ui;
			} catch (Exception e) {
                Logger.warn(//SecDev not accessible
                        "Could not access security system " + this.getDataAccessDeviceID() + " working on backup user", e, this);
                // security service not reachable
                ui = this.getUserBackup();
                if (ui != null && wasAlive()) {
                    // using backup - thats okay, person is alive if it
                    // was known as alive before
                    putInAliveCache(ui);
                }
                return ui;
            }
        }
        return ui;
	}

    /**
     * Get the global roles as a Collection of BoundedRoles
     * 
     * @return the global roles. May be empty but not <code>null</code>.
     */
	public Collection getGlobalRoles() {
        return resolveWrappers(GLOBAL_ROLES_ATTR);
    }
    
    /**
     * Add a global role to the Person
     * 
     * @param aGlobalRole    the global role. May be <code>null</code> (code has no effect then).
     * @throws DataObjectException if creation of the KA fails
     */
	public void addGlobalRole(BoundedRole aGlobalRole) throws DataObjectException {
        if (aGlobalRole == null) {
            return;
        }
        
        KnowledgeObject      theSource = this.tHandle();
        KnowledgeObject      theDest   = aGlobalRole.tHandle();
		theSource.getKnowledgeBase().createAssociation(theSource, theDest, Person.GLOBAL_ROLE_KA);
    }
    
    /**
     * Remove a global role from the Person
     * 
     * @param aGlobalRole    the global role. May be <code>null</code> (code has no effect then).
     * @throws DataObjectException if removal of the KA fails
     */
	public void removeGlobalRole(BoundedRole aGlobalRole) throws DataObjectException {
    	if (this != current) {
    		current.removeGlobalRole(aGlobalRole);
    		return;
    	}
    	
        if (aGlobalRole == null) {
            return;
        }
		KBUtils.deleteAllKI(tHandle().getOutgoingAssociations(Person.GLOBAL_ROLE_KA, aGlobalRole.tHandle()));
    }
    
    /**
     * the customer name of this user
     */
	public String getCustomerName() {
		return getUser().getCustomerName();
    }

    /**
     * the external email of this user
     */
	public String getExternalMail() {
		return getUser().getExternalMail();
    }

    /**
     * the external number of this user
     */
	public String getExternalNumber() {
		return getUser().getExternalNumber();
    }

    /**
     * the firstname of this user
     */
	public String getFirstName() {
		return getUser().getFirstName();
    }

    /**
     * Return the full name of the person -
     * a formatted String for Username, should be "Title FirstName LastName".
     * 
     * If this person is invalid, the method will return "invalid person",
     * if there is no user defined for this person, it'll return the 
     * {@link #getName() name} of this person. If something fails in
     * requesting the name, the method will return "unknown".
     *  
     * @return    The full name of the person as mentioned above.
     */
	public String getFullName() {
		String theFullName = getUser().getFullName();
        if (StringServices.isEmpty(theFullName)) {
            // TODO this is not better than the above ?
            theFullName = this.getFirstName() + ' ' + this.getLastName();
        }
        return theFullName;
    }


    /**
     * the internal email of this user
     */
	public String getInternalMail() {
		return getUser().getInternalMail();
    }

    /**
     * the internal number of this user
     */
	public String getInternalNumber() {
		return getUser().getInternalNumber();
    }

    /**
     * the lastname of this user
     */
	public String getLastName() {
		return getUser().getLastName();
    }

    /**
     * the mobile number of this user
     */
	public String getMobileNumber() {
		return getUser().getMobileNumber();
    }

    /**
     * get formatted username: lastname, Title firstname.
     * Suppress Title via param includeTitle
     * 
     * @param  includeTitle true to include, false to suppress 
     * @return formatted username
     */
	public String getNameAs_LastTitleFirst(boolean includeTitle) {
		return getUser().getNameAs_LastTitleFirst(includeTitle);
    }

    /**
     * the organization unit of this user
     */
	public String getOrgUnit() {
		return getUser().getOrgUnit();
    }

    /**
     * the private number of this user
     */
	public String getPrivateNumber() {
		return getUser().getPrivateNumber();
    }

    /**
     * the title of this user
     */
	public String getTitle() {
		return getUser().getTitle();
    }

    /**
     * Deletes this persons user
     * 
     */
	void deleteUser() {
        SecurityDeviceFactory.getPersonAccessDevice(getDataAccessDeviceID()).deleteUserData(this.getName());
        this.resetUser();
    }

    /**
     * Forces the person to re-read its user from security. All caches are
     * updated depending on the result
     */
     void resetUser() {
    	getPersonManager().disconnect(current);
        // reload the user to make sure it is cached again
        // in the valid persons cache
		getUser();
    }
     
     @Override
	public void refetchWrapper() {
    	super.refetchWrapper();
    	resetUser();
    }

    /**
     * This method is for usage during initialization / user refresh only. It is
     * used to set a Person to "Alive" if its user was amongst those being
     * found in the SecurityDevices without the need for each Person to query
     * its user individually (as would be done by resetUser()) for performance
     * reasons.
     * 
     * This is useful as initially all Users are read from security anyway and
     * then the Persons are resolved / created according to them.
     * 
     * This method does exactly the same as if this Person would query the user
     * itself in the resetUser() method - just uses the given user instead of
     * querying it from security.`
     * 
     * It can set the given user only if it originates from the same security device
     * as the person is referring to. Usually the security device is set into the users
     * DO when it is read by one of the security accesses. If the userDo has cannot
     * provide info about its secDev or it originates from a different secDev, this method
     * will return false.
     * If the secDev of  the user and this person match, the user is taken and true is returned.
     * @param ui -
     *            the User belonging to this person
     */
     boolean setUser(UserInterface ui) {
    	 if (this != current) {
    		 return current.setUser(ui);
    	 }
    	 
        String userSecDev = null;
        String ownSecDev = null;
        try {
			userSecDev = ui.getDataAccessDeviceID();
            ownSecDev = this.getDataAccessDeviceID(); 
        } catch (NoSuchAttributeException nae) {
            return false;// userDo cannot provide secDevID, that means setUser is not possible
        } catch (Exception e) {
            Logger.error("Error getting secDev from user", e, this);
            return false;
        }
        
        if (!StringServices.isEmpty(userSecDev) && userSecDev.equals(ownSecDev)) {
            putInAliveCache(ui);
			updateUserBackup(ui);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Can be used to change the security device of a person during its
     * lifecycle, e.g. if this userID was first present in one active directory
     * and is later moved into another
     */
	public void setDataAccessDeviceID(String securityDeviceName) {
    	if (this != current) {
    		current.setDataAccessDeviceID(securityDeviceName);
    		return;
    	} 
		if(StringServices.isEmpty(securityDeviceName)){
			Logger.warn("Attempting to set an empty dataAccessDeviceID. Will be ignored.",this);
			return;
		}
		this.tSetData(PersonAttributes.DATA_ACCESS_DEVICE_ID, securityDeviceName);
    }
    
    /**
     * Can be used to change the AuthenticationDevice of a person during its
     * lifecycle
     */
	public void setAuthenticationDeviceID(String securityDeviceName) {
    	if (this != current) {
    		current.setAuthenticationDeviceID(securityDeviceName);
    		return;
    	}
    	
		this.tSetData(PersonAttributes.AUTHENTICATION_DEVICE_ID, securityDeviceName);
    }

    
    /**
     * whether this person still is "alive" which means for this KO
     *         exists an alive user This is true if the user can be read
     *         directly from security. Also returns true if the security system
     *         cannot be reached currently although this person would be in
     *         backup mode then. If however, the security system IS accessible and
     *         it is found that this persons user data has been deleted from
     *         security device, then this person would NOT be alive anymore. It
     *         would be still in backup mode, though.
     */
    public boolean isAlive() {
		{
            // all the getUser() call is for, is to force an update
            // if this person should not be alive already. So it could have
            // changed its state after calling this method
			return tValid() && getUser() != null
                && getPersonManager().isKnown(current);
        }
    }

    /**
     * Overridden do remove the Person from the allUser table.
     * 
     * @see com.top_logic.knowledge.wrap.AbstractWrapper#checkInvalid()
     */
    @Override
	protected void checkInvalid() throws InvalidWrapperException {
        try {
            super.checkInvalid();
        } catch (InvalidWrapperException e) {
            // if this person is found to be invalid,
            // remove it from the alive person cache
        	if (this == current) {
        		getPersonManager().disconnect(current);
        	}
            throw e;
        }
    }
    
    /**
     * If this persons user data were changed through the application,
     * this method triggers the person to write back the changed data into
     * its data device
     */
	public void updateUserData() {
    	if (this != current) {
    		current.updateUserData();
    		return;
    	}
        this.resetUser();
    }
    
    /**
     * Static utility method to create the corresponding representative group for the given
     * person. Should be called for newly created persons.
     *
     * @param aPerson
     *        the person to create the representative group for
     */
    public static void createRepresentativeGroup(Person aPerson){
        if (aPerson.getRepresentativeGroup() == null) {
            try{
                Group thePersonsGroup = Group.createGroup(aPerson.getName());
                thePersonsGroup.setIsSystem(true);
                thePersonsGroup.bind(aPerson);
            } catch(Exception e) {
                Logger.error("Failed to create personal group for person", e, Person.class);
            }
        }
    }

	/**
	 * This method determines whether this person is a restricted user, i.e. whether it has
	 * restricted rights due to license reasons.
	 */
	public Boolean isRestrictedUser() {
		Boolean restrictedUser = getBoolean(PersonAttributes.RESTRICTED_USER);
		if (restrictedUser == null) {
			restrictedUser = Boolean.FALSE;
		}
		return restrictedUser;
	}
	
	/**
	 * Sets the "restricted user" property.
	 * 
	 * @param isRestrictedUser
	 *            must not be <code>null</code>
	 * @return <code>true</code> iff the value of this property changed.
	 */
	public boolean setRestrictedUser(Boolean isRestrictedUser) {
    	if (this != current) {
			return current.setRestrictedUser(isRestrictedUser);
    	}
    	
		Boolean currVal = getBoolean(PersonAttributes.RESTRICTED_USER);

		if (!isRestrictedUser.equals(currVal)) {
			setValue(PersonAttributes.RESTRICTED_USER, isRestrictedUser);
			return true;
		}
		return false;
	}

	void init(PersonManager personManager, Person current) {
		this.personManager = personManager;
		this.current = current;
	}

	/**
	 * The representative in the current revision.
	 * 
	 * @return The current version of this object, <code>null</code>, if this
	 *         {@link Person} is deleted.
	 */
	public Person getCurrent() {
		return current;
	}

	private PersonManager getPersonManager() {
		if (personManager == null) {
			/* This may happen when this person object is not created or accessed using the
			 * PersonManager but using the general WrapperFactory mechanism. */
			Logger.info(
				"Person '" + this + "' not already initialized, use default " + PersonManager.class.getSimpleName(),
				Person.class);
			Wrapper currentVersion = WrapperHistoryUtils.getCurrent(this);
			init(PersonManager.getManager(), (Person) currentVersion);
		}
		return personManager;
	}

	@Override
	protected void handleDelete() {
		super.handleDelete();
		if (personManager != null) {
			personManager.disconnect(this);
		}
	}

	/**
	 * Whether this {@link Person} has any of the given {@link Group}s.
	 */
	public boolean isInAnyGroup(Iterable<? extends Group> groups) {
		for (Group group : groups) {
			if (isInGroup(group)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Whether this {@link Person} is member of the given {@link Group}.
	 */
	public boolean isInGroup(Group group) {
		return group.containsPerson(this);
	}

	/**
	 * The date when the password for this account has changed last time.
	 */
	public Date getLastPasswordChange() {
		Date lastChange = (Date) getValue(Person.LAST_PWD_CHANGE);
		if (lastChange == null) {
			return getCreated();
	    }
		return lastChange;
	}

	/**
	 * @see #getLastPasswordChange()
	 */
	public void setLastPasswordChange(Date changeDate) {
		setValue(Person.LAST_PWD_CHANGE, changeDate);
	}

	/**
	 * Whether this account is marked as super user.
	 */
	public boolean isAdmin() {
		if (!isAlive()) {
			return false;
		}

		return tGetDataBooleanValue("admin");
	}

	/**
	 * @see #isAdmin()
	 */
	public void setAdmin(boolean value) {
		tSetDataBoolean("admin", value);
	}

	/**
	 * Null-safe check for the given {@link Person} to have administrative rights.
	 */
	public static boolean isAdmin(Person aUser) {
		if (aUser == null) {
			return false;
		}
	
		return aUser.isAdmin();
	}

}
