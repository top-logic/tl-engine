
/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.user;

import com.top_logic.base.security.authorisation.roles.ACL;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.dob.DataObject;


/**
 * Interface for userobjects within <i>TopLogic</i>.
 *
 * A user is basically a dataobject. This interface only allows 
 * easier access to it's attributes. It can be used as dataobject 
 * anyway.
 *
 * @author    <a href="mailto:tri@top-logic.com">Thomas Richter</a>
 */
public interface UserInterface extends DataObject {

    /**
     * getter for username, i.e. the login-name.
     * As such this is the unique ID of this user  which is the same
     * as Person::getName() for the according person.
     * This name / id connects a user to a person ! 
     * @return the username of this user
     */
    public String getUserName ();

    /**
     * the firstname of this user
     */
    public String getFirstName ();

    /**
     * the lastname of this user
     */
    public String getLastName ();

    /**
     * get a formatted String for Username, should be "Title FirstName LastName".
     * @return the fullname of this user
     */
    public String getFullName ();
    
	/**
	 * get formatted username: Lastname, Title Firstname.
	 * Suppress Title via param includeTitle
	 * 
	 * @param  includeTitle true to include, false to suppress 
	 * @return formatted username
	 */
	public String getNameAs_LastTitleFirst(boolean includeTitle);

	/**
	 * Is this user restricted or not.
	 */
	@BooleanDefault(false)
	public Boolean isRestricted();

    /**
     * the roles of this user as String sparated by ','
     */
    public String getRoleString ();

    /**
     * the roles of this user as HashSet
     */
    public ACL getACLRoles ();           

    /**
     * the object class of this user (should be always "person")
     */
    public String getObjectClass ();

    /**
     * the password of this user
     */
    public String getPassword ();

     /**
     * the internal email of this user
     */
    public String getInternalMail ();

    /**
     * the external email of this user
     */
    public String getExternalMail ();

    /**
     * the customer name of this user
     */
    public String getCustomerName ();

    /**
     * the title of this user
     */
    public String getTitle ();

    /**
     * the organization unit of this user
     */
    public String getOrgUnit ();

    /**
     * the internal number of this user
     */
    public String getInternalNumber ();

    /**
     * the external number of this user
     */
    public String getExternalNumber ();

    /**
     * the mobile number of this user
     */
    public String getMobileNumber ();

    /**
     * the private number of this user
     */
    public String getPrivateNumber ();

    /**
     * the encapsulated DataObject (if any)
     */
    public DataObject getDataObject ();
    
    /**
     * the ID of the device this user data object originates from
     */
    public String getDataAccessDeviceID();
}
