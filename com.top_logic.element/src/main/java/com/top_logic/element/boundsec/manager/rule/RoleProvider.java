/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager.rule;

import java.util.Collection;
import java.util.Set;

import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tool.boundsec.wrap.Group;

/**
 * The RoleProvider represents one reason to grant a role to groups on bound objects.
 * 
 * @author    <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public interface RoleProvider {

	/**
	 * The Type classifies the {@link RoleProvider}s 
	 * 
	 * @author    <a href=mailto:tsa@top-logic.com>tsa</a>
	 */
	enum Type {
		/**
		 * The {@link RoleProvider} grants roles based on relations between objects and {@link Group}s.
		 * The exact characteristics of this relation depends on the implementation.
		 */
	    reference, 
	    /**
	     * The {@link RoleProvider} grants roles based on roles granted on other objects;
	     * in this case {@link RoleProvider#getBaseObjects(Object)} and {@link RoleProvider#getSourceRole()}
	     * must provide a proper implementation.
	     */
	    inheritance;
	}

	/**
	 * Get an id for this {@link RoleProvider}; the id must be unique within the {@link SecurityManager}.
	 * 
	 * @return the id, never <code>null</code>
	 */
	public String getId();

	/**
	 * the {@link BoundedRole} this role provider grants to {@link Group}s on {@link BoundObject}s.  
	 */
	public BoundRole getRole();

	/**
	 * Determines all groups that receive the {@link #getRole()} based on this role provider
	 * on the given object.
	 *
	 * @param anObject  the o on which the groups will receive the role
	 * @return all groups that receive the role based on this role
	 */
	public Collection<Group> getGroups(BoundObject anObject);
	
	/**
	 * Check, if this {@link RoleProvider} is responsible for the given object.
	 * 
	 * @param anObject  the object to check
	 * @return true iff the given object may be affected
	 */
	public boolean matches(BoundObject anObject);
	
	/**
	 * the type
	 */
	public Type getType();

	/**
	 * For {@link Type#inheritance} {@link RoleProvider}, get all objects
	 * affected by this {@link RoleProvider} assumed that the given object's
	 * roles changed
	 * 
	 * @param aSource an object from which roles are inherited 
	 * @return the objects affected by a change of roles on the given source object, never <code>null</code>
	 */
	public Set<BoundObject> getBaseObjects(Object aSource);

	/**
	 * For {@link Type#inheritance} {@link RoleProvider}, 
	 * get the {@link BoundedRole} needed on the source.
	 * 
	 * @return the role needed on the source, never <code>null</code>
	 */
	public BoundRole getSourceRole();

}
