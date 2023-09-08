/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Collection;

/**
 * Useful for testcases and simple, flat dimensions. 
 * 
 * @author     <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class StringRangeIterator extends FixedRangeIterator {

    /**
     * Creates a {@link StringRangeIterator} for the given array.
     * 
     * @param anObjectArray A object array.
     */
    public StringRangeIterator(Object[] anObjectArray){
        super(anObjectArray);
    }
    
    /**
     * Creates a {@link StringRangeIterator} for the given
     * collection.
     * 
     * @param aCollection A {@link Collection}. The collection must be
     *        containing strings and only strings.
     */
    public StringRangeIterator(Collection aCollection){
        super(aCollection);
    }

    /** 
     * @see com.top_logic.basic.col.FixedRangeIterator#getIDFor(java.lang.Object)
     */
    @Override
	public Object getIDFor(Object anObject) {
        if (contains(anObject)){
			return anObject;
        }
        throw new IllegalArgumentException();
    }
    
    /** 
     * @see com.top_logic.basic.col.FixedRangeIterator#getUIStringFor(java.lang.Object)
     */
    @Override
	public String getUIStringFor(Object anObject) {
        if (contains(anObject)){
            return (String)anObject;
        }
        throw new IllegalArgumentException();
    }
    
}
