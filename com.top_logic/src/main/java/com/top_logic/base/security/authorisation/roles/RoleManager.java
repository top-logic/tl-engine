/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.authorisation.roles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.StringServices;

/** 
 * Manager for all roles used by this application.
 * 
 * Every role within the system has a (transient) unique ID of type 
 * <code>int</code>, which can be used for faster comparement done by 
 * {@link com.top_logic.base.security.authorisation.roles.ACL ACLs}. If there is a role, which
 * is not known by the manager, it'll be appended to the manager.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class RoleManager {

	/**
	 * Singleton {@link RoleManager} instance.
	 */
	public static final RoleManager INSTANCE = new RoleManager();

    /** The map holding all known roles. */
    private Map roles;

    /** The invers map of all known roles. */
    private Map keys;

    /** The counter to be used for unique IDs. */
    private int counter;

	/**
	 * Well-known role name for superusers.
	 */
	public static final String ADMIN_ROLE_NAME = "tl.admin";

	/** Hard-coded role for a superuser. */
	public static final int ADMIN_ROLE = getInstance().getID(ADMIN_ROLE_NAME);

    /**
     * Constructor for RoleManager.
     */
    public RoleManager() {
        super();

        this.roles = new HashMap(512);
        this.keys  = new HashMap(512);
    }

    /**
     * Return the transparent unique ID of the given role.
     * 
     * The ID is unique for the whole runtime of the application.
     * 
     * @param    aRole    The role to be found.
     * @return   The requested ID.
     * @throws   IllegalArgumentException    If parameter is null or empty.
     */
    public synchronized int getID(String aRole) throws IllegalArgumentException {
        if (StringServices.isEmpty(aRole)) {
            throw new IllegalArgumentException("Given parameter is null or " +
                                               "empty (" + aRole + ')');
        }

        Integer theInt = (Integer) this.roles.get(aRole);

        if (theInt == null) {
            theInt = this.nextInt();

            this.roles.put(aRole, theInt);
            this.keys.put(theInt, aRole);
        }

        return (theInt.intValue());
    }

    /**
     * Return the transparent unique IDs of the given roles.
     * 
     * The ID is unique for the whole runtime of the application.
     * 
     * @param    someRoles    The roles to be found.
     * @return   The requested IDs.
     * @throws   IllegalArgumentException    If parameter or a value within
     *                                       the array is null or empty.
     */
    public int[] getID(String[] someRoles) throws IllegalArgumentException {
        if (StringServices.isEmpty(someRoles)) {
            throw new IllegalArgumentException("Given parameter is empty");
        }
        
        int[] theArray = new int[someRoles.length];

        for (int thePos = 0; thePos < someRoles.length; thePos++) {
            theArray[thePos] = this.getID(someRoles[thePos]);
        }

        return (theArray);
    }

    /**
     * Return the transparent unique IDs of the given roles.
     * 
     * The ID is unique for the whole runtime of the application.
     * 
     * @param    someRoles     The roles to be found.
     * @return   The requested IDs, null when input was null
     */
    public int[] getID(Collection someRoles)  {
        
        if (someRoles == null)
            return null; 
        int[] theArray = new int[someRoles.size()];

        int      thePos   = 0;
        Iterator theRoles = someRoles.iterator();
        while (theRoles.hasNext()) {
            theArray[thePos++] = this.getID((String) theRoles.next());
        }

        return theArray;
    }

    /**
     * Return the name of the role identified by the given ID.
     *
     * @param    anID    The ID of the role.
     * @return   The role defined by the given ID.
     * @throws   IllegalArgumentException    If there is no role for
     *                                       the given ID or the ID 
     *                                       is invalid.
     */
    public synchronized String getRole(int anID) throws IllegalArgumentException {
		String theResult = (String) this.keys.get(Integer.valueOf(anID));

        if (theResult == null) {
            throw new IllegalArgumentException("No role for ID " + anID + 
                                               "found");
        }

        return (theResult);
    }

    /**
     * Return the list of all roles currently used by the application.
     * 
     * @return    The list of all roles.
     */
    public synchronized List getRoles() {
        return (new ArrayList(this.roles.entrySet()));
    }

    /**
     * Return the next ID for the role manager.
     * 
     * @return    The next integer for the role manager.
     */
    private synchronized Integer nextInt() {
        this.counter = (this.counter + 1);

		return (Integer.valueOf(this.counter));
    }

    /**
     * Return the only instance of this manager.
     * 
     * @return    The only instance of this manager.
     */
    public static RoleManager getInstance() {
        return INSTANCE;
    }
}
