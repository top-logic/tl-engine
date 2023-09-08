/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.mandatoraware;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.contact.business.AbstractContact;
import com.top_logic.contact.business.CompanyContact;
import com.top_logic.contact.business.ContactFactory;
import com.top_logic.element.structured.wrap.Mandator;
import com.top_logic.element.structured.wrap.MandatorAware;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.wrap.currency.Currency;
import com.top_logic.tool.boundsec.BoundObject;

/**
 * Extends the CompanyContact for COS Specific values.
 * 
 * @author    <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public class COSCompanyContact extends CompanyContact implements COSContactConstants, MandatorAware {

    public static final String ATTRIBUTE_NAME       = CompanyContact.NAME_ATTRIBUTE;

    public static final String ATTRIBUTE_SAP_NR     = CompanyContact.FKEY2_ATTRIBUTE;
    
	public static final String ATTRIBUTE_FKEY = CompanyContact.FKEY_ATTRIBUTE;
    
    public static final String ATTRIBUTE_MANDATOR   = COSContactConstants.ATTRIBUTE_MANDATOR;

    /** Lead Buyer attribute. */
    public static final String ATTRIBUTE_LEAD_BUYER = "leadBuyer";
	
    /** 
     * CTor as of Contract with WrapperFactory.
     */
    public COSCompanyContact(KnowledgeObject ko) {
        super(ko);
    }
    
    /**
     * Get the Currency for the Contacts year volume.
     * 
     * @return null when not set, Wrapper is invalid or Attribute is not an ISO currency..
     */
    public Currency getCurrency() {
        return (Currency) getValue(ATTRIBUTE_CURRENCY);
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
	 * Get the the total amount of money in {@link #getCurrency()} for last year.
	 * <p>
	 * This will not aggregate volumes of subcompanies. The definition of "last year" is vague, we
	 * assume that some external system will update this information on some regular (yearly) base.
	 * </p>
	 * 
	 * @return null when not set or Wrapper is invalid, optional attribute.
	 */
    public Double getVolume() {
        return getDouble(ATTRIBUTE_VOLUME);
    }
    
    /**
     * Get the the lead buyer of the company contact.  
     * 
     * @return null if not set, lead buyer otherwise.
     */
    public COSPersonContact getLeadBuyer() {
        return (COSPersonContact) getValue(ATTRIBUTE_LEAD_BUYER);
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
	 * Get the contacts of the given type whose fkey no. matches the given one
	 * and one of the given mandators
	 * 
	 * @param aType the contact type (ContactFactory.PERSON_TYPE/COMPANY_TYPE(default))
	 * @param fKey may be null
	 * @param aMandatorList may be null and will not be checked then
	 * @return the list of AbstractContacts
	 */
	public static List getListByFKey(String aType, String fKey, List aMandatorList) {
		List theContacts =
			ContactFactory.getInstance()
				.getAllContactsWithAttribute(aType, AbstractContact.FKEY_ATTRIBUTE, fKey, false);
	   return getFilteredList(theContacts, aMandatorList);
	}

	public static COSCompanyContact getBySAP(KnowledgeBase aKB, String aSAPNo) {
	    final AbstractContact theContact = CompanyContact.getByForeignKey2(aKB, SAPFormatHelper.fillSAPNo(aSAPNo));

	    return (theContact instanceof COSCompanyContact) ? (COSCompanyContact) theContact : null;
	}

	/**
	 * Fast way to fetch some AbstractContacts by their SAP No.
	 * 
	 * @param aType the contact type (ContactFactory.PERSON_TYPE/COMPANY_TYPE(default))
	 * @param aSAPNo may be null and will be ignored then.
	 * @return the list of AbstractContacts
	 */
	public static List getListBySAP (String aType, String aSAPNo, Mandator aMandator) {
	   List theContacts  = ContactFactory.getInstance().getAllContactsWithAttribute(aType, AbstractContact.FKEY2_ATTRIBUTE, aSAPNo, false);
	   return getFilteredList(theContacts, (aMandator == null) ? null : Collections.singletonList(aMandator));
	}
	
	/**
	 * Fast way to fetch some AbstractContacts by their SAP No.
	 * 
	 * @param aType the contact type (ContactFactory.PERSON_TYPE/COMPANY_TYPE(default))
	 * @param aSAPNo may be null and will be ignored then.
	 * @param aMandatorList the list of allowed Mandators as owners of the contacts. No check if <code>null</code>
	 * @return the list of AbstractContacts
	 */
	public static List getListBySAP (String aType, String aSAPNo, List aMandatorList) {
	   List theContacts  = ContactFactory.getInstance().getAllContactsWithAttribute(aType, AbstractContact.FKEY2_ATTRIBUTE, aSAPNo, false);
	   return getFilteredList(theContacts, aMandatorList);
	}
	
	/**
	 * Fast way to fetch some AbstractContacts by their Mandator.
	 * 
	 * @param aType the contact type (ContactFactory.PERSON_TYPE/COMPANY_TYPE(default))
	 * @param aMandatorList the list of allowed Mandators as owners of the contacts. No check if <code>null</code>
	 * @return the list of AbstractContacts
	 */
	public static List getListByMandators (String aType, List aMandatorList) {
	   List theContacts  = ContactFactory.getInstance().getAllContacts(aType);
	   return getFilteredList(theContacts, aMandatorList);
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
}
