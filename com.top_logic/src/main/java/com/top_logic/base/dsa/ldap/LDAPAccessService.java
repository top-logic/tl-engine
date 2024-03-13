/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.dsa.ldap;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import com.top_logic.base.security.attributes.GroupAttributes;
import com.top_logic.base.security.attributes.LDAPAttributes;
import com.top_logic.base.security.device.DeviceMapping;
import com.top_logic.base.security.device.ldap.LDAPAuthenticationAccessDevice;
import com.top_logic.base.security.device.ldap.LDAPDataObject;
import com.top_logic.base.security.device.ldap.LDAPMetaObject;
import com.top_logic.base.user.UserInterface;
import com.top_logic.base.user.douser.DOUser;
import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.util.ResKey;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dsa.DataAccessException;
import com.top_logic.util.TopLogicDirContext;
import com.top_logic.util.error.TopLogicException;

/**
 * @author <a href=mailto:kbu@top-logic.com>Karsten Buch</a>
 */
public class LDAPAccessService {

	/**
	 * Configuration of a {@link LDAPAccessService}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends ServiceProviderInfo.Config {

		/** Name of configuration {@link Config#isParseNestedGroups()}. */
		String PARSE_NESTED_GROUPS_NAME = "parse-nested-groups";

		/** Name of configuration {@link Config#getAttribFilter()}. */
		String ATTRIB_FILTER_NAME = "attrib-filter";

		/** Name of configuration {@link Config#getAccessGroups()}. */
		String ACCESS_GROUPS_NAME = "access-groups";

		/** Name of configuration {@link Config#getAvailableAttributes()}. */
		String AVAILABLE_ATTRIBUTES_NAME = "available-attributes";

		/**
		 * Flag to indicated whether nested groups (groups who appear to members of another group)
		 * should be parsed for userdata or not.
		 */
		@Name(PARSE_NESTED_GROUPS_NAME)
		boolean isParseNestedGroups();

		/**
		 * Additional Filter taken from config. Typically used to filter out other objects (group members)
		 * except users. i.e.: Accept only entries for type "Person" with their login Name not being empty,
		 * clould be like: attrib-filter = "(&amp;(objectClass=Person)(samAccountName=*))" When no Access
		 * groups are given (empty group-dn), this query alone is used to determine user entries. i.e:
		 * 
		 * Accept all members of group "top-logic" that are of type "Person" with their login name not being
		 * empty. could look like: attrib-filter =
		 * "(&amp;(&amp;(objectClass=Person)(samAccountName=*))(memberof=CN=top-logic,OU=BOS-Frankfurt,OU=BOS,DC=BOS,DC=local))"
		 * 
		 * That way the user entries would be looked up without explicit access to the group object.
		 */
		@Name(ATTRIB_FILTER_NAME)
		String getAttribFilter();

		@Name(ACCESS_GROUPS_NAME)
		@ListBinding(attribute = "name")
		List<String> getAccessGroups();

		/**
		 * configure the attributes that will be queried for LDAP objects Attn: Comma separated,
		 * without spaces. The given names must match the foreign System. If empty, all attributes
		 * will be queried. If given, this list must be complete (also take technical requirements
		 * into account, i.e. objectClass), other than those will not be available to the system!
		 * 
		 * It is not possible to configure objectClass specific attribute lists. All objects will be
		 * queried for the same attributes.
		 */
		@Format(CommaSeparatedStrings.class)
		@Name(AVAILABLE_ATTRIBUTES_NAME)
		List<String> getAvailableAttributes();
	}

	/**
	 * Additional Filter taken from config. Typically used to filter out other objects except users
	 */
	private String				attribFilter;
	/**
	 * A list of the full distinguished names of the groups to be searched (parsed) for users
	 */
	private List				groupDNs;
	/**
	 * A cache which holds the full distinguished names of the known group members with their
	 * <i>TopLogic</i> login name (Constants.USER_NAME) as key. This avoids unnecessary LDAP requests,
	 * as such improves performance a lot
	 */
	private Map					knownMemberDNs;
	private Map                 userDObyDN;
	/**
	 * Flag to indicated whether nested groups (groups who appear to members of another group)
	 * should be parsed for userdata or not
	 */
	private boolean				parseNestedGroups;
	
	/**
	 * The unique device ID
	 */
	private String deviceID;
	/**
	 * CFG Keys that are common to most Security Devices
	 */
	private Map		mappings;

	private String baseDN;

	/*
	 * the attribute names that will be queried for LDAP objects, attn: other than those attributes
	 * will not be available to the system
	 */
	private String[] availableAttrs;

	private final LDAPAuthenticationAccessDevice _securityDevice;

	/**
	 * Configuration params of this instance
	 */
	private ServiceProviderInfo _spi;

	/** The connections to LDAP currently not used. */
	private final List _unusedConnections = new ArrayList();

	/**
	 * Creates a new {@link LDAPAccessService} from the given configuration.
	 * 
	 * @param config
	 *        Configuration for this {@link LDAPAccessService}.
	 */
	public LDAPAccessService(String deviceId, Config config, LDAPAuthenticationAccessDevice securityDevice) {
		_spi = ServiceProviderInfo.createFromConfig(deviceId, config);
		deviceID = deviceId;
		_securityDevice = securityDevice;
		parseNestedGroups = config.isParseNestedGroups();
		attribFilter = encloseStringInBrackets(config.getAttribFilter());
		groupDNs = new ArrayList<String>();
		baseDN = config.getBaseDn();
		for (String name : config.getAccessGroups()) {
			if (StringServices.isEmpty(name)) {
				// Ignore an empty configuration to allow introducing separate configuration
				// variables for a fixed number of default group configurations.
				continue;
			}
			groupDNs.add(name);
		}

		List<String> configuredAvailableAttributes = config.getAvailableAttributes();
		if (configuredAvailableAttributes.isEmpty()) {
			availableAttrs = null;
			Logger.info("Available attribute names for " + getDeviceID() + ": All", LDAPAccessService.class);
		} else {
			availableAttrs = configuredAvailableAttributes.toArray(ArrayUtil.EMPTY_STRING_ARRAY);
			Logger.info("Available attribute names for " + getDeviceID() + ": " + configuredAvailableAttributes,
				LDAPAccessService.class);

		}

		knownMemberDNs = new HashMap();
		userDObyDN = new HashMap();
		mappings = new HashMap();
	}

	protected ServiceProviderInfo getServiceProviderInfo() {
		return this._spi;
	}

	/**
	 * Tries to create a LDAP context. To build this, this method uses the data coming from
	 * {@link #getServiceProviderInfo()}.
	 * 
	 * While trying the hostname of serviceproviderinfo is resolved into the IPs using DNS, then all
	 * of the IPs beeing configured for this hostname are tried until a context is created
	 * 
	 * @return The requested context or null if none of the IPs worked.
	 * 
	 * @throws SecurityException
	 *         If creation fails.
	 */
	protected DirContext createContext() throws NamingException {
		// get copy of the the provider infos
		return createContext(getServiceProviderInfo());
	}

	/**
	 * Tries to create a LDAP context from the given {@link ServiceProviderInfo}.
	 * 
	 * While trying the hostname of {@link ServiceProviderInfo} is resolved into the IPs using DNS,
	 * then all of the IPs being configured for this hostname are tried until a context is created.
	 * 
	 * @return The requested context or <code>null</code> if none of the IPs worked.
	 * 
	 * @throws SecurityException
	 *         If creation fails.
	 */
	public static DirContext createContext(ServiceProviderInfo spi) throws NamingException {
		return new TopLogicDirContext(spi);
	}

	/**
	 * Get the context from the LDAP.
	 * 
	 * If there is no context usable, the method will create a new one. The created context is a
	 * subclass of DirContext, to avoid the connection timeout from special LDAP services (like
	 * ADS).
	 * 
	 * @return The context to be used.
	 * 
	 * @throws SecurityException
	 *         If creation fails.
	 */
	public synchronized DirContext getContext() throws NamingException {
		DirContext theContext = null;
		// try to get a valid context from those which have been created before
		if (!_unusedConnections.isEmpty()) {
			theContext = (DirContext) this._unusedConnections.get(0);
			this._unusedConnections.remove(0);
		}
		// if it was not possible to reuse a previously created context, create
		// a new one
		if (theContext == null) {
			theContext = this.createContext();
		}
		return (theContext);
	}

	/**
	 * Release the context from the LDAP. Which means putting the given context into a list of
	 * unused contexts for later use
	 * 
	 * @param aContext
	 *        The context to be released.
	 * @param dropContext
	 *        If this caused a communication exception, it was not valid (probably IP change of AD
	 *        server), it should not be reused, but dropped then
	 */
	public synchronized void releaseContext(DirContext aContext, boolean dropContext) {
		if (!dropContext) {
			this._unusedConnections.add(aContext);
		} else {
			try {
//              ... try to close
				aContext.close();
			} catch (NamingException e1) {
				Logger.warn("failed to close ldapcontext", e1, this);
			}
//			... set to null                
			aContext = null;
			_unusedConnections.clear();// clear cache, because if this context is not valid, the
										// others in this cache aren't either
		}
	}

	/**
	 * Execute the given query and return the result of it as Enumeration. This query should define
	 * the name of the attribute and the value of that attribute. Additionally, a search DN can be
	 * specified which is appended to the base DN.
	 *
	 * @param aSearchDN
	 *        the full DN (including base) of the node to search in. If null or empty, configured
	 *        root node is used.
	 * @param aPattern
	 *        The search pattern. (the query)
	 * @param someAttr
	 *        The list of attributes to be returned. If null all attribs are returned
	 * @param searchScope
	 *        The Scope of the search as defined by the constants of this class
	 * @return The result as enumeration.
	 */
	public Enumeration executeQuery(String aSearchDN, String aPattern, String[] someAttr, int searchScope)
			throws NamingException {
		Enumeration theEnum = null;
		DirContext theContext = this.getContext();
		boolean dropContext = false;
		try {
			SearchControls theControls = new SearchControls();
			theControls.setSearchScope(searchScope);
			theControls.setReturningAttributes(someAttr);
			if (StringServices.isEmpty(aSearchDN)) {
				aSearchDN = (String) getServiceProviderInfo().get(ServiceProviderInfo.KEY_SEARCH_BASE);
			}
			theEnum = theContext.search(aSearchDN, aPattern, theControls);
		} catch (NamingException ex) {
			dropContext = true;
			throw new DataAccessException("Unable to execute LDAP query for " +
				"pattern \"" + aPattern + "\"!",
				ex);
		} finally {
			this.releaseContext(theContext, dropContext);
		}
		return (theEnum);
	}

	/**
	 * a copy of the own ServiceProiderInfo
	 */
	public ServiceProviderInfo getCopyOfSPI() {
		return new ServiceProviderInfo(getServiceProviderInfo());
	}

	public String getDeviceID() {
		return this.deviceID;
	}

	/**
	 * Helper Method to extend the given Query with the configured filter
	 * @return the Query as AND construct with the configured filter
	 */
	private String addAttributeFilterToQuery(String aQuery) {
		String filterQuery = this.attribFilter;
		if (!StringServices.isEmpty(filterQuery)) {
			aQuery = encloseStringInBrackets(aQuery);
			filterQuery = encloseStringInBrackets(filterQuery);
			aQuery = "(&" + aQuery + filterQuery + ")";
		}
		return aQuery;
	}

	/**
	 * the same string enclosed in brackets. If the string already was
	 *         enclosed in brackets it is returned unchanged
	 */
	private String encloseStringInBrackets(String aString) {
		if (!StringServices.startsWithChar(aString, '(')) {
			aString = "(" + aString;
		}
		if (!StringServices.endsWithChar(aString, ')')) {
			aString = aString + ")";
		}
		return aString;
	}

	/**
	 * get all members of a group either as DataObjects or just as list of their DNs (Strings)
	 * This method automatically parses all configured groups
	 * @param returnDOs - flag to indicate whether to return the DOs or just the DNs
	 * @return List of DOs or List of Strings, may be empty but not null
	 */
	protected Set getAllGroupMembers(boolean returnDOs) {
		if (this.groupDNs.isEmpty()) {
			if (StringServices.isEmpty(attribFilter)) {
				throw new ConfigurationError(
					ResKey.text("Configuration Error. No TL UserGroup and no filter query configured."));
			}
			if (returnDOs) {
				Logger.info(this.getDeviceID()
					+ ": No TL UserGroup configured, using filter query to determine user entries...", this);
				Logger.info(this.getDeviceID() + ": Using query: " + this.attribFilter + " on search start node: "
					+ this.baseDN + "...", this);
			}
			try {
				return getUserEntriesByQuery(returnDOs);
			} catch (Exception e) {
				Logger.error(this.getDeviceID() + ": Unable to query users.", e, this);
				throw new TopLogicException(LDAPAccessService.class, "ldaperror", e);
			}
		} else {
			Set theResult = new HashSet();
			Iterator itGroupDNs = this.groupDNs.iterator();
			int idx = 1;
			while (itGroupDNs.hasNext()) {
				String aGroupDN = (String) itGroupDNs.next();
				if (returnDOs) {
					Logger.info(
						this.getDeviceID() + ": Getting user data for members of group " + idx + " of "
							+ this.groupDNs.size() + "...",
						this);
				}
				idx++;
				try {
					theResult.addAll(getMembersForGroup(aGroupDN, returnDOs, new ArrayList()));
				} catch (Exception ne) {
					Logger.error(this.getDeviceID() + ": Unable to lookup group: " + aGroupDN, ne, this);
					throw new TopLogicException(LDAPAccessService.class, "ldaperror", ne);
				}
			}
			return theResult;
		}
	}

	/**
	 * Allows to query all directory user data objects without explicit group access. Instead the
	 * attribute filter query alone is used, as such is expected to fully determine the user objects to
	 * be imported.
	 * 
	 * @param returnDOs
	 *        - flag to indicate whether to return the DOs or just the DNs
	 * @return List of DOs or List of Strings (DNs), may be empty but not null
	 */
	protected Set getUserEntriesByQuery(boolean returnDOs) throws NamingException {
		Set theResult = new HashSet();
		Enumeration memberObjects =
			this.executeQuery(this.baseDN, this.attribFilter, this.getAvailableAttributeNames(),
				SearchControls.SUBTREE_SCOPE);
		while (memberObjects.hasMoreElements()) {
			SearchResult dataSet = (SearchResult) memberObjects.nextElement();
			LDAPDataObject memberObj = getLDAPDataObject(dataSet.getAttributes());
			if(memberObj!=null) {
				if (returnDOs) {
					theResult.add(memberObj);
				} else {
					String memberDN = (String) memberObj.getAttributeValue(LDAPAttributes.DN);
					if (StringServices.isEmpty(memberDN)) {
						// for unknown attibute this should already have been upon lookup, instead (dependig on backend
						// system?)
						// empty value was returned?!
						throw new NoSuchAttributeException(getDeviceID()
							+ " : Unable to query dn attribute from user entry. Check attribute name mappings!");
					} else {
						theResult.add(memberDN);
					}
				}
			}
		}
		if (returnDOs) {
			Logger.info(this.getDeviceID() + ": Users found: " + theResult.size(), this);
		}
		return theResult;
	}

	/**
	 * @see com.top_logic.base.security.device.interfaces.PersonDataAccessDevice#getAllUserData()
	 */
	public Set<DataObject> getAllUserData() {
		// clear cache before all entries are queried- this is to ensure update of changed attributes
		// cache will be (re-)filled automatically by the following call
		// in effect that disables the cache for this getAllUserData() call while leaving it in place for more specific getUserData(aName) calls
		userDObyDN.clear();
		Set<DataObject> memberDOs = getAllGroupMembers(true);
		return memberDOs;
	}

	/**
	 * Get the DataObject for an entry as specified by the given DN
	 * @param fullUserDN
	 *            full DN of requested entry
	 * @param useAttribFilter
	 *            true if the tpm attribute filter should be applied, that means null is returned, if the object does not match the filter
	 * @return the queried userDO only if this user is found to be a member of
	 *         at least one of the groups configured for this device.
	 */
	protected DataObject getEntryByDN(String fullUserDN, boolean useAttribFilter) {
		try {
			if (!StringServices.isEmpty(fullUserDN)) {
				String theCacheKey = useAttribFilter + fullUserDN;
				DataObject theEntry = (DataObject) this.userDObyDN.get(theCacheKey);
				if (theEntry == null) {
    				String[] idAndSearchBase = splitDNinIDandBase(fullUserDN);
					if (StringServices.isEmpty(idAndSearchBase[0]) || StringServices.isEmpty(idAndSearchBase[1])) {
						Logger.warn("Invalid user DN encountered. No split between ID and base possible. Ignoring: "
							+ fullUserDN, this);
						return null;
					}
    				String query = idAndSearchBase[0];
    				String searchDN = idAndSearchBase[1];
    				if (useAttribFilter) {
    					//add additional filter - maybe used to filter out
    					// non-person entries
    					query = addAttributeFilterToQuery(query);
    				}
					Enumeration enumeration =
						this.executeQuery(searchDN, query, getAvailableAttributeNames(),
							SearchControls.SUBTREE_SCOPE);
    				if (enumeration.hasMoreElements()) {
    					SearchResult aResult = (SearchResult) enumeration.nextElement();
    					theEntry = (this.getLDAPDataObject(aResult.getAttributes()));
						if (theEntry != null) {
							this.userDObyDN.put(theCacheKey, theEntry);
    					}
    				} else {
    					Logger.warn("No Entry returned for query "+query+" in searchBase "+searchDN,this);
    					return null;
    				}
    				if (enumeration.hasMoreElements()) {
    					Logger.error(
    							"GetEntry " + fullUserDN + " in device " + this.getDeviceID() + " was ambigious, make sure the userIDs within TPM are unique!",
    							this);
    				}
				}
				return theEntry;
			} else {
				Logger.warn("Empty user DN given to lookup entry for, returning null",this);
				return null;
			}
		} catch (Exception e) {
			Logger.error("Problem resolving groupMember DN to User Object. DN: " + fullUserDN, e, this);
			return null;
		}
	}

	/**
	 * fetches the full DN for the given username from the configured groups
	 * 
	 * @return the Full DN or null if the username was not found within the
	 *         groups (not a member)
	 */
	public String getFullUserDN(String aName) {
		//do we know a DN for this name already ?
		String theFullMemberDN = (String) this.knownMemberDNs.get(aName);
		//DNs of all members in access groups
		List matchingDNs = new ArrayList(this.getAllGroupMembers(false));
		if (!StringServices.isEmpty(theFullMemberDN) && matchingDNs.contains(theFullMemberDN)) {
			return theFullMemberDN; //if so and this DN is member of the
			// groups, return it.
		} else {
			this.knownMemberDNs.remove(aName); //otherwise, make sure it is not
			// cached anymore
			//check if we find a DN for the given name....
			Iterator allMembersDNs = matchingDNs.iterator();
			String memberName = null;
	
			while (allMembersDNs.hasNext()) {
				String aMemberDN = (String) allMembersDNs.next();
				DataObject aMemberDO = getEntryByDN(aMemberDN, true);
				try {
					memberName = null;
					if (aMemberDO != null) {
						memberName = (String) aMemberDO.getAttributeValue(UserInterface.USER_NAME);
					}
					if (StringServices.isEmpty(memberName) || !memberName.equals(aName)) {
						allMembersDNs.remove();
					}
				} catch (Exception e) {
	                allMembersDNs.remove();
					Logger.error("Unable to get Attribute username for DN: " + aMemberDN, e, this);
				}
			}
			//should never happen
			if (matchingDNs.size() > 1) {
				Logger.warn("Name " + aName + " is not unique within TPM Groupmembers!", this);
				String result = null;
				for (int i = 0; i < matchingDNs.size(); i++) {
					String tmp = (String) matchingDNs.get(i);
					DataObject anEntry = this.getEntryByDN(tmp, true);
					boolean valid = anEntry != null;
					if (valid) {
						result = tmp;
					}
					Logger.warn("DN " + tmp + " is ambigious. Found to be a valid member: " + valid, this);
				}
				Logger.warn("Using DN: " + result, this);
				return result;
			} else {
				if (matchingDNs.isEmpty()) {
					return null;
				} else {
					theFullMemberDN = (String) matchingDNs.get(0);
					this.knownMemberDNs.put(aName, theFullMemberDN);
					return theFullMemberDN;
				}
			}
		}
	}

	/**
	 * Creates a DataObject out of JNDI Attributes
	 * @return a DataObject, herby using the implementation LDAPDataObject
	 */
	protected LDAPDataObject getLDAPDataObject(Attributes anObject) throws NamingException {
		if (anObject == null) {
			return (null);
		} else {
			return (new LDAPDataObject(anObject, _securityDevice));
		}
	}

	/**
	 * Return the members of a given group - either as DNs (Strings) or DataObjects
	 * 
	 * @param groupDN     LDAP DN of the group
	 * @param returnDOs -
	 *            a boolean flag- true says a list of full Member DOs is
	 *            returned, false means a List of MemberDN Strings in returned
	 * @param parsedGroups  if true nested group objects will be parsed recursively
	 * 
	 * @return all DNs found to be member of the group given by searchPatch and
	 *         Name (Query)
	 * @throws NamingException
	 *             Note: The returned DOs do already match the defined
	 *             attributeFilter (i.e. objectClass=Person), the returned DNs,
	 *             do NOT(!) So you may get DNs of contained nested groups and
	 *             stuff amongst the members if you chose not to return DOs
	 */
	protected List getMembersForGroup(String groupDN, boolean returnDOs, List parsedGroups) throws NamingException {
		List theResult = new ArrayList();
		parsedGroups.add(groupDN);
		String[] idAndSearchBase = splitDNinIDandBase(groupDN);
		if (StringServices.isEmpty(idAndSearchBase[0]) || StringServices.isEmpty(idAndSearchBase[1])) {
			Logger.warn("Invalid group DN encountered. No split between ID and base possible. Ignoring: " + groupDN,
				this);
			return theResult;
		}
		Enumeration enumeration =
			this.executeQuery(idAndSearchBase[1], idAndSearchBase[0], this.getAvailableAttributeNames(),
				SearchControls.SUBTREE_SCOPE);
		SearchResult groupResult = null;
		Attributes attrs = null;
		LDAPDataObject theGroup = null;
		DeviceMapping groupSecurityMapping = null;
		Attribute memberAttribute = null;
		Enumeration memberDNs = null;
		String aMemberDN = null;
		String deviceID = getDeviceID();
		if (enumeration.hasMoreElements()) {
			groupResult = (SearchResult) enumeration.nextElement();
			attrs = groupResult.getAttributes();
			theGroup = this.getLDAPDataObject(attrs);
			groupSecurityMapping = theGroup.getTypeSpecificMapping();
			String objClass_Group = theGroup.getObjectClass();
			memberAttribute = attrs.get(groupSecurityMapping.mapAttribute(GroupAttributes.MEMBER, GroupAttributes.MEMBER));
			if (memberAttribute != null) {
				memberDNs = memberAttribute.getAll();
				while (memberDNs.hasMoreElements()) {
					aMemberDN = (String) memberDNs.nextElement();
					//if parsing of nested groups is enabled
					if (this.parseNestedGroups) {
						//always get the dataobject of the groupmember to check
						// its objectclass
						DataObject aMemberDo = this.getEntryByDN(aMemberDN, false);
						//if it is found to be a group....
						if (aMemberDo != null
							&& LDAPMetaObject.isInstanceOf(((LDAPDataObject) aMemberDo).getObjectClass(),
							objClass_Group)) {
							//parse this group if not done before
							if (!parsedGroups.contains(aMemberDN)) {
								theResult.addAll(getMembersForGroup(aMemberDN, returnDOs, parsedGroups));
							}
							continue;
						}
					}
					if (returnDOs) {
						DataObject aMemberDo = getEntryByDN(aMemberDN, true);
						if (aMemberDo != null) {
							theResult.add(aMemberDo);
							int currentUsrNo = theResult.size();
							if(currentUsrNo % 10 == 0){
								Logger.info("User data retrieved from " + deviceID + ": " + currentUsrNo + " users.",
									this);
							}
						}
					} else {
						theResult.add(aMemberDN);
					}
				}
			}
		} else {
			Logger.error("Group as Specified in config could not be found for securitydevice: " + this.getDeviceID() + " Search Query was: "
					+ idAndSearchBase[0] + " in Subtree: " + idAndSearchBase[1], this);
			throw new ConfigurationError("Group Configuration Error. Configured Group not found.");
		}
		if (enumeration.hasMoreElements()) {
			Logger.warn("More than one TL User group found as specified in cfg, make sure it is unique !", this);
		}
		if (returnDOs) {
			Logger.info("User found in " + deviceID + ": " + theResult.size() + " users.", this);
		}
		return theResult;
	}

	/** 
	 * @see com.top_logic.base.security.device.interfaces.PersonDataAccessDevice#getUserData(java.lang.String)
	 */
	public UserInterface getUserData(String aName) {
		String theDN = this.getFullUserDN(aName);
		if(!StringServices.isEmpty(theDN)){
			return DOUser.getInstance(getEntryByDN(theDN, true));
		}else{
			//user name unknown
			return null;
		}
	}

	/**
	 * HelperMethod
	 * @return a String Array with the ID part of the DN as first element and the
	 * searchBase part as second
	 */
	protected String[] splitDNinIDandBase(String aDN) {
		String[] result = new String[2];
		int sepIdx = aDN.indexOf(",");
		if (sepIdx > 0) {
			while (aDN.charAt(sepIdx - 1) == '\\') {
				sepIdx = aDN.indexOf(",", sepIdx + 1);
			}
			String identifier = aDN.substring(0, sepIdx);
			String searchBase = aDN.substring(sepIdx + 1);
			result[0] = identifier;
			result[1] = searchBase;
		} else {
			result[0] = aDN; // no separation between id and base possible
			result[1] = "";
		}
		return result;
	}

	/**
	 * an array containing all attribute names that can be queried for any ldap objects
	 *         (unified set ignoring the objectClass)
	 */
	protected String[] getAvailableAttributeNames() {
		return this.availableAttrs;
	}
}
