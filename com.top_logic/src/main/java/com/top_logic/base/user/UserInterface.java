
/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.user;

import com.top_logic.dob.DataObject;


/**
 * Interface for user information in <i>TopLogic</i>.
 *
 * @author <a href="mailto:tri@top-logic.com">Thomas Richter</a>
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
	 * The e-mail of the user that the system may send mails to, if the user must be notified.
	 */
	public String getMail();

    /**
     * the title of this user
     */
    public String getTitle ();

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

}
