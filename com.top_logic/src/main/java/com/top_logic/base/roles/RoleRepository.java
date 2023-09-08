/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.roles;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * This class implements a container for Role objects. 
 *
 * @author  Mathias Maul
 */
public class RoleRepository extends HashMap {

	/**
	 * Default constructor.
	 */
	public RoleRepository() {
	}

    /**
     * Constructor.with given size
     */
    public RoleRepository(int size) {
        super(size);
    }


	/**
	 * Removes all roles from the repository.
	 */
	public void removeAllRoles() {
		super.clear();
	}
	
	/**
	 * Returns a Role object specified by its name. If there are two Roles with
	 * the same name in the repository, returns the "first" one as specified
	 * in the underlying Vector object. If the role is not found, null is returned.
	 *
	 * @param	aName	the name of the requested role
	 * @return			the role with name==aName, or null if not found
	 */
	public Role getRoleByName(String aName) {
        return (Role) super.get(aName)		;
	}


	/**
	 * Adds a Role to the repository.
	 *
	 * @param	aRole	the role to be added
	 */
	public void addRole(Role aRole) {
		super.put(aRole.getName(), aRole);
	}

	/**
	 * Removes the specified Role from the repository.
	 *
	 * @param	aRole	the role to be removed
	 */
	public void removeRole(Role aRole) {
		super.remove(aRole.getName());
	}

	/**
	 * Removes a Role from the repository, taking the role name as parameter.
	 *
	 * @param	aRoleName	the name of the role to be removed
	 */
	public void removeRole(String aRoleName) {
        super.remove(aRoleName);
	}

	/**
	 * Checks whether the repository contains a role with the name supplied.
	 *
	 * @param	aName	the role name that is checked for
	 */
	public boolean contains(String aName) {
		return super.containsKey(aName);
	}

	/**
	 * Checks whether the repository contains the role specified in the parameter.
	 *
	 * @param	aRole	the role that is checked for
	 */
	public boolean contains(Role aRole) {
        return super.containsKey(aRole.getName());
	}
	

	/**
	 * Returns a CSV (comma separated values) representation of the repository.
	 * The returned string contains the names of the roles separated by commas.
	 *
	 * @return	a String containing all role names in the repository separated by ","
	 */
	public String getCSV() {
		Iterator     it  = super.keySet().iterator();
		StringBuffer csv = new StringBuffer(super.size() << 5);
		
        if (it.hasNext())
            csv.append((String) it.next());
        
		while (it.hasNext())  {
            csv.append(',');
            csv.append((String) it.next());
		}
		
		return csv.toString();
	}



	/**
	 * Returns a Collection with the names of all Roles. 
     * Used e. g.  for rendering the HTML representation of the Repository.
	 */
	public Collection getNames() {
		return super.keySet();
	}

    /**
     * Returns a Collection with all Roles. 
     */
    public Collection getRoles() {
        return super.values();
    }

	/**
	 * Returns a string representation of the repository.
	 */
	@Override
	public String toString() {
        Iterator     it  = super.keySet().iterator();
		StringBuffer result = new StringBuffer(super.size() << 5);
        
        result.append('[');
        if (it.hasNext())
            result.append((String) it.next());
        
        while (it.hasNext())  {
            result.append(',');
            result.append((String) it.next());
        }
        
        result.append(']');
		
		return result.toString();
	}
}
