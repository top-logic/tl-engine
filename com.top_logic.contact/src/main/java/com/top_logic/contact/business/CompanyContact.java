/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.business;

import java.util.Collection;

import com.top_logic.basic.StringServices;
import com.top_logic.element.boundsec.ElementBoundHelper;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.util.Country;
import com.top_logic.util.Utils;


/**
 * This class represents a company (thus a legal entity).
 * A CompanyContact may refer to a number of PersonContacts.
 * 
 * @author    <a href=mailto:jco@top-logic.com>jco</a>
 */
public class CompanyContact extends AbstractContact {
   
    /** 
     * Key identifying the line where company makes its money (Branche)
     *  
     * (it does not identify that company is a branch of some other company !) 
     */ 
    public static final String SECTOR_ATTRIBUTE = "sector"; 
    
	/** Type to use by default for {@link CompanyContact}s. */
    public static final String META_ELEMENT = "Contact.Company";

    /**
     * Reverse resolution of PersonContact.company
     */
	public static final String ATTRIBUTE_STAFF = "staff";

	public static final String ATT_MAIL = "email";
	public static final String ATT_DEPARTMENT = "department";
	public static final String ATT_SECTOR = "sector";
	public static final String ATT_SUPPLIER = "supplier";
	public static final String ATT_CLIENT = "client";

    /**
     * Creates a new {@link CompanyContact}.
     */
    public CompanyContact(KnowledgeObject ko) {
        super(ko);
    }

    public void setAddress (AddressHolder anAddress) {
        setValue (AddressHolder.STREET,anAddress.getProperty(AddressHolder.STREET));
        setValue (AddressHolder.ZIP_CODE,anAddress.getProperty(AddressHolder.ZIP_CODE));
        setValue (AddressHolder.CITY,anAddress.getProperty(AddressHolder.CITY));      
        setValue (AddressHolder.COUNTRY,anAddress.getCountry());
        setValue (AddressHolder.PHONE,anAddress.getProperty(AddressHolder.PHONE));
        setValue (AddressHolder.STATE,anAddress.getProperty(AddressHolder.STATE));
		setValue(AddressHolder.REMARKS, anAddress.getProperty(AddressHolder.REMARKS));
		setValue(AddressHolder.FKEY, anAddress.getProperty(AddressHolder.FKEY));
		setValue(AddressHolder.FKEY2, anAddress.getProperty(AddressHolder.FKEY2));
		setValue(AddressHolder.MAIL, anAddress.getProperty(AddressHolder.MAIL));
		setValue(AddressHolder.DEPARTMENT, anAddress.getProperty(AddressHolder.DEPARTMENT));
		setValue(AddressHolder.SECTOR, anAddress.getProperty(AddressHolder.SECTOR));
		boolean isSupplier = Utils.getbooleanValue(anAddress.getProperty(AddressHolder.SUPPLIER));
		setValue(AddressHolder.SUPPLIER, isSupplier);
		boolean isClient = Utils.getbooleanValue(anAddress.getProperty(AddressHolder.CLIENT));
		setValue(AddressHolder.CLIENT, isClient);
    }
    
    public AddressHolder getAddress () {
        AddressHolder theAddress = new AddressHolder();
        theAddress.setProperty(AddressHolder.STREET,(String)this.getValue(AddressHolder.STREET));
        theAddress.setProperty(AddressHolder.ZIP_CODE,(String)this.getValue(AddressHolder.ZIP_CODE));
        theAddress.setProperty(AddressHolder.CITY,(String)this.getValue(AddressHolder.CITY));

        Country    theCountry = (Country)this.getValue(AddressHolder.COUNTRY);
        theAddress.setProperty(AddressHolder.COUNTRY,theCountry != null ? theCountry.getCode() : StringServices.EMPTY_STRING);
        theAddress.setProperty(AddressHolder.PHONE,(String)this.getValue(AddressHolder.PHONE));
        theAddress.setProperty(AddressHolder.STATE,(String)this.getValue(AddressHolder.STATE));
		theAddress.setProperty(AddressHolder.REMARKS, (String) this.getValue(AddressHolder.REMARKS));
		theAddress.setProperty(AddressHolder.FKEY, (String) this.getValue(AddressHolder.FKEY));
		theAddress.setProperty(AddressHolder.FKEY2, (String) this.getValue(AddressHolder.FKEY2));
		theAddress.setProperty(AddressHolder.MAIL, (String) this.getValue(AddressHolder.MAIL));
		theAddress.setProperty(AddressHolder.DEPARTMENT, (String) this.getValue(AddressHolder.DEPARTMENT));
		theAddress.setProperty(AddressHolder.SECTOR, (String) this.getValue(AddressHolder.SECTOR));
		Boolean isSupplier = (Boolean) this.getValue(AddressHolder.SUPPLIER);
		theAddress.setProperty(AddressHolder.SUPPLIER, Boolean.toString(isSupplier == null ? false : isSupplier));
		Boolean isClient = (Boolean) this.getValue(AddressHolder.CLIENT);
		theAddress.setProperty(AddressHolder.CLIENT, Boolean.toString(isClient == null ? false : isClient));
        return theAddress;
    }

    /**
     * Allow reading of the remarks.
     * 
     * @return null when value was null or Wrapper is invalid.
     */
    public String getSector() {
        return (String) this.getValue(SECTOR_ATTRIBUTE);
    }

	/**
	 * {@link PersonContact}s connected to this company contact via their <code>company</code>
	 * attribute.
	 */
	public Collection<PersonContact> getStaff() {
		return (Collection) this.getValue(ATTRIBUTE_STAFF);
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

    /** 
     * Return the SAP number of the company contact.
     * 
     * @return    The SAP number of this company contact.
     * @see       #getForeignKey2()
     */
    public String getSAPNo() {
        return this.getForeignKey2();
    }
    
}
