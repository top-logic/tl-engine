/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.user;

import java.util.HashMap;

import com.top_logic.base.security.attributes.LDAPAttributes;
import com.top_logic.base.user.douser.DOUser;
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
        String[] attribs    = new String[] {
			UserInterface.USER_NAME, UserInterface.NAME,
			UserInterface.FIRST_NAME, UserInterface.TITLE, UserInterface.PHONE,
			UserInterface.EMAIL, LDAPAttributes.OBJECT_CLASS,
			Person.DATA_ACCESS_DEVICE_ID, Person.RESTRICTED_USER
		};
        int      len        = attribs.length;
        HashMap<Object, Object>  new_person = new HashMap<>(len);

        for (int i = 0; i < len; i++) {
            String attr = attribs[i];

			if (attr.equals(LDAPAttributes.OBJECT_CLASS)) {
                new_person.put(attr, "person"); //initiate object class with value person
            }
			else if (attr.equals(UserInterface.USER_NAME)
				|| attr.equals(UserInterface.NAME)
				|| attr.equals(UserInterface.FIRST_NAME)) {
            	new_person.put(attr, aName);
            }else if(attr.equals(Person.RESTRICTED_USER)) {
            	new_person.put(attr, Boolean.FALSE);
            }else {
				new_person.put(attr,"");
			}
        }

        return new UserDataObject(new_person);
    }

}
