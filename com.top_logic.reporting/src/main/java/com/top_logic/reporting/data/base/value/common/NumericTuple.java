/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.data.base.value.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.top_logic.reporting.data.base.value.Value;

/**
 * Implementation for the NumericValue.
 * 
 * This class implements the interface {@link NumericValue} and uses the
 * basic type <b>double</b> for the operations.
 * 
 * @author    <a href="mailto:tri@top-logic.com">tri</a>
 */
public class NumericTuple implements NumericValue {

	/** The list of held entries. */
	private List entries;

	/** The list of the entries names */
	private String[] entryNames;
	
	/** Flag, if a value has changed. */
	private boolean dirty = true;

	/** Cache for the display of all values in a string. */
	private String cache;

	/**
	 * Creates a new NumericTuple which holds no values.
	 */
	public NumericTuple() {
		setEntries(new ArrayList());
	}

	/**
	 * Creates a new NumericValue with the given Entries.
	 * 
	 * @param    someEntries    The entries of the new Numeric value.
	 */
	public NumericTuple(double[] someEntries) {
		setEntries(new ArrayList(someEntries.length));
		for (int thePos = 0; thePos < someEntries.length; thePos++) {
			this.getEntries().add(Double.valueOf(someEntries[thePos]));
		}
	}

	/**
	 * Creates a new NumericValue with the given Entries.
	 * 
	 * @param    someEntries    The entries of the new Numeric value.
	 */
	public NumericTuple(int[] someEntries) {
		setEntries(new ArrayList(someEntries.length));

		for (int thePos = 0; thePos < someEntries.length; thePos++) {
			this.getEntries().add(Integer.valueOf(someEntries[thePos]));
		}
	}

	/**
	 * Creates a new NumericValue with the given Entries.
	 * 
	 * @param    someEntries    The entries of the new Numeric value.
	 */
	public NumericTuple(float[] someEntries) {
		setEntries(new ArrayList(someEntries.length));

		for (int thePos = 0; thePos < someEntries.length; thePos++) {
			this.getEntries().add(Float.valueOf(someEntries[thePos]));
		}
	}

	/**
	 * Creates a new NumericValue with the given Entries.
	 * 
	 * @param    someEntries    The entries of the new Numeric value.
	 */
	public NumericTuple(Number[] someEntries) {
		setEntries(Arrays.asList(someEntries));
	}

	/**
	 * @see com.top_logic.reporting.data.base.value.common.NumericValue#plus(NumericValue)
	 */
	@Override
	public NumericValue plus(NumericValue anOther) {
		double theOwnValue;
		double theOtherValue;
		int theSize;
		NumericTuple theResult;

		if (anOther != null) {
			theSize = Math.max(this.getNumberEntries(), anOther.getNumberEntries());
		} else {
			theSize = this.getNumberEntries();
		}

		theResult = new NumericTuple(new Number[theSize]);

		for (int thePos = 0; thePos < theSize; thePos++) {
			theOwnValue = this.getValueWith(this, thePos, 0);
			theOtherValue = this.getValueWith(anOther, thePos, 0);

			theResult.setEntryAt(thePos, Double.valueOf(theOwnValue + theOtherValue));
		}

		return (theResult);
	}

	/**
	 * @see com.top_logic.reporting.data.base.value.common.NumericValue#minus(NumericValue)
	 */
	@Override
	public NumericValue minus(NumericValue anOther) {
		double theOwnValue;
		double theOtherValue;
		int theSize;
		NumericTuple theResult;

		if (anOther != null) {
			theSize = Math.max(this.getNumberEntries(), anOther.getNumberEntries());
		} else {
			theSize = this.getNumberEntries();
		}

		theResult = new NumericTuple(new Number[theSize]);

		for (int thePos = 0; thePos < theSize; thePos++) {
			theOwnValue = this.getValueWith(this, thePos, 0);
			theOtherValue = this.getValueWith(anOther, thePos, 0);

			theResult.setEntryAt(thePos, Double.valueOf(theOwnValue - theOtherValue));
		}

		return (theResult);
	}

	/**
	 * @see com.top_logic.reporting.data.base.value.common.NumericValue#multiply(NumericValue)
	 */
	@Override
	public NumericValue multiply(NumericValue anOther) {
		double theOwnValue;
		double theOtherValue;
		int theSize;
		NumericTuple theResult;

		if (anOther != null) {
			theSize = Math.max(this.getNumberEntries(), anOther.getNumberEntries());
		} else {
			theSize = this.getNumberEntries();
		}

		theResult = new NumericTuple(new Number[theSize]);

		for (int thePos = 0; thePos < theSize; thePos++) {
			theOwnValue = this.getValueWith(this, thePos, 1);
			theOtherValue = this.getValueWith(anOther, thePos, 1);

			theResult.setEntryAt(thePos, Double.valueOf(theOwnValue * theOtherValue));
		}

		return (theResult);
	}

	/**
	 * @see com.top_logic.reporting.data.base.value.common.NumericValue#devide(NumericValue)
	 */
	@Override
	public NumericValue devide(NumericValue anOther) {
		double theOwnValue;
		double theOtherValue;
		int theSize = Math.max(this.getNumberEntries(), anOther.getNumberEntries());
		NumericTuple theResult = new NumericTuple(new Number[theSize]);

		for (int thePos = 0; thePos < theSize; thePos++) {
			theOwnValue = this.getValueWith(this, thePos, 1);
			theOtherValue = this.getValueWith(anOther, thePos, 1);

			theResult.setEntryAt(thePos, Double.valueOf(theOwnValue / theOtherValue));
		}

		return (theResult);
	}

	/**
	 * @see com.top_logic.reporting.data.base.value.common.NumericValue#max(NumericValue)
	 */
	@Override
	public NumericValue max(NumericValue anOther) {
		double theOwnValue;
		double theOtherValue;
		int theSize;
		NumericTuple theResult;

		if (anOther != null) {
			theSize = Math.max(this.getNumberEntries(), anOther.getNumberEntries());
		} else {
			theSize = this.getNumberEntries();
		}

		theResult = new NumericTuple(new Number[theSize]);

		for (int thePos = 0; thePos < theSize; thePos++) {
			theOwnValue = this.getValueWith(this, thePos, Double.MIN_VALUE);
			theOtherValue = this.getValueWith(anOther, thePos, Double.MIN_VALUE);

			theResult.setEntryAt(thePos, Double.valueOf(Math.max(theOwnValue, theOtherValue)));
		}

		return (theResult);
	}

	/**
	 * @see com.top_logic.reporting.data.base.value.common.NumericValue#min(NumericValue)
	 */
	@Override
	public NumericValue min(NumericValue anOther) {
		double theOwnValue;
		double theOtherValue;
		int theSize;
		NumericTuple theResult;

		if (anOther != null) {
			theSize = Math.max(this.getNumberEntries(), anOther.getNumberEntries());
		} else {
			theSize = this.getNumberEntries();
		}

		theResult = new NumericTuple(new Number[theSize]);

		for (int thePos = 0; thePos < theSize; thePos++) {
			theOwnValue = this.getValueWith(this, thePos, Double.MAX_VALUE);
			theOtherValue = this.getValueWith(anOther, thePos, Double.MAX_VALUE);

			theResult.setEntryAt(thePos, Double.valueOf(Math.min(theOwnValue, theOtherValue)));
		}

		return (theResult);
	}

	/**
	 * @see com.top_logic.reporting.data.base.value.Value#getEntryAt(int)
	 */
	@Override
	public String getEntryAsString(int idx) {
		Object theValue = this.getEntryAt(idx);

		return ((theValue == null) ? "" : theValue.toString());
	}

	/**
	 * @see com.top_logic.reporting.data.base.value.Value#getEntryAt(int)
	 */
	@Override
	public Object getEntryAt(int anIdx) {
		return ((anIdx >= this.getEntries().size()) ? null : this.getEntries().get(anIdx));
	}

	/**
	 * @see com.top_logic.reporting.data.base.value.Value#setEntryAt(int,Object)
	 */
	@Override
	public void setEntryAt(int idx, Object obj) {
		if(obj==null){
			throw new IllegalArgumentException("NumericTuple does not support null values.");
		}
		if (obj instanceof Number) {
			this.getEntries().set(idx, obj);
			this.dirty = true;
		}
	}

	/**
	 * @see com.top_logic.reporting.data.base.value.Value#getAllEntriesAsString()
	 */
	@Override
	public String getAllEntriesAsString() {
		if (this.dirty) {
			int theSize = this.getEntries().size();

			if (theSize > 0) {
				Object theValue;
				StringBuffer theResult = null;

				for (int thePos = 0; thePos < theSize; thePos++) {
					theValue = this.getEntryAt(thePos);

					if (theValue != null) {
						if (thePos == 0) {
							theResult = new StringBuffer();
						} else {
							theResult.append(", ");
						}

						theResult.append(theValue);
					}
				}

				if (theResult == null) {
					this.cache = "";
				} else {
					this.cache = theResult.toString();
				}
			} else {
				this.cache = "";
			}

			this.dirty = false;
		}

		return (this.cache);
	}

	/**
	 * @see com.top_logic.reporting.data.base.value.common.NumericValue#getNumberEntries()
	 */
	@Override
	public int getNumberEntries() {
		if (this.getEntries() == null) return 0;
		return (this.getEntries().size());
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (this.getAllEntriesAsString().hashCode());
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object anObject) {
		if (anObject instanceof NumericValue) {
			Object theObj1;
			Object theObj2;
			NumericValue theValue = (NumericValue) anObject;
			int theSize = theValue.getNumberEntries();
			boolean theResult = this.getNumberEntries() == theSize;

			for (int thePos = 0; theResult && (thePos < theSize); thePos++) {
				theObj1 = this.getEntries().get(thePos);
				theObj2 = theValue.getEntryAt(thePos);
				theResult = (theObj1.equals(theObj2));
			}

			return (theResult);
		} else {
			return (false);
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return (this.getClass().getName() + " [" + "entries: " + this.getAllEntriesAsString() + ']');
	}

	/**
	 * Get the value at the specified position from the given tuple.
	 * 
	 * If there is no such value, this method returns the zero value.
	 * 
	 * @param    aTuple      The tuple to be used.
	 * @param    aPos        The requested position.
	 * @param    aNeutral    The neutral element, if the value is <code>null</code>.
	 * @return   The found value or '0'.
	 */
	protected double getValueWith(NumericValue aTuple, int aPos, double aNeutral) {
        
        if (aTuple != null) {
    		Object theResult = aTuple.getEntryAt(aPos);
    
    		if (theResult != null && theResult instanceof Number) {
    			return (((Number) theResult).doubleValue());
            }
        }
	    return aNeutral;
	}

	/**
	 * @see com.top_logic.reporting.data.base.value.Value
	 */
	@Override
	public String[] getEntryNames() {
		return this.entryNames;
	}

	/**
	 * @see com.top_logic.reporting.data.base.value.Value
	 */
	@Override
	public String getEntryNameAt(int idx) {
		if((entryNames==null) || (idx >= entryNames.length) ) return "";
		return this.entryNames[idx];
	}

	/**
	 * @see com.top_logic.reporting.data.base.value.Value
	 */
	@Override
	public void setEntryNames(String[] entryNames){
		if (entryNames == null){
			this.entryNames = entryNames;
			return;
		}
		if ((getNumberEntries() != entryNames.length)) {
			throw new ArrayIndexOutOfBoundsException("If you provide EntryNames at all, you have to provide one for each of your entries."+
												  "That means the size of the given arrays has to be the same.");
		}
		this.entryNames = entryNames;

	}

	/**
	 * TODO TRI A projection of a subset of this tuple as given by indices.
	 */
	@Override
	public Value projectOn(int[] indices) {
		
        int      size          = indices.length;
		Number[] temp          = new Number[size];
		String[] theEntryNames = new String[size];
		for (int i = 0; i < size; i++) {
				Object tmp = getEntryAt(indices[i]);
				if(tmp!=null){
					temp[i]=(Number)tmp;
				}
				theEntryNames[i] = getEntryNameAt(indices[i]);			
		}
		NumericTuple res = new NumericTuple(temp);
		res.setEntryNames(theEntryNames);
		return res;
	}

	/**
	 * NumericTuple already has customized it's equals method. So we just return this
	 * @see com.top_logic.reporting.data.base.value.Value#valueEquals(Value)
	 */	
	@Override
	public boolean valueEquals(Value anotherValue){
		return this.equals(anotherValue);
	}

	protected void setEntries(List entries) {
		this.entries = entries;
	}

	protected List getEntries() {
		return entries;
	}
	
	protected void setDirty(boolean flag){
		this.dirty = flag;
	}
	
	protected boolean isDirty(){
		return dirty;
	}

}
