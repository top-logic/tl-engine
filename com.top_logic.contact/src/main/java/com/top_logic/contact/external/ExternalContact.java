/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.external;

/**
 * Contact from an external system.
 * 
 * @author <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public interface ExternalContact extends Comparable {
	
	/**
	 * Name of the U-number attribute.
	 */
	public static final String U_NUMBER = "uNumber";
	/**
	 * Name of the first name attribute.
	 */
	public static final String FIRST_NAME = "firstName";
	/**
	 * Name of the family name attribute.
	 */
	public static final String FAMILY_NAME = "familyName";
	/**
	 * Name of the division attribute.
	 */
	public static final String DIVISION = "division";
	/**
	 * Name of the phone attribute.
	 */
	public static final String PHONE = "phone";
	/**
	 * Name of the e-mail attribute.
	 */
	public static final String E_MAIL = "eMail";
	/**
	 * Name of the attribute holding the ID of the source system.
	 */
	public static final String SYS_ID = "sysId";
	
	/**
	 * The U-number of this contact.
	 */
	public String getUNumber();
	/**
	 * The first name of this contact.
	 */
	public String getFirstName();
	/**
	 * The family name of this contact.
	 */
	public String getFamilyName();
	/**
	 * The division of this contact.
	 */
	public String getDivision();
	/**
	 * The phone number of this contact.
	 */
	public String getPhone();
	/**
	 * The e-mail address of this contact.
	 */
	public String getEMail();
	/**
	 * The ID of the system this contact comes from.
	 */
	public String getSysId();
}

