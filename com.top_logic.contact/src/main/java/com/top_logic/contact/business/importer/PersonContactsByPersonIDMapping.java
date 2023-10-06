/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.business.importer;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.StringServices;
import com.top_logic.contact.business.PersonContact;
import com.top_logic.element.genericimport.interfaces.GenericCache;
import com.top_logic.element.genericimport.interfaces.GenericConverterFunction;
import com.top_logic.knowledge.wrap.person.Person;

/**
 * @author     <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class PersonContactsByPersonIDMapping implements GenericConverterFunction {

    @Override
	public Object map(Object aValue, GenericCache aCache) {
        if (aValue instanceof String) {
            String[] parts = StringServices.toArray((String) aValue, ",");
            List<PersonContact> result = new ArrayList<>(parts.length);
            for (String part:parts) {
                Person thePerson = Person.byName(part);
                if (thePerson != null) {
                    result.add(PersonContact.getPersonContact(thePerson));
                }
            }
            return result;
        }
        return null;
    }

}
