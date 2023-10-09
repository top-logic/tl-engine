/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.business;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

import com.top_logic.base.user.UserInterface;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.element.boundsec.ElementBoundHelper;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.AssociationQuery;
import com.top_logic.knowledge.service.db2.AssociationSetQuery;
import com.top_logic.knowledge.wrap.StringWrapperAttributeComparator;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.util.TLContext;


/**
 * A PersonContact is the representation of a real contact
 * This contact as a specific set of attributes.
 * The PersonContact may be related to a top-logic Person.
 * If this is the case all attributes managed in the Person are not changeable in
 * the PersonContact, the PersonContact cannot be deleted if the Person is not deleted.
 * 
 * @author    <a href=mailto:jco@top-logic.com>jco</a>
 */
public class PersonContact extends AbstractContact implements UserInterface {

	public static final String META_ELEMENT = "Contact.Person";

    //values for these are to be delivered by the contacts person if:
    // - this contact currently belongs to a person
    // - this contact has no own value for the given attribute
	public static final String PHONE_MOBILE = "phone_mobile";
	public static final String PHONE_PRIVATE = "phone_private";
	public static final String FULLNAME = "fullName";
	public static final String POSITION = "position";
	public static final String BOSS = "boss";
	public static final String COMPANY = "company";
	public static final String FAX = "fax";

	private static final AssociationSetQuery<Account> PERSON_ATTR =
		AssociationQuery.createQuery("person", Account.class, Person.OBJECT_NAME, "contact");
	
	
    public PersonContact(KnowledgeObject ko) {
        super(ko);
    }

	@Override
	public String getName() {
		return super.getName();
	}

    /** 
     * Use global security
     * 
     * @see com.top_logic.tool.boundsec.wrap.AbstractBoundWrapper#getSecurityParent()
     */
    @Override
	public BoundObject getSecurityParent() {
        return ElementBoundHelper.getSecurityRoot();
    }
    
    @Override
	protected String toStringValues() {
		return super.toStringValues() + ", firstName: " + getFirstName();
    }
    
    /** 
     * Overridden to relay some attributes to the optional Person.
     * 
     * If this contact is related to a person, then for selected attributes always the value
     * of the respective person attribute will be returned in favor of the own value.
     * 
	 * @see com.top_logic.element.meta.kbbased.AttributedWrapper#getValue(java.lang.String)
	 */
	@Override
	public Object getValue(String anAttribute) {
		if(FULLNAME.equals(anAttribute)){ //used for tables. Better give those tables an accessor, and remove this code here
			return this.getFullname();
		}
        else {

            return (super.getValue(anAttribute));
        }
	}

	/**
	 * Return the full name of the contact.
	 * 
	 * <p>
	 * This method is used as method of the calculated {@link TLStructuredTypePart}
	 * {@link PersonContact#FULLNAME}.
	 * </p>
	 * 
	 * @return The full name, never <code>null</code>.
	 */
	@CalledByReflection
	public String getFullname() {
        String       theTitle  = (String) this.getValue(TITLE);
        StringBuffer theResult = new StringBuffer (64);

        if (!StringServices.isEmpty(theTitle)) {
            theResult.append(theTitle).append(' ');
        }

        theResult.append((String) this.getValue(FIRST_NAME)).append(' ')
                 .append((String) this.getValue(NAME_ATTRIBUTE));

        return (theResult.toString());
    }

	@Override
	public String getTitle() {
		return (String) getValue(PersonContact.TITLE);
	}

	@Override
	public void setTitle(String value) {
		setValue(TITLE, value);
	}

	@Override
	public String getFirstName() {
	    return (String)getValue(PersonContact.FIRST_NAME);
	}

	@Override
	public void setFirstName(String value) {
		setValue(FIRST_NAME, value);
	}

	@Override
	public String getEMail() {
        return ((String) this.getValue(EMAIL));
    }

	@Override
	public void setEMail(String value) {
		setValue(EMAIL, value);
	}

	@Override
	public String getPhone() {
		return ((String) this.getValue(PHONE));
	}

	@Override
	public void setPhone(String value) {
		setValue(PHONE, value);
	}

	@Override
	public String getUserName() {
		Person person = getPerson();
		return person == null ? null : person.getName();
	}

    /**
     * if this person contact is connected to a person, this person is returned, null otherwise.
     * 
     * @return    This contacts person or null.
     */
    public Person getPerson() {
    	if (!tValid()) return null;
		Iterator<Account> theIt = resolveLinks(PERSON_ATTR).iterator();
    	Person           thePerson = null;

        if (theIt.hasNext()) {
    		thePerson = theIt.next();
    	}
    	if (theIt.hasNext()) {
    		Logger.warn("Contact refers to more than one Person !!", this);
    	}

        return (thePerson);
    }
    
    /**
     * For diagnostic purposes only.
     * A contact always should refer to max one person.
     * So don't call this method in business code, it will be removed again later.
     * 
     * @return a list with all persons this contact is referring to.
     */
	public Collection<Account> getPersons() {
        try{
			return new ArrayList<>(resolveLinks(PERSON_ATTR));
        }catch(Exception e){
			return Collections.emptyList();
        }
    }

    /** 
     * Return the login ID of the person represented by this contact.
     * 
     * @return    The requested login ID or <code>null</code> if there is no such ID.
     */
    public String getPersonID() {
        try {
            Person thePerson = this.getPerson();

            if (thePerson != null) {
                return thePerson.getName();
            }
        }
        catch (Exception ex) {
            Logger.warn("Unable to get person ID for contact " + this, ex, this);

        }
        return null;
    }
    
    /**
     * Connects this contact to the given person via a knowledge association.
     * 
     * If this contact already is connected to a(nother) person, the old connection
     * is thrown away and it is connected to the given person. A contact can only be connected
     * to one person at a time
     * Likewise, if another contact is currently connected to the given person, this connection is
     * thrown away as well because only one contact can refer to a person at the a time.
     */
	public void connectToPerson(Person aPerson) {
		aPerson.tUpdateByName("contact", this);
    }
    
    /**
     * If there is a PersonContact connected to the given person, this contact is returned,
     * <code>null</code> otherwise.
     *
     * @param person the person to get the person contact for 
     * @return the (first if multiple) PersonContact or <code>null</code>
     */
    public static PersonContact getPersonContact(Person person) {
        if (person != null) {
			return ContactFactory.getInstance().getContactForPerson(person);
        }
		return null;
    }

    /**
     * Return a collection of {@link PersonContact} for all given {@link Person}s.
     * 
     * @see #getPersonContact(Person)
     */
    public static Collection<PersonContact> getPersonContacts(Collection<Person> somePersons) {
        Set<PersonContact> theResult = CollectionUtil.newSet(somePersons.size());
        
        for (Person thePerson : somePersons) {
            PersonContact theContact = getPersonContact(thePerson);
            if (theContact != null) {
                theResult.add(theContact);
            }
        }
        
        return theResult;
    }
    
    public static PersonContact getCurrentPersonContact() {
		TLContext context = TLContext.getContext();
		if (context != null) { // will happen at system startup
			return getPersonContact(context.getCurrentPersonWrapper()); // null check included
		}
		return null;
    }

	/** Convenient comparator to sort PersonContacts in "natural" order */
	public static Comparator getComparator() {
		// Note: Make not static, since it depends on the current locale.
		return new StringWrapperAttributeComparator(NAME_ATTRIBUTE, FIRST_NAME, /* ascending */ true);
	}
}
