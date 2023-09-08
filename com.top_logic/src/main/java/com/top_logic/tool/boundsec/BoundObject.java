/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import java.util.Collection;

import com.top_logic.basic.TLID;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.wrap.Group;

/**
 * Objects defining access restrictions.
 *
 * @author <a href="mailto:dro@top-logic.com">Dieter Rothb&auml;cher</a>
 */
public interface BoundObject extends TLObject {
        
    /**
     * Get the ID for this BoundObject
     * 
     * @return the ID for this BoundObject
     *         <code>null</code> if ID could not be retrieved
     */
    public TLID getID();

    /**
     * Get the child bound objects for this bound object
     * 
     * @return a collection of child BoundObjects, <code>null</code> 
     *         or empty collection in case there are no children
     */
	public Collection<? extends BoundObject> getSecurityChildren();
    
    /**
     * Get the parent object for this bound object.
     * 
     * @return the parent bound object for this bound object, 
     *         <code>null</code> if there is no parent (e.g. this is a root element)
     */
    public BoundObject getSecurityParent();
    
    /**
     * Get the {@link com.top_logic.tool.boundsec.BoundRole}s of the given 
     * {@link com.top_logic.knowledge.wrap.person.Person} for this object.
     * 
     * This will usually fall back to the BoundRoles of the Parent (Global Roles).
     * 
     * @param aPerson The person to get the roles for
     * @return a collection of roles the given person has in/for this Object,
     *         <code>null</code> or empty collection in case there are no roles
     */
	public Collection<BoundRole> getRoles(Person aPerson);
    
    /** 
     * Get the roles available for the wrapped 
     * {@link com.top_logic.tool.boundsec.BoundObject} 
     * and the given person including the global roles
     * of the person.
     * 
     * @param   aPerson   The person to get the roles for
     * @return the roles available for the object and the given person,
     *         <code>null</code> if an error occurs
     */
	public Collection<BoundRole> getLocalAndGlobalRoles(Person aPerson);
    
    /**
     * Get the roles the given Group has on
     * this object.
     * 
     * @param aGroup the Group
     * @return the roles available for the object and the given group,
     *         <code>null</code> if an error occurs
     */
	public Collection<BoundRole> getRoles(Group aGroup);
    
    /** 
     * Get the roles available for the wrapped 
     * {@link com.top_logic.tool.boundsec.BoundObject} 
     * and the given person including the global roles
     * of the person and the roles the person has according to group
     * memberships.
     * 
     * @param   aPerson   The person to get the roles for
     * @return the roles available for the object and the given person,
     *         <code>null</code> if an error occurs
     */
	public Collection<BoundRole> getLocalAndGlobalAndGroupRoles(Person aPerson);

    /**
	 * Get all {@link com.top_logic.tool.boundsec.BoundRole}s for this object.
	 * 
	 * This will usually fall back to the BoundRoles of the Parent (Global Roles).
	 * 
	 * @return a collection of roles for this object,
     *         <code>null</code> or empty collection in case there are no roles
	 */
	public Collection<BoundRole> getRoles();
    
    /**
     * Check if the given Person has any role ot this object.
     * 
     * This will usually fall back to the BoundRoles of the Parent (Global Roles).
     * 
     * @param aPerson The person to get the roles for
     * @return true when <code>getRoles(aPerson)</code> == null (or empty)
     */
    public boolean hasAnyRole(Person aPerson);

}