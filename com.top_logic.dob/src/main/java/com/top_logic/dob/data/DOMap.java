/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.data;

import java.util.Iterator;

import com.top_logic.dob.DataObject;

/** This defines a Map of Dataobjects.
 *
 * @author  Marco Perra
 */
public interface DOMap {

    /** Returns an iterator of the values in this map.
     *
     * @return     java.lang.Iterator of DataObjects
     */
    public Iterator keys ();

    /** Returns an iterator of the values in this map.
     *
     * @return     java.lang.Iterator of DataObjects
     */
    public Iterator values ();

    /** Put a new entry to this map. If this map already contains the key value pair
     * nothing happens.
     * True is return if and only if the key/value-pair was added.
     */
    public boolean put (DataObject key, DataObject value);

    /** Remove all key/value-pairs in this map wich first component is
     * equal to the specifed one. If the key doesn´t exists nothing happens.
     */
    public void remove (DataObject key);

    /** Remove the specified key/value-pair from this map. If it not exist nothing happens..
     */
    public void remove (DataObject key, DataObject value);

    /** Returns a DOCollection for the specified key. This means
     * that the specified key maps to the DOCollection of DataObjects.
     * For instance,
     * key -------> value1
     * key -------> value2
     * key -------> value3
     *
     * get(key) ------------> {value1,value2,value3}   (DOCollection)
     */
    public DOCollection get (DataObject key);

    /**
     * Returns the upper bound of the size from the DOCollection of values
     * the key maps.
     * key ------> value_1
     * key ------> value_2
     *  .....
     * key ------> value_getMaxKey()
     *
     * If this map represent a dictionary, getMaxKey() returns 1.
     * If the return value is -1, it means the upper bound is arbitrary.
     * The constraint for a maximum is defined in the interface
     * com.top_logic.mig.dataobjects.meta.MOMap.
     *
     */
    public long getMaxKey ();

    /** Returns the lower bound of the size from the DOCollection of values
     * the key maps.
     *
     * The constraint for a minimum is defined in the interface
     * com.top_logic.mig.dataobjects.meta.MOMap.
     */
    public long getMinKey ();

    /** Returns the upper bound of occurence of a fixed value to wich
     * a key can map. This means how frequently a value is in this map.
     *
     */
    public long getMaxValue ();

    /** Returns the lower bound of occurence of a fixed value.
     */
    public long getMinValue ();
}
