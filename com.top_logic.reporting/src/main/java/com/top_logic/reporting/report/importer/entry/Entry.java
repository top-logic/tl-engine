/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.importer.entry;

/**
 * The Entry is a simple value value with a key and an value.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
@Deprecated
public class Entry {

    private String key;
    private Object value;

    /**
     * Creates a {@link Entry}.
     * 
     * @param aKey
     *        The key must not be <code>null</code> or empty.
     * @param aValue
     *        The value must not be <code>null</code>.
     */
    public Entry(String aKey, Object aValue) {
        this.key    = aKey;
        this.value = aValue;
    }

    /** 
     * Returns the key of this entry.
     */
    public String getKey() {
        return this.key;
    }

    /** 
     * Returns the value of this entry.
     */
    public Object getValue() {
        return this.value;
    }
    
    @Override
	public String toString() {
        return getClass() + "[key=" + getKey() + ",value=" + getValue() + "]";
    }
    
}

