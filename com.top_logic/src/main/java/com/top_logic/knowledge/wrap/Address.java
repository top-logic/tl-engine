/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap;

import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.searching.FullTextBuBuffer;
import com.top_logic.knowledge.service.KnowledgeBase;

/** 
 * This class wraps address-KnowledgeObjects.
 * 
 *
 * @author    Holger Borchart
 */
public class Address extends AbstractWrapper {

    /** type of KO for this Wrapper */
	public    static final String OBJECT_NAME 			= "Address";
    
	protected static final String ASSOCIATION_ATTRIBUTE	= "type";
	
	/** Street of address. */
	protected static final String STREET_1 		= "street1";
	/** Street of address. */
	protected static final String STREET_2 		= "street2";
	/** ZIP-Code of address. */
	protected static final String ZIP 			= "zip";
	/** City of address. */
	protected static final String CITY 			= "city";
	/** Country of address. */
	protected static final String COUNTRY 		= "country";
	/** State of address. */
	protected static final String STATE 		= "state";
	/** Telephone-Nr. of address. */
	protected static final String TELEPHONE_1 	= "telephone1";
	/** Telephone-Nr. of address. */
	protected static final String TELEPHONE_2 	= "telephone2";
	/** Mobile-Telephone-Nr. of address. */
	protected static final String MOBILE 		= "mobile";
	/** Fax-Nr. of address. */
	protected static final String FAX_1 		= "fax1";
	/** Fax-Nr. of address. */
	protected static final String FAX_2 		= "fax2";
	/** E-Mail of address. */
	protected static final String E_MAIL 		= "eMail";
	
	/**
	 * Public constructor for this Address-Wrapper.
	 * @param ko a wrapped KnowledgeObject
	 */
	public Address(KnowledgeObject ko) {
		super(ko);
	}
	/**
	 * Create and return an empty address-object by using the default knowledge-base.
	 * The knowledge-base is not committed after creation of address.
	 * 
	 */
	public static Address createAddress() {
		
		return createAddress(getDefaultKnowledgeBase());
	}
	
	/**
	* Create and return an empty address-object by using the given knowledge-base.
	* The knowledge-base is not committed after creation of address.
	* 
	*/
	public static Address createAddress(KnowledgeBase aKnowledgeBase) {
		KnowledgeObject theKO = aKnowledgeBase.createKnowledgeObject(OBJECT_NAME);
		return Address.getInstance(theKO);
	}
	
	/**
	 * Create and return an address-object by using the default knowledge-base.
	 * The knowledge-base is not committed after creation of address.
	 * The attributes street1, city, zip and countries are set by using the 
	 * given attributes.  
	 */
	public static Address createAddress (String aStreet1, String aCity, 
	                   String aZip, String aCountry) {
		return createAddress(aStreet1, aCity, aZip, aCountry, getDefaultKnowledgeBase());
	}
	
	/**
	* Create and return an address-object by using the given knowledge-base.
	* The knowledge-base is not committed after creation of address.
	* The attributes street1, city, zip and countries are set by using the 
	* given attributes.  
	*/
	public static Address createAddress(String aStreet1, String aCity, 
	    String aZip, String aCountry, KnowledgeBase aKnowledgeBase) {
		Address theAddress = createAddress(aKnowledgeBase);
		theAddress.tSetData(STREET_1, aStreet1);
		theAddress.tSetData(CITY, aCity);
		theAddress.tSetData(ZIP, aZip);
		theAddress.tSetData(COUNTRY, aCountry);
		return theAddress;
	}
    
	/**
	 * Get the single wrapper for the given knowledge object.
	 *
	 * @param aKO the KnowledgeObject, never null.
	 * @return the wrapper
	 */
	public static Address getInstance(KnowledgeObject aKO) {
		Address.verifyType(aKO, OBJECT_NAME);
		Wrapper theWrapper = WrapperFactory.getWrapper(aKO);
		return (Address) theWrapper; 
	}
	
	/**
	 * Get-method for attribute city.
	 */
	public String getCity() {
		return tGetDataString(CITY);
	}

	/**
	 * Get-method for attribute country.
	 */
	public String getCountry() {
		return tGetDataString(COUNTRY);
	}

	/**
	 * Get-method for attribute fax.
	 */
	public String getFax1() {
		return tGetDataString(FAX_1);
	}
	
	/**
	* Get-method for attribute fax.
	*/
	public String getFax2() {
		return tGetDataString(FAX_2);
	}

	/**
	 * Get-method for attribute mobile.
	 */
	public String getMobile() {
		return tGetDataString(MOBILE);
	}

	/**
	 * Get-method for attribute state.
	 */
	public String getState() {
		return tGetDataString(STATE);
	}

	/**
	 * Get-method for attribute street1.
	 */
	public String getStreet1() {
		return tGetDataString(STREET_1);
	}

	/**
	 * Get-method for attribute street2.
	 */
	public String getStreet2() {
		return tGetDataString(STREET_2);
	}

	/**
	 * Get-method for attribute telephone1.
	 */
	public String getTelephone1() {
		return tGetDataString(TELEPHONE_1);
	}

	/**
	 * Get-method for attribute telephone2.
	 */
	public String getTelephone2() {
		return tGetDataString(TELEPHONE_2);
	}

	/**
	 * Get-method for attribute zip.
	 */
	public String getZip() {
		return tGetDataString(ZIP);
	}
	
	/**
	* Get-method for attribute eMail.
	*/
	public String getEMail() {
		return tGetDataString(E_MAIL);
	}

	/**
	 * Set-method for attribute city.
	 */
	public void setCity(String aCity) {
		this.tSetData(CITY, aCity);
	}

	/**
	 * Set-method for attribute country.
	 */
	public void setCountry(String aCountry) {
		this.tSetData(COUNTRY, aCountry);
	}

	/**
	 * Set-method for attribute fax1.
	 */
	public void setFax1(String aFax1) {
		this.tSetData(FAX_1, aFax1);
	}
	
	/**
	* Set-method for attribute fax1.
	*/
	public void setFax2(String aFax2) {
		this.tSetData(FAX_2, aFax2);
	}

	/**
	 * Set-method for attribute mobile.
	 */
	public void setMobile(String aMobile) {
		this.tSetData(MOBILE, aMobile);
	}

	/**
	 * Set-method for attribute state.
	 */
	public void setState(String aState) {
		this.tSetData(STATE, aState);
	}

	/**
	 * Set-method for attribute street1.
	 */
	public void setStreet1(String aStreet1) {
		this.tSetData(STREET_1, aStreet1);
	}

	/**
	 * Set-method for attribute street1.
	 */
	public void setStreet2(String aStreet2) {
		this.tSetData(STREET_2, aStreet2);
	}

	/**
	 * Set-method for attribute telephone1.
	 */
	public void setTelephone1(String aTelephone1) {
		this.tSetData(TELEPHONE_1, aTelephone1);
	}

	/**
	 * Set-method for attribute telephone2.
	 */
	public void setTelephone2(String aTelephone2) {
		this.tSetData(TELEPHONE_2, aTelephone2);
	}

	/**
	 * Set-method for attribute zip.
	 */
	public void setZip(String aZip) {
		this.tSetData(ZIP, aZip);
	}
	
	/**
	* Set-method for attribute zip.
	*/
	public void setEMail(String anEMail) {
		this.tSetData(E_MAIL, anEMail);
	}
    
    @Override
	public void generateFullText(FullTextBuBuffer buffer) {
		buffer.add(getStreet1());
		buffer.add(getStreet2());
		buffer.add(getZip());
		buffer.add(getCity());
		buffer.add(getCountry());
		buffer.add(getState());
		buffer.add(getTelephone1());
		buffer.add(getTelephone2());
		buffer.add(getMobile());
		buffer.add(getFax1());
		buffer.add(getFax2());
		buffer.add(getEMail());
    }
    
}
