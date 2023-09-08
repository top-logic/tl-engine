/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.data.base.value.common;

import com.top_logic.reporting.data.base.value.Value;
import com.top_logic.reporting.data.base.value.exception.NotSupportedException;

/**
 * This class describes a null value. That means a value which actually doesn't exist.
 * Huh ? What ? Yeah - soundz strange, I know.
 * See:
 * I have a value table containing costs (values) for each year ok all my projects.
 * Now my first Project end in 2006, but my second project ends in 2005.
 * So the second project has no cost value in 2006. Still I want to have both in my table !
 * So what to put into the table in the column 2006 for my second project ? "null" would be an option.
 * Problem is that this table also should be visualized. When doing this, each entry (Value) of this table
 * is asked for a String representation. When I would ask "null" for it, I would get an Exception.
 * So this Value is the solution. Instead of "null" I put a NullValue into the table !
 * Operators and Transformators as well as the ChartPreparation simply ignore NullValues.
 * Still a NullValue has a string representation, so it can be visualized as non available entry,
 * 
 * @author    <a href="mailto:tri@top-logic.com">tri</a>
 */
public class NullValue implements Value {

    /** There can be only one NullValue */
    public static final NullValue INSTANCE = new NullValue();
    
	public static final String NULL = "n./a.";
    
    public static final String[] ENTRY_NAMES = new String[0];
    
	private NullValue(){
	
	}

	/**
	 * A NullValue has no Entries...ehrm...no, it has one... null *g*
	 * @see com.top_logic.reporting.data.base.value.Value#getEntryAt(int)
	 */
	@Override
	public Object getEntryAt(int anIndex) {
		return null;
	}

	/**
	 * A NullValue contains only null as Entry - in fact it has no entries
	 * Still the String representation of "null" is available and may be
	 * customized
	 * @see com.top_logic.reporting.data.base.value.Value#getEntryAsString(int)
	 */
	@Override
	public String getEntryAsString(int anIndex) {
		return NULL;
	}

	/**
	 * No Entries can be set in a NullValue
	 * @see com.top_logic.reporting.data.base.value.Value#setEntryAt(int, Object)
	 */
	@Override
	public void setEntryAt(int anIndex, Object anEntry) {
		throw new NotSupportedException("setEntryAt(int anIndex, Object anEntry): A NullValue has no Entries, as such the setEntry() method is not supported");
	}

	/**
	 * This Value has no Entries
	 * As said before we introduce a String representation for null here
	 * @see com.top_logic.reporting.data.base.value.Value#getAllEntriesAsString()
	 */
	@Override
	public String getAllEntriesAsString() {
		return NULL;
	}

	/**
	 * @see com.top_logic.reporting.data.base.value.Value#getNumberEntries()
	 */
	@Override
	public int getNumberEntries() {
		return 0;
	}

	/**
	 * @see com.top_logic.reporting.data.base.value.Value#projectOn(int[])
	 */
	@Override
	public Value projectOn(int[] indices) {
		throw new NotSupportedException("projectOn(int[] indices): A NullValue has no Entries, as such no entries can be extracted.");
	}

	/**
	 * 	This Value has no Entries and as such the EntryNames are null
	 *  As said before we introduce a String representation for null here
	 *
	 * @see com.top_logic.reporting.data.base.value.Value#getEntryNames()
	 */
	@Override
	public String[] getEntryNames() {
		return ENTRY_NAMES;
	}

	/**
	 * @see com.top_logic.reporting.data.base.value.Value#getEntryNameAt(int)
	 */
	@Override
	public String getEntryNameAt(int idx) {
		return NULL;
	}

	/**
	 * @see com.top_logic.reporting.data.base.value.Value#setEntryNames(String[])
	 */
	@Override
	public void setEntryNames(String[] theNames) {
		throw new NotSupportedException("setEntryNames(String[] theNames): Since NullValue has no Entries, no EntryNames can be set.");		
	}

	/**
	 * All NullValues equal each other ...since they all represent "null" - nothing more, nothing less.
	 * @see com.top_logic.reporting.data.base.value.Value#valueEquals(Value)
	 */
	@Override
	public boolean valueEquals(Value another) {
		return another == INSTANCE;
	}

    /** Return some arbitraty hashCode */
	@Override
	public int hashCode(){
		return 0x89872357;
	}
	
    /** Null can only be equal to null, well */
	@Override
	public boolean equals(Object another){
		return another == INSTANCE;
	}

}
