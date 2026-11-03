/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.layout.company;

import com.top_logic.knowledge.wrap.WrapperAccessor;
import com.top_logic.layout.ReadOnlyAccessor;

/**
 * Accessor class to company contacts.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class CompanyContactAccessor extends ReadOnlyAccessor<Object> {

    /** The default instance of this accessor (will display overall validity). */ 
	public static final CompanyContactAccessor INSTANCE = new CompanyContactAccessor();

    /**
	 * Creates a {@link CompanyContactAccessor}.
	 */
    public CompanyContactAccessor() {
    }

    /**
     * @see com.top_logic.knowledge.wrap.WrapperAccessor#getValue(java.lang.Object, java.lang.String)
     */
    @Override
	public Object getValue(Object anObject, String aProperty) {
		return WrapperAccessor.INSTANCE.getValue(anObject, aProperty);
    }
}

