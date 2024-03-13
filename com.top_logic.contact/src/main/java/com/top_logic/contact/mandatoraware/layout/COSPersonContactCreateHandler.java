/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.mandatoraware.layout;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.contact.business.ContactFactory;
import com.top_logic.contact.business.PersonContact;
import com.top_logic.contact.layout.person.PersonContactCreateHandler;
import com.top_logic.contact.mandatoraware.COSContactConstants;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.element.structured.wrap.Mandator;
import com.top_logic.util.error.TopLogicException;

/**
 * Mandator aware create handler for person contacts.
 * 
 * @author     <a href="mailto:kbu@top-logic.com">Karsten Buch</a>
 */
public class COSPersonContactCreateHandler extends PersonContactCreateHandler {

	public static final COSPersonContactCreateHandler INSTANCE = newInstance(COSPersonContactCreateHandler.class,
		COMMAND_ID);

	public COSPersonContactCreateHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

    /** 
     * Create without mandator not allowed in CPM
     * 
     * @see com.top_logic.contact.layout.person.PersonContactCreateHandler#createPersonContact(java.lang.String, java.lang.String)
     */
    @Override
	public PersonContact createPersonContact(String aName, String aFirst) {
    	throw new TopLogicException(this.getClass(), "Use create method with Mandator in CPM");
    }

    @Override
    public Wrapper createNewObject(Map<String, Object> someValues, Wrapper aModel) {
        return this.createPersonContact((String) someValues.get(PersonContact.NAME_ATTRIBUTE), 
                                        (String) someValues.get(PersonContact.FIRST_NAME), 
                                        (Mandator) someValues.get(COSContactConstants.ATTRIBUTE_MANDATOR));
    }

    /**
     * Create a new person contact with the given name and first name.
     * 
     * @param    aName     The sur name of the contact.
     * @param    aFirst    The first name of the contact.
     * @param    aMandator    Must not be <code>null</code>.
     * @return   The new created contact, never <code>null</code>. 
     */
    public PersonContact createPersonContact(String aName, String aFirst, Mandator aMandator) {
    	if (aMandator == null) {
    		throw new IllegalArgumentException("Mandator must not be null");
    	}

    	PersonContact thePerson = ContactFactory.getInstance().createNewPersonContact(aName, aFirst);
        
    	thePerson.setValue(COSContactConstants.ATTRIBUTE_MANDATOR, aMandator);
    	
    	return thePerson;
    }
}
