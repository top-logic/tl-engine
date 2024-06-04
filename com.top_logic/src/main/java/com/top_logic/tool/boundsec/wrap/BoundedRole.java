/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.wrap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Log;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.TLID;
import com.top_logic.basic.col.NameValueBuffer;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.AssociationQuery;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.db2.AssociationSetQuery;
import com.top_logic.knowledge.service.db2.DBKnowledgeAssociation;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.util.ItemByNameCache;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.BoundRole;

/**
 * Persistent {@link BoundRole}.
 *
 * @author <a href="mailto:dro@top-logic.com">dro</a>
 */
@Label("role")
public class BoundedRole extends AbstractBoundWrapper implements BoundRole {
    
    /** The type of KO wrapped by this class. */
    public static final String OBJECT_NAME      = "BoundedRole";
    
	/** Full qualified name of the {@link TLType} of a {@link BoundedRole}. */
	public static final String ROLE_TYPE = "tl.accounts:Role";

	/**
	 * Resolves {@link #ROLE_TYPE}.
	 * 
	 * @implNote Casts result of {@link TLModelUtil#resolveQualifiedName(String)} to
	 *           {@link TLStructuredType}. Potential {@link ConfigurationException} are wrapped into
	 *           {@link ConfigurationError}.
	 * 
	 * @return The {@link TLStructuredType} representing the {@link BoundedRole}s.
	 * 
	 * @throws ConfigurationError
	 *         iff {@link #ROLE_TYPE} could not be resolved.
	 */
	public static TLStructuredType getRoleType() throws ConfigurationError {
		return (TLStructuredType) TLModelUtil.resolveQualifiedName(ROLE_TYPE);
	}

	/** The KO attribute used to store the system role flag. */
    public static final String ROLE_SYSTEM      = "isSystem";    
    
    /**
     * KO Attr Description
     */
    public static final String ATTRIBUTE_DESCRIPTION      = "description";    
    
	/**
	 * Name of the {@link KnowledgeAssociation} assigning a {@link BoundedRole} to {@link Group} in
	 * a certain context.
	 * 
	 * <p>
	 * The source is the context object, the destination is the assigned role and the {@link Group}
	 * that gets assigned the role is given in the {@link #ATTRIBUTE_OWNER} attribute.
	 * </p>
	 */
    public static final String HAS_ROLE_ASSOCIATION = "hasRole";

	/**
	 * Attribute of the {@link #HAS_ROLE_ASSOCIATION} pointing to the {@link Group} that owns the role
	 * on the source object of the link.
	 */
	public static final String ATTRIBUTE_OWNER = "owner";

	/**
	 * Name of the {@link KnowledgeAssociation} binding a {@link BoundedRole} to a scope defining
	 * that role.
	 */
    public static final String DEFINES_ROLE_ASSOCIATION = "definesRole";

	private static final AssociationSetQuery<KnowledgeAssociation> ROLE_SCOPE =
		AssociationQuery.createIncomingQuery("roleScope", DEFINES_ROLE_ASSOCIATION);

	private static final AssociationSetQuery<KnowledgeAssociation> ROLES_IN_SCOPE =
		AssociationQuery.createOutgoingQuery("rolesInScope", DEFINES_ROLE_ASSOCIATION);

	private static volatile ItemByNameCache<String> BY_NAME_CACHE;

	/**
	 * Creates a {@link BoundedRole}.
	 */
	@CalledByReflection
    public BoundedRole(KnowledgeObject ko) {
        super(ko);
    }
    
    /**
     * Check if this is a system role,
     * i.e. cannot be deleted by users
     * 
     * @return true if this is a system role
     */
    public boolean isSystem() {
		return tGetDataBooleanValue(ROLE_SYSTEM);
    }
    
    /**
     * Set if this is a system role,
     * i.e. cannot be deleted by users
     * 
     * @param isSystem the system flag value
     */
    public void setIsSystem(boolean isSystem) {
		this.tSetData(ROLE_SYSTEM, Boolean.valueOf(isSystem));
    }

    @Override
	public void setName(String aName) {
        this.setValue(NAME_ATTRIBUTE, aName);
    }    

    /**
     * Create a new BoundedRole with the specified name.
     * 
     * The role will <em>not</em> be commited.
     *
     * @param aName             the name of the BoundedRole; must not be null
     * @param aKnowledgeBase    the KnowledgeBase in which to create the BoundedRole;
     *                          must not be null
     *
     * @return the new BoundedRole wrapper; never null
     */
    public static BoundedRole createBoundedRole (String aName,
                                                 KnowledgeBase aKnowledgeBase) {
            
        BoundedRole theRole = null;

		{
			KnowledgeObject theKO = aKnowledgeBase.createKnowledgeObject(OBJECT_NAME);
            theKO.setAttributeValue (NAME_ATTRIBUTE, aName);

            theRole = (BoundedRole) WrapperFactory.getWrapper(theKO);
        }

        return theRole;
    }

    /**
     * Create a new BoundedRole with the specified name in the defaultKB.
     *
     * @param aName             the name of the BoundedRole; must not be null
     *
     * @return the new BoundedRole wrapper; never null
     */
    public static BoundedRole createBoundedRole (String aName) {
        return createBoundedRole(aName, getDefaultKnowledgeBase());
    }
    
    /**
	 * Get a BoundedRole by its name.
	 * 
	 * @param kb
	 *        the KnowledgeBase to fetch the Role from.
	 * @param name
	 *        the name of the role
	 * @return the BoundedRole or null if it doesn't exist
	 */
	public static BoundedRole getRoleByName(KnowledgeBase kb, String name) {
		if (kb == getDefaultKnowledgeBase()) {
			return fromCache(kb, name);
		}
        
		KnowledgeObject theRoleKO = (KnowledgeObject) kb
			.getObjectByAttribute(OBJECT_NAME, NAME_ATTRIBUTE, name);
            
        if (theRoleKO != null) {
            return (BoundedRole) WrapperFactory.getWrapper(theRoleKO);
        }
        return null;
    }

    /**
     * Get a BoundedRole by its name in the default KB.
     * 
     * @param   aName           the name of the role
     * @return  the BoundedRole or null if it doesn't exist
     */
    public static BoundedRole getRoleByName(String aName) {
		return fromCache(getDefaultKnowledgeBase(), aName);
    }

	private static BoundedRole fromCache(KnowledgeBase defaultKB, String name) {
		if (StringServices.isEmpty(name)) {
			return null;
		}

		KnowledgeItem cachedRole = getOrInstallByNameCache(defaultKB).getValue().get(name);
		if (cachedRole == null) {
			return null;
		}
		return cachedRole.getWrapper();

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
     * Get a BoundedRole by its identifier
     * 
     * @param   aKB             the KnowledgeBase to fetch the Role from.
     * @param   anID            the identifier of the role
     * @return  the BoundedRole or null if it doesn't exist
     */
	public static final BoundedRole getInstance(KnowledgeBase aKB, TLID anID) {
		return (BoundedRole) WrapperFactory.getWrapper(anID, OBJECT_NAME, aKB);
     }

    /**
     * Get a BoundedRol by its identifier in the default KB.
     * 
     * @param   anID            the identifier of the role
     * @return  the BoundedRole or null if it doesn't exist
     */
	public static BoundedRole getInstance(TLID anID) {
		return (BoundedRole) WrapperFactory.getWrapper(anID, OBJECT_NAME);
    }

    /**
     * Get all BoundedRole for the given KnowledgeBase.
     * 
     * @param   aKB  the KnowledgeBase to fetch the Role from.
     * @return  A list of BoundedRoles, never null.
     */
	public static final List<BoundedRole> getAll(KnowledgeBase aKB) {
		Collection<KnowledgeObject> allKOs = aKB.getAllKnowledgeObjects(BoundedRole.OBJECT_NAME);
		List<BoundedRole> result = new ArrayList<>(allKOs.size());
		Iterator<KnowledgeObject> koIter = allKOs.iterator();
        while (koIter.hasNext()) {
			result.add(WrapperFactory.getWrapper(koIter.next()));
        }
        return result;
     }

    /**
     * Get all BoundedRole for the default KnowledgeBase.
     * 
     * @return  a list of BoundedRoles, never null.
     */
	public static final List<BoundedRole> getAll() {
        return getAll(getDefaultKnowledgeBase());
    }
    
    /**
     * Get all global roles
     * 
     * @return the list of global BoundedRoles
     */
	public static List<BoundedRole> getAllGlobalRoles() {
		Iterator<BoundedRole> theRoles = getAll(getDefaultKnowledgeBase()).iterator();
		List<BoundedRole> theList = new ArrayList<>();
        while (theRoles.hasNext()) {
			BoundedRole theRole = theRoles.next();
            if (theRole.isGlobal()) {
                theList.add(theRole);
            }
        }
        
        return theList;
    }

    /**
     * Get all scoped roles, i.e. the ones which 
     * don't have a BoundObject.
     * 
     * @return the list of global BoundedRoles
     */
	public static List<BoundedRole> getAllScopedRoles() {
		Iterator<BoundedRole> theRoles = getAll(getDefaultKnowledgeBase()).iterator();
		List<BoundedRole> theList = new ArrayList<>();
        while (theRoles.hasNext()) {
			BoundedRole theRole = theRoles.next();
            if (!theRole.isGlobal()) {
                theList.add(theRole);
            }
        }
        
        return theList;
    }
    
    @Override
	public TLModule getScope() {
		return CollectionUtil.getSingleValueFromCollection(resolveWrappersTyped(ROLE_SCOPE, TLModule.class));
    }
    
    /**
     * Check if the role is global, i.e.
     * does not have a BoundObject
     * 
     * @return true if the role is global
     */
    public boolean isGlobal() {
        return this.getScope() == null;
    }
    
    /**
	 * Get the roles bound to the given {@link TLModule}.
	 * 
	 * @param scope
	 *        The {@link TLModule}. Must not be <code>null</code>.
	 * @return the roles. May be empty but not <code>null</code>.
	 */
	public static Set<BoundedRole> getDefinedRoles(TLModule scope) {
		return AbstractWrapper.resolveWrappersTyped(scope, ROLES_IN_SCOPE, BoundedRole.class);
    }
    
    /**
	 * The role with the given name defined in the given scope.
	 * 
	 * @param scope
	 *        The object defining roles.
	 * @param roleName
	 *        The role name requested.
	 * @return The role with the given name defined in the given scope, or <code>null</code> if no
	 *         such role exists.
	 */
	public static BoundedRole getDefinedRole(TLModule scope, String roleName) {
		Collection<BoundedRole> roles = BoundedRole.getDefinedRoles(scope);
		for (BoundedRole role : roles) {
			if (roleName.equals(role.getName())) {
				return role;
            }
        }
		return null;
    }
    
    @Override
	public void bind(TLModule scope) throws IllegalStateException {
		TLModule currentScope = getScope();
		if (currentScope == null) {
			// Not bound, bind it.
			KnowledgeItem roleHandle = tHandle();
			KnowledgeBase kb = roleHandle.getKnowledgeBase();
			kb.createAssociation(scope.tHandle(), roleHandle, DEFINES_ROLE_ASSOCIATION);
		} else if (currentScope != scope) {
			throw new IllegalStateException("Cannot bind role '" + getName() + "' to '" + scope
				+ "', already bound to '" + currentScope + "'.");
		}
    }
    
    @Override
	public void unbind() {
		tKnowledgeBase().deleteAll(resolveLinks(ROLE_SCOPE));
	}

	/**
	 * Removes a direct role association for the given person to the given role.
	 *
	 * @param person
	 *        The person to remove the role from.
	 * @param role
	 *        The role to remove from the given person, may be <code>null</code> to indicate all
	 *        roles.
	 *
	 * @return Whether anything was actually removed.
	 */
	public static boolean removeRoleForPerson(TLObject context, Person person, BoundedRole role) {
		Group group = person.getRepresentativeGroup();
		if (group == null) {
			return false;
		}
		return removeRoleAssignments(context, group, role);
	}

	/**
	 * All roles available for the {@link Person} in the given context including roles globally
	 * assigned to the {@link Person}.
	 * 
	 * @param person
	 *        The {@link Person} to get the roles for.
	 * @return All available roles for the given {@link Person}.
	 * 
	 * @see BoundObject#getLocalAndGlobalRoles(Person)
	 */
	public static Set<BoundRole> getLocalAndGlobalRoles(TLObject context, Person person) {
		Set<BoundRole> result = new HashSet<>();
		result.addAll(person.getGlobalRoles());

		addRoles(result, context, person.getRepresentativeGroup());
		return result;
	}

	/**
	 * Whether the given {@link Person} has any role in the given context.
	 * 
	 * @see #getLocalAndGlobalRoles(TLObject, Person)
	 */
	public static boolean hasLocalOrGlobalOrGroupRole(TLObject context, Person aPerson) {
		if (!aPerson.getGlobalRoles().isEmpty()) {
			return true;
		}

		for (Group group : Group.getGroups(aPerson, true, true)) {
			if (hasRole(context, group)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * All roles available in the given context for the given {@link Person} including the global
	 * roles of the {@link Person} and the roles the {@link Person} has according to {@link Group}
	 * memberships.
	 * 
	 * @param person
	 *        The {@link Person} to get the roles for.
	 * @return The roles available for the given person.
	 */
	public static Collection<BoundRole> getLocalAndGlobalAndGroupRoles(TLObject context, Person person) {
		Set<BoundRole> result = new HashSet<>();
		result.addAll(person.getGlobalRoles());

		// Note: The Group.getGroups() resolution does not deliver the representative group of a
		// person. It also only resolves group membership of representative groups, not groups in
		// general. Therefore, adding groups to groups will not have the desired effect.
		addRoles(result, context, person.getRepresentativeGroup());

		for (Group group : Group.getGroups(person, true, true)) {
			addRoles(result, context, group);
		}

		return result;
	}

	/**
	 * The roles assigned to members of the given group in the given context.
	 * @param owner
	 *        The group to get the roles for
	 *
	 * @return the roles available for the object and the given person, <code>null</code> if no
	 *         roles are available for that person
	 */
	public static Set<BoundRole> getRoles(TLObject context, Group owner) {
		Set<BoundRole> result = new HashSet<>();
		addRoles(result, context, owner);
		return result;
	}

	private static boolean hasRole(TLObject context, Group owner) {
		if (BoundedRole.hasLocalRole(context, owner)) {
			return true;
		}
		if (context instanceof BoundObject) {
			BoundObject ancestor = (BoundObject) context;
			while (ancestor != null) {
				BoundObject parent = ancestor.getSecurityParent();
				if (parent != null) {
					if (BoundedRole.hasLocalRole(parent, owner)) {
						return true;
					}
				}
				ancestor = parent;
			}
		}
		return false;
	}

	private static boolean hasLocalRole(TLObject context, Group owner) {
		return queryAssignedRoles(context, owner).hasNext();
	}

	private static void addRoles(Set<BoundRole> result, TLObject context, Group owner) {
		BoundedRole.addLocalRoles(result, context, owner);
		if (context instanceof BoundObject) {
			BoundObject ancestor = (BoundObject) context;
			while (ancestor != null) {
				BoundObject parent = ancestor.getSecurityParent();
				if (parent != null) {
					BoundedRole.addLocalRoles(result, parent, owner);
				}
				ancestor = parent;
			}
		}
	}

	private static void addLocalRoles(Collection<BoundRole> result, TLObject context, Group owner) {
		Iterator<? extends KnowledgeAssociation> it = queryAssignedRoles(context, owner);
		while (it.hasNext()) {
			result.add(it.next().getDestinationObject().getWrapper());
		}
	}

	/**
	 * Copies all role assignments from the given context to the given context.
	 * 
	 * @param toContext
	 *        The destination context to copy role assignments to.
	 * @param fromContext
	 *        The source context of role assignments to copy from.
	 */
	public static void copyRoleAssignments(TLObject toContext, TLObject fromContext) {
		copyRoleAssignments(toContext, fromContext, null);
	}

	/**
	 * Copies role assignments from the given context to the given context.
	 * 
	 * @param toContext
	 *        The destination context to copy role assignments to.
	 * @param fromContext
	 *        The source context of role assignments to copy from.
	 * @param filter
	 *        Only copy the given roles, <code>null</code> means to copy all roles.
	 */
	public static void copyRoleAssignments(TLObject toContext, TLObject fromContext,
			Collection<? extends BoundedRole> filter) {
		KnowledgeObject fromHandle = (KnowledgeObject) fromContext.tHandle();
		KnowledgeObject toHandle = (KnowledgeObject) toContext.tHandle();
		Iterator<KnowledgeAssociation> it = fromHandle.getOutgoingAssociations(HAS_ROLE_ASSOCIATION);
		KnowledgeBase kb = toHandle.getKnowledgeBase();
		while (it.hasNext()) {
			KnowledgeAssociation fromLink = it.next();
			KnowledgeObject roleHandle = fromLink.getDestinationObject();
			if (filter == null || filter.contains(roleHandle.getWrapper())) {
				KnowledgeAssociation toLink = kb.createAssociation(toHandle, roleHandle, HAS_ROLE_ASSOCIATION);
				toLink.setAttributeValue(ATTRIBUTE_OWNER, fromLink.getAttributeValue(ATTRIBUTE_OWNER));
			}
		}
	}

	/**
	 * Removes a direct role associations for the given group to the given role in the given
	 * context.
	 * 
	 * @param group
	 *        The Group to remove the role from.
	 * @param role
	 *        The role to remove from the group, may be <code>null</code> to indicate all roles.
	 *
	 * @return Whether when anything was actually removed.
	 */
	public static boolean removeRoleAssignments(TLObject context, Group group, BoundedRole role) {
		KnowledgeObject contextHandle = (KnowledgeObject) context.tHandle();
		Iterator<? extends KnowledgeAssociation> it;
		if (role != null) {
			KnowledgeObject theRoleObj = role.tHandle();
			it = contextHandle.getOutgoingAssociations(HAS_ROLE_ASSOCIATION, theRoleObj);
		} else {
			it = contextHandle.getOutgoingAssociations(HAS_ROLE_ASSOCIATION);
		}
		KnowledgeBase kb = contextHandle.getKnowledgeBase();
		KnowledgeObject groupHandle = group.tHandle();
		while (it.hasNext()) {
			KnowledgeAssociation link = it.next();
			if (groupHandle.equals(link.getAttributeValue(ATTRIBUTE_OWNER))) {
				kb.delete(link);
				return true;
			}
		}
		return false;
	}

	/**
	 * Remove all directly assigned roles in the given context.
	 * 
	 * @return Whether something was actually removed.
	 */
	public static boolean removeRoleAssignments(TLObject context, BoundedRole aRole) {
		{
			KnowledgeObject contextItem = (KnowledgeObject) context.tHandle();
			KnowledgeObject roleItem = aRole.tHandle();
			Iterator<? extends KnowledgeAssociation> iter =
				contextItem.getOutgoingAssociations(HAS_ROLE_ASSOCIATION, roleItem);
			return KBUtils.deleteAllKI(iter);
		}
	}

	/**
	 * Removes all {@link #assignRole(TLObject, Group, BoundedRole) explicit role assignments} from
	 * the given context.
	 * 
	 * @param context
	 *        The context to remove role assignments from.
	 * @return Whether anything has been actually removed.
	 */
	public static boolean removeRoleAssignments(TLObject context) {
		{
			KnowledgeObject item = (KnowledgeObject) context.tHandle();
			Iterator<? extends KnowledgeAssociation> iter =
				item.getOutgoingAssociations(HAS_ROLE_ASSOCIATION);
			return KBUtils.deleteAllKI(iter);
		}
	}

	private static boolean hasRoleAssigned(TLObject context, Group owner, BoundedRole role) {
		KnowledgeObject roleHandle = role.tHandle();

		Iterator<? extends KnowledgeAssociation> it = queryAssignedRoles(context, owner);
		while (it.hasNext()) {
			if (it.next().getDestinationObject() == roleHandle) {
				return true;
			}
		}
		return false;
	}

	private static Iterator<? extends KnowledgeAssociation> queryAssignedRoles(TLObject context, Group group) {
		KnowledgeObject ko = (KnowledgeObject) context.tHandle();
		Iterator<? extends KnowledgeAssociation> it;
		if (group == null) {
			it = ko.getOutgoingAssociations(HAS_ROLE_ASSOCIATION);
		} else {
			it = (Iterator)ko.getKnowledgeBase().getObjectsByAttribute(HAS_ROLE_ASSOCIATION,
				new String[] { DBKnowledgeAssociation.REFERENCE_SOURCE_NAME, ATTRIBUTE_OWNER },
				new Object[] { ko, group.tHandle() });
		}
		return it;
	}

	/**
	 * Assigns the given {@link BoundedRole} to the given {@link Person} in the given context.
	 */
	public static void assignRole(TLObject context, Person person, BoundedRole role) {
		assignRole(context, person.getRepresentativeGroup(), role);
	}

	/**
	 * Assigns the given {@link BoundedRole} to the given {@link Group} in the given context.
	 */
	public static void assignRole(TLObject context, Group group, BoundedRole role) {
		if (group == null) {
			return;
		}

		if (!hasRoleAssigned(context, group, role)) {
			KnowledgeItem contextHandle = context.tHandle();
			KnowledgeBase kb = contextHandle.getKnowledgeBase();
			NameValueBuffer initialValues = new NameValueBuffer(2);
			initialValues.put(DBKnowledgeAssociation.REFERENCE_SOURCE_NAME, contextHandle);
			initialValues.put(DBKnowledgeAssociation.REFERENCE_DEST_NAME, role.tHandle());
			initialValues.put(ATTRIBUTE_OWNER, group.tHandle());
			kb.createKnowledgeItem(HistoryUtils.getTrunk(), HAS_ROLE_ASSOCIATION, initialValues);
		}
	}

	/**
	 * Fetches the {@link BoundedRole}s for the given role names.
	 * 
	 * <p>
	 * If a configured role could not be retrieved an error is logged.
	 * </p>
	 * 
	 * @param log
	 *        {@link Protocol} to log errors to.
	 * @param roleNames
	 *        Names of the desired roles.
	 * 
	 * @return Collection containing the roles. May be unmodifiable.
	 */
	public static Collection<BoundedRole> getRolesByName(Log log, Collection<String> roleNames) {
		Map<String, BoundedRole> rolesByName = getRolesByName(roleNames);
		roleNames.stream()
			.filter(requiredRole -> !rolesByName.containsKey(requiredRole))
			.forEach(missingRole -> errorMissingRole(log, missingRole));
		return rolesByName.values();
	}

	private static void errorMissingRole(Log log, String roleName) {
		StringBuilder noRoleFound = new StringBuilder();
		noRoleFound.append("No role with name '");
		noRoleFound.append(roleName);
		noRoleFound.append("' found.");
		log.error(noRoleFound.toString());
	}

	/**
	 * Fetches the required roles from the default {@link KnowledgeBase}.
	 * 
	 * <p>
	 * When a role for a given name could not be found, there is no entry with the key in the
	 * result.
	 * </p>
	 * 
	 * @param roleNames
	 *        The required roles.
	 * 
	 * @return Mapping from the name of the found roles to the role.
	 */
	public static Map<String, BoundedRole> getRolesByName(Collection<String> roleNames) {
		switch (roleNames.size()) {
			case 0: {
				return Collections.emptyMap();
			}
			case 1: {
				String roleName = roleNames.iterator().next();
				BoundedRole role = BoundedRole.getRoleByName(roleName);
				if (role != null) {
					return Collections.singletonMap(roleName, role);
				} else {
					return Collections.emptyMap();
				}
			}
			default: {
				Map<String, KnowledgeItem> allCachedRoles =
					getOrInstallByNameCache(getDefaultKnowledgeBase()).getValue();
				Map<String, BoundedRole> result = new HashMap<>();
				for (String roleName : roleNames) {
					KnowledgeItem roleItem = allCachedRoles.get(roleName);
					if (roleItem == null) {
						// No such item
						continue;
					}
					result.put(roleName, roleItem.getWrapper());
				}
				return result;
			}
		}
	}

}

