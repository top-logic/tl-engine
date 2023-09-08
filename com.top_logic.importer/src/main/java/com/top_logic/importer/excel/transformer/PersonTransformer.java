/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.excel.transformer;

import com.top_logic.base.office.excel.ExcelContext;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.importer.excel.AbstractExcelFileImportParser;
import com.top_logic.importer.logger.ImportLogger;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;

/**
 * Transform given text into a {@link Person}.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class PersonTransformer implements Transformer<Person> {

	private boolean mandatory;

	public PersonTransformer(InstantiationContext aContext, Config aConfig){
		this.mandatory = aConfig.isMandatory();
	}
	
	@Override
	public Person transform(ExcelContext aContext, String aColumnName, AbstractExcelFileImportParser<?> aParser, ImportLogger aLogger) {
		String theText = aContext.getString(aColumnName);

		if (StringServices.isEmpty(theText)) {
			if (this.mandatory) {
				throw new TransformException(I18NConstants.VALUE_EMPTY, aContext.row() + 1, aColumnName, theText);
			}

			return null;
		}
		else {
		    theText = theText.trim();
		
    		// Try to get contact via person id
    		Person thePerson = PersonManager.getManager().getPersonByName(theText);

    		if (thePerson != null) {
    		    return thePerson;
    		}
    		else {
    			throw new TransformException(I18NConstants.PERSON_NAME_NOT_FOUND, aContext.row() + 1, aColumnName, theText);
    		}
		}
	}
}
