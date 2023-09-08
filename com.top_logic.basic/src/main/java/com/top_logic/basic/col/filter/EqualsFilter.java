/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.filter;

import com.top_logic.basic.col.Filter;


/**
 * A Filter that will accept objects equals to a given one.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class EqualsFilter implements Filter<Object> {

    /** The object to be used for testing. */
    protected Object object;

    /**
     * Default constructor.
     * 
     * @param    anObject    The object to be used for testing.
     */
    public EqualsFilter(Object anObject) {
        if (anObject == null) {
            throw new IllegalArgumentException("Given object is null!");
        }

        this.object = anObject;
    }
    
    /**
     * Show something reasonable for debugging.
     */
    @Override
	public String toString() {
        return this.getClass().getName() + " = " + object;
    }

    /**
     * true when object is equals given object.
     */
    @Override
	public boolean accept(Object anObject) {
        return (this.object.equals(anObject));
    }
}
