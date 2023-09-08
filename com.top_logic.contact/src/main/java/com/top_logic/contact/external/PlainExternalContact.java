/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.external;

import com.top_logic.util.Utils;

/**
 * POJO implementing {@link ExternalContact}.
 * 
 * @author <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public class PlainExternalContact implements ExternalContact {

	private final String uNumber;
	private final String firstName;
	private final String familyName;
	private final String division;
	private final String eMail;
	private final String phone;
	private final String sysId;

	public PlainExternalContact(String uNumber, String firstName, String familyName, String division, String eMail, String phone, String aSysId) {
		this.uNumber = uNumber;
		this.firstName = firstName;
		this.familyName = familyName;
		this.division = division;
		this.eMail = eMail;
		this.phone = phone;
		this.sysId = aSysId;
	}

	@Override
	public String getUNumber() {
		return uNumber;
	}
	
	@Override
	public String getFirstName() {
		return firstName;
	}
	
	@Override
	public String getFamilyName() {
		return familyName;
	}
	
	@Override
	public String getDivision() {
		return division;
	}

	@Override
	public String getEMail() {
		return eMail;
	}

	@Override
	public String getPhone() {
		return phone;
	}
	
	@Override
	public String getSysId() {
		return sysId;
	}
	
	@Override
	public int hashCode() {
		return uNumber.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (! (obj instanceof ExternalContact)) return false;
		return equalsContact((ExternalContact) obj);
	}

	public boolean equalsContact(ExternalContact contact) {
		boolean isEqual = 
            Utils.equals(uNumber,contact.getUNumber()) && 
            Utils.equals(phone,contact.getPhone()) &&
            Utils.equals(eMail,contact.getEMail()) && 
            Utils.equals(division,contact.getDivision()) && 
            Utils.equals(firstName,contact.getFirstName()) &&
            Utils.equals(familyName, contact.getFamilyName());
			Utils.equals(sysId, contact.getSysId());
      
        return isEqual;
	}
	
	/** 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Object o) {
		if (o == null) {
			return 1;
		}
		
		if (!(o instanceof ExternalContact)) {
			return -1;
		}
		
		ExternalContact theExtCont = (ExternalContact) o;
		int theResult = this.getFamilyName().compareTo(theExtCont.getFamilyName());
		if (theResult != 0) {
			return theResult;
		}
		
		theResult = this.getFirstName().compareTo(theExtCont.getFirstName());
		if (theResult != 0) {
			return theResult;
		}
		
		return this.getUNumber().compareTo(theExtCont.getUNumber());
	}
}
