/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta;

import static com.top_logic.model.util.TLModelUtil.*;

import java.util.Collection;

import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.model.StorageDetail;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.export.PreloadContribution;
import com.top_logic.model.export.PreloadOperation;
import com.top_logic.util.error.TopLogicException;

/**
 * Model operations that can be performed on behalf of a {@link TLStructuredTypePart}.
 * 
 * <p>
 * Note: When implementing this interface, use at least {@link AbstractStorageBase} as base class.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface StorageImplementation extends StorageDetail, Unimplementable {

	/**
	 * Initialization method called immediately after construction.
	 * 
	 * @param attribute
	 *        The {@link TLStructuredTypePart} this {@link StorageImplementation} is created for.
	 */
	void init(TLStructuredTypePart attribute);

	/**
	 * Check if a given update should be performed, i.e. does not violate some constraints.
	 * 
	 * @param update
	 *        The value to set.
	 * 
	 * @throws TopLogicException
	 *         If the update is not valid and the problem can be explained to the user.
	 * @throws IllegalArgumentException
	 *         If the operation cannot be performed due to to invalid program logic, e.g. invalid
	 *         objects, values of incompatible types, undefined attributes.
	 */
	public void checkUpdate(AttributeUpdate update)
			throws TopLogicException;

	/**
	 * Update the attribute value taking the correct update from the container.
	 * 
	 * @param update
	 *        The value to set.
	 * 
	 * @throws AttributeException
	 *         if the update fails
	 */
	public void update(AttributeUpdate update) throws AttributeException;

	/**
	 * Add a value to the value collection for a given MetaAttributed object.
	 * 
	 * @param object
	 *        The object to modify.
	 * @param attribute
	 *        The attribute to access.
	 * @param aValue
	 *        the value
	 * @throws NoSuchAttributeException
	 *         if this is not an attribute of aMetaAttributed
	 * @throws IllegalArgumentException
	 *         if the argument does not match constraints
	 * @throws AttributeException
	 *         if something goes wrong setting the value
	 */
	public void addAttributeValue(TLObject object, TLStructuredTypePart attribute, Object aValue) throws NoSuchAttributeException,
			IllegalArgumentException, AttributeException;

	/**
	 * Get the values of this CollectionMetaAttribute for a given MetaAttributed object.
	 * 
	 * @param object
	 *        The object to take the value from.
	 * @param attribute
	 *        The attribute to access.
	 * @return the values. May be empty but not <code>null</code>.
	 * @throws AttributeException
	 *         if getting one of th4e values fails
	 */
	public Object getAttributeValue(TLObject object, TLStructuredTypePart attribute) throws AttributeException;

	/**
	 * Get the simulated result of a given update.
	 * 
	 * @param update
	 *        The value to get.
	 * 
	 * @return theValue that would result in performing the update.
	 * @throws AttributeException
	 *         if the access to the attribute value fails
	 * @throws IllegalArgumentException
	 *         if aContext or anUpdate is <code>null</code>
	 * @throws NoSuchAttributeException
	 *         if this is not an attribute of aContext
	 */
	public Object getUpdateValue(AttributeUpdate update)
			throws NoSuchAttributeException, IllegalArgumentException, AttributeException;

	/**
	 * Remove a value from the value collection for a given MetaAttributed object.
	 * 
	 * @param object
	 *        the object object. Must not be <code>null</code>.
	 * @param attribute
	 *        The attribute to access.
	 * @param aValue
	 *        the value
	 * @throws NoSuchAttributeException
	 *         if this is not an attribute of aMetaAttributed
	 * @throws AttributeException
	 *         if something goes wrong removing the value
	 */
	public void removeAttributeValue(TLObject object, TLStructuredTypePart attribute, Object aValue) throws NoSuchAttributeException,
			AttributeException;

	/**
	 * Replace the current values with the given ones for a MetaAttributed.
	 * 
	 * @param object
	 *        the object. Must not be <code>null</code>.
	 * @param attribute
	 *        The attribute to access.
	 * @param aValues
	 *        a Collection of values. May be empty or <code>null</code> (in that case an empty
	 *        collection will be returned as value in #getAttributeValues())
	 * @throws NoSuchAttributeException
	 *         if this is not an attribute of aMetaAttributed
	 * @throws IllegalArgumentException
	 *         if some of the values do not match constraints
	 * @throws AttributeException
	 *         if something goes wrong setting the values
	 */
	public void setAttributeValue(TLObject object, TLStructuredTypePart attribute, Object aValues) throws NoSuchAttributeException,
			IllegalArgumentException, AttributeException;

	/**
	 * {@link PreloadContribution} that contribute {@link PreloadOperation} to load the values of
	 * this {@link TLStructuredTypePart} for a given set of {@link TLObject}s.
	 */
	PreloadContribution getPreload();

	/**
	 * {@link PreloadContribution} that contribute {@link PreloadOperation} to load the
	 * {@link TLObject}s of this {@link TLStructuredTypePart} for a given set of values.
	 */
	PreloadContribution getReversePreload();

	/**
	 * Whether "live {@link Collection}s" are supported.
	 * 
	 * @see #getLiveCollection(TLObject, TLStructuredTypePart)
	 */
	default boolean supportsLiveCollections() {
		return false;
	}

	/**
	 * Creates a "live collection".
	 * <p>
	 * Changes to a "live collection" directly change the attribute value. The caller has to take
	 * care of the transaction handling.
	 * </p>
	 * 
	 * @param object
	 *        Is not allowed to be null.
	 * @param attribute
	 *        Is not allowed to be null.
	 * @return Never null.
	 * @throws UnsupportedOperationException
	 *         If it is not possible to create a live-collection for the given attribute.
	 * 
	 * @implNote When implementing, {@link #supportsLiveCollections()} must also be adjusted to
	 *           return <code>true</code>.
	 */
	default Collection<?> getLiveCollection(TLObject object, TLStructuredTypePart attribute) {
		throw new UnsupportedOperationException(
			"There is no live-collection for attribute " + qualifiedName(attribute) + ".");
	}

}
