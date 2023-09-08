/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.simple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.DummyIDFactory;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringID;
import com.top_logic.basic.TLID;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TransientObject;
import com.top_logic.tool.boundsec.BoundHelper;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.IGroup;
import com.top_logic.tool.boundsec.wrap.Group;

/**
 * Simple Example Implementation of a Bound Object. 
 * 
 * @author    <a href="mailto:kha@top-logic.com">Klaus  Halfmann</a>
 */
public class SimpleBoundObject extends TransientObject implements BoundObject {

    /** An ID for this Object. */
	protected TLID id;

    /** Bitflags specifiying variants of Behaviours. */
    protected int           flags; 

    /** The children of this object, may be null */
    protected List          children;

    /** The parent of this object, may be null */
    protected BoundObject   parent;

    /** Map of Set of Roles a specific Person may have */
    protected Map           rolesForPersons;

    /** Create a default SimpleBoundObject */
	public SimpleBoundObject(String anID) {
		this(StringID.valueOf(anID));
    }

	/** Create a default SimpleBoundObject */
	public SimpleBoundObject() {
		this(DummyIDFactory.createId());
	}

	/** Create a default SimpleBoundObject */
	public SimpleBoundObject(TLID id) {
		this(id, 0);
	}

    /** Create a SimpleBoundObject with some Flags */
	public SimpleBoundObject(String anID, int flags) {
		this(StringID.valueOf(anID), flags);
    }

	public SimpleBoundObject(TLID anId, int flags) {
		this.id = anId;
		this.flags = flags;
	}

	/**
	 * The ID of this object
	 */
    @Override
	public TLID getID() {
        return id;
    }

    /**
     * Get the child bound objects for this bound object.
     * 
     * @return a collection of child BoundObjects, null or 
     *         empty collection in case there are no children.
     */
    @Override
	public Collection<? extends BoundObject> getSecurityChildren() {
        return children;
    }

    /** Add a new Child to the List of Children */
    public void addChild(BoundObject aChild) {
        if (children == null)
            children = new ArrayList();
        children.add(aChild);
        if (aChild instanceof SimpleBoundObject)
            ((SimpleBoundObject) aChild).setParent(this);
    }

    /**
     * Get the parent object for this bound object.
     * 
     * @return the parent bound object for this bound object, 
     *         <code>null</code> if there is no parent (e.g. this is a root element)
     */
    @Override
	public BoundObject getSecurityParent() {
        return parent;
    }

    /**
     * Allow setting of a new Parent.
     */
    protected void setParent(BoundObject aParent) {
        parent = aParent;
    }

    /**
     * Get the {@link com.top_logic.tool.boundsec.BoundRole}s of the given 
     * person for this object.
     * 
     * This may fall back to the BoundRoles of the Parent (Global Roles).
     * 
     * @param   aPerson     The person to get the roles for
     * @return a collection of roles the given person has in/for this Object.
     */
    @Override
	public Collection<BoundRole> getRoles(Person aPerson) {
        Collection result = null;
        if (rolesForPersons != null) {
            result = (Collection) rolesForPersons.get(aPerson);
        }
        if (0 != (flags & BoundHelper.INHERIT_ROLES)) {
            BoundObject parent = getSecurityParent();
            if (parent != null) {
                result = BoundHelper.merge(result, parent.getRoles(aPerson));
            }
        }
        
        return result;
    }
    
    /**
     * @see com.top_logic.tool.boundsec.BoundObject#getLocalAndGlobalRoles(com.top_logic.knowledge.wrap.person.Person)
     */
    @Override
	public Collection<BoundRole> getLocalAndGlobalRoles(Person aPerson) {
        Collection result = this.getRoles(aPerson);
		result = BoundHelper.merge(result, aPerson.getGlobalRoles());
        return result;
    }

    /**
     * Check if the given person has any role ot this object.
     * 
     * This will usually fall back to the BoundRoles of the Parent (Global Roles).
     * 
     * @param aPerson The person to get the roles for
     * @return true when <code>getRoles(aPerson)</code> == null (or empty)
     */
   @Override
public boolean hasAnyRole(Person aPerson) {
       Collection coll = null;
       if (rolesForPersons != null) {
           coll = (Collection) rolesForPersons.get(aPerson);
       }
       if (0 != (flags & BoundHelper.INHERIT_ROLES)) {
           BoundObject parent = getSecurityParent();
           if (parent != null) {
               coll = BoundHelper.merge(coll, parent.getRoles(aPerson));
           }
       }
       
		coll = BoundHelper.merge(coll, aPerson.getGlobalRoles());
       
       return coll != null && coll.size() > 0;
   }

   /**
     * Make the given person have the given Role for this Objects
     * 
     * This may fall back to the BoundRoles of the Parent (Global Roles).
     * 
     * @param   aPerson     The person to get the roles for
     */
    public void addRoleForPerson(Person aPerson, BoundRole aRole) {

        if (rolesForPersons == null) 
            rolesForPersons = new HashMap();
        
        Set personRoles = (Set) rolesForPersons.get(aPerson);
        if (personRoles == null) {
            personRoles = new HashSet();
            rolesForPersons.put(aPerson, personRoles);
        }
        personRoles.add(aRole);
    }
    
    /**
     * Make the given person have the given Role for this Objects
     * 
     * This may fall back to the BoundRoles of the Parent (Global Roles).
     * 
     * @param   aPerson     The person to get the roles for
     */
    public void removeRoleForPerson(Person aPerson, BoundRole aRole) {

        if (rolesForPersons == null) 
            rolesForPersons = new HashMap();
        
        Set personRoles = (Set) rolesForPersons.get(aPerson);
        if (personRoles == null) {
            personRoles = new HashSet();
        }
        
        personRoles.remove(aRole);
    }
    
	/**
	 * @see com.top_logic.tool.boundsec.BoundObject#getRoles()
	 */
	@Override
	public Collection<BoundRole> getRoles() {
        Collection result = new HashSet();
        if (rolesForPersons != null) {
            Iterator allRoles = rolesForPersons.values().iterator();
            while (allRoles.hasNext()) {
                Set personRoles = (Set) allRoles.next();
                result.addAll(personRoles);
            }
        }
        if (0 != (flags & BoundHelper.INHERIT_ROLES)) {
            BoundObject parent = getSecurityParent();
            if (parent != null) {
                result = BoundHelper.merge(result, parent.getRoles());
            }
        }
        return result;
	}
    
    /**
     * @see com.top_logic.tool.boundsec.BoundObject#getLocalAndGlobalAndGroupRoles(com.top_logic.knowledge.wrap.person.Person)
     */
    @Override
	public Collection<BoundRole> getLocalAndGlobalAndGroupRoles(Person aPerson) {
        Collection thePersonRoles = null;
        try {
            thePersonRoles = this.getLocalAndGlobalRoles(aPerson);
            
			Set<Group> theGroups = Group.getGroups(aPerson);
            if (theGroups != null) {
				Iterator<Group> theGroupIt = theGroups.iterator();
                while (theGroupIt.hasNext()) {
					Group theGroup = theGroupIt.next();
                    Collection theGroupRoles = this.getRoles(theGroup);
                    thePersonRoles = BoundHelper.merge(thePersonRoles, theGroupRoles);
                }
            }
        }
        catch (Exception ex) {
            Logger.error ("Failed to getLocalAndGlobalAndGroupRoles : " + ex, ex, this);
            thePersonRoles = null;
        }
        
        return thePersonRoles;
    }
    
    /**
     * @see com.top_logic.tool.boundsec.BoundObject#getRoles(Group)
     */
    @Override
	public Collection<BoundRole> getRoles(Group aGroup) {
        Collection result = null;
        if (rolesForPersons != null) {
            result = (Collection) rolesForPersons.get(aGroup);
        }
        if (0 != (flags & BoundHelper.INHERIT_ROLES)) {
            BoundObject parent = getSecurityParent();
            if (parent != null) {
                result = BoundHelper.merge(result, parent.getRoles(aGroup));
            }
        }
        
        return result;
    }

    /** @see java.lang.Object#toString() */
    @Override
	public String toString() {
		return this.id.toString();
    }

    /**
     * Make the given group have the given Role for this Object
     * 
     * @param aGroup The group to add the role for
     * @param aRole The role to add for the group
     */
    public void addRoleForGroup(IGroup aGroup, BoundRole aRole) {
        if (rolesForPersons == null) 
            rolesForPersons = new HashMap();
        
        Set personRoles = (Set) rolesForPersons.get(aGroup);
        if (personRoles == null) {
            personRoles = new HashSet();
            rolesForPersons.put(aGroup, personRoles);
        }
        personRoles.add(aRole);
    }

    /**
     * Remove all "has_role" Associations for the Role and a Group.
     * {@link com.top_logic.tool.boundsec.BoundObject}
     * 
     * @param aGroup  The Group to add the role for
     * @param aRole    The role to remove for the Group, may be null to indicate all Roles.
     */
    public void removeRoleForGroup(IGroup aGroup, BoundRole aRole) {
        if (rolesForPersons == null) 
            rolesForPersons = new HashMap();
        
        Set personRoles = (Set) rolesForPersons.get(aGroup);
        if (personRoles == null) {
            personRoles = new HashSet();
        }
        
        personRoles.remove(aGroup);
    }
}
