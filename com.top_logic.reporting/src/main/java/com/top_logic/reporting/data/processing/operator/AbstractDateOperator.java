/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.data.processing.operator;

import java.util.Date;

import com.top_logic.basic.StringServices;
import com.top_logic.reporting.data.base.value.Value;
import com.top_logic.reporting.data.base.value.common.DateValue;
import com.top_logic.reporting.data.base.value.common.NullValue;

/**
 * @author    <a href="mailto:mga@top-logic.com">mga</a>
 */
public abstract class AbstractDateOperator implements Operator {

    /** The unique name of this operation. */
    private String name;

    /** The neutral value for this date operation. */
    private Value neutral;

    /**
     * Constructor for AbstractDateOperator.
     * 
     * @param    aName    The unique name of this operation.
     * @throws   IllegalArgumentException    If the given name is <code>null</code>.
     */
    public AbstractDateOperator(String aName) throws IllegalArgumentException {
        super();

        if (StringServices.isEmpty(aName)) {
            throw new IllegalArgumentException("Given name is null");
        }

        this.name = aName;
    }

    /**
     * Execute the operation on the two given values.
     * 
     * Depending on the implementation, this method returns another date.
     * 
     * @param    aCurrent    The currently active value.
     * @param    aNew        The value to be compared.
     * @return   The result of the operation.
     */
    protected abstract Date process(Date aCurrent, Date aNew);

    /**
     * Return the neutral date for this operation.
     * 
     * This date will be used, if there is a <code>null</code> value
     * in the set of values to be processed.
     * 
     * @return    The neutral date for this operation.
     */
    protected abstract Date getNeutralDate();

    /**
     * @see com.top_logic.reporting.data.processing.operator.Operator#process(Value[])
     */
    @Override
	public Value process(Value[] aValueArray) {
        if (aValueArray != null && aValueArray.length > 0) {
            Date theValue;
            Date theResult = this.getDate(aValueArray[0]);
    
            for (int thePos = 1; thePos < aValueArray.length; thePos++) {
                theValue  = this.getDate(aValueArray[thePos]);
                theResult = this.process(theResult, theValue);
            }
            
            return (new DateValue(theResult));
        }
        // else
        return NullValue.INSTANCE;
    }

    /**
     * @see com.top_logic.reporting.data.processing.operator.Operator#getName()
     */
    @Override
	public String getName() {
        return (this.name);
    }


    /**
     * @see com.top_logic.reporting.data.processing.operator.Operator#getNeutralValue()
     */
    @Override
	public Value getNeutralValue() {
        if (this.neutral == null) {
            this.neutral = new DateValue(this.getNeutralDate());
        }
        
        return (this.neutral);
    }

    /**
     * @see com.top_logic.reporting.data.processing.operator.Operator#getDisplayName()
     */
    @Override
	public String getDisplayName() {
        return this.getName();
    }

    /**
     * @see com.top_logic.reporting.data.processing.operator.Operator#getSupportedTypes()
     */
    @Override
	public Class[] getSupportedTypes() {
        return (new Class[] {DateValue.class});
    }

    /**
     * Return the debug representation of this instance.
     * 
     * @return    The debug representation of this class.
     */
    @Override
	public String toString() {
        return (this.getClass().getName() + " [" +
                    "name: " + this.name +
                    ']');
    }

    protected Date getDate(Value aValue) {
        return (aValue instanceof DateValue) ? (Date) aValue.getEntryAt(0)
                                             : this.getNeutralDate();
    }

}
