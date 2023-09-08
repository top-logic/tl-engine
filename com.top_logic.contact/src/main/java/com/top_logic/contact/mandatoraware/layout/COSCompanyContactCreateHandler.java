/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.mandatoraware.layout;

import java.util.Collection;
import java.util.Map;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.contact.business.AddressHolder;
import com.top_logic.contact.business.CompanyContact;
import com.top_logic.contact.layout.company.CompanyContactCreateHandler;
import com.top_logic.contact.layout.company.CreateCompanyContactComponent;
import com.top_logic.contact.mandatoraware.COSCompanyContact;
import com.top_logic.contact.mandatoraware.COSContactConstants;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.structured.wrap.Mandator;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.util.error.TopLogicException;

/**
 * @author     <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public class COSCompanyContactCreateHandler extends CompanyContactCreateHandler {

	public COSCompanyContactCreateHandler(InstantiationContext context, Config config) {
		super(context, config);
	}
	
    @Override
	public Object createObject(LayoutComponent component, Object createContext,
			FormContainer formContainer, Map arguments) {
        CreateCompanyContactComponent theComp    = (CreateCompanyContactComponent) component;
        AttributeFormContext          theContext = (AttributeFormContext) formContainer;
        TLClass                   theME      = theComp.getMetaElement();
        try {
            String         theName    = (String) this.getField(theME, CompanyContact.NAME_ATTRIBUTE, theContext).getValue();
            AddressHolder theAddress  = createAddressFromFormContext(theContext, theME);
            
            Object theMandator = this.getField(theME, COSContactConstants.ATTRIBUTE_MANDATOR, theContext).getValue();
            if(theMandator instanceof Collection){
                theMandator = CollectionUtil.getSingleValueFromCollection((Collection)theMandator);
            }
            CompanyContact theCompany = this.createCompany(theName, theAddress,(Mandator)theMandator);
            
            FormField theField = this.getField(theME, COSCompanyContact.SUPPLIER,theContext);

            if (theField != null) {
                theCompany.setValue(COSCompanyContact.SUPPLIER, theField.getValue());
                theCompany.setValue(COSCompanyContact.CLIENT,  this.getField(theME, COSCompanyContact.CLIENT,theContext).getValue());
            }

            return theCompany;
        }
        catch (Exception ex) {
            throw new TopLogicException(this.getClass(), "create.contact.company", ex);
        }
//        return super.createNewObject(aContext, aRequest, aComponent, aModel);
    }
    /** 
     * Must not be used in CPM
     * 
     * @see com.top_logic.contact.layout.company.CompanyContactCreateHandler#createCompany(java.lang.String, com.top_logic.contact.business.AddressHolder)
     */
    @Override
	public CompanyContact createCompany(String aName, AddressHolder anAddress) {
        return super.createCompany(aName, anAddress);
    }
    
    /**
     * Create a Company
     * 
     * @param    aName        Must not be <code>null</code>.
     * @param    anAddress    Must not be <code>null</code>.
     * @param    aMandator    Must not be <code>null</code>.
     * @return   The created company.
     */
    public CompanyContact createCompany(String aName, AddressHolder anAddress, Mandator aMandator) {
    	if (aMandator == null) {
    		throw new IllegalArgumentException("Mandator must not be null");
    	}
    	
    	CompanyContact theCompany = this.createCompany(aName, anAddress);
    	theCompany.setValue(COSContactConstants.ATTRIBUTE_MANDATOR, aMandator);
    	
    	return theCompany;
    }

    /**
    /**
     * Create a Company
     * 
     * @param    aName       The name of the company.
     * @param    aStreet     The street, the company is located, may be <code>null</code>.
     * @param    aZip        The ZIP code of the city, the company is located, may be <code>null</code>.
     * @param    aCity       The city, the company is located, may be <code>null</code>.
     * @param    aCountry    The country, the company is located, may be <code>null</code>.
     * @param    aState      The state, the company is located, may be <code>null</code>.
     * @param	 aMandator	 the Mandator, must not be <code>null</code>.
     * @return   The created company.
     */
    public CompanyContact createCompany(String aName, String aStreet, String aZip, String aCity, String aCountry, String aState, Mandator aMandator) {
        AddressHolder theAddress = new AddressHolder();

        if (aStreet != null) {
            theAddress.setProperty(AddressHolder.STREET, aStreet);
        }
        if (aZip != null) {
            theAddress.setProperty(AddressHolder.ZIP_CODE, aZip);
        }
        if (aCity != null) {
            theAddress.setProperty(AddressHolder.CITY, aCity);
        }
        if (aCountry != null) {
            theAddress.setProperty(AddressHolder.COUNTRY, aCountry);
        }
        if (aState != null) {
            theAddress.setProperty(AddressHolder.STATE, aState);
        }

        return (this.createCompany(aName, theAddress, aMandator));
    }
}
