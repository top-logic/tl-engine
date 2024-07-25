/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.kbbased.storage.AbstractStorage;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TransientObject;

/**
 * Dummy {@link StorageImplementation} for {@link TransientObject}s.
 */
public class TransientStorage extends AbstractStorage<AbstractStorageBase.Config<?>> {

	/**
	 * Singleton {@link TransientStorage} instance.
	 */
	public static final TransientStorage INSTANCE =
		new TransientStorage(null, TypedConfiguration.newConfigItem(Config.class));

	/**
	 * Creates a {@link TransientStorage} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TransientStorage(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public void initUpdate(TLObject object, TLStructuredTypePart attribute, AttributeUpdate update) {
		StorageImplementation realStorage = AttributeOperations.getStorageImplementation(attribute);
		if (realStorage != this) {
			realStorage.initUpdate(object, attribute, update);
		} else {
			super.initUpdate(object, attribute, update);
		}
	}

	@Override
	public Object getAttributeValue(TLObject object, TLStructuredTypePart attribute) throws AttributeException {
		return object.tValue(attribute);
	}

	@Override
	public void addAttributeValue(TLObject object, TLStructuredTypePart attribute, Object value)
			throws NoSuchAttributeException, IllegalArgumentException, AttributeException {
		object.tAdd(attribute, value);
	}

	@Override
	public void removeAttributeValue(TLObject object, TLStructuredTypePart attribute, Object value)
			throws NoSuchAttributeException, AttributeException {
		object.tRemove(attribute, value);
	}

	@Override
	protected void internalSetAttributeValue(TLObject object, TLStructuredTypePart attribute, Object value)
			throws NoSuchAttributeException, IllegalArgumentException, AttributeException {
		object.tUpdate(attribute, value);
	}

}
