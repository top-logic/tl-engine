/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.layout.person;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.contact.business.ContactFactory;
import com.top_logic.contact.business.PersonContact;
import com.top_logic.element.meta.gui.AbstractCreateAttributedCommandHandler;
import com.top_logic.knowledge.wrap.Wrapper;


/**
 * Handler to create new person contacts.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class PersonContactCreateHandler extends AbstractCreateAttributedCommandHandler {

    public static final String COMMAND_ID = "personContactCreate";

    public PersonContactCreateHandler(InstantiationContext context, Config config) {
        super(context, config);
    }

    @Override
    public Wrapper createNewObject(Map<String, Object> someValues, Wrapper aModel) {
        return this.createPersonContact((String) someValues.get(PersonContact.NAME_ATTRIBUTE), 
                                        (String) someValues.get(PersonContact.FIRST_NAME));
    }

    /**
     * Create a new person contact with the given name and first name.
     * 
     * @param    aName     The surname of the contact.
     * @param    aFirst    The first name of the contact.
     * @return   The new created contact, never <code>null</code>. 
     */
    public PersonContact createPersonContact(String aName, String aFirst) {
        return ContactFactory.getInstance().createNewPersonContact(aName, aFirst);
    }
}
