/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.AbstractStorageBase;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.StorageImplementation;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link StorageImplementation} that directly stores a value, even if its getter implementation
 * calculates its values and therefore cannot store updates.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DirectStorage<C extends DirectStorage.Config<?>> extends AbstractStorageBase<C> {

	/**
	 * Configuration options for {@link DirectStorage}.
	 */
	@TagName("direct-storage")
	public interface Config<I extends DirectStorage<?>> extends AbstractStorageBase.Config<I> {

		/**
		 * The {@link StorageImplementation} that is used to lookup default values.
		 */
		PolymorphicConfiguration<StorageImplementation> getDefaultStorage();

	}

	private final StorageImplementation _defaultStorage;

	/**
	 * Creates a {@link DirectStorage} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DirectStorage(InstantiationContext context, C config) {
		super(context, config);
		_defaultStorage = context.getInstance(config.getDefaultStorage());
	}

	@Override
	public Object getAttributeValue(TLObject object, TLStructuredTypePart attribute)
			throws AttributeException {

		Object theValue = object.tGetData(attribute.getName());
		if (theValue == null) {
			theValue = _defaultStorage.getAttributeValue(object, attribute);
		}
		return theValue;
	}

	@Override
	public void internalSetAttributeValue(TLObject aMetaAttributed, TLStructuredTypePart attribute, Object aValue)
			throws NoSuchAttributeException, IllegalArgumentException, AttributeException {

		aMetaAttributed.tSetData(attribute.getName(), aValue);
		AttributeOperations.touch(aMetaAttributed, attribute);
	}

	@Override
	public void checkUpdate(AttributeUpdate update) {
		return;
	}

	@Override
	public void update(AttributeUpdate update)
			throws AttributeException {
		// Ignore.
	}

	@Override
	public void addAttributeValue(TLObject object, TLStructuredTypePart attribute, Object aValue)
			throws NoSuchAttributeException, IllegalArgumentException, AttributeException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getUpdateValue(AttributeUpdate update)
			throws NoSuchAttributeException, IllegalArgumentException, AttributeException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeAttributeValue(TLObject object, TLStructuredTypePart attribute, Object aValue)
			throws NoSuchAttributeException, AttributeException {
		throw new UnsupportedOperationException();
	}

}
