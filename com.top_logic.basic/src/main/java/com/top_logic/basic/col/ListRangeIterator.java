/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.List;

/**
 * IDRangeIterator that is based on a List. 
 * 
 * The Iterator is no awrae of changes in the List.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public abstract class ListRangeIterator implements IDRangeIterator {

    /**
     * The internal data structure for this iterator.
     */
    protected List elements;
    
    /**
     * The current position of the iterator.
     */
    int position;

    /**
     * Creates a {@link ListRangeIterator} for the given List.
     * 
     * @param aList will be owned by this object,
     */
    public ListRangeIterator(List aList){
        this.elements = aList;
        this.position = -1;
    }

    /** 
     * @see com.top_logic.basic.col.IDRangeIterator#nextObject()
     */
    @Override
	public Object nextObject() {
        this.position++;
        if(this.position < elements.size()){
            return this.elements.get(this.position);
        }
        return null;
    }

    /**
     * Reset to position before first element.
     */
    @Override
	public void reset() {
       this.position = -1;
    }
    
   /**
     * @see com.top_logic.basic.col.IDRangeIterator#createCoords()
     */
    @Override
	public Object[] createCoords() {
        int size = elements.size();
		Object[] result = new Object[size];
        for (int i = 0; i < size; i++) {
            result[i] = getIDFor(elements.get(i));
        }
        return result;
    }

    
}
