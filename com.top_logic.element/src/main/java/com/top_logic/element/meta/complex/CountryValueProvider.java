/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.complex;

import com.top_logic.element.meta.ComplexValueProvider;
import com.top_logic.element.meta.OptionProvider;
import com.top_logic.util.Country;

/**
 * This implementation takes care of converting ISO-3166 country codes into
 * a {@link Country} object and vice versa.
 *
 * @author    <a href="mailto:jco@top-logic.com">jco</a>
 */
public class CountryValueProvider implements ComplexValueProvider<Country> {

	/**
	 * Singleton {@link CountryValueProvider} instance.
	 */
	public static final CountryValueProvider INSTANCE = new CountryValueProvider();

	@Override
	public Class<Country> getApplicationType() {
		return Country.class;
	}

    /**
     * The expected storage object is of type {@link String} and represents a
     * country in uppercase two-letter ISO-3166 code.
     * This is transformed into a Country object.
     * If the storage object is not ISO-3166 the code and name of the country are somewhat arbitrary.
     *
     * @see com.top_logic.element.meta.ComplexValueProvider#getBusinessObject(java.lang.Object)
     */
    @Override
	public Country getBusinessObject(Object aStorageObject) {
        if (aStorageObject instanceof String)
            return new Country((String)aStorageObject);
        return null;
    }

    /**
     * The expected business object is a {@link Country} and the
     * method returns an uppercase 2-letter ISO-3166 code as a {@link String}.
     * (If the Country was created with a non ISO-3166 code the returned String is also no ISO-3166 code).
     * If
     * @see com.top_logic.element.meta.ComplexValueProvider#getStorageObject(java.lang.Object)
     * @param aBusinessObject expect a {@link Country}
     * @return a {@link String}
     */
    @Override
	public Object getStorageObject(Object aBusinessObject) {
        if(aBusinessObject instanceof Country)
            return ((Country)aBusinessObject).getCode();
        return null;
    }

    @Override
	public OptionProvider getOptionProvider() {
        return CountryOptionProvider.INSTANCE;
    }

	@Override
	public boolean isCompatible(Object businessObject) {
		return businessObject == null || businessObject instanceof Country;
	}

}

