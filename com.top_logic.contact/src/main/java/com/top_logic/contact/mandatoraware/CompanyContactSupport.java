/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.mandatoraware;

import java.util.ArrayList;
import java.util.Collection;

import com.top_logic.basic.util.ResKey;
import com.top_logic.contact.business.CompanyContact;
import com.top_logic.knowledge.service.KnowledgeBase;

/**
 * Provide methods for getting company contacts via the SAP number.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
@Deprecated 
public class CompanyContactSupport {
    
    /** 
     * Return the company contact defined by the given SAP number.
     * 
     * @param    aSAPNo    The SAP number of the requested company contact, must not be <code>null</code>.
     * @return   The requested company contact, never <code>null</code>.
     * @throws   RuntimeException    If there is no company contact with the given SAP number.
     */
    public static CompanyContact getCompanyContact(String aSAPNo) {
        return CompanyContactSupport.getCompanyContact(COSCompanyContact.getDefaultKnowledgeBase(), aSAPNo);
    }

    /** 
     * Return the company contacts defined by the given SAP numbers.
     * 
     * @param    someSAPNo    The SAP numbers of the requested company contacts, must not be <code>null</code>.
     * @return   The requested company contacts, never <code>null</code>.
     * @throws   RuntimeException    If there is no company contact with the given SAP number.
     */
    public static Collection getCompanyContacts(String[] someSAPNo) {
        KnowledgeBase theKB   = COSCompanyContact.getDefaultKnowledgeBase();
        Collection    theColl = new ArrayList();

        for (int thePos = 0; thePos < someSAPNo.length; thePos++) {
            theColl.add(getCompanyContact(theKB, someSAPNo[thePos]));
        }

        return theColl;
    }

    /** 
     * Return the company contact defined by the given SAP number.
     * 
     * @param    aKB       The knowledge base to look up the company contacts.
     * @param    aSAPNo    The SAP number of the requested company contact, must not be <code>null</code>.
     * @return   The requested company contact, never <code>null</code>.
     * @throws   RuntimeException    If there is no company contact with the given SAP number.
     */
    protected static CompanyContact getCompanyContact(KnowledgeBase aKB, String aSAPNo) {
        CompanyContact theCompany = COSCompanyContact.getBySAP(aKB, aSAPNo);

        if (theCompany != null) {
            return theCompany;
        }
        else {
            theCompany = (CompanyContact) COSCompanyContact.getByForeignKey2(aKB, SAPFormatHelper.stripSAPNo(aSAPNo));

            if (theCompany != null) {
                return theCompany;
            }
            else {
				throw new RuntimeException(ResKey.encode(I18NConstants.COMPANY_UNKNOWN__SAPNO.fill(aSAPNo)));
            }
        }
    }
}

