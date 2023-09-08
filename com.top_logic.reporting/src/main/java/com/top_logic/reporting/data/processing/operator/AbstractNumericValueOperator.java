/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.data.processing.operator;

import com.top_logic.reporting.data.base.value.Value;
import com.top_logic.reporting.data.base.value.common.NullValue;
import com.top_logic.reporting.data.base.value.common.NumericTuple;
import com.top_logic.reporting.data.base.value.common.NumericValue;

/**
 * Basic implementation for the processing of 
 * {@link com.top_logic.reporting.data.base.value.common.NumericValue}.
 * 
 * The implementing classes can concentrate on their basic operations, such
 * as SUM, MIN, MAX...
 * 
 * @author    <a href="mailto:tri@top-logic.com">tri</a>
 */
public abstract class AbstractNumericValueOperator implements Operator {

    /** The supported types of objects for this operator. */
    private static Class[] types;

    /** The name for the display. */
    private String name;

    /** The neutral element for this operation. */
    private NumericValue neutralValue;

    /**
     * Basic constructor with then display name of this operation.
     * 
     * Because this is an abstract class, the subclasses have to call
     * this constructor to specify the display name.
     * 
     * @param    aName    The unique name of this operator, must not
     *                    be <code>null</code>.
     */
    public AbstractNumericValueOperator(String aName) {
        this.name = aName;
    }

    /**
     * Execute the processing methon on a set of two values.
     * 
     * This method is used by the {@link #process(com.top_logic.reporting.data.base.value.Value[])} method
     * and contains the real processing part. The give values must 
     * not be <code>null</code>.
     * 
     * @param    aSource    The source of the operation.
     * @param    aDest      The destination of the operation.
     * @return   The result of the operation.
     * @throws   ClassCastException    If one of the given values is no
     *                                 {@link com.top_logic.reporting.data.base.value.common.NumericValue}.
     */
    protected abstract Value process(Value aSource, Value aDest);

    /**
     * Returns the number, which is neutral for this operation.
     * 
     * This method is needed to get the neutral element for this operation.
     * Because that is constant, this method will only be called once.
     * 
     * @return    The neutral number, must not be <code>null</code>.
     */
    protected abstract Number getNeutralNumber();

    /**
     * Process the given values and return the result.
     * 
     * Depending on the implementation (which will be done in the 
     * {@link #process(Value,Value)} method), the result will be returned.
     * If the given array is empty, this method returns the element from
     * <br /><br />
     * The given parameter must not be <code>null</code>, as described in
     * the definition.
     * 
     * @param    aValueArray    The values which should be processed.
     * @return   The result value or a NullValue
     * @see      com.top_logic.reporting.data.base.value.common.NumericValue
     * @throws   ClassCastException    If the given array contains at least
     *                                 one none NumericValue.
     */
    @Override
	public Value process(Value[] aValueArray) {
        if (aValueArray != null && aValueArray.length > 0) {
            Value theResult        = null;
			int   firstElementIdx  = 0;
            int   size             = aValueArray.length;
			while(((theResult == null) || (theResult == NullValue.INSTANCE)) 
                && (firstElementIdx < size)){
		           theResult = aValueArray[firstElementIdx];
		           firstElementIdx++;
			}

            for (int thePos = firstElementIdx; thePos < size; thePos++) {
                Value theValue  = aValueArray[thePos];

                if ((theValue instanceof NullValue) || (theValue == null)) {
                    theValue = this.getNeutralValue();
                }

		        String[] sourceEntryNames = theResult.getEntryNames();
		        String[] destEntryNames   = theValue.getEntryNames();

                theResult = this.process(theResult, theValue);

		        if((sourceEntryNames!=null) && (sourceEntryNames.length==theResult.getNumberEntries())){
		        	theResult.setEntryNames(sourceEntryNames);
		        }else
			        if((destEntryNames!=null) && (destEntryNames.length==theResult.getNumberEntries())){
			        	theResult.setEntryNames(destEntryNames);
			        } //else set no entryNames at all
            }
            
            return (theResult);
        }
        // else
        return NullValue.INSTANCE;
    }

    
    /**
     * @see com.top_logic.reporting.data.processing.operator.Operator#getNeutralValue()
     */
    @Override
	public Value getNeutralValue() {
        if (this.neutralValue == null) {
            Number[] theValue = new Number[] {this.getNeutralNumber()};

            this.neutralValue = new NumericTuple(theValue);
        }

        return (this.neutralValue);
    }

    /**
     * @see com.top_logic.reporting.data.processing.operator.Operator#getName()
     */
    @Override
	public String getName() {
        return (this.name);
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
        return getTypes();
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

    /**
     * Return the different types, this operator can process.
     * 
     * The returned value must not be <code>null</code>, otherwise an
     * operator doesn't make sense.
     * 
     * @return    The array of supported types (classes).
     */
    public static Class[] getTypes() {
        if (types == null) {
            types = new Class[] {NumericValue.class};
        }
        
        return (types);
    }
}
