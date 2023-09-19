/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.user;

import java.util.HashMap;

import com.top_logic.base.security.attributes.PersonAttributes;
import com.top_logic.base.user.douser.DOUser;
import com.top_logic.basic.Logger;
import com.top_logic.dob.DataObject;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;

/**
 * @deprecated noone should need this class anymore, 
 *             but its used in lots of tests and stuff We can remove it later.
 *             
 * All handling for persons should be accomplished by the {@link PersonManager} which also provides access to the list of the persons
 * respective users.
 * Each person allows access to its respective user.
 * All further handling of users is done by the TLSecurityDeviceManager or the TLSecuriyDeviceProxy
 * @author    <a href="mailto:tri@top-logic.com">Thomas Richter</a>
 */
@Deprecated
public class UserService {    
    
	/**
	 * Placeholder that is used upon user creation as initial password-hash as long as no explicit
	 * password has been set.
	 */
	public static final String INITIAL_PWD_HASH_PLACEHOLDER = "*";

    /**
     * Returns the userobject for the Person with given name.
     *
     * @param aName   the username
     * @return theUser or null
     */
    public static UserInterface getUser(String aName) {
    	if (PersonManager.Module.INSTANCE.isActive()) {
			Person thePers = PersonManager.getManager().getPersonByName(aName);
			if (thePers != null) {
				return Person.getUser(thePers);
			}
			Logger.info("No person with name ('" + aName + "')", UserService.class);
			return null;
    	} else {
    		Logger.info("Failed to getUser('" + aName + "'): PersonManager not configured", UserService.class);
    		return null;
    	}
    }


    /**
	 * Creates an empty userobject with the given name.
	 * 
	 * All attributes are initally filled with the given username as value except the Object_Class,
	 * the User_Password and the restriction.
	 * <ul>
	 * <li>For Object_Class the value "person" is used (to identify the new Object as User).</li>
	 * <li>For the password the default "bup" is used.</li>
	 * <li>For the restriction the default is FALSE that means it's not restricted.</li>
	 * </ul>
	 * 
	 * @param aName
	 *        the Name for the new user
	 * @return the new User
	 */
    public static UserInterface getEmptyUser(String aName) {
   
        return DOUser.getInstance(getNewUserDO(aName));
    }

	/** 
     * {@link #getEmptyUser(String)}
     */
    public static DataObject getNewUserDO(String aName) {
        String[] attribs    = PersonAttributes.PERSON_INFO;
        int      len        = attribs.length;
        HashMap<Object, Object>  new_person = new HashMap<>(len);

        for (int i = 0; i < len; i++) {
            String attr = attribs[i];

            if (attr.equals(PersonAttributes.OBJECT_CLASS)) {
                new_person.put(attr, "person"); //initiate object class with value person
            }
            else if (attr.equals(PersonAttributes.PASSWORD)) {
				// Don't allow login with a default password.
				// Neither null nor the empty string can be used,
				// as the password is mandatory in the database
				// and oracle does not allow empty strings in a mandatory columns.
				new_person.put(attr, INITIAL_PWD_HASH_PLACEHOLDER);
            }
			else if (attr.equals(PersonAttributes.USER_NAME)
				|| attr.equals(PersonAttributes.GIVEN_NAME)
				|| attr.equals(PersonAttributes.SUR_NAME)
				|| attr.equals(PersonAttributes.DISPLAY_NAME)) {
            	new_person.put(attr, aName);
            }else if(attr.equals(PersonAttributes.RESTRICTED_USER)) {
            	new_person.put(attr, Boolean.FALSE);
            }else {
				new_person.put(attr,"");
			}
        }

        return new UserDataObject(new_person);
    }

}
