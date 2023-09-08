/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.data.base.value;

/**
 * Represents a value of the data which can be hold by a value holder
 * and which is described by a description.
 * 
 * A value can have more than one entries. E.g. a value of type "Kenngroesse"
 * may have a "Soll" and an "Ist" value.
 * This interface provides access to the entries of this value.
 * The names of the entries can be retrieved from the description.
 * 
 * @author    <a href="mailto:tri@top-logic.com">tri</a>
 */
public interface Value {

    /**
     * Return the value on the given position.
     * 
     * This method has to be able to return values for an index from 
     * <code>0</code> to <code>(n -1)</code>, where <code>n</code> is the 
     * value returned by {@link #getNumberEntries()}.
     * 
     * @param    anIndex    The index of the entry to be returned.
     * @return   The entry at the given index.
     */
    public Object getEntryAt(int anIndex);
    
    /**
     * Return the string representation of this value on the given position.
     * 
     * This method has to be able to return values for an index from 
     * <code>0</code> to <code>(n -1)</code>, where <code>n</code> is the 
     * value returned by {@link #getNumberEntries()}.
     * 
     * @param    anIndex    The index of the entry to be returned.
     * @return   The string representation of the entry at the given index.
     */
    public String getEntryAsString(int anIndex);
    
	/**
     * Set the values within this value.
     * 
     * This method has to be able to accept values for an index from 
     * <code>0</code> to <code>(n -1)</code>, where <code>n</code> is the 
     * value returned by {@link #getNumberEntries()}.
     *
	 * @param    anIndex   The index of the entry to be set.
	 * @param    anEntry   The entry to be set at the given index.
	 */
	public void setEntryAt(int anIndex, Object anEntry);
	
	/**
     * Return a string for displaying this value in the user interface.
     * 
	 * @return    A implementation specific string representation of 
     *            this value containing all entires.
	 */
	public String getAllEntriesAsString();
	
	/**
     * Return the number of entries in this value.
     * 
     * The generic UI cannot know, how many entries are in this instance.
     * This method return the number of elements held in this instance. The
     * {@link #getEntryAt(int)} method has to be able to return values for
     * all indices from <code>0</code> to <code>(n -1)</code>, where 
     * <code>n</code> is the value returned by this method.
     * 
	 * @return    The number of entries in this value.
	 */
	public int getNumberEntries();
	
	public Value projectOn(int[] indices);
	
	
		/**
	 * Each value of the described data might consist of one or more entries. 
     * 
     * This method gives you the names of those entries (e.g. Soll-wert 
     * and Ist-wert). Those names are to be used within GUIs to inform 
     * the user of which entries can be found in a certain value.<br /><br />
     * 
	 * <b>NOTE</b>: The entry names should be always returned in the same order
     * as the actual entries are stored in the value, because access to the 
     * values entries is provided via index only.
     * 
	 * @return    The entry names of the values of the described data,
     *            must not be <code>null</code>.
	 */
	public String[] getEntryNames();
	
	/**
	 * the name of the entry with the given index
	 */
	public String  getEntryNameAt(int idx);

	/**
	 * sets the names for this values entries
	 * @param theNames	The Names to be set
	 * @exception ArrayIndexOutOfBoundsException if the number of given names does not match the number of entries
	 */
	public void setEntryNames(String[] theNames);

	/**
	 * There has to be a way to compare two Values by their meaning (entires, actual values whatever).
	 * Just in the same way, you can compare two strings. Both maybe differnt objects, but they equal eachother
	 * as long as they have the same string value.
	 * This method has to act just like that ! Of course your implementation of this interface can override
	 * equals() and hashcode(), but this interface cannot force you to do so. Still this interface wants to provide
	 * access to an "equals" method which compares the content, not the references. This is what this method is for,
	 * If you already plan to override equals() and hashCode(), just call equals() in your implementation of this method.
     * 
     * KHA: So there is actually no need to have this function since every sane
     *      class should have some meaningfull equals Method().
	 */
	public boolean valueEquals(Value another);
	
}
