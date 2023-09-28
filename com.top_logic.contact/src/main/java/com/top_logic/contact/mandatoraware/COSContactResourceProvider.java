/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.mandatoraware;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.contact.business.AbstractContact;
import com.top_logic.contact.business.AddressHolder;
import com.top_logic.contact.business.CompanyContact;
import com.top_logic.contact.business.PersonContact;
import com.top_logic.contact.layout.ContactResourceProvider;

/**
 * Overridden to show SAP and {@link AbstractContact#FKEY_ATTRIBUTE} for companies.
 * 
 * @author <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public class COSContactResourceProvider extends ContactResourceProvider {

	/**
	 * Create a {@link COSContactResourceProvider}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public COSContactResourceProvider(InstantiationContext context, Config config) {
		super(context, config);
	}

    /**
	 * Overridden to add {@link AbstractContact#FKEY_ATTRIBUTE} and SAP-Nr.
	 * 
	 * @param aCompany
	 *        The company to get the label for.
	 * @return The label for the company.
	 * 
	 * @see com.top_logic.contact.layout.ContactResourceProvider#getLabelForCompany(com.top_logic.contact.business.CompanyContact)
	 */
    @Override
	protected String getLabelForCompany(CompanyContact aCompany) {
        try {
            String fKey = aCompany.getFKey();
            String sapNr  = this.getSAPNo(aCompany);
            return aCompany.getName() + " (" + aCompany.getValue(AddressHolder.CITY) + ")"
                   + (sapNr  == null ? "" : " " + sapNr)
                   + (fKey == null ? "" : " " + fKey);
        }
        catch (Exception ex) {
            return ex.getMessage();
        }
    }

    /** 
     * Return the tool tip Text for a CompanyContact.
     */
    @Override
	protected ResKey getTooltipForCompany(CompanyContact aCompany) {
        String theText = TagUtil.encodeXML(aCompany.getName());
        String theKey  = TagUtil.encodeXML(this.getSAPNo(aCompany));

		return com.top_logic.contact.layout.I18NConstants.COMPANY_TOOLTIP__NAME_KEY.fill(
			theText, theKey);
    }

    @Override
	protected ResKey getTooltipForPerson(PersonContact aPerson) {
        String theName  = TagUtil.encodeXML(this.getLabel(aPerson));
        String theMail  = TagUtil.encodeXML(aPerson.getEMail());
        String thePhone = TagUtil.encodeXML((String) aPerson.getValue(PersonContact.PHONE));

		return com.top_logic.contact.layout.I18NConstants.PERSON_TOOLTIP__NAME_MAIL_PHONE.fill(theName, theMail,
			thePhone);
    }
    
    /** 
     * Return the SAP number of the given company contact.
     * 
     * @param    aCompany    The company to get the number from.
     * @return   The requested number.
     */
    protected String getSAPNo(CompanyContact aCompany) {
        return (SAPFormatHelper.stripSAPNo(aCompany.getForeignKey2()));
    }
}
