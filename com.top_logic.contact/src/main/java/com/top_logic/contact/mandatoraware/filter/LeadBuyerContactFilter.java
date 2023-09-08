/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.mandatoraware.filter;

import com.top_logic.basic.Logger;
import com.top_logic.contact.business.CompanyContact;
import com.top_logic.contact.business.ContactFactory;
import com.top_logic.contact.mandatoraware.COSCompanyContact;
import com.top_logic.contact.mandatoraware.COSPersonContact;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.MetaElementFactory;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.kbbased.WrapperMetaAttributeUtil;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Get suitable PersonContacts for leadbuyership.
 * 
 * @author    <a href=mailto:kbu@top-logic.com>kbu</a>
 */
@Deprecated
public class LeadBuyerContactFilter extends COSPersonContactFilter {

	private static TLStructuredTypePart LEADBUYER_ATTRIBUTE_ID;

	@Override
	protected boolean checkBasics(TLObject anAttributed, EditContext editContext,
			Object anObject) {
		if (!super.checkBasics(anAttributed, editContext, anObject)) {
			return false;
		}
		
		if (MandatorAwareContactFilter.isSearch(anAttributed, editContext)) {
        	// Check if supplier has contracts in the search
        	if (getLeadBuyerAttID() != null) {
        		return WrapperMetaAttributeUtil.hasWrappersWithValue(getLeadBuyerAttID(), (COSPersonContact) anObject);
        	}
        }
        
        return true;
	}

	/**
	 * Get the Supplier {@link TLStructuredTypePart} of Contracts
	 * 
	 * @return the {@link TLStructuredTypePart} or null if an error occurs
	 */
	private static TLStructuredTypePart getLeadBuyerAttID() {
		if (LEADBUYER_ATTRIBUTE_ID == null) {
			try {
				LEADBUYER_ATTRIBUTE_ID = MetaElementUtil.getMetaAttribute(MetaElementFactory.getInstance()
					.getGlobalMetaElement(ContactFactory.STRUCTURE_NAME, CompanyContact.META_ELEMENT), COSCompanyContact.ATTRIBUTE_LEAD_BUYER);
			} catch (NoSuchAttributeException e) {
				Logger.error("Failed to get supplier attribute needed to check if supplier has contract in searches", e, SupplierFilter.class);
			}
		}
		
		return LEADBUYER_ATTRIBUTE_ID;
	}
}
