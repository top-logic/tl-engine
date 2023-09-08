/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.data.retrieval.simple;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.top_logic.reporting.data.base.value.Value;
import com.top_logic.reporting.data.retrieval.Range;
import com.top_logic.reporting.data.retrieval.ValueHolder;

/** 
 * Value holder, which can be changed during usage.
 *
 * @author    <a href="mailto:mga@top-logic.com">mga</a>
 */
public class ExtendableValueHolder implements ValueHolder {

    /** The display name of this value holder. */
    private String name;

    /** The map containing the ranges and values of this holder. */
    private Map map;

    /** The array containing the range. */
    private Range[] array;
    
    /** The flag for cleaning the range array. */
    private boolean dirty;

    /**
     * Constructor for ExtendableValueHolder.
     */
    public ExtendableValueHolder() {
        super();

        this.dirty = true;
    }

    /**
     * Constructor for ExtendableValueHolder.
     * 
     * @param    aName    The display name of this value holder.
     */
    public ExtendableValueHolder(String aName) {
        this();

        this.setDisplayName(aName);
    }

    /**
     * @see com.top_logic.reporting.data.retrieval.ValueHolder#getRange()
     */
    @Override
	public Range[] getRange() {
        if (this.dirty) {
            Set     theSet   = this.getMap().keySet();
            int     theSize  = theSet.size();
            Range[] theArray = new Range[theSize];
    
            this.array = (Range[]) theSet.toArray(theArray);
            this.dirty = false;
        }

        return (this.array);
    }

    /**
     * @see com.top_logic.reporting.data.retrieval.ValueHolder#getValue(Range)
     */
    @Override
	public Value getValue(Range aKey) {
        return ((Value) this.getMap().get(aKey));
    }

    /**
     * @see com.top_logic.reporting.data.retrieval.ValueHolder#getDisplayName()
     */
    @Override
	public String getDisplayName() {
        return (this.name);
    }

    /**
     * Set the display name for this instance.
     * 
     * @param    aName    The display name of this instance.
     */
    public void setDisplayName(String aName) {
        this.name = aName;
    }

    /**
     * Store one value in the value holder.
     * 
     * @param    aRange    The range the value stands for.
     * @param    aValue    The value to be stored.
     * @return   The value stored before under the given range.
     */
    public Value addValue(Range aRange, Value aValue) {
        return ((Value) this.getMap().put(aRange, aValue));
    }

    /**
     * Return the debug representation of this instance.
     * 
     * @return    The debug representation of this class.
     */
    @Override
	public String toString() {
        String theArray = (this.array == null) ? "null" : "#" + this.array.length;

        return (this.getClass().getName() + " [" +
                    "name: " + this.name +
                    ", values: " + theArray +
                    ']');
    }

    /**
     * Return the map of held ranges and values.
     * 
     * @return    The map of ranges and values.
     */
    protected Map getMap() {
        if (this.map == null) {
            this.map = new HashMap();
        }

        return (this.map);
    }
}
