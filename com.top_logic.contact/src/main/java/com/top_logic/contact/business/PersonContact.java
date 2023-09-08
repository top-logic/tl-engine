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

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.element.boundsec.ElementBoundHelper;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.AssociationQuery;
import com.top_logic.knowledge.service.KnowledgeBase;
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
public class PersonContact extends AbstractContact {

	public static final String META_ELEMENT = "Contact.Person";

    public static final String ATT_FIRSTNAME = "firstname";
    public static final String ASS_NAME_TO_PERSON = "representsUser";
    
    //values for these are to be delivered by the contacts person if:
    // - this contact currently belongs to a person
    // - this contact has no own value for the given attribute
    public static final String ATT_TITLE = "title";
	public static final String ATT_PHONE_MOBILE = "phone_mobile";
	public static final String ATT_PHONE_PRIVATE = "phone_private";
	public static final String ATT_PHONE_OFFICE = "phone";
	public static final String ATT_MAIL = "email";
	public static final String ATT_FULLNAME = "fullName";
	public static final String ATT_POSITION = "position";
	public static final String ATT_BOSS = "boss";
	public static final String ATT_COMPANY = "company";
	public static final String ATT_FAX = "fax";

	private static final AssociationSetQuery<KnowledgeAssociation> PERSON_ATTR = AssociationQuery.createOutgoingQuery(
		"person", ASS_NAME_TO_PERSON);
	
	
    public PersonContact(KnowledgeObject ko) {
        super(ko);
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
		if(ATT_FULLNAME.equals(anAttribute)){ //used for tables. Better give those tables an accessor, and remove this code here
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
	 * {@link PersonContact#ATT_FULLNAME}.
	 * </p>
	 * 
	 * @return The full name, never <code>null</code>.
	 */
	@CalledByReflection
	public String getFullname() {
        String       theTitle  = (String) this.getValue(ATT_TITLE);
        StringBuffer theResult = new StringBuffer (64);

        if (!StringServices.isEmpty(theTitle)) {
            theResult.append(theTitle).append(' ');
        }

        theResult.append((String) this.getValue(ATT_FIRSTNAME)).append(' ')
                 .append((String) this.getValue(NAME_ATTRIBUTE));

        return (theResult.toString());
    }

	public String getFirstName() {
	    return (String)getValue(PersonContact.ATT_FIRSTNAME);
	}

    /** 
     * Return the mail address of the contact.
     * 
     * @return    The mail address or <code>null</code>, if no mail configured.
     */
    public String getMail() {
        return ((String) this.getValue(ATT_MAIL));
    }

    /**
     * if this person contact is connected to a person, this person is returned, null otherwise.
     * 
     * @return    This contacts person or null.
     */
    public Person getPerson() {
    	if (!tValid()) return null;
    	Iterator<Person> theIt     = resolveWrappersTyped(PERSON_ATTR, Person.class).iterator();
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
    public Collection getPersons(){
        try{
            return new ArrayList(resolveWrappers(PERSON_ATTR));
        }catch(Exception e){
            return Collections.EMPTY_LIST;
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
    	KnowledgeObject theKO   = this.tHandle();
        KnowledgeObject thePKO  = aPerson.tHandle();
        KnowledgeBase   theBase = theKO.getKnowledgeBase();

		// remove old
		Collection<KnowledgeAssociation> oldAssociations = new ArrayList<>();
		CollectionUtil.addAll(oldAssociations, theKO.getOutgoingAssociations(ASS_NAME_TO_PERSON));
		CollectionUtil.addAll(oldAssociations, theKO.getIncomingAssociations(ASS_NAME_TO_PERSON));
		theBase.deleteAll(oldAssociations);

        //create new;
		theBase.createAssociation(theKO, thePKO, ASS_NAME_TO_PERSON);
        this.copyPersonAttributesToContact(aPerson);
    }
    
    public void copyPersonAttributesToContact(Person myPerson){
    	try {
    	    try {
                this.setValue(NAME_ATTRIBUTE     ,myPerson.getLastName());
                String fName = myPerson.getFirstName();
                if (fName == null) { // Person         _does_  allow null FirstName
                    fName = "";      // PersonContact does not allow null FirstName.
                }
                this.setValue(ATT_FIRSTNAME      ,fName);
                this.setValue(ATT_TITLE          ,myPerson.getTitle());
                this.setValue(ATT_MAIL           ,myPerson.getInternalMail());
                this.setValue(ATT_PHONE_MOBILE   ,myPerson.getMobileNumber());
                this.setValue(ATT_PHONE_PRIVATE  ,myPerson.getPrivateNumber());
                this.setValue(ATT_PHONE_OFFICE   ,myPerson.getInternalNumber());
            }
            catch (Exception ex) {
                Logger.error("Failed to copyPersonAttributesToContact", ex, this);
            }    	}
    	catch (Exception ex) {
    		Logger.error("Failed to copyPersonAttributesToContact", ex, this);
    	}
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
		return new StringWrapperAttributeComparator(NAME_ATTRIBUTE, ATT_FIRSTNAME, /* ascending */ true);
	}
}
