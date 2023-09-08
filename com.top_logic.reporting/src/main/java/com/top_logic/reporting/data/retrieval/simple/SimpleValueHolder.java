/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.data.retrieval.simple;

import com.top_logic.basic.StringServices;
import com.top_logic.reporting.data.base.value.Value;
import com.top_logic.reporting.data.retrieval.Range;
import com.top_logic.reporting.data.retrieval.ValueHolder;

/** 
 * Simple implementation for the value holder.
 * 
 * This class takes two arrays of values and provides them to the
 * rest of the framework.
 *
 * @author    <a href="mailto:mga@top-logic.com">mga</a>
 */
public class SimpleValueHolder implements ValueHolder {

    /** The display name of this instance. */
    private String name;

    /** The supported range of the values. */
    private Range[] range;

    /** The held values. */
    private Value[] values;

    /**
     * Internal constructor for a value holder.
     * 
     * This one should only be used, if the creation of values and ranges
     * is a little mor complex, and the subclass want's to do this by its
     * own. Be shure to use the {@link #init(Range[], Value[])} method, 
     * when constructing the instance, otherwise the methods can fail.
     * 
     * @param    aName         The display name of this instance.
     * @throws   IllegalArgumentException     If the range has another size
     *                                        than the values or the given
     *                                        name is empty.
     */
    protected SimpleValueHolder(String aName) {
        super();

        if (StringServices.isEmpty(aName)) {
            throw new IllegalArgumentException("Name is empty");
        }
        else {
            this.name = aName;
        }
    }

    /**
     * Constructor for SimpleValueHolder.
     * 
     * @param    aName         The display name of this instance.
     * @param    aRange        The supported range of the values.
     * @param    someValues    The values held by the instance.
     * @throws   IllegalArgumentException     If the range has another size
     *                                        than the values or the given
     *                                        name is empty.
     */
    public SimpleValueHolder(String aName, 
                             Range[] aRange, 
                             Value[] someValues) {
        this(aName);
        this.init(aRange, someValues);
    }

    /**
     * @see com.top_logic.reporting.data.retrieval.ValueHolder#getRange()
     */
    @Override
	public Range[] getRange() {
        Range[] newArray = new Range[this.range.length];
        System.arraycopy(this.range,0,newArray,0,this.range.length);
        return (newArray);
    }

    /**
     * @see com.top_logic.reporting.data.retrieval.ValueHolder#getValue(Range)
     */
    @Override
	public Value getValue(Range aKey) {
        int   theResult;
        Value theValue = null;

        if (aKey != null) {
            for (int thePos = 0; 
                 (theValue == null) && (thePos < this.values.length); 
                 thePos++) {
                theResult = aKey.compareTo(this.range[thePos]);
    
                if (theResult == 0) {
                    theValue = this.values[thePos];
                }
            }
        }

        return (theValue);
    }

    /**
     * @see com.top_logic.reporting.data.retrieval.ValueHolder#getDisplayName()
     */
    @Override
	public String getDisplayName() {
        return (this.name);
    }

    /**
     * Initialize this value holder with the right values.
     * 
     * @param    aRange        The supported range of the values.
     * @param    someValues    The values held by the instance.
     * @throws   IllegalArgumentException     If the range has another size
     *                                        than the values.
     */
    protected void init(Range[] aRange, Value[] someValues) {
        String theMessage = null;

        if ((aRange == null) || (aRange.length == 0)) {
            theMessage = "Range is empty";
        }
        else if ((someValues == null) || (someValues.length == 0)) {
            theMessage = "Values are empty";
        }
        else if (aRange.length != someValues.length) {
            theMessage = "Range and values has different length";
        }

        if (theMessage != null) {
            throw new IllegalArgumentException(theMessage);
        }
        else {
            this.range  = new Range[aRange.length];
            this.values = new Value[someValues.length];
            System.arraycopy(aRange,0,this.range,0,aRange.length);
            System.arraycopy(someValues,0,this.values,0,someValues.length);
        }
    }
}
