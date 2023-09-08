/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.layout;

import com.top_logic.mig.html.DefaultResourceProvider;
import com.top_logic.util.Country;
import com.top_logic.util.TLContext;

/**
 * @author    <a href="mailto:jco@top-logic.com">jco</a>
 */
public class CountryResourceProvider extends DefaultResourceProvider {
    /**
     * @see com.top_logic.mig.html.DefaultResourceProvider#getLabel(java.lang.Object)
     */
    @Override
	public String getLabel(Object anObject) {
        if(anObject instanceof Country){
            return ((Country)anObject).getName(TLContext.getLocale());
        }
        return super.getLabel(anObject);
    }
}

