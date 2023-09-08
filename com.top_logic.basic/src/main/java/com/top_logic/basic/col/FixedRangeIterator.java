/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Collection;

/**
 * The FixedRangeIterator can iterates over an collection or an array.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public abstract class FixedRangeIterator implements IDRangeIterator {

    /**
     * The internal data structure for this iterator.
     */
    protected Object[] elements;
    
    /**
     * The size of the {@link #elements}.
     */
    protected int length;

    /**
     * The current position of the iterator.
     */
    protected int position;

    /**
     * Creates a {@link FixedRangeIterator} for the given array.
     * 
     * @param anObjectArray  will not be copied.
     */
    public FixedRangeIterator(Object[] anObjectArray){
        if(anObjectArray == null){
            this.elements = new Object[]{};
            this.length   = 0;
        }
        else{
           this.elements = anObjectArray;
           this.length   = anObjectArray.length;
        }
        this.position = -1;
    }
    
    /**
     * Creates a {@link FixedRangeIterator} for the given
     * collection.
     * 
     * @param aCollection Order will be preserved, Elements will be copied.
     */
    public FixedRangeIterator(Collection aCollection){
        if(aCollection == null || aCollection.size() == 0){
            this.elements = new Object[]{};
            this.length   = 0;
        }
        else{
            this.elements = aCollection.toArray(new Object[aCollection.size()]);
            this.length   = aCollection.size();
        }
        this.position = -1;
    }

    /** 
     * @see com.top_logic.basic.col.IDRangeIterator#nextObject()
     */
    @Override
	public Object nextObject() {
        this.position++;
        if(this.position < this.length){
            return this.elements[this.position];
        }
        return null;
    }

    /**
     * This method returns <code>true</code> if and only if the given object
     * is one element of the iterator elements, <code>false</code> otherwise.
     * 
     * @param  anObject An object. Must not be <code>null</code>.
     * @return Returns <code>true</code> if and only if the given object is
     *         one element of the iterator elements, <code>false</code>
     *         otherwise.
     */
    protected boolean contains(Object anObject) {
        for (int i = 0; i < this.length; i++) {
            Object element = this.elements[i];
            if (element.equals(anObject)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Reset to position before first element.
     */
    @Override
	public void reset() {
       this.position = -1;
    }
    
    @Override
	public Object[] createCoords() {
		Object[] result = new Object[elements.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = getIDFor(elements[i]);
        }
        return result;
    }

    
}
