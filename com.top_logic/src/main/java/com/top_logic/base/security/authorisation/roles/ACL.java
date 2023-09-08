/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */ 
package com.top_logic.base.security.authorisation.roles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.StringServices;

/**
 * Access Control List for storing Lists of roles.
 * <p>
 * An ACL has an array of strings, which can be inspected by the method hasRole(). To has a better
 * performance, the internal array is sorted. The roles managed within this ACL are not allowed to
 * has spaces or comma in their names.
 * </p>
 * <p>
 * By definition an empty (or null) ACL allows everything. ACLs are uses for other list of roles
 * ({@link com.top_logic.base.user.douser.DOUser}) , too. But the semantics of an empty RoleList for
 * a user is different (No roles, no access). The DOUser has a special case for this by now. But be
 * aware of this before you use the ACL for anything else.
 * </p>
 *
 * @author <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class ACL {

    /** The sorted array of roles, null in case no role */
	private int[] roleList;

    /** The list of roles, empty in case no roles */
	private String roles;

    /**
     * Create an empty ACL.
     */
    public ACL () {
        roleList = null;
        roles    = "";
    }

    /**
     * Create the ACL for the given list of roles.
     *
     * @param    aRoleList    The list to be used for this ACL.
     */
    public ACL (String aRoleList) {
        this(StringServices.toArray(aRoleList, ','));
    }

    /**
     * Create the ACL for the given list of roles.
     *
     * @param    aRoleList    The array to be used for this ACL.
     */
    public ACL (String[] aRoleList) {

        if (aRoleList == null || aRoleList.length == 0) {
            this.roles    = "";   
            this.roleList = null;
        }
        else if (aRoleList.length == 1) {
            this.roles    = aRoleList[0];  
	        this.roleList = new int[] {RoleManager.getInstance().getID(this.roles)};
        }
        else {
            this.roleList = RoleManager.getInstance().getID(aRoleList);
            Arrays.sort(this.roleList);
            // will be created on demand
            // this.roles = this.createACLString(this.roleList);
        }
    }
    
    /** Copy Constructor */
    public ACL(ACL otherACL) {
        roles = otherACL.roles;
        if (otherACL.roleList != null) {
            int len = otherACL.roleList.length;
            roleList = new int[len];
            System.arraycopy(otherACL.roleList, 0, roleList, 0, len);
        }
    }

    /** Create an ACl from a Collection of String */
	public ACL(Collection<String> users) {
        int theLength = (users == null) ? 0 : users.size();

        if (theLength == 0) {
            this.roles    = "";   
            this.roleList = null;
        }
        else {
            this.roleList = RoleManager.getInstance().getID(users);
            Arrays.sort(this.roleList);
        }
    }

    /**
     * Returns the string representation of this instance.
     *
     * This output is only for debugging.
     *
     * @return   The debugging output.
     */
    @Override
	public String toString () {
        return (this.getClass ().getName () + 
	       	" [roles: " + this.getACLString() + ']');
    }
    
    /** 
     * Implement equals, anything other than an ACL will be compread 
     * using toString().
     * 
     * @param    other    The object to be compared.
     * @return   <code>true</code>, if object is equal.
     */
    @Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (other == this) {
			return true;
		}
        if (other instanceof ACL) {
            ACL otherACL = (ACL) other;
            if (this.roleList == null)
                return otherACL.roleList == null;
                
            if (otherACL.roleList == null)
                return false;
            int len = this.roleList.length;
            if (len != otherACL.roleList.length) 
                return false;
                
            for (int i=0; i < len; i++)
                if (this.roleList[i] != otherACL.roleList[i])
                    return false;
            
            return true;    // everything is equal
        }
        else {
            return this.getACLString().equals(other.toString());
        }
    }

	@Override
	public int hashCode() {
		int hash = 6701;
		if (roleList != null) {
			for (int role : roleList) {
				hash = 31 * hash + role;
			}
		}
		return hash;
	}

    /** Return true when the ACL list is empty */
    public boolean isEmpty() {
        return roleList == null;
    }
    

    /**
     * Checks, whether the given ACL contains at least one role, which
     * is also contained in this ACL.
     * 
     * @param    anACL    The list to be inspected against.
     * @return   true, if at least one role of the given ACL is contained
     *           in this instance.
     */
    public boolean hasRole (ACL anACL) {
        return (this.hasRole(anACL.roleList));
    }

    /**
     * Checks, whether the given ACL contains at least one role, which
     * is also contained in this ACL.
     *
     * @param    aRoleList    The list to be inspected against.
     * @return   true, if at least one role of the given ACL is contained
     *           in this instance.
     */
    public boolean hasRole(String aRoleList) {
        if (this.roleList == null) {
            return (false);   // An empty list cannot have any role
        }

        if (StringServices.isEmpty(aRoleList)) {
            return (false);
        }
		int[] theRoles = this.toList(aRoleList);
		Arrays.sort(theRoles);
        return (this.hasRole(theRoles));
    }

    /**
     * Checks, whether the given ACL contains at least one role, which
     * is also contained in this ACL.
     * 
     * An empty ACL will allow all.
     *
     * @param    anACL    The list to be inspected against.
     * @return   true, if at least one role of the given ACL is contained
     *           in this instance.
     */
    public boolean hasAccess(ACL anACL) {
        if (this.roleList == null) {
            return (true);   // An empty list allows all
        }

        if (anACL == null) {
            return (false);   // No ACL, no access
        }

        return (this.hasRole(anACL.roleList));
    }

    /**
     * Checks, whether the given ACL contains at least one role, which
     * is also contained in this ACL.
     *
     * An empty ACL will allow all
     *
     * Please consider using {@link #hasRole(ACL)} for complex cases.
     * 
     * @param    aRoleList    The list to be inspected against.
     * @return   true, if at least one role of the given ACL is contained
     *           in this instance.
     * 
     */
    public boolean hasAccess(String aRoleList) {
        if (this.roleList == null) {
            return (true);   // An empty list allows all
        }

        if (StringServices.isEmpty(aRoleList)) {
            return (false);
        }

        return (this.hasRole(this.toList(aRoleList)));
    }

    /**
     * Checks, whether the given user ID is allowd to acess.
     *
     * An empty ACL will allow all (as opposed to <code>hasRole()</code> !)
     *
     * @param    aUserId    The list to be inspected against.
     * @return   true, if at least one role of the given ACL is contained
     *           in this instance.
     * 
     */
    public boolean hasAccess(int aUserId) {
        if (this.roleList == null) {
            return (true);   // An empty list allows all
        }

        return (this.hasRole(aUserId));
    }

    /**
     * Add a Role to the List of roles, but only if it does not yet exist.
     *
     * @param   aRole      The new role (id ) to be added,
     * @return  true       when role was inserted.
     */
    public boolean addRole (int aRole) {
        if (this.roleList == null) {
            this.roles    = null;   // create on demand
            this.roleList = new int[] {aRole};

            return true;   // was inserted
        }
        
        int index = Arrays.binarySearch(this.roleList, aRole);

        if (index >= 0) {
            return false;   // already contained
        }

        index = -index -1;

        int[] old  = this.roleList;
        int   len  = old.length;

        this.roleList        = new int[len+1];
        this.roleList[index] = aRole;

        int diff = len - index;

        if (index != 0) {
            System.arraycopy(old,0,this.roleList,0, index);
        }

        if (diff != 0) {
            System.arraycopy(old,index,this.roleList,index+1, diff);
        }

        this.roles = null;

        return true;
    }

    /**
     * Add some other ACL to this ACL, in fact a merge.
     *
     * @param   anACL      The ACL (resp. its ids) to be added,
     * @return  true       when ACL was actually modified.
     */
    public boolean add (ACL anACL) {
        
        if (anACL.roleList == null)
            return false;   // nothing to merge
        
        int len = anACL.roleList.length;
        if (this.roleList == null) {
            this.roleList = new int[len];
            System.arraycopy(anACL.roleList, 0, this.roleList, 0, len); 
            this.roles    =  anACL.roles;
            return true;    // was indeed changed
        }
        
        // Do actual merge now
        boolean result = false;
        for (int i=0; i < len; i++) {
			boolean added = addRole(anACL.roleList[i]);
			if (added) {
				result = true;
			}
        }
        if (result)
            roles = null;   // recreate the lazy way
        
        return result;
    }

    /**
     * Add a Role to the List of roles, but only if it does not yet exist.
     *
     * @param    aRole      The new role to be added.
     * @return  true        when role was inserted.
     */
    public boolean addRole (String aRole) {
        int theID = RoleManager.getInstance().getID(aRole);

        if (this.roleList == null) {
            this.roles    = aRole;
            this.roleList = new int[] {theID};

            return true;   // was inserted
        }
        
        int index = Arrays.binarySearch(this.roleList, theID);

        if (index >= 0) {
            return false;   // already contained
        }

        index = -index -1;

        int[] old  = this.roleList;
        int   len  = old.length;

        this.roleList        = new int[len+1];
        this.roleList[index] = theID;

        int diff = len - index;

        if (index != 0) {
            System.arraycopy(old,0,this.roleList,0, index);
        }

        if (diff != 0) {
            System.arraycopy(old,index,this.roleList,index+1, diff);
        }

        this.roles = null;

        return true;
    }

    /**
     * Returns the list of roles.
     *
     * @return    The list of roles as a comma separated string.
     */
    public String getACLString () {
        if (this.roleList != null) {
            if (this.roles == null) {
                this.roles = this.createACLString(this.roleList);
            }

            return (this.roles);
        }
        else {
            return ("");
        }
    }

    /**
     * Returns the list of roles as List ofString
     *
     * @return    A List of Strings with the roles int he acl.
     *             empty list in case of no roles.
     */
	public List<String> getACLList() {
        
        if (roleList == null)
			return Collections.emptyList();
        
        int          len        = roleList.length;
        RoleManager  theManager = RoleManager.getInstance();
		ArrayList<String> theRoles = new ArrayList<>(len);

        for (int i= 0; i < len; i++) {
            theRoles.add(theManager.getRole(roleList[i]));
        }
        
        return theRoles;
    }

    /**
     * Check, if the given role is contained in this ACL.
     * 
     * A role is contained in this ACL, if it is named in it. This is also
     * defined for empty ACLs, whereas this method returns <code>false</code>.
     * 
     * @return    <code>true</code>, if the given role can be found in this
     *            ACL.
     */
    public boolean hasRole(int aRole) {
        if (this.roleList == null) {
            return (false);  // no role can be found in an empty list
        }

        return (Arrays.binarySearch(this.roleList, aRole) >= 0);
    }

    /**
     * Checks, whether the given List of role-names contains at least one role, 
     * which is also contained in this ACL.
     *
     * @param    aRoleList    The list to be inspected against.
     * @return   true, if at least one role of the given ACL is contained
     *           in this instance.
     */
    public boolean hasRole(int[] aRoleList) {
        if (aRoleList == null || this.roleList == null) {
            return false;  // no role can be found in an empty list
        }

		//Loop over aRoleList and check every entry against roleList.
		//start comparing the next item at the last position 
		int i = 0; //index for 1st ACL
		int j = 0; //index for 2nd ACL
		int aRLlength = aRoleList.length;
		int rLlength  = roleList.length;
		while (i < aRLlength && j < rLlength){
			while(j < rLlength){
				if(aRoleList[i] > this.roleList[j])
					j++;	
				else if (aRoleList[i] < this.roleList[j])
					break;
				else
					return true;
			}
			i++;
		}

        return false;	// nothing found at all
    }

    protected int[] toList(String aRoleList) {
        String[] theArray = StringServices.toArray(aRoleList, ',');

        return (RoleManager.getInstance().getID(theArray));
    }

	/** 
     * Internal method to (re)create the normalized ACL String.
     * 
     * @param    someRoles    The list of roles to be reordered.
     * @return   The string representation of the roles.
     */
    protected String createACLString(int[] someRoles) {
        RoleManager  theManager = RoleManager.getInstance();
        StringBuffer theRoles   = new StringBuffer(someRoles.length << 5);

        theRoles.append(theManager.getRole(someRoles[0]));
    
        for (int thePos = 1; thePos < someRoles.length; thePos++) {
            theRoles.append(',').append(theManager.getRole(someRoles[thePos]));
        }

        return (theRoles.toString());
    }

    /** Optimized code to cerate a ',' seperated String of roles.
     * 
     * @param aRoles must not be null.
     */ 
    protected String createACLString(String[] aRoles) {
		int          theLength = aRoles.length;
        StringBuffer theRoles  = new StringBuffer(theLength << 5);

        theRoles.append(aRoles[0]);
    
        for (int thePos = 1; thePos < theLength; thePos++) {
            theRoles.append(',');
            theRoles.append(aRoles[thePos]);
        }

        return (theRoles.toString());
	}

}
