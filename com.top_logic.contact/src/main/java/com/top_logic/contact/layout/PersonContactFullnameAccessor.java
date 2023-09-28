/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.layout;

import com.top_logic.contact.business.PersonContact;
import com.top_logic.layout.ReadOnlyAccessor;

/**
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class PersonContactFullnameAccessor extends ReadOnlyAccessor<PersonContact> {

    /** 
     * Creates a {@link PersonContactFullnameAccessor}.
     * 
     */
    public PersonContactFullnameAccessor() {
    }

    /**
     * @see com.top_logic.layout.Accessor#getValue(java.lang.Object, java.lang.String)
     */
    @Override
	public Object getValue(PersonContact aObject, String aProperty) {
		return aObject.getName() + ", " + aObject.getValue(PersonContact.FIRST_NAME);
    }

}

