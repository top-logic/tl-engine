/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.searching;

import com.top_logic.basic.StringServices;

/** 
 * Additional qualifier for a search result.
 * 
 * Every search result can have a list of attributes defining the quality
 * of the hit. This attribute can be the importance of the hit in the
 * sum of the results.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class SearchResultAttribute {

    /** The key of this attribute. */
    private String key;

    /** The value of this attribute. */
    protected Object value;

    /**
     * Constructor for SearchResultAttribute.
     * 
     * @param    aKey      The key of the attribute.
     * @param    aValue    The value of the attribute.
     * @throws   IllegalArgumentException    If one of the parameters is empty
     *                                       or <code>null</code>.
     */
    public SearchResultAttribute(String aKey, Object aValue) 
                                            throws IllegalArgumentException {
        super();

        String theMessage = null;

        if (StringServices.isEmpty(aKey)) {
            theMessage = "Key is empty!";
        }
        else if (aValue == null) {
            theMessage = "Value is null!";
        }

        if (theMessage != null) {
            throw new IllegalArgumentException(theMessage);
        }

        this.key   = aKey;
        this.value = aValue;
    }

    /**
     * Return the string representation of this instance for debugging.
     * 
     * @return    The string representation for debugging.
     */
    @Override
	public String toString() {
        return (this.getClass().getName() + " ["
                        + "key: " + this.key
                        + ", value: " + this.value
                        + ']');
    }

    /**
     * Return the key identifying this attribute.
     * 
     * @return    The key of this attribute (cannot be <code>null</code> 
     *            or empty).
     */
    public String getKey() {
        return (this.key);
    }

    /**
     * Return the value of this attribute.
     * 
     * @return    The value of this attribute (cannot be <code>null</code>).
     */
    public Object getValue() {
        return (this.value);
    }
}
