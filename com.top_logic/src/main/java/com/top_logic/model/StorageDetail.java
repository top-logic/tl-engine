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
 * Methods from <code>com.top_logic.element.meta.StorageImplementation</code> that are needed in
 * "com.top_logic".
 * <p>
 * These methods are used by the transient {@link TLModel} which is used by the
 * <code>com.top_logic.element.model.generate.WrapperGenerator</code>.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface StorageDetail {

	/**
	 * Whether the attribute is read-only and therefore does not support storing values.
	 */
	boolean isReadOnly();

	/**
	 * Retrieves the actual value for the given attribute of the given object.
	 * 
	 * @param object
	 *        The object to take the value from.
	 * @param attribute
	 *        The attribute to access.
	 * @return the values. May be empty but not <code>null</code>.
	 */
	public Object getAttributeValue(TLObject object, TLStructuredTypePart attribute);

	/**
	 * Replace the current value of the given attribute with the given new value.
	 * 
	 * @param object
	 *        The object to modify.
	 * @param attribute
	 *        The attribute to access.
	 * @param newValue
	 *        The new value to set, may be <code>null</code> to clear the current value.
	 * @throws NoSuchAttributeException
	 *         if this is not an attribute of aMetaAttributed
	 * @throws IllegalArgumentException
	 *         if some of the values do not match constraints
	 */
	public void setAttributeValue(TLObject object, TLStructuredTypePart attribute, Object newValue)
			throws NoSuchAttributeException, IllegalArgumentException;

	/**
	 * Adds a value to the a collection of values.
	 * 
	 * <p>
	 * Must only be called for an attribute that supports multiple values.
	 * </p>
	 * 
	 * @param object
	 *        The object to modify.
	 * @param attribute
	 *        The attribute to access.
	 * @param newEntry
	 *        The new entry to add to the current values of the given attribute.
	 * @throws NoSuchAttributeException
	 *         if this is not an attribute of aMetaAttributed
	 * @throws IllegalArgumentException
	 *         if the argument does not match constraints
	 */
	public void addAttributeValue(TLObject object, TLStructuredTypePart attribute, Object newEntry)
			throws NoSuchAttributeException, IllegalArgumentException;

	/**
	 * Remove a value from the collection of values for a given attribute.
	 * 
	 * <p>
	 * Must only be called for an attribute that supports multiple values.
	 * </p>
	 * 
	 * @param object
	 *        The object to modify.
	 * @param attribute
	 *        The attribute to access.
	 * @param oldEntry
	 *        The entry to remove from the collection of values for the given attribute.
	 * @throws NoSuchAttributeException
	 *         if this is not an attribute of aMetaAttributed
	 */
	public void removeAttributeValue(TLObject object, TLStructuredTypePart attribute, Object oldEntry)
			throws NoSuchAttributeException;

	/**
	 * Retrieves constraints for the given attribute.
	 * 
	 * @param attribute
	 *        The attribute to create checks for.
	 * @param checks
	 *        List to which the implementation can add checks to.
	 */
	void addConstraints(TLStructuredTypePart attribute, List<InstanceCheck> checks);

	/**
	 * The value for this attribute in the given (transient) form object.
	 */
	Object getFormValue(TLFormObjectBase formObject, TLStructuredTypePart part);

}
