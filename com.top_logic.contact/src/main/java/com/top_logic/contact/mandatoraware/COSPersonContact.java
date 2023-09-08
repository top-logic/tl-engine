/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.mandatoraware;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.contact.business.AbstractContact;
import com.top_logic.contact.business.CompanyContact;
import com.top_logic.contact.business.ContactFactory;
import com.top_logic.contact.business.PersonContact;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.MetaElementFactory;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.structured.wrap.Mandator;
import com.top_logic.element.structured.wrap.MandatorAware;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.wrap.Group;

/**
 * Mandator-aware person contact
 *
 * @author     <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public class COSPersonContact extends PersonContact implements COSContactConstants, MandatorAware {

	/** Flag that denotes that a contact may be a contract owner if set to true. */
	public static final String ATT_FAX        = "fax";

	public COSPersonContact(KnowledgeObject ko) {
		super(ko);
	}

	/**
	 * @see com.top_logic.element.structured.wrap.MandatorAware#getMandator()
	 */
	@Override
	public Mandator getMandator() {
		return (Mandator) this.getValue(ATTRIBUTE_MANDATOR);
	}

    /**
     * @see com.top_logic.element.structured.wrap.MandatorAware#setMandator(com.top_logic.element.structured.wrap.Mandator)
     */
    @Override
	public void setMandator(Mandator aMandator) {
    	this.setValue(ATTRIBUTE_MANDATOR, aMandator);
    }

    /**
     * Use Mandator as security parent
     *
     * @see com.top_logic.contact.business.PersonContact#getSecurityParent()
     */
    @Override
	public BoundObject getSecurityParent() {
        return this.getMandator();
    }

    /**
     * the companies of which the current contact is lead buyer
     */
    public List getCompaniesOfLeadBuyer() {
		List result = new ArrayList();
		try {
			TLClass theME = MetaElementFactory.getInstance().getGlobalMetaElement(ContactFactory.STRUCTURE_NAME, CompanyContact.META_ELEMENT);
			TLStructuredTypePart theMA = MetaElementUtil.getMetaAttribute(theME, COSCompanyContact.ATTRIBUTE_LEAD_BUYER);
			result.addAll(AttributeOperations.getReferers(this, theMA));
		} catch (Exception e) {
			Logger.error("Unable to retrieve personcontacts for companycontact",e,this);
		}

		return result;
    }

	/**
	 * Filter contacts matching one of the given mandators
	 *
	 * @param aContactList the contact list
	 * @param aMandatorList may be null and will not be checked then
	 * @return the list of AbstractContacts
	 */
	protected static List getFilteredList(List aContactList, List aMandatorList) {
		if (aContactList == null || aMandatorList == null) {
			return aContactList;
		}

	    int  size    = aContactList.size();
		List theList = new ArrayList();

		for (int i=0; i < size; i++) {
	        MandatorAware theAware = (MandatorAware) aContactList.get(i);
			if (aMandatorList.contains(theAware.getMandator())) {
				theList.add(theAware);
			}
		}

		return theList;
	}

	/**
	 * Get the PersonContacts whose name matches the given name
	 * and of of the given mandators
	 *
	 * @param aSurname may be null
	 * @param aFirstName may be null
	 * @param aMandatorList may be null and will not be checked then
	 * @return the list of AbstractContacts
	 */
	public static List getListByName (String aSurname, String aFirstName, List aMandatorList) {
	   List theContacts;

	   // Get list, maybe filter by surname
	   if (aSurname == null) {
		   theContacts = ContactFactory.getInstance().getAllContacts(ContactFactory.PERSON_TYPE);
	   }
	   else {
		   theContacts = ContactFactory.getInstance().getAllContactsWithAttribute(ContactFactory.PERSON_TYPE, AbstractContact.NAME_ATTRIBUTE, aSurname, false);
	   }

	   // filter by mandator
	   theContacts = getFilteredList(theContacts, aMandatorList);

	   // Filter by first name
	   Iterator theIt = theContacts.iterator();
	   if (aFirstName == null) {	// No first name filter
		   return theContacts;
	   }

	   List theResult = new ArrayList();
	   while (theIt.hasNext()) {
		   PersonContact thePerson = (PersonContact) theIt.next();
		   if (aFirstName.equals(thePerson.getValue(ATT_FIRSTNAME))) {
			   theResult.add(thePerson);
		   }
	   }

	   return theResult;
	}

    /**
     * Return the list of persons, the given person is a representative for.
     *
     * @param    aPerson    The person to get the representations for, may be <code>null</code>.
     * @return   The list of representations, never <code>null</code>, may be empty.
     */
    public static List getRepresentations(Person aPerson) {
        List theResult = new ArrayList();

        if (aPerson != null) {
            for (Iterator theIt = Group.getGroups(aPerson, false, true).iterator(); theIt.hasNext(); ){
                Group theGroup = (Group) theIt.next();

                if (theGroup.isRepresentativeGroup()) {
                    Person        thePerson = (Person)theGroup.getBoundObject();
                    PersonContact theContact = ContactFactory.getInstance().getContactForPerson(thePerson);

                    if ((theContact != null) && !theResult.contains(theContact)) {
                        theResult.add(theContact);
                    }
                }
            }
        }

        return (theResult);
    }
    
    /**
     * Returns the mandator of the actual person/user.
     */
    public static Mandator getActualMandator() {
        PersonContact theContact = getCurrentPersonContact();
        if (theContact instanceof MandatorAware) {
            return ((MandatorAware) theContact).getMandator();
        }
        return Mandator.getRootMandator();
    }
}
