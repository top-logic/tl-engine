/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.business.importer;

import com.top_logic.contact.business.PersonContact;
import com.top_logic.element.genericimport.interfaces.GenericCache;
import com.top_logic.element.genericimport.interfaces.GenericConverterFunction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;

/**
 * @author     <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class PersonContactByPersonIDMapping implements GenericConverterFunction {

    @Override
	public Object map(Object aValue, GenericCache aCache) {
        if (aValue instanceof String) {
            Person thePerson = PersonManager.getManager().getPersonByName((String) aValue);
            if (thePerson != null) {
                return PersonContact.getPersonContact(thePerson);
            }
        }
        return null;
    }

}
