/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.contact.business;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import test.com.top_logic.basic.DemoPersonNames;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.base.user.UserInterface;
import com.top_logic.contact.business.ContactFactory;
import com.top_logic.contact.business.PersonContact;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;

/**
 * Common Generator for Contacts for Testing and Demonstration.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class DemoContactGenerator implements DemoPersonNames {

    /** Hold a List of the generated PersonContacts */
    protected List personContacts;

    /** Hold a List of the generated Persons */
    protected List persons;
    
    /** When true additional Persons will be created, too */
    boolean createPersons;
    
    public DemoContactGenerator(boolean createPersons) {
        this.createPersons = createPersons;
    }

    public DemoContactGenerator() {
        this.createPersons = false;
    }

    /**
     * Help setting Up a bunch of Demo {@link PersonContact}s.
     * 
     * Commiting the KBase is a Task for the Caller.
     */
	public List/* <PersonContact> */ createDemoContacts(int numContacts, Random aRand) {

        if (personContacts != null){
            return personContacts;
        }
        
        ContactFactory cf    = ContactFactory.getInstance();
        
        personContacts = new ArrayList(numContacts);
        PersonManager pmgr = null;
        String accessID = null, authID = null;
        if (createPersons) {
            persons   = new ArrayList(numContacts);
            pmgr      = PersonManager.getManager();
            accessID  = TLSecurityDeviceManager.getInstance().getDefaultDataAccessDevice().getDeviceID();
            authID    = TLSecurityDeviceManager.getInstance().getDefaultAuthenticationDevice().getDeviceID();
        }

        for (int i=0; i < numContacts; i++) {
            String firstName = FIRSTNAMES[aRand.nextInt(FIRSTNAMES.length)];
            String lastName  = LASTNAMES [aRand.nextInt(LASTNAMES.length)];
            Person p = null;
            if (createPersons ) {
                p = createPerson(aRand, pmgr, accessID, authID, firstName, lastName, i);
                persons.add(p);
                personContacts.add(cf.getContactForPerson(p));
            } else {
                personContacts.add(createContact(aRand, cf, firstName, lastName, i, p));
            }
        }

        return personContacts;
    }

    /** 
     * Create a new Person via the given PersonManager.
     */
    private Person createPerson(Random aRand, PersonManager pmgr, String accessID, String authID,
            String firstName, String lastName, int i) {
         String name = "" + lastName.charAt(0) + firstName.charAt(0) + Integer.toString(i);
			Person p = pmgr.createPerson(name, accessID, authID, Boolean.FALSE);
         UserInterface user =  Person.userOrNull(p);
			user.setName(lastName);
			user.setFirstName(firstName);
         return  p;
    }

    /** 
     * Create a new PersonContact via the given ContactFactory.
     */
    protected PersonContact createContact(Random aRand, ContactFactory cf, 
			String firstName, String lastName, int i, Person p) {

         PersonContact pc = cf.createNewPersonContact(lastName,firstName);
         
         if (p != null) {
             pc.connectToPerson(p);
         }
         
         
         return pc;
    }

}

