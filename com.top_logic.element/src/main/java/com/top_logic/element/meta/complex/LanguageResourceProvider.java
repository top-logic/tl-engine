/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.complex;

import java.util.Locale;

import com.top_logic.mig.html.DefaultResourceProvider;
import com.top_logic.util.Resources;

/**
 * This resource provider takes the current locale of the context.
 * to get the language displayname of a {@link Locale} in the language of the context.
 * 
 * @author    <a href="mailto:jco@top-logic.com">jco</a>
 */
public class LanguageResourceProvider extends DefaultResourceProvider {

    /**
     * @see com.top_logic.layout.LabelProvider#getLabel(java.lang.Object)
     */
    @Override
	public String getLabel(Object anObject) {
        if(anObject instanceof Locale){
			return Resources.getDisplayLanguage((Locale) anObject);
        }
        return super.getLabel(anObject);
    }

}

