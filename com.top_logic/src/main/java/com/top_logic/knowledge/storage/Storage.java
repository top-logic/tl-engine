/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.storage;

import java.util.Collection;

import com.top_logic.basic.NamedConstant;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.meta.MOStructure;

/**
 * A storage of {@link DataObject}s that are identified by a complex key.
 * 
 * @see #getStorageType() For the type of objects stored in this storage.
 * @see MOStructure#getPrimaryKey() for the index vector of a {@link Storage}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Storage {

	/**
	 * Wildcard value that can be used in {@link Storage#query(DataObject)} to match more than
	 * one {@link DataObject}.
	 */
	NamedConstant ANY = new NamedConstant("any");
	
	/**
	 * The type of objects that can be stored in this storage.
	 */
	public MOStructure getStorageType();

	public DataObject createKey();
	
	public DataObject createObject(DataObject key);
	
	/**
	 * Finds the object that is indexed with the given key values.
	 * 
	 * <p>
	 * The object returned from this method has exactly the values in the given
	 * key value map for its {@link MOStructure#getPrimaryKey() key
	 * attributes}. A <code>null</code> value in the given key value map is
	 * interpreted as a simple value. If one of the given key values is
	 * <code>null</code>, an object is only returned from this method, if it was
	 * stored with a <code>null</code> in its key vector.
	 * </p>
	 * 
	 * @param key
	 *        A map of {@link MOAttribute} to {@link Object} that provides
	 *        values for {@link MOStructure#getPrimaryKey() key attributes}
	 *        of this storage type (see {@link #getStorageType()}).
	 * 
	 * @return The object identified with the given key values, or
	 *         <code>null</code>, if no such object exists in this storage.
	 */
	public DataObject get(DataObject key);

	/**
	 * Finds all objects that are identified by the given key values.
	 * 
	 * @param key
	 *        A map of {@link MOAttribute} to {@link Object} that provides
	 *        values for {@link MOStructure#getPrimaryKey() key attributes}
	 *        of this storage type (see {@link #getStorageType()}). The value
	 *        {@link #ANY} serves as wildcard value.
	 * 
	 * @return A list of {@link DataObject}s that match the given search
	 *         criterion.
	 */
	public Collection query(DataObject key);

	/**
	 * Stores the given object into this storage.
	 * 
	 * @param object
	 *        The object to store.
	 */
	public void store(DataObject object);

}
