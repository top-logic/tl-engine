/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.data.base.value.simple;

import com.top_logic.reporting.data.base.value.Value;
import com.top_logic.reporting.data.base.value.exception.NotSupportedException;


/** 
 * Simple implementation of a value of type Object.
 * 
 * This value has only one value.
 *
 * @author    <a href="mailto:mga@top-logic.com">mga</a>
 */
public class SimpleValue implements Value {

    /** The represented value. */
    private Object value;
	private String[] entryNames;

    /**
     * Constructor for SimpleValue.
     */
    public SimpleValue(Object anObject) {
        super();

        if (anObject == null) {
            throw new IllegalArgumentException("Given value is null");
        }

        this.value = anObject;
    }

    /**
     * @see com.top_logic.reporting.data.base.value.Value#getEntryAt(int)
     */
    @Override
	public Object getEntryAt(int anIndex) {
        if (anIndex == 0) {
            return (this.value);
        }
        else {
            return (null);
        }
    }

    /**
     * @see com.top_logic.reporting.data.base.value.Value#getEntryAsString(int)
     */
    @Override
	public String getEntryAsString(int anIndex) {
        if (anIndex == 0) {
            return (this.value.toString());
        }
        else {
            return (null);
        }
    }

    /**
     * @see com.top_logic.reporting.data.base.value.Value#setEntryAt(int, Object)
     */
    @Override
	public void setEntryAt(int anIndex, Object anEntry) {
        if (anIndex == 0) {
            this.value = anEntry;
        }
    }

    /**
     * @see com.top_logic.reporting.data.base.value.Value#getAllEntriesAsString()
     */
    @Override
	public String getAllEntriesAsString() {
        return (this.getEntryAsString(0));
    }

    /**
     * @see com.top_logic.reporting.data.base.value.Value#getNumberEntries()
     */
    @Override
	public int getNumberEntries() {
        return 1;
    }

    /**
     * Compare the given object to this one.
     * <br /><br />
     * Another object is equal to this one, if its instanceof SimpleValue
     * and the held value is equal to the value held by this instance.
     * 
     * @param    anObject    The object to be compared.
     * @return   <code>true</code>, if the given value is equal.
     */
    @Override
	public boolean equals(Object anObject) {
		if (anObject == null) {
			return false;
		}
		if (anObject == this) {
			return true;
		}
		if (anObject.getClass() == SimpleValue.class) {
            return (this.value.equals(((SimpleValue) anObject).value));
        }
        else {
            return (false);
        }
    }

    /**
     * Return the hash code for this object.
     * 
     * The hash code is the hash code of the held object. This is important
     * because maps are using this method to store an object, but need to
     * have a method for finding the correct object later from the hash. That
     * method is the {@link #equals(java.lang.Object) equals} method, which
     * is also overwritten from this implementation.
     * 
     * @return    The hash code of this object.
     */
    @Override
	public int hashCode() {
        return (this.value.hashCode());
    }

    /**
     * Returns the debug representation of this instance.
     * 
     * @return    The debug representation of this instance.
     */
    @Override
	public String toString() {
        return (this.getClass().getName() + " [" +
                        "value: " + this.value +
                        ']');
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
		if ((entryNames == null) || (idx >= entryNames.length)) {
			return "";
		}
		return this.entryNames[idx];
	}

	/**
	 * @see com.top_logic.reporting.data.base.value.Value
	 */
	@Override
	public void setEntryNames(String[] entryNames){
		if (entryNames == null){
			this.entryNames = null;
			return;
		}
		if ((getNumberEntries() != entryNames.length)) {
			throw new ArrayIndexOutOfBoundsException("If you provide EntryNames at all, you have to provide one for each of your entries."+
												  "That means the size of the given arrays has to be the same.");
		}
		this.entryNames = entryNames;
	}

	protected Object getValue(){
		return this.value;
	}

	/**
	 * @see com.top_logic.reporting.data.base.value.Value#projectOn(int[])
	 */
	@Override
	public Value projectOn(int[] indices) {

		throw new NotSupportedException("Projection not supported by"+this.getClass());
	}

	/* Not in use, but why still here ?
	 *
	public Value projectOn(int idx) {
        return ((idx == 0) ? this : null);		
	}
    */
    
	/**
	 * SimpleValue already has customized it's equals method. So we just return this
	 * @see com.top_logic.reporting.data.base.value.Value#valueEquals(Value)
	 */	
	@Override
	public boolean valueEquals(Value anotherValue){
		return this.equals(anotherValue);
	}

}
