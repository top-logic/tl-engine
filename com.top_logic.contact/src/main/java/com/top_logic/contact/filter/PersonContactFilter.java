/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.filter;

import com.top_logic.contact.business.PersonContact;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.kbbased.filtergen.ContextFreeAttributeValueFilter;

/**
 * Accept the value if it is a PersonContact
 * 
 * TODO KHA/HBU use some generic class filter.
 * 
 * @author    <a href="mailto:kbu@top-logic.com>Karsten Buch</a>
 */
@Deprecated
public class PersonContactFilter extends ContextFreeAttributeValueFilter {

    /**
     * Default public CTor for FilterFactory
     */
    public PersonContactFilter() {
        super();
    }

    /**
     * Accept the value if it is a PersonContact
     * 
     * @see com.top_logic.element.meta.kbbased.filtergen.AttributedValueFilter#accept(Object, EditContext)
     */
    @Override
	public boolean accept(Object anObject) {
        return (anObject instanceof PersonContact);
    }

}
