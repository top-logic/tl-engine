/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.StorageImplementation;
import com.top_logic.model.TLFormObjectBase;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Base class for derived {@link StorageImplementation}s.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractDerivedStorage<C extends AbstractDerivedStorage.Config<?>> extends AtomicStorage<C> {

	/**
	 * Creates a {@link AbstractDerivedStorage}.
	 */
	public AbstractDerivedStorage(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public boolean isReadOnly() {
		return true;
	}

	@Override
	public Object getFormValue(TLFormObjectBase formObject, TLStructuredTypePart part) {
		return getAttributeValue(formObject, part);
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
	public void internalSetAttributeValue(TLObject aMetaAttributed, TLStructuredTypePart attribute, Object aValue)
			throws NoSuchAttributeException, IllegalArgumentException, AttributeException {
		throw new AttributeException("Setting of values is forbidden for calculated attribute '" + attribute + "'.");

	}

}
