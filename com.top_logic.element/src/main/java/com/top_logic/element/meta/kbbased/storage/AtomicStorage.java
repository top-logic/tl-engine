/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.AbstractStorageBase;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.element.meta.StorageImplementation;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link StorageImplementation} of atomic values (no element add and remove).
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AtomicStorage<C extends AtomicStorage.Config<?>> extends AbstractStorageBase<C> {

	/**
	 * Creates a {@link AtomicStorage} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AtomicStorage(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public void addAttributeValue(TLObject object, TLStructuredTypePart attribute, Object aValue)
			throws NoSuchAttributeException, IllegalArgumentException,
			AttributeException {
		throw new AttributeException("Add value not supported.");
	}

	@Override
	public void removeAttributeValue(TLObject object, TLStructuredTypePart attribute, Object aValue)
			throws NoSuchAttributeException, AttributeException {
		throw new AttributeException("Remove value not supported.");
	}

}
