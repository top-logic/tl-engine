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
	
	public static final String U_NUMBER = "uNumber";
	public static final String FIRST_NAME = "firstName";
	public static final String FAMILY_NAME = "familyName";
	public static final String DIVISION = "division";
	public static final String PHONE = "phone";
	public static final String E_MAIL = "eMail";
	public static final String SYS_ID = "sysId";
	
	public String getUNumber();
	public String getFirstName();
	public String getFamilyName();
	public String getDivision();
	public String getPhone();
	public String getEMail();
	public String getSysId();
}

