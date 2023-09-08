/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.complex;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.top_logic.element.meta.ListOptionProvider;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.util.Country;
import com.top_logic.util.TLContext;

/**
 * We use the {@link Country} class to return an ordered list of countries
 * 
 * @author    <a href="mailto:jco@top-logic.com">jco</a>
 */
public class CountryOptionProvider implements ListOptionProvider {

    public static final CountryOptionProvider INSTANCE = new CountryOptionProvider();
    
    private CountryOptionProvider() {
        // enforce usage of INSTANCE
    }
    
    @Override
	public List<Country> getOptionsList(EditContext editContext) {
		return getOptionsList(TLContext.getLocale());
	}

	/**
	 * Delivers the {@link Country} having the same language as the given {@link Locale}.
	 * 
	 * @param languageLocale
	 *        May be <code>null</code>.
	 */
	public List<Country> getOptionsList(Locale languageLocale) {
		Country[] theListOfCountries = null;
		if (languageLocale == null) {
            theListOfCountries = Country.getCountryList();
        } else {
			theListOfCountries = Country.getCountryList(languageLocale.getLanguage());
        }
        return Arrays.asList(theListOfCountries);
	}

}

