/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util;

import java.io.IOException;
import java.util.Locale;

import junit.framework.TestCase;

import com.top_logic.util.Country;

/**
 * @author    <a href="mailto:jco@top-logic.com">jco</a>
 */
public class TestCountry extends TestCase {

    /** 
     * Creates a {@link TestCountry}.
     */
    public TestCountry(String name) {
        super(name);
    }

    /**
     * Test method for {@link com.top_logic.util.Country#getName(java.util.Locale)}.
     */
    public void testGetMissingCountryName() {
        Locale theLocale = new Locale ("de");
        String aCode = "ST";
        System.out.println(Country.getDisplayName(aCode, theLocale));
    }

    /**
     * Test method for {@link com.top_logic.util.Country#getCountryList(java.lang.String)}.
     */
    public void testGetCountryListString() throws IOException {        
        Country[] allCountries = Country.getCountryList("de");
        assertTrue(allCountries.length > 100);
    }

}

