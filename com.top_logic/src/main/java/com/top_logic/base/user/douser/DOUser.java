/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.user.douser;


import com.top_logic.base.security.attributes.LDAPAttributes;
import com.top_logic.base.user.UserInterface;
import com.top_logic.basic.Logger;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.ex.NoSuchAttributeException;

/**
 * This class is an implementation of Userinterface and represents a 
 * userobject in top-logic.
 *
 * In fact this class just encapsules an dataobject and provides methods
 * to simplyfy access to its attributes.
 * The roles in form of an ACL will be cached in this instance, so changes
 * in the underlying data object will not be automatically checked.
 *
 * This class <b>does NOT</b> represent the <b>Person</b> KnowledgeObject.
 * 
 * @author    <a href="mailto:tri@top-logic.com">Thomas Richter</a>
 */
public class DOUser implements UserInterface, LDAPAttributes {

    /** The internal dataobject. */
    protected DataObject internalUser;

    /**
     * Creates a new DOUser.
     *
     * @param    aUser    The user as DataObject.
     */
    protected DOUser (DataObject aUser) {
        this.internalUser = aUser;
    }

    @Override
	public String toString() {
        return this.getClass().toString() + '[' + this.getUserName () + ']';
    }
 
    /**
     * Reset the user attributes, i.e. re-read from given DataObject.
     */
    public void reset (DataObject aUser) {
        this.internalUser = aUser;
    }

    /**
     * Get or create the DOUser with the given user data.
     * 
     * @param   aUserDO the user data.
     * @return  the existing or new user, null on error
     */
    public static synchronized DOUser getInstance (DataObject aUserDO) {
		if (aUserDO instanceof UserInterface) {
			return (DOUser) aUserDO;
		}
		return new DOUser(aUserDO);
    }

//    /** equals() based on key or internalUser */
//    public boolean equals (Object anObject) {
//        if (anObject instanceof DOUser) {
//            return (this.key.equals (((DOUser) anObject).key));
//        }
//        else if (anObject instanceof DataObject) {
//            return (this.internalUser.equals (anObject));
//        }
//        else {
//            return false;
//        }
//    }
//
//    /** Extarct hashCode from key */
//    public int hashCode () {
//        return (this.key.hashCode ());
//    }

    /**
     * getter for username, i.e. the login-name.
     * As such this is the unique ID of this user  which is the same
     * as Person::getName() for the according person.
     * This name / id connects a user to a person ! 
     * @return the username of this user
     */
    @Override
	public String getUserName () {
        try {
            return (String) internalUser.getAttributeValue (UserInterface.USER_NAME);
        }
        catch (NoSuchAttributeException nae) {
            Logger.error ("Tried to retrieve Users USERNAME from dataobject " + internalUser + ", which is no User or has no such attribute", this);
            return "";
        }
    }    

    /**
    * the firstname of this user
    */
    @Override
	public String getFirstName () {
        try {
			return (String) (internalUser.getAttributeValue(UserInterface.FIRST_NAME));
        }
        catch (NoSuchAttributeException nae) {
            Logger.error ("Tried to retrieve Users GIVENNAME from dataobject " + internalUser + ", which is no User or has no such attribute", this);
            return "";
        }
    }    

    /**
    * the lastname of this user
    */
    @Override
	public String getName () {
        try {
			return (String) (internalUser.getAttributeValue(UserInterface.NAME));
        }catch (NoSuchAttributeException nae) {
            Logger.error ("Tried to retrieve Users SUR_NAME from dataobject " + internalUser + ", which is no User or has no such attribute", this);
            return "";
        }
    }

	/**
     * the internal email of this user
     */
    @Override
	public String getEMail () {
        try {
            return(String)(internalUser.getAttributeValue (UserInterface.EMAIL));                
        }catch (NoSuchAttributeException nae) {
            Logger.error ("Tried to retrieve Users MAIL_NAME from dataobject " + internalUser + ", which is no User or has no such attribute", this);
            return "";
        }
    }

    /**
     * the title of this user
     */
    @Override
	public String getTitle () {

        try {
            return(String)(internalUser.getAttributeValue (UserInterface.TITLE));                
        }catch (NoSuchAttributeException nae) {
            Logger.error ("Tried to retrieve Users TITLE from dataobject " + internalUser + ", which is no User or has no such attribute", this);
            return "";
        }
    }

    /**
     * the internal number of this user
     */
    @Override
	public String getPhone () {

        try {
            return(String)(internalUser.getAttributeValue (UserInterface.PHONE));                
        }catch (NoSuchAttributeException nae) {
            Logger.error ("Tried to retrieve Users INTERNAL_NUMBER from dataobject " + internalUser + ", which is no User or has no such attribute", this);
            return "";
        }
    }

	@Override
	public void setFirstName(String value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setName(String value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setEMail(String value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setTitle(String value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setPhone(String value) {
		throw new UnsupportedOperationException();
	}

}
