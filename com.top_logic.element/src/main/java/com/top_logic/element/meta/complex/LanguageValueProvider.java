/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.complex;

import java.util.Locale;

import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.element.meta.ComplexValueProvider;
import com.top_logic.element.meta.OptionProvider;

/**
 * @author    <a href="mailto:jco@top-logic.com">jco</a>
 */
public class LanguageValueProvider implements ComplexValueProvider<Locale> {

    private LanguageOptionProvider optionProvider;

	/**
	 * Singleton {@link LanguageValueProvider} instance.
	 */
	public static final LanguageValueProvider INSTANCE = new LanguageValueProvider();

	private LanguageValueProvider() {
		// Singleton constructor.
	}

	@Override
	public Class<Locale> getApplicationType() {
		return Locale.class;
	}

    /**
     * Here the storage object is a lowercase two-letter ISO-639 code string
     * and the business Object a {@link Locale}.
     * @see com.top_logic.element.meta.ComplexValueProvider#getBusinessObject(java.lang.Object)
     */
    @Override
	public Locale getBusinessObject(Object aStorageObject) {
        if (aStorageObject instanceof String) {
			return ResourcesModule.localeFromString((String) aStorageObject);
        }
        return null;
    }

    /**
     * @see com.top_logic.element.meta.ComplexValueProvider#getOptionProvider()
     */
    @Override
	public OptionProvider getOptionProvider() {
        if (optionProvider == null) {
            optionProvider = new LanguageOptionProvider();            
        }
        return optionProvider;
    }

    /**
     * The business object is a {@link Locale} the storage object is the 
     * corresponding lowercase two-letter ISO-639 code string for the language.
     * @see com.top_logic.element.meta.ComplexValueProvider#getStorageObject(java.lang.Object)
     */
    @Override
	public Object getStorageObject(Object aBusinessObject) {
        if(aBusinessObject instanceof Locale){
			return ((Locale) aBusinessObject).toString();
        }
        return null;
    }

	@Override
	public boolean isCompatible(Object businessObject) {
		return businessObject == null || businessObject instanceof Locale;
	}

}

