/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.mandatoraware;

/** 
 * Constants for additional attributes of
 * CompanyContacts in COS
 *
 * @author    <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public interface COSContactConstants {
    
    /** Marker for company contacts denoting that they are
     * suitable as supplier.
     */
    public static final String SUPPLIER = "supplier";

    /** Marker for company contacts denoting that they are
     * suitable as client.
     */
    public static final String CLIENT = "client";
    
    /**
     * Name used for the default person and company contacts.
     */
    public static final String DEFAULT_CONTACT_NAME       = "Default";
    public static final String DEFAULT_CONTACT_FIRST_NAME = "Person";
    public static final String DEFAULT_CONTACT_SUPPLIER   = DEFAULT_CONTACT_NAME + " Supplier";
    public static final String DEFAULT_CONTACT_CLIENT     = DEFAULT_CONTACT_NAME + " Client";

    /** Name of CurrencyAttribute, compare to contactFactoryConfiguration.xml */
    public static final String ATTRIBUTE_CURRENCY   = "currency";
    
    /** Name of volme attribute, compare to contactFactoryConfiguration.xml */
    public static final String ATTRIBUTE_VOLUME     = "volume";

    /** 
     * Name of optional cage attribute, compare to contactFactoryConfiguration.xml 
     * 
     * (Commercial And Government Entity-Code)
     */
    public static final String ATTRIBUTE_CAGE       = "cage";

    public static final String ATTRIBUTE_EMAIL      = "email";
    public static final String ATTRIBUTE_DEPT       = "department";
    
    /** the mandator to which the contact is attached. */
    public static final String ATTRIBUTE_MANDATOR = "mandator";
    
    /** timestamp of the last volume import */
    public static final String VOLUME_IMPORT_DATE = "LAST_IMPORT_VOLUME";
}
