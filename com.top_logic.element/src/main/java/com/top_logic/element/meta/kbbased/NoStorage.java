/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased;

import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.AbstractStorageBase;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.StorageImplementation;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.util.error.TopLogicException;

/**
 * Dummy {@link StorageImplementation} used if instantiating the {@link StorageImplementation} of an
 * attribute fails.
 * 
 * <p>
 * Note: This class must be non-public to prevent the UI from offering it as option for the storage
 * implementation of an attribute.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
final class NoStorage extends AbstractStorageBase<AbstractStorageBase.Config<?>> {

	/**
	 * Singleton {@link NoStorage} instance.
	 */
	public static final NoStorage INSTANCE = new NoStorage();

	private NoStorage() {
		super(null, null);
	}

	@Override
	public void update(AttributeUpdate update)
			throws AttributeException {
		TLStructuredTypePart attribute = update.getAttribute();
		throw unsupported(attribute);
	}

	@Override
	public void internalSetAttributeValue(TLObject object, TLStructuredTypePart attribute, Object aValues)
			throws NoSuchAttributeException, IllegalArgumentException, AttributeException {
		throw unsupported(attribute);
	}

	@Override
	public void removeAttributeValue(TLObject object, TLStructuredTypePart attribute, Object aValue)
			throws NoSuchAttributeException, AttributeException {
		throw unsupported(attribute);
	}

	@Override
	public Object getUpdateValue(AttributeUpdate update)
			throws NoSuchAttributeException, IllegalArgumentException, AttributeException {
		TLStructuredTypePart attribute = update.getAttribute();
		throw unsupported(attribute);
	}

	@Override
	public Object getAttributeValue(TLObject object, TLStructuredTypePart attribute)
			throws AttributeException {
		return null;
	}

	@Override
	public void checkUpdate(AttributeUpdate update)
			throws TopLogicException {
		// Ignore.
	}

	@Override
	public void addAttributeValue(TLObject object, TLStructuredTypePart attribute, Object aValue)
			throws NoSuchAttributeException, IllegalArgumentException, AttributeException {
		throw unsupported(attribute);
	}

	private UnsupportedOperationException unsupported(TLStructuredTypePart attribute) {
		return new UnsupportedOperationException(
			"Invalid storage implementation for attribute '" + attribute + "'.");
	}
}