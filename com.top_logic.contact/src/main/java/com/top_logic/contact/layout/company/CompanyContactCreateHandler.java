/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.layout.company;

import java.util.Collection;
import java.util.Map;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.contact.business.AddressHolder;
import com.top_logic.contact.business.CompanyContact;
import com.top_logic.contact.business.ContactFactory;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.gui.MetaAttributeGUIHelper;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.component.AbstractCreateCommandHandler;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.util.Country;
import com.top_logic.util.error.TopLogicException;


/**
 * Create handler for {@link CompanyContact}.
 * 
 * @author    <a href=mailto:jco@top-logic.com>jco</a>
 */
public class CompanyContactCreateHandler extends AbstractCreateCommandHandler {

    public static final String COMMAND_ID = "companyContactCreate";
    
    
    public CompanyContactCreateHandler(InstantiationContext context, Config config) {
        super(context, config);
    }

    @Override
	public Object createObject(LayoutComponent component, Object createContext, FormContainer formContainer,
			Map<String, Object> arguments) {
        CreateCompanyContactComponent theComp    = (CreateCompanyContactComponent) component;
        TLClass                   theME      = theComp.getMetaElement();
        try {
            AttributeFormContext theContext = (AttributeFormContext)formContainer;
            String         theName    = (String) this.getField(theME, CompanyContact.NAME_ATTRIBUTE, theContext).getValue();
            AddressHolder theAddress  = createAddressFromFormContext(theContext, theME);

            return this.createCompany(theName, theAddress);
        }
        catch (Exception ex) {
            throw new TopLogicException(this.getClass(), "create.contact.company", ex);
        }
    }

    protected AddressHolder createAddressFromFormContext(AttributeFormContext theContext,
            TLClass theME) throws NoSuchAttributeException {
        AddressHolder  theAddress = new AddressHolder();
        
        this.setValueToAddress(theContext, theME, AddressHolder.STREET,   theAddress);
        this.setValueToAddress(theContext, theME, AddressHolder.ZIP_CODE, theAddress);
        this.setValueToAddress(theContext, theME, AddressHolder.CITY,     theAddress);
        this.setValueToAddress(theContext, theME, AddressHolder.COUNTRY,  theAddress);
        this.setValueToAddress(theContext, theME, AddressHolder.STATE,    theAddress);
        this.setValueToAddress(theContext, theME, AddressHolder.PHONE,    theAddress);
		this.setValueToAddress(theContext, theME, AddressHolder.REMARKS, theAddress);
		this.setValueToAddress(theContext, theME, AddressHolder.FKEY, theAddress);
		this.setValueToAddress(theContext, theME, AddressHolder.FKEY2, theAddress);
		this.setValueToAddress(theContext, theME, AddressHolder.MAIL, theAddress);
		this.setValueToAddress(theContext, theME, AddressHolder.DEPARTMENT, theAddress);
		this.setValueToAddress(theContext, theME, AddressHolder.SECTOR, theAddress);
		this.setValueToAddress(theContext, theME, AddressHolder.SUPPLIER, theAddress);
		this.setValueToAddress(theContext, theME, AddressHolder.CLIENT, theAddress);
        return theAddress;
    }

    protected void setValueToAddress(AttributeFormContext aContext, TLClass aME, String aMAName, AddressHolder anAddress) throws IllegalArgumentException, NoSuchAttributeException {
        Object theValue = this.getField(aME, aMAName, aContext).getValue();
        if (theValue instanceof Collection) {
			theValue = CollectionUtil.getSingleValueFromCollection((Collection<?>) theValue);
        }
        if(theValue instanceof Country){
            anAddress.setCountry((Country)theValue);
		} else if (theValue != null) {
            anAddress.setProperty(aMAName, theValue.toString());
        }
    }

    /** 
     * Extract the FormField from the AttributeFormContext by providing a readeable attribute name.
     */
    protected FormField getField(TLClass aME, String aMAName, AttributeFormContext aContext) throws IllegalArgumentException, NoSuchAttributeException {
		String theID = MetaAttributeGUIHelper.getAttributeIDCreate(MetaElementUtil.getMetaAttribute(aME, aMAName));
        
        return (aContext.hasMember(theID)) ? aContext.getField(theID) : null;
    }

    /**
     * @param    aName        Must not be <code>null</code>.
     * @param    anAddress    Must not be <code>null</code>.
     * @return   The created company.
     */
    public CompanyContact createCompany(String aName, AddressHolder anAddress) {
        return ContactFactory.getInstance().createNewCompanyContact(aName, anAddress);
    }

}
