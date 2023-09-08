/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap;

/**
 * A Common interface to copy Data between such ValueProviders.
 * 
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public interface ValueProvider {

    /**
     * Return the attribute value for the given key.
     *
     * @param    aKey    The name of the needed attribute, must not be <code>null</code> or empty.
     * @return   The read value, may be <code>null</code>.
     */
    public Object getValue(String aKey);

    /**
     * Set the value defined by the given name.
     * 
     * @param    aName     The name of the attribute to be set, must not be <code>null</code> or empty.
     * @param    aValue    The value to be set, may be <code>null</code>.
     */
    public void setValue(String aName, Object aValue);
    
}

