/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.excel.transformer;

import com.top_logic.base.office.excel.ExcelContext;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.contact.business.ContactFactory;
import com.top_logic.contact.business.PersonContact;
import com.top_logic.importer.excel.AbstractExcelFileImportParser;
import com.top_logic.importer.logger.ImportLogger;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.provider.LabelProviderService;

/**
 * Transform given text into a {@link PersonContact}.
 * 
 * @author     <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public class PersonContactTransformer implements Transformer<PersonContact> {

	public interface Config extends Transformer.Config {

		@BooleanDefault(true)
		boolean getUseLogin();

		@BooleanDefault(true)
		boolean getUseName();

		@BooleanDefault(true)
		boolean getUseMail();
	}

	private final boolean mandatory;
	private final boolean useLogin;
	private final boolean useName;
	private final boolean useMail;

	public PersonContactTransformer(InstantiationContext context, Config config){
		this.mandatory = config.isMandatory();
		this.useLogin  = config.getUseLogin();
		this.useName   = config.getUseName();
		this.useMail   = config.getUseMail();
	}
	
	@Override
	public PersonContact transform(ExcelContext aContext, String columnName, AbstractExcelFileImportParser<?> uploadHandler, ImportLogger logger) {
		String theText = aContext.getString(columnName);
		if (StringServices.isEmpty(theText)) {
			if (mandatory) {
				throw new TransformException(I18NConstants.VALUE_EMPTY, aContext.row() + 1, columnName, theText);
			}
			
			return null;
		}
		
		theText = theText.trim();
		
		PersonContact theContact = null;

		if (this.useLogin) {
			// Try to get contact via person id
			theContact = this.getPersonContactByPerson(aContext, columnName, theText);
		}

		if ((theContact == null) && this.useMail) {
			theContact = this.getPersonContactByMail(aContext, columnName, theText);
		}

		if ((theContact == null) && this.useName) {
            theContact = this.getPersonContactByLabel(aContext, columnName, theText);
		}

		if (theContact != null) {
		    return theContact;
		}
		else {
		    throw new TransformException(I18NConstants.PERSON_NAME_NOT_FOUND, aContext.row() + 1, columnName, theText);
		}
	}

    protected PersonContact getPersonContactByPerson(ExcelContext aContext, String columnName, String aName) {
        Person thePerson = this.getPerson(aName);

		if (thePerson != null) {
			{
			    PersonContact thePContact = ContactFactory.getInstance().getContactForPerson(thePerson);

				if (thePContact != null) {
					return thePContact;
				}

				throw new TransformException(I18NConstants.PERSON_NAME_NOT_FOUND, aContext.row() + 1, columnName, aName);
			}
		}
		else {
		    return null;
		}
    }

    /** 
     * Return a person contact by the label in the UI.
     */
    protected PersonContact getPersonContactByLabel(ExcelContext aContext, String columnName, String aName) {
        LabelProvider theProvider = null;

        for (Object theContact : ContactFactory.getInstance().getAllContactsUnsorted(ContactFactory.PERSON_TYPE)) {
            if (theProvider == null) {
				theProvider = LabelProviderService.getInstance().getLabelProvider(theContact);
            }

            String theName = theProvider.getLabel(theContact);

            if (aName.equals(theName)) {
                return (PersonContact) theContact;
            }
        }

        return null;
    }

	/**
	 * Return a person contact by its mail address.
	 */
	protected PersonContact getPersonContactByMail(ExcelContext aContext, String columnName, String aMail) {
		for (Object theContact : ContactFactory.getInstance().getAllContactsUnsorted(ContactFactory.PERSON_TYPE)) {
			PersonContact thePerson = (PersonContact) theContact;
			String        theMail   = thePerson.getEMail();

			if (!StringServices.isEmpty(theMail) && aMail.equals(theMail)) {
				return thePerson;
			}
		}

		return null;
	}

    protected Person getPerson(String aName) {
        return PersonManager.getManager().getPersonByName(aName);
    }

}
