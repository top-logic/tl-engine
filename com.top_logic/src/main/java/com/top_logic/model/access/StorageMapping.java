/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.access;


/**
 * Conversion back and forth from storage values to business objects.
 * 
 * @param <T>
 *        The application value type.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface StorageMapping<T> {

	/**
	 * The dynamic type of the processed application values.
	 */
	Class<? extends T> getApplicationType();

    /**
     * Converts the given object into a Business Object
     * 
     * @param aStorageObject
	 *        The value loaded from the database.
	 * @return The value to hand out to the application.
     */
	public T getBusinessObject(Object aStorageObject);

    /**
     * Converts the given business object in an object (a primitive type) 
     * able to store in the Knowledgebase.
     * 
     * @param aBusinessObject
	 *        The value provided from the application.
	 * @return The value to store to the database.
	 */
    public Object getStorageObject(Object aBusinessObject);

	/**
	 * Whether this {@link StorageMapping} can transform the given business object to a storage
	 * object.
	 * 
	 * @param businessObject
	 *        The value provided from the application.
	 */
	boolean isCompatible(Object businessObject);

}
