/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.attributes;

/**
 * This interface defines all attributes, which will be used inside the
 * application to represent information about persons.
 *
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public interface PersonAttributes extends LDAPAttributes {

    /** The attribute "username", i.e. login name */
    public static final String USER_NAME     = "username";
    
    /** The attribute "password". */
    public static final String PASSWORD      = "userpassword";

    /** The attribute "givenname". */
    public static final String GIVEN_NAME    = "givenName";

    /** The attribute "surname". */
    public static final String SUR_NAME      = "sn";

    /** The attribute "mailname". */
    public static final String MAIL_NAME     = "mail";

    /** The attribute "title". */
    public static final String TITLE         = "personalTitle";

    /** The attribute "internal number". */
    public static final String INTERNAL_NR   = "telephoneNumber";

    /** The attribute "locale". */
    public static final String LOCALE        = "locale";
    
    /** attribute to indicate to which data device the person or a user belongs to **/
    public static final String DATA_ACCESS_DEVICE_ID = "dataDeviceID";
    
    /** attribute to indicate against which auth system the person should be authenticated, needed in the KO only, not in the DO **/
    public static final String AUTHENTICATION_DEVICE_ID = "authDeviceID";
    
	/**
	 * Attribute to identify a person who is registered as restricted user, i.e. a user who can do
	 * nothing but read in the application
	 */
    public static final String RESTRICTED_USER = "restrictedUser";
    
	/** List of all DataObject attributes provided in this interface. */
    public static final String [] PERSON_INFO = {
		USER_NAME, GIVEN_NAME,
		SUR_NAME, TITLE, INTERNAL_NR,
		MAIL_NAME, OBJECT_CLASS,
		LOCALE, PASSWORD, DATA_ACCESS_DEVICE_ID, RESTRICTED_USER
    };
}








