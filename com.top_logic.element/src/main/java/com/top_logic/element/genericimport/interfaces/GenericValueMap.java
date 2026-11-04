/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.genericimport.interfaces;

import com.top_logic.basic.TLID;
import com.top_logic.dob.ex.NoSuchAttributeException;

/**
 * Imported data of a single object, a map of attribute names to values.
 * 
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public interface GenericValueMap {

    /**
     * The internal type of the imported object.
     */
    public String getType();

    /**
     * The identifier of the imported data.
     */
    public TLID getIdentifier();

    /**
     * The names of all attributes contained in this map.
     */
    public String[] getAttributeNames();

    /**
     * Sets the value of the given attribute.
     *
     * @param anAttribute
     *        The attribute name.
     * @param aValue
     *        The new value.
     */
    public void    setAttributeValue(String anAttribute, Object aValue);

    /**
     * The value of the given attribute.
     *
     * @param anAttribute
     *        The attribute name.
     * @throws NoSuchAttributeException
     *         If this map has no such attribute.
     */
    public Object  getAttributeValue(String anAttribute) throws NoSuchAttributeException;

    /**
     * Whether this map contains the given attribute.
     *
     * @param anAttribute
     *        The attribute name.
     */
    public boolean hasAttribute(String anAttribute);
}

