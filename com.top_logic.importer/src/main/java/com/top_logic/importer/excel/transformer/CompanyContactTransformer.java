/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.excel.transformer;

import java.util.List;

import com.top_logic.base.office.excel.ExcelContext;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.contact.business.CompanyContact;
import com.top_logic.contact.business.ContactFactory;
import com.top_logic.importer.excel.AbstractExcelFileImportParser;
import com.top_logic.importer.logger.ImportLogger;

/**
 * Transform given text into a {@link CompanyContact}.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class CompanyContactTransformer implements Transformer<CompanyContact> {

	private boolean mandatory;

	public CompanyContactTransformer(InstantiationContext aContext, Config aConfig){
		this.mandatory = aConfig.isMandatory();
	}
	
	@Override
	public CompanyContact transform(ExcelContext aContext, String aColumnName, AbstractExcelFileImportParser<?> aParser, ImportLogger aLogger) {
		String theText = aContext.getString(aColumnName);

		if (StringServices.isEmpty(theText)) {
			if (this.mandatory) {
				throw new TransformException(I18NConstants.VALUE_EMPTY, aContext.row() + 1, aColumnName, theText);
			}

			return null;
		}
		else {
		    theText = theText.trim();

    		// Try to get contact via company name
    		List<?> theContacts = ContactFactory.getInstance().getAllContactsWithAttribute(ContactFactory.COMPANY_TYPE, CompanyContact.NAME_ATTRIBUTE, theText, false);

    		if (!CollectionUtil.isEmptyOrNull(theContacts)) {
    		    return (CompanyContact) CollectionUtil.getSingleValueFrom(theContacts);
    		}
    		else {
    			throw new TransformException(I18NConstants.COMPANY_NAME_NOT_FOUND, aContext.row() + 1, aColumnName, theText);
    		}
		}
	}

}
