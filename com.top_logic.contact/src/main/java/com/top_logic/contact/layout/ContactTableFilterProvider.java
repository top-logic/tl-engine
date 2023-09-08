/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.layout;

import com.top_logic.contact.business.AbstractContact;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.table.filter.FirstCharacterFilterProvider;

/**
 * @author    <a href="mailto:jco@top-logic.com">jco</a>
 */
public class ContactTableFilterProvider extends FirstCharacterFilterProvider {

    public ContactTableFilterProvider() {
    	super(ContactLastNameMapping.INSTANCE);
    }

    private static class ContactLastNameMapping implements LabelProvider {
        
        static final ContactLastNameMapping INSTANCE = new ContactLastNameMapping();
        
        @Override
        public String getLabel(Object object) {
            if (object instanceof AbstractContact) {
                return ((AbstractContact) object).getString(AbstractContact.NAME_ATTRIBUTE);
            }
            else {
                return String.valueOf(object);
            }
        }
    }

}
