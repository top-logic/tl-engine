/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.top_logic.contact.business.CompanyContact;
import com.top_logic.contact.business.ContactFactory;
import com.top_logic.contact.mandatoraware.COSContactConstants;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.kbbased.filtergen.GeneratorFactory;
import com.top_logic.element.meta.kbbased.filtergen.ListGeneratorAdaptor;

/**
 * Cache the result (Clients)
 * 
 * @author    <a href=mailto:kbu@top-logic.com>kbu</a>
 */
public class CachedClientGenerator extends ListGeneratorAdaptor {

	/**
	 * Public Ctor for {@link GeneratorFactory}
	 */
	public CachedClientGenerator() {
		super();
	}

	@Override
	public synchronized List<?> generateList(EditContext editContext) {
		Collection<?> theSupps = ContactFactory.getInstance().getAllContacts(ContactFactory.COMPANY_TYPE);
		Iterator<?> theSuppsIt = theSupps.iterator();
		List<Object> result = new ArrayList<>(theSupps.size());
		while (theSuppsIt.hasNext()) {
			CompanyContact theCompany = (CompanyContact) theSuppsIt.next();
			
			Boolean isSupplierB = (Boolean) theCompany.getValue(getBooleanAttributeToCheck());
			boolean isSupplier = (isSupplierB == null) ? false : isSupplierB.booleanValue();

			if (isSupplier) {
				result.add(theCompany);
			}
		}
		return result;
	}
	
	protected String getBooleanAttributeToCheck() {
		return COSContactConstants.CLIENT;
	}

}
