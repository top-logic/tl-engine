/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.business;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import com.top_logic.util.Country;

/**
 * A simple data container for an address.
 * 
 * @author    <a href=mailto:jco@top-logic.com>jco</a>
 */
public class AddressHolder extends Properties {

    public static final String STREET   = "street";
    public static final String ZIP_CODE = "ZIPCode";
    public static final String CITY     = "city";
    public static final String COUNTRY  = "country";
    public static final String PHONE    = "phone";
    public static final String STATE    = "state";

	/** @see AbstractContact#REMARKS_ATTRIBUTE */
    public static final String REMARKS = AbstractContact.REMARKS_ATTRIBUTE;

	/** @see AbstractContact#FKEY_ATTRIBUTE */
    public static final String FKEY = AbstractContact.FKEY_ATTRIBUTE;

	/** @see AbstractContact#FKEY2_ATTRIBUTE */
	public static final String FKEY2 = AbstractContact.FKEY2_ATTRIBUTE;

	/** @see CompanyContact#ATT_MAIL */
	public static final String MAIL = CompanyContact.ATT_MAIL;

	/** @see CompanyContact#ATT_DEPARTMENT */
	public static final String DEPARTMENT = CompanyContact.ATT_DEPARTMENT;

	/** @see CompanyContact#ATT_SECTOR */
	public static final String SECTOR = CompanyContact.ATT_SECTOR;

	/** @see CompanyContact#ATT_SUPPLIER */
	public static final String SUPPLIER = CompanyContact.ATT_SUPPLIER;

	/** @see CompanyContact#ATT_CLIENT */
	public static final String CLIENT = CompanyContact.ATT_CLIENT;
    
    private static Set validParameter;
    
    static {
        validParameter = new HashSet();
        validParameter.add(STREET);
        validParameter.add(ZIP_CODE);
        validParameter.add(CITY);
        validParameter.add(COUNTRY);
        validParameter.add(PHONE);
        validParameter.add(STATE);
		validParameter.add(REMARKS);
		validParameter.add(FKEY);
		validParameter.add(FKEY2);
		validParameter.add(MAIL);
		validParameter.add(DEPARTMENT);
		validParameter.add(SECTOR);
		validParameter.add(SUPPLIER);
		validParameter.add(CLIENT);
    }

    public AddressHolder() {
        super();
    }

    public AddressHolder(Properties someDefaults) {
        super(someDefaults);
    }
    
    /**
     * avoid access to illegal parameters
     * @see java.util.Properties#getProperty(java.lang.String, java.lang.String)
     */
    @Override
	public String getProperty(String someKey, String someDefaultValue) {
        if (validParameter.contains(someKey)) {
            return super.getProperty(someKey);
        } else {
            throw new IllegalArgumentException ("the key must be some valid parameter");
        }
    }
    /** 
     * @see java.util.Properties#getProperty(java.lang.String)
     */
    @Override
	public String getProperty(String someKey) {
        return getProperty(someKey,null);
    }
    
    /**
     * only valid parameters are allowed to be set 
     * @see java.util.Properties#setProperty(java.lang.String, java.lang.String)
     */
    @Override
	public Object setProperty(String someKey, String someValue) {
        if (validParameter.contains(someKey)) {
            if (someValue != null) {
                return super.setProperty(someKey, someValue);
            }
            else {
                return super.remove(someKey);
            }
        } else {
            throw new IllegalArgumentException ("the key must be some valid parameter");
        }       
    }
    
    public Country getCountry(){
        String countryCode = getProperty(COUNTRY);
        return countryCode != null ? new Country(countryCode) : null;
    }
    public void setCountry(Country aCountry){
        if(aCountry != null){
            setProperty(AddressHolder.COUNTRY,aCountry.getCode());                        
        }
    }
}
