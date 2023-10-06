/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.provider;

import java.util.Collection;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.contact.business.ContactFactory;
import com.top_logic.contact.business.PersonContact;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.importer.base.ObjectProvider;
import com.top_logic.importer.text.TextImportParser;

/**
 * @author    <a href="mailto:mga@top-logic.com">mga</a>
 */
public class PersonContactObjectProvider implements ObjectProvider {

    @Override
    public Collection<? extends Wrapper> getObjects(Wrapper aModel) {
        return CollectionUtil.dynamicCastView(PersonContact.class, ContactFactory.getInstance().getAllContacts(ContactFactory.PERSON_TYPE));
    }

    @Override
    public boolean supportsModel(Wrapper aModel) {
        return true;
    }

    /**
     * Special parser for {@link PersonContact}s. 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public static class PersonContactParser extends TextImportParser<PersonContact> {

        public interface Config extends TextImportParser.Config {
        }

        public PersonContactParser(InstantiationContext aContext, PersonContactParser.Config aConfig) {
            super(aContext, aConfig);
        }

        @Override
        public PersonContact map(String aString) {
            String theString = aString.trim();

            if (StringServices.isEmpty(theString)) {
                return null;
            }
            else { 
                return PersonContact.getPersonContact(Person.byName(theString));
            }
        }
    }
}

