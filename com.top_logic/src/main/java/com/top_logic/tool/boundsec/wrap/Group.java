/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.wrap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.TLID;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.OneWayListSink;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.objects.DestinationIterator;
import com.top_logic.knowledge.objects.InvalidLinkException;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.objects.SourceIterator;
import com.top_logic.knowledge.service.AssociationQuery;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.db2.AssociationSetQuery;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.util.ItemByNameCache;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.knowledge.wrap.WrapperNameComparator;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.IGroup;
import com.top_logic.tool.boundsec.simple.SimpleBoundObject;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * A Group of members that are BoundObjects (normally Persons).
 *
 * @author    <a href="mailto:kbu@top-logic.com>Karsten Buch</a>
 */
public class Group extends AbstractBoundWrapper implements IGroup {

	/** The type of KO wrapped by this class. */
    public static final String OBJECT_NAME      = "Group";

	/** Full qualified name of the {@link TLType} of a {@link Group}. */
	public static final String GROUP_TYPE = "tl.accounts:Group";

	/**
	 * Resolves {@link #GROUP_TYPE}.
	 * 
	 * @implNote Casts result of {@link TLModelUtil#resolveQualifiedName(String)} to
	 *           {@link TLStructuredType}. Potential {@link ConfigurationException} are wrapped into
	 *           {@link ConfigurationError}.
	 * 
	 * @return The {@link TLStructuredType} representing the {@link Group}s.
	 * 
	 * @throws ConfigurationError
	 *         iff {@link #GROUP_TYPE} could not be resolved.
	 */
	public static TLStructuredType getGroupType() throws ConfigurationError {
		return (TLStructuredType) TLModelUtil.resolveQualifiedName(GROUP_TYPE);
	}

    /** The KO attribute used to store the system Group flag. */
    public static final String GROUP_SYSTEM      = "isSystem";

	/** The KO attribute used to store the "is default group" flag. */
	public static final String GROUP_DEFAULT = "defaultGroup";

	/**
	 * The name of the KnowledgeAssociation between a {@link Group} and its contents (other
	 * {@link Group}s or {@link Person}s).
	 */
	public static final String GROUP_MEMBERS_ASSOCIATION = "hasGroupMembers";

	/** The name of the {@link KnowledgeAssociation} between {@link Person} and {@link Group}. */
    public static final String DEFINES_GROUP_ASSOCIATION = "definesGroup";

	private static final BoundObject EMPTY_BO = new SimpleBoundObject("__empty__");

	private static final AssociationSetQuery<KnowledgeAssociation> MEMBERS_ATTR = AssociationQuery.createOutgoingQuery(
		"groupMembers", Group.GROUP_MEMBERS_ASSOCIATION);

	static final AssociationSetQuery<KnowledgeAssociation> GROUPS_ATTR = AssociationQuery.createIncomingQuery(
		"groups", Group.GROUP_MEMBERS_ASSOCIATION);

	private static volatile ItemByNameCache<String> BY_NAME_CACHE;

    private transient BoundObject boundObject;

    /**
     * CTor for WrapperFactory
     *
     * @param ko           the KO
     */
    public Group(KnowledgeObject ko) {
        super(ko);
    }

	/**
	 * Whether the given {@link Person} is a member of this {@link Group} (either directly,
	 * recursively, or via {@link #isRepresentativeGroup()}).
	 */
	public boolean containsPerson(Person account) {
		if (getBoundObject() == account) {
			// Representative group.
			return true;
		}
		Set<? extends TLObject> members = getDirectMembers();
		if (members.contains(account)) {
			return true;
		}
		for (TLObject member : members) {
			if (member instanceof Group) {
				if (((Group) member).containsPerson(account)) {
					return true;
				}
			}
		}
		return false;
	}

    /**
	 * Get all the {@link Person} members of this {@link Group} and its contained {@link Group}s
	 * (recursively).
	 * 
	 * <p>
	 * Note: This method does not return the {@link Person}, this {@link Group} is potentially a
	 * {@link #isRepresentativeGroup() representative group} for. Therefore, the result of this
	 * method is not directly suitable for computing access decisions, use
	 * {@link #containsPerson(Person)} instead.
	 * </p>
	 *
	 * @return The {@link Group} members. May be empty but not <code>null</code>
	 */
    @Override
	public Collection<Person> getMembers(){
    	return getMembers( /* recursive */ true);
    }

    /**
	 * Get all the {@link Person} members of this {@link Group}.
	 * 
	 * <p>
	 * Note: This method does not return the {@link Person}, this {@link Group} is potentially a
	 * {@link #isRepresentativeGroup() representative group} for. Therefore, the result of this
	 * method is not directly suitable for computing access decisions, use
	 * {@link #containsPerson(Person)} instead.
	 * </p>
	 * 
	 * @param recursive
	 *        If <code>true</code> members of nested {@link Group}s are also returned, only direct
	 *        members otherwise
	 * @return The {@link Group} members. May be empty but not <code>null</code>
	 * 
	 * @see #containsPerson(Person)
	 */
    public Collection<Person> getMembers(boolean recursive) {
        try {
        	Set<Person> result = new HashSet<>();
        	if (recursive) {
				addMembersRecursive(result, new HashSet<>());
        	} else {
				Set<? extends TLObject> directMembers = getDirectMembers();
        		for (Object directMember : directMembers){
        			if (directMember instanceof Person){
        				result.add((Person) directMember);
        			}
        		}
        	}
        	return result;
        }
        catch (Exception ex) {
            Logger.error("Failed to getMembers() for Group " + this, ex, this);
            throw new TopLogicException(Group.class, "Failed to getMembers() for Group " + this, ex);
        }
    }

	/**
	 * The direct members ({@link Person}s or {@link Group}s) of this {@link Group}.
	 */
	public final Set<? extends TLObject> getDirectMembers() {
		return resolveWrappers(MEMBERS_ATTR);
	}

	private void addMembersRecursive(Collection<? super Person> result, Set<Group> seen) {
		if (! seen.add(this)) {
			return;
		}
		
		Set<? extends TLObject> directMembers = resolveWrappers(MEMBERS_ATTR);
		for (Object directMember : directMembers){
			if (directMember instanceof Group){
				Group innerGroup = (Group) directMember;
				innerGroup.addMembersRecursive(result, seen);
			} else {
				result.add((Person) directMember);
			}
		}
	}

    /**
     * Check if this is a system Group,
     * i.e. cannot be deleted by users
     *
     * @return true if this is a system Group
     */
    public boolean isSystem() {
		return tGetDataBooleanValue(GROUP_SYSTEM);
    }

    /**
     * Set if this is a system Group,
     * i.e. cannot be deleted by users
     *
     * @param isSystem the system flag value
     */
    public void setIsSystem(boolean isSystem) {
		this.tSetData(GROUP_SYSTEM, Boolean.valueOf(isSystem));
    }

    /**
	 * Check if this is a default group, i.e. a group in which each user is a member
	 *
	 * @return true if this is a default group
	 */
	public boolean isDefaultGroup() {
		return tGetDataBooleanValue(GROUP_DEFAULT);
	}

	/**
	 * Setter for {@link #isDefaultGroup()}.
	 *
	 * @param isDefault
	 *        Whether this is a default group.
	 */
	public void setDefaultGroup(boolean isDefault) {
		this.tSetData(GROUP_DEFAULT, Boolean.valueOf(isDefault));
	}

	/**
	 * Sets the name of this instance.
	 *
	 * @param aName
	 *        The name to be set.
	 */
    @Override
	public void setName(String aName) {
        this.setValue(NAME_ATTRIBUTE, aName);
    }

    /**
	 * Create a {@link Group} with the specified name.
	 *
	 * @param name
	 *        The name of the Group.
	 * @return The new {@link Group}.
	 */
	public static Group createGroup(String name) {
		Group result = ModelService.getInstance().createGroup();
		result.setName(name);
		return result;
    }

	/**
	 * Create a representative {@link Group}.
	 *
	 * @see Person#getRepresentativeGroup()
	 */
	public static Group createRepresentativeGroup(Person account) {
		Group result = ModelService.getInstance().createRepresentativeGroup();
		result.setName(account.getName());
		result.setIsSystem(true);
		result.bind(account);
		return result;
	}

    /**
     * Get a Group by its name.
     *
     * @param   aKB             the KnowledgeBase to fetch the Group from.
     * @param   aName           the name of the Group
     * @return  the Group or null if it doesn't exist
     */
    public static Group getGroupByName (KnowledgeBase aKB, String aName) {
		if (aKB == getDefaultKnowledgeBase()) {
			return fromCache(aKB, aName);
		}

        KnowledgeObject theGroupKO = (KnowledgeObject) aKB
            .getObjectByAttribute(OBJECT_NAME, NAME_ATTRIBUTE, aName);

        if (theGroupKO != null) {
            return (Group) WrapperFactory.getWrapper(theGroupKO);
        }
        return null;
    }

    /**
     * Get a Group by its name in the default KB.
     *
     * @param   aName           the name of the Group
     * @return  the Group or null if it doesn't exist
     */
    public static Group getGroupByName(String aName) {
		return fromCache(getDefaultKnowledgeBase(), aName);
    }

	private static Group fromCache(KnowledgeBase defaultKB, String name) {
		if (StringServices.isEmpty(name)) {
			return null;
		}

		KnowledgeItem cachedGroup = getOrInstallByNameCache(defaultKB).getValue().get(name);
		if (cachedGroup == null) {
			return null;
		}
		return cachedGroup.getWrapper();

	}

	private static ItemByNameCache<String> getOrInstallByNameCache(KnowledgeBase defaultKB) {
		ItemByNameCache<String> byNameCache = BY_NAME_CACHE;
		if (byNameCache == null || byNameCache.kb() != defaultKB) {
			// First access to cache or default KB has changed (PersistencyLayer may have been
			// restarted).
			byNameCache = new ItemByNameCache<>((DBKnowledgeBase) defaultKB, OBJECT_NAME, NAME_ATTRIBUTE, String.class);
			BY_NAME_CACHE = byNameCache;
		}
		return byNameCache;
	}

	/**
	 * Get a Group by its identifier
	 *
	 * @param aKB
	 *        the KnowledgeBase to fetch the Group from.
	 * @param anID
	 *        the identifier of the Group
	 * @return the Group or null if it doesn't exist
	 */
    public static final Group getInstance (KnowledgeBase aKB, TLID anID) {
		return (Group) WrapperFactory.getWrapper(anID, OBJECT_NAME, aKB);
     }

    /**
     * Get a BoundedRol by its identifier in the default KB.
     *
     * @param   anID            the identifier of the Group
     * @return  the Group or null if it doesn't exist
     */
	public static Group getInstance(TLID anID) {
		return (Group) WrapperFactory.getWrapper(anID, OBJECT_NAME);
    }

    /**
     * Get all Group for the given KnowledgeBase
     *
     * @return a list of filtered Groups, never null.
     */
	public static final List<Group> getAll(KnowledgeBase aKB) {
        return getAll(aKB, FilterFactory.trueFilter());
    }

	/**
	 * Get all Group for the given KnowledgeBase , matching the given filter criteria
	 * 
	 * @param aKB
	 *        the KnowledgeBase to fetch the Group from.
	 * @return A list of Groups, never null.
	 */
	public static final List<Group> getAll(KnowledgeBase aKB, Filter<? super Group> aFilter) {
		Collection<KnowledgeObject> allKOs = aKB.getAllKnowledgeObjects(Group.OBJECT_NAME);
		List<Group> result = new ArrayList<>(allKOs.size());
		Iterator<KnowledgeObject> koIter = allKOs.iterator();
        while (koIter.hasNext()) {
			Group aGroup = (Group) WrapperFactory.getWrapper(koIter.next());
			if (aFilter.accept(aGroup) && !aGroup.isRepresentativeGroup())
            result.add(aGroup);
        }
        Collections.sort(result, WrapperNameComparator.getInstance());
        return result;
     }

    /**
     * Get all Group for the default KnowledgeBase.
     *
     * @return  a list of Groups, never null.
     */
	public static final List<Group> getAll() {
        return getAll(FilterFactory.trueFilter());
    }

    /**
     * Get all Group for the default KnowledgeBase, matching the given filter criteria
     *
     * @return a list of filtered Groups, never null.
     */
	public static final List<Group> getAll(Filter<? super Group> aFilter) {
        return getAll(getDefaultKnowledgeBase(), aFilter);
    }

    /**
     * Add a group to a BoundObject which has to be a Wrapper
     *
     * @param anObject    the BoundObject. May be <code>null</code> (code has no effect then).
     * @throws DataObjectException if creation of the KA fails
     * @throws IllegalArgumentException it the BoundObject is not a Wrapper
     */
	public void addMember(TLObject anObject) {
        if (anObject == null) {
            return;
        }

        if (this.getMembers().contains(anObject)) {	// Do not add a member twice
        	return;
        }
        
		if (anObject instanceof Group innerGroup) {
			// Check that no membership cycle is created.
        	HashSet<Group> descendantsOrSelf = new HashSet<>();
			innerGroup.addMembersRecursive(OneWayListSink.INSTANCE, descendantsOrSelf);
			
        	if (descendantsOrSelf.contains(this)) {
        		throw new IllegalArgumentException(
        			"A group '" + this.getName() + "' must neither directly nor indirectly contain itself (through '" + innerGroup.getName() + "').");
        	}
        }

		KnowledgeObject memberHandle = (KnowledgeObject) anObject.tHandle();
		KnowledgeObject groupHandle = this.tHandle();
		groupHandle.getKnowledgeBase().createAssociation(groupHandle, memberHandle, Group.GROUP_MEMBERS_ASSOCIATION);

		if (!this.isRepresentativeGroup() && anObject instanceof Person account) {
			// Also add the person's representative group (if this is group not a representative
			// group itself)
			Group representativeGroup = account.getRepresentativeGroup();

			// Note: The representative group may be null, if the added account is newly created,
			// since the representative groups are allocated only during commit by the
			// PersonGroupsInitializer.
			if (representativeGroup != null) {
				addMember(representativeGroup);
			}
        }
    }

    /**
     * Get the groups of which the given object is a member as a Collection of Groups.<br/>
     * Note: Representative Groups are not returned by this method! However, if the given
     * person is member of another persons representative group, and this representative
     * group itself is member of another group, then this other (top-level) group is
     * returned. But not the representative group itself.
     *
     * @param anObject
     *        the BoundObject to get the groups for
     * @return the groups the given object is member of; may be empty but not <code>null</code>
     */
	public static Set<Group> getGroups(TLObject anObject) {
    	return Group.getGroups(anObject, true, false);
    }

    /**
	 * Get the groups of which the given object is a member as a collection of groups.
	 * 
	 * <p>
	 * Note: Use <code>resolveRepresentativeGroups == true</code> and
	 * <code>includeRepresentativeGroups == false</code> to get all groups but representative groups
	 * recursively.
	 * </p>
	 * 
	 * <p>
	 * Use <code>resolveRepresentativeGroups == false</code> and
	 * <code>includeRepresentativeGroups == true</code> to get all DIRECT groups.
	 * </p>
	 *
	 * @param anObject
	 *        the BoundObject to get the groups for
	 * @param resolveRepresentativeGroups
	 *        if <code>true</code>, representative groups are resolved recursively
	 * @param includeRepresentativeGroups
	 *        if <code>true</code>, representative groups are included into the result set
	 * @return the groups the given object is member of; may be empty but not <code>null</code>
	 */
	public static Set<Group> getGroups(TLObject anObject, boolean resolveRepresentativeGroups,
			boolean includeRepresentativeGroups) {
		Set<Group> theResult = new HashSet<>(resolveWrappersTyped(anObject, GROUPS_ATTR, Group.class));
		Set<Group> theAdditionals = new HashSet<>();
		Iterator<Group> it = theResult.iterator();
		while (it.hasNext()) {
			Group group = it.next();
			if (group.isRepresentativeGroup()) {
                if (resolveRepresentativeGroups) {
					theAdditionals.addAll(getGroups(group, true, includeRepresentativeGroups));
                }
                if (!includeRepresentativeGroups) {
					it.remove();
                }
            }
        }
        theResult.addAll(theAdditionals);
        return theResult;
    }

    /**
	 * Checks whether the given object is member of one of the given groups.
	 * 
	 * @param aObject
	 *        the BoundObject to check whether it is member of one of the given groups
	 * @param aGroupList
	 *        the Collection of {@link Group}s to check
	 * @return <code>true</code>, if the given object is member of one of the given groups,
	 *         <code>false</code> otherwise
	 */
	public static boolean isMemberOfAnyGroup(TLObject aObject, Collection<?> aGroupList) {
		Set<Group> theGroups = Group.getGroups(aObject);
        return CollectionUtil.containsAny(aGroupList, theGroups);
    }

    /**
	 * Remove a BoundObject from the Group
	 *
	 * @param member
	 *        the BoundObject. May be <code>null</code> (code has no effect then).
	 * @throws DataObjectException
	 *         if removal of the KA fails
	 */
	public void removeMember(TLObject member) {
		if (member == null) {
            return;
        }

		KnowledgeObject groupHandle = this.tHandle();
		KnowledgeObject memberHandle = (KnowledgeObject) member.tHandle();
		KBUtils.deleteAllKI(groupHandle.getOutgoingAssociations(Group.GROUP_MEMBERS_ASSOCIATION, memberHandle));

		if (member instanceof Person) {
        	//also remove the persons representative group
			removeMember(((Person) member).getRepresentativeGroup());
        }
    }

    /**
     * Bind the group to a BoundObject.
     *
     * @param aBoundObject  the BoundObject has to be an AbstractBoundWrapper
     * @throws IllegalStateException if the BoundObject is not an AbstractBoundWrapper
     * @throws IllegalArgumentException if the Group is already bound
     */
    public void bind(BoundObject aBoundObject) throws IllegalStateException, IllegalArgumentException {
        if (!(aBoundObject instanceof AbstractBoundWrapper)) {
            throw new IllegalArgumentException("Can only bind to AbstractBoundWrappers!");
        }

        AbstractBoundWrapper theABW = (AbstractBoundWrapper) aBoundObject;
		{
            KnowledgeObject theGroupKO = this.tHandle();
            KnowledgeObject theABW_KO = theABW.tHandle();

			Iterator<KnowledgeAssociation> it = theGroupKO.getIncomingAssociations(DEFINES_GROUP_ASSOCIATION);
            if (it.hasNext()) {   // is already bound
				KnowledgeAssociation theKA = it.next();
                try {
                    if (!theKA.getSourceObject().equals(theABW_KO)) {    // is not bound to the given object
                        throw new IllegalStateException("Group already bound to an Object");
                    }
                }
                catch (InvalidLinkException ile) {
                    Logger.warn ("Link between BoundObject and Group is invalid!", this);
                }
            }
            else {  // is not bound -> bind it
                try {
					this.getKnowledgeBase().createAssociation(theABW_KO, theGroupKO, DEFINES_GROUP_ASSOCIATION);
                    /* Reset the bound object (failed commit could result in a wrong binding otherwise). */
                    this.boundObject = null;
                } catch (Exception ex) {
                    Logger.error("Couldn't create KnowledgeAssociation of type " + tTable()
                            + " between " + theABW_KO + " and " + theGroupKO + ".", ex, this);
                    // TODO KBU check exception type
                    throw new RuntimeException("Could not create KnowledgeAssociation of type " + tTable() + ".", null);
                }
            }
        }
    }

    /**
     * Get the BoundObject
     *
     * @return the BoundObject. May be <code>null</code> if the Group is not bound.
     */
    public BoundObject getBoundObject() {
        if (this.boundObject == null) {
			{
                KnowledgeObject theKO  = this.tHandle();
                SourceIterator  theBOs = new SourceIterator(theKO, Group.DEFINES_GROUP_ASSOCIATION);

                if (theBOs.hasNext()) {
                    KnowledgeObject theBOKO = theBOs.nextKO();
                    this.boundObject = (BoundObject) WrapperFactory.getWrapper(theBOKO);
                } else {
                    this.boundObject = EMPTY_BO;    // Prevent multiple initializations
                }
            }
        }

        if (this.boundObject == EMPTY_BO) { // Do NOT use else here because of init above
            return (null);
        }

        return (this.boundObject);
    }

    /**
     * Check if the group is global, i.e.
     * does not have a BoundObject
     *
     * @return true if the group is global
     */
    public boolean isGlobal() {
        return this.getBoundObject() == null;
    }

	/**
	 * Whether this {@link Group} is bound to a {@link Person}.
	 * 
	 * @see #getBoundObject()
	 */
    public boolean isRepresentativeGroup(){
    	return this.getBoundObject() instanceof Person;
    }

    /**
     * Get all global groups
     *
     * @return the list of global Groups
     */
	public static List<Group> getAllGlobalGroups() {
		Iterator<Group> theGroups = getAll(getDefaultKnowledgeBase()).iterator();
		ArrayList<Group> theList = new ArrayList<>();
        while (theGroups.hasNext()) {
			Group theGroup = theGroups.next();
            if (theGroup.isGlobal()) {
                theList.add(theGroup);
            }
        }

        return theList;
    }

    /**
     * Get all scoped groups, i.e. the ones which
     * don't have a BoundObject.
     *
     * @return the list of global Groups
     */
	public static List<Group> getAllScopedGroups() {
		Iterator<Group> theGroups = getAll(getDefaultKnowledgeBase()).iterator();
		ArrayList<Group> theList = new ArrayList<>();
        while (theGroups.hasNext()) {
			Group theGroup = theGroups.next();
            if (!theGroup.isGlobal()) {
                theList.add(theGroup);
            }
        }

        return theList;
    }

    /**
	 * Get the groups bound to the given {@link Person}.
	 *
	 * @param person
	 *        The {@link Person} to get {@link Group}s for. Must not be <code>null</code>.
	 * @return The {@link Group}s for the given object. May be empty, but not <code>null</code>.
	 */
	public static Collection<Group> getDefinedGroups(Person person) {
		HashSet<Group> groups = new HashSet<>();
		KnowledgeObject ko = person.tHandle();
		DestinationIterator allGroups = new DestinationIterator(ko, Group.DEFINES_GROUP_ASSOCIATION);

		while (allGroups.hasNext()) {
			KnowledgeObject groupKO = allGroups.nextKO();
			groups.add((Group) groupKO.getWrapper());
		}
		return groups;
    }

}
