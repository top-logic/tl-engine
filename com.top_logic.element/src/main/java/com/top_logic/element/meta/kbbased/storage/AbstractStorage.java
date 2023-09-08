/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link AtomicStorage} that can be updated.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractStorage<C extends AbstractStorage.Config<?>> extends AtomicStorage<C> {

	/**
	 * Creates a {@link AbstractStorage} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AbstractStorage(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public void checkUpdate(AttributeUpdate update) throws TopLogicException {
		if (update == null) { // no update -> ok
			return;
		}

		switch (update.getUpdateType()) {
			case TYPE_SET_COLLECTION:
				checkSetValue(update.getObject(), update.getAttribute(), update.getCollectionSetUpdate());
				return;
			case TYPE_SET_SIMPLE:
				checkSetValue(update.getObject(), update.getAttribute(), update.getSimpleSetUpdate());
				return;
			default: // other types are not allowed for collections
				return;
		}
	}

	@Override
	public void update(AttributeUpdate update) throws AttributeException {
		try {
			if (update == null || update.isDisabled() || !update.isChanged()) {
				return;
			}

			TLObject object = update.getObject();
			TLStructuredTypePart attribute = update.getAttribute();

			Object value;
			switch (update.getUpdateType()) {
				case TYPE_SET_COLLECTION:
					value = update.getCollectionSetUpdate();
					break;
				case TYPE_SET_SIMPLE:
					value = update.getSimpleSetUpdate();
					break;
				default: // other types are not allowed for collections
					return;
			}

			if (update.isTouched()) {
				AttributeOperations.touch(object, attribute);
			}

			// Check value
			checkSetValue(object, attribute, value);
			setAttributeValue(object, attribute, value);
		} catch (AttributeException e) {
			throw e;
		} catch (Exception e) {
			throw new AttributeException(e);
		}
	}

	@Override
	public Object getUpdateValue(AttributeUpdate update)
			throws NoSuchAttributeException, IllegalArgumentException, AttributeException {
		TLObject object = update.getObject();
		TLStructuredTypePart attribute = update.getAttribute();
		try {
			Object simpleValue = update.getSimpleSetUpdate();

			checkSetValue(object, attribute, simpleValue);

			return simpleValue;
		} catch (RuntimeException e) {
			throw new IllegalArgumentException("Invalid update for attribute " + attribute);
		}
	}

}
