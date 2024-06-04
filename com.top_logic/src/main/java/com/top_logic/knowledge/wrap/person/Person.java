/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.person;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.base.security.device.interfaces.AuthenticationDevice;
import com.top_logic.base.services.InitialGroupManager;
import com.top_logic.base.user.UserInterface;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.TLID;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.time.TimeZones;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.NamedValues;
import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.AssociationQuery;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.db2.AssociationSetQuery;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.util.ItemByNameCache;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLType;
import com.top_logic.model.core.Author;
import com.top_logic.model.provider.DefaultCountryDefault;
import com.top_logic.model.provider.DefaultLocaleDefault;
import com.top_logic.model.provider.UserTimeZoneDefault;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.wrap.AbstractBoundWrapper;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.util.Country;
import com.top_logic.util.Utils;

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

	/**
	 * Attribute to identify a person who is registered as restricted user, i.e. a user who can do
	 * nothing but read in the application
	 */
	public static final String RESTRICTED_USER = "restrictedUser";

	/**
	 * @see #getAuthenticationDeviceID()
	 **/
	public static final String AUTHENTICATION_DEVICE_ID = "authDeviceID";

	/** The attribute "locale". */
	public static final String LOCALE = "locale";

	/** The attribute "language". */
	public static final String LANGUAGE_ATTR = "language";

	/** The attribute "country". */
	public static final String COUNTRY_ATTR = "country";

	/** Full qualified name of the {@link TLType} of a {@link Person}. */
	public static final String PERSON_TYPE = "tl.accounts:Person";

	private static volatile ItemByNameCache<String> BY_NAME_CACHE;

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

    protected static final String GLOBAL_ROLE_KA = "hasGlobalRole";
    
	private static final AssociationSetQuery<KnowledgeAssociation> GLOBAL_ROLES_ATTR = AssociationQuery
		.createOutgoingQuery("globalRoles",
		Person.GLOBAL_ROLE_KA);
    
    /**
     * Caches the representative group for this person.
     * 
     * The representative group of a person may never change,
     * therefore it can be cached here.
     */
    private Group representativeGroup = null;
    
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
	 * 
	 * @implNote The {@link Person#tType() type} of a {@link Person} does not exist in tl-core,
	 *           therefore the access must happen over the KB attribute.
	 */
	public Locale getLocale() {
		return Utils.parseLocale(computeLocale());
    }
    
	/**
	 * Computes the String representation of the {@link #getLocale()} based on the
	 * {@link #getLanguage() language} and {@link #getCountry() country} of this {@link Person}.
	 * 
	 * @implNote This is the implementation of the attribute `tl.accounts:Person#locale`.
	 */
	@CalledByReflection
	public final String computeLocale() {
		Locale language = getLanguage();
		Country country = getCountry();
		if (language == null) {
			if (country == null) {
				return null;
			} else {
				return "_" + country.getCode();
			}
		} else {
			if (country == null) {
				return language.getLanguage();
			} else {
				return language.getLanguage() + "_" + country.getCode();
			}
		}
	}

	/**
	 * Access to the attribute {@link #LANGUAGE_ATTR} of type {@link Person}.
	 * 
	 * @implNote The {@link Person#tType() type} of a {@link Person} does not exist in tl-core,
	 *           therefore the access must happen over the KB attribute.
	 */
	public Locale getLanguage() {
		return Utils.parseLocale(tGetDataString(LANGUAGE_ATTR));
	}

	/**
	 * Setter for {@link #getLanguage()}.
	 * 
	 * @implNote The {@link Person#tType() type} of a {@link Person} does not exist in tl-core,
	 *           therefore the access must happen over the KB attribute.
	 */
	public void setLanguage(Locale newValue) {
		tSetData(LANGUAGE_ATTR, Utils.formatLocale(newValue));
	}

	/**
	 * Access to the attribute {@link #COUNTRY_ATTR} of type {@link Person}.
	 * 
	 * @implNote The {@link Person#tType() type} of a {@link Person} does not exist in tl-core,
	 *           therefore the access must happen over the KB attribute.
	 */
	public Country getCountry() {
		String countryString = tGetDataString(COUNTRY_ATTR);
		if (StringServices.isEmpty(countryString)) {
			return null;
		}
		return new Country(countryString);
	}

	/**
	 * Setter for {@link #getCountry()}.
	 * 
	 * @implNote The {@link Person#tType() type} of a {@link Person} does not exist in tl-core,
	 *           therefore the access must happen over the KB attribute.
	 */
	public void setCountry(Country newValue) {
		if (newValue == null) {
			tSetData(COUNTRY_ATTR, null);
		} else {
			tSetData(COUNTRY_ATTR, newValue.getCode());
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
	 * The ID of the {@link AuthenticationDevice}, this account can be authenticated with.
	 */
	public String getAuthenticationDeviceID() {
		return tGetDataString(Person.AUTHENTICATION_DEVICE_ID);
    }

	/**
	 * The {@link AuthenticationDevice} responsible for authenticating this {@link Person}.
	 */
	public AuthenticationDevice getAuthenticationDevice() {
		String authDevice = getAuthenticationDeviceID();
		if (StringServices.isEmpty(authDevice)) {
			return null;
		}
		return TLSecurityDeviceManager.getInstance().getAuthenticationDevice(authDevice);
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
            if (!attrName.equals(UserInterface.USER_NAME)) {
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
	 * User information for this account.
	 * 
	 * @return The account information for this account, or <code>null</code> when not available (if
	 *         the contact module is not linked to the application).
	 */
	public UserInterface getUser() {
		return null;
    }

	/**
	 * @see #getUser()
	 */
	public void setUser(UserInterface user) {
		throw new UnsupportedOperationException("No contact module available.");
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
        if (aGlobalRole == null) {
            return;
        }
		KBUtils.deleteAllKI(tHandle().getOutgoingAssociations(Person.GLOBAL_ROLE_KA, aGlobalRole.tHandle()));
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
		UserInterface user = getUser();
		if (user == null) {
			return getName();
		}

		String title = user.getTitle();
		String first = user.getFirstName();
		String last = user.getName();

		StringBuilder result = new StringBuilder();
		if (!StringServices.isEmpty(title)) {
			result.append(title.trim());
		}

		if (!StringServices.isEmpty(first)) {
			if (result.length() > 0) {
				result.append(' ');
			}
			result.append(first.trim());
		}

		if (!StringServices.isEmpty(last)) {
			if (result.length() > 0) {
				result.append(' ');
			}
			result.append(last.trim());
		}

		if (result.length() == 0) {
			return getName();
        }

		return result.toString();
    }

    /**
     * Can be used to change the AuthenticationDevice of a person during its
     * lifecycle
     */
	public void setAuthenticationDeviceID(String securityDeviceName) {
		this.tSetData(Person.AUTHENTICATION_DEVICE_ID, securityDeviceName);
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
		return tValid();
    }

	/**
	 * This method determines whether this person is a restricted user, i.e. whether it has
	 * restricted rights due to license reasons.
	 */
	public Boolean isRestrictedUser() {
		Boolean restrictedUser = getBoolean(Person.RESTRICTED_USER);
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
		Boolean currVal = getBoolean(Person.RESTRICTED_USER);

		if (!isRestrictedUser.equals(currVal)) {
			setValue(Person.RESTRICTED_USER, isRestrictedUser);
			return true;
		}
		return false;
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
	 * Create a new {@link Person} in given {@link KnowledgeBase}. Does NOT check if such a person
	 * already exists! Note: No user object will be created by this method
	 * 
	 * @param kb
	 *        the {@link KnowledgeBase} in which the person is created.
	 * @param userName
	 *        the person (login) name
	 * 
	 * @return the created person
	 */
	public static Person create(KnowledgeBase kb, String userName, String authenticationDeviceID) {
		KnowledgeObject handle = kb.createKnowledgeObject(OBJECT_NAME);
		handle.setAttributeValue(AbstractWrapper.NAME_ATTRIBUTE, userName);
		Person result = handle.getWrapper();
		result.setAuthenticationDeviceID(authenticationDeviceID);
		result.setCountry(DefaultCountryDefault.INSTANCE.defaultCountry());
		result.setLanguage(DefaultLocaleDefault.INSTANCE.defaultLocale());
		result.setTimeZone(UserTimeZoneDefault.INSTANCE.defaultUserTimeZone());

		Group representativeGroup = Group.createGroup(result.getName());
		representativeGroup.setIsSystem(true);
		representativeGroup.bind(result);

		Group defaultGroup = InitialGroupManager.getInstance().getDefaultGroup();
		if (defaultGroup != null) {
			defaultGroup.addMember(result);
		}

		PersonManager.getManager().initUser(result);

		return result;
	}

	/**
	 * All {@link Person}s that are known in the system.
	 */
	public static List<Person> all() {
		return all(PersistencyLayer.getKnowledgeBase());
	}

	/**
	 * All {@link Person}s that are known in the given {@link KnowledgeBase}.
	 */
	public static List<Person> all(KnowledgeBase kb) {
		Collection<KnowledgeObject> handles = kb.getAllKnowledgeObjects(OBJECT_NAME);
		List<Person> result = new ArrayList<>(handles.size());
		for (KnowledgeObject handle : handles) {
			Person person = handle.getWrapper();
			result.add(person);
		}
		return result;
	}

	/**
	 * Gets the {@link Person} for a given login name.
	 * 
	 * @param kb
	 *        The {@link KnowledgeBase} to look for the {@link Person}.
	 * @param name
	 *        A login name of a {@link Person}.
	 * 
	 * @return The requested account or <code>null</code> if not such account exists.
	 */
	public static Person byName(KnowledgeBase kb, String name) {
		if (kb == getDefaultKnowledgeBase()) {
			return fromCache(kb, name);
		}
		if (StringServices.isEmpty(name)) {
			return null;
		}
	
		KnowledgeObject result =
			(KnowledgeObject) kb.getObjectByAttribute(OBJECT_NAME, AbstractWrapper.NAME_ATTRIBUTE, name);
		if (result == null) {
			return null;
		}
	
		return result.getWrapper();
	}

	/**
	 * Gets the {@link Person} for a given login name.
	 * 
	 * @param name
	 *        A login name of a person.
	 * @return The requested account or <code>null</code> if not such account exists.
	 */
	public static Person byName(String name) {
		return fromCache(getDefaultKnowledgeBase(), name);
	}

	private static Person fromCache(KnowledgeBase defaultKB, String name) {
		if (StringServices.isEmpty(name)) {
			return null;
		}

		KnowledgeItem cachedPerson = getOrInstallByNameCache(defaultKB).getValue().get(name);
		if (cachedPerson == null) {
			return null;
		}
		return cachedPerson.getWrapper();
	}

	private static ItemByNameCache<String> getOrInstallByNameCache(KnowledgeBase defaultKB) {
		ItemByNameCache<String> byNameCache;
		if (BY_NAME_CACHE != null) {
			byNameCache = BY_NAME_CACHE;
		} else {
			byNameCache = new ItemByNameCache<>((DBKnowledgeBase) defaultKB, OBJECT_NAME, NAME_ATTRIBUTE, String.class);
			BY_NAME_CACHE = byNameCache;
		}
		return byNameCache;
	}

	/**
	 * Return the instance of person for the given identifier.
	 * 
	 * @param kb
	 *        the knowledge base
	 * @param historyContext
	 *        The revision number in which the requested {@link Person} was known to be alive.
	 * @param id
	 *        The Knowledge Identifier of the person KO.
	 * 
	 * @return The requested {@link Person} in the given history context, or <code>null</code>, if
	 *         there was no such account at the requested point in time.
	 */
	public static Person byId(KnowledgeBase kb, long historyContext, TLID id) {
		if (historyContext != Revision.CURRENT_REV) {
			// Fall back: Find historic version of a deleted account.
			HistoryManager historyManager = HistoryUtils.getHistoryManager(kb);
			if (historyManager.hasHistory()) {
				Revision revision = historyManager.getRevision(historyContext);
				Person historicPerson =
					(Person) WrapperFactory.getWrapper(historyManager.getTrunk(), revision, id, OBJECT_NAME);
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
	
				return historicPerson;
			}
		}
	
		return (Person) WrapperFactory.getWrapper(id, OBJECT_NAME, kb);
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
