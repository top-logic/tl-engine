/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model;

import java.util.List;

import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.util.model.check.InstanceCheck;

/**
 * Implementation of an attribute of a {@link TLObject}.
 * 
 * @see TLStructuredTypePart#getStorageImplementation()
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface StorageDetail {

	/**
	 * Whether this {@link StorageDetail} does not support storing values.
	 * <p>
	 * In a read-only {@link StorageDetail}, modifications are not allowed.
	 * </p>
	 */
	boolean isReadOnly();

	/**
	 * Get the values of the given attribute for a given object.
	 * 
	 * @param object
	 *        The object to take the value from.
	 * @param attribute
	 *        The attribute to access.
	 * @return the values. May be empty but not <code>null</code>.
	 */
	public Object getAttributeValue(TLObject object, TLStructuredTypePart attribute);

	/**
	 * Replace the current values with the given ones for an object.
	 * 
	 * @param object
	 *        the object. Must not be <code>null</code>.
	 * @param attribute
	 *        The attribute to access.
	 * @param aValues
	 *        a Collection of values. May be empty or <code>null</code> (in that case an empty
	 *        collection will be returned as value in #getAttributeValues())
	 * @throws NoSuchAttributeException
	 *         If this is not an attribute of the given object.
	 * @throws IllegalArgumentException
	 *         if some of the values do not match constraints
	 */
	public void setAttributeValue(TLObject object, TLStructuredTypePart attribute, Object aValues)
			throws NoSuchAttributeException, IllegalArgumentException;

	/**
	 * Add a value to the value collection in the given attribute of the given object.
	 * 
	 * @param object
	 *        The object to modify.
	 * @param attribute
	 *        The attribute to access.
	 * @param aValue
	 *        the value
	 * @throws NoSuchAttributeException
	 *         If this is not an attribute of the given object.
	 * @throws IllegalArgumentException
	 *         if the argument does not match constraints
	 */
	public void addAttributeValue(TLObject object, TLStructuredTypePart attribute, Object aValue)
			throws NoSuchAttributeException, IllegalArgumentException;

	/**
	 * Remove a value from the value collection for a given object.
	 * 
	 * @param object
	 *        the object object. Must not be <code>null</code>.
	 * @param attribute
	 *        The attribute to access.
	 * @param aValue
	 *        the value
	 * @throws NoSuchAttributeException
	 *         If this is not an attribute of the given object.
	 */
	public void removeAttributeValue(TLObject object, TLStructuredTypePart attribute, Object aValue)
			throws NoSuchAttributeException;

	/**
	 * Add constraints for the given attribute.
	 * 
	 * @param attribute
	 *        The attribute to create checks for.
	 * @param checks
	 *        List to add checks to.
	 */
	void addConstraints(TLStructuredTypePart attribute, List<InstanceCheck> checks);

}
