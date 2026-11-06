/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.genericimport.interfaces;

/**
 * Function converting a single imported value to the value stored in an attribute.
 *
 * @author    <a href="mailto:TEH@top-logic.com">TEH</a>
 */
public interface GenericConverterFunction {

    /**
     * Converts the given imported value.
     *
     * @param aValue
     *        The imported value.
     * @param aCache
     *        The cache of already imported objects, e.g. to resolve references.
     * @return The converted value.
     */
    public Object map(Object aValue, GenericCache aCache);
}

