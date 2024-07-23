/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta;

import static com.top_logic.model.util.TLModelUtil.*;

import java.util.Collection;

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
	 * Initializes the base value for an edit operation.
	 * 
	 * @param object
	 *        The object being edited.
	 * @param attribute
	 *        The attribute being edited.
	 * @param update
	 *        The {@link AttributeUpdate} representing the edit operation.
	 * 
	 * @see AttributeUpdate#setValue(Object)
	 */
	default void initUpdate(TLObject object, TLStructuredTypePart attribute, AttributeUpdate update) {
		update.setValue(object.tValue(attribute));
	}

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
