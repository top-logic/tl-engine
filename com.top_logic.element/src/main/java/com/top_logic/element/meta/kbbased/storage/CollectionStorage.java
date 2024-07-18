/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage;

import java.util.Collection;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.meta.AbstractStorageBase;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.StorageImplementation;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link StorageImplementation} of a collection value.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class CollectionStorage<C extends CollectionStorage.Config<?>> extends AbstractStorageBase<C> {

	/**
	 * Creates a {@link CollectionStorage} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public CollectionStorage(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public void checkUpdate(AttributeUpdate update) throws TopLogicException {
		if (update == null) { // no update -> ok
			return;
		}

		switch (update.getUpdateType()) {
			case TYPE_SET_COLLECTION:
				checkSetValues(update.getObject(), update.getAttribute(), update.getCollectionSetUpdate());
				return;
			case TYPE_SET_SIMPLE:
				checkSetValue(update.getObject(), update.getAttribute(), update.getSimpleSetUpdate());
				return;
			default: // other types are not allowed for collections
				return;
		}
	}

	@Override
	protected void checkSetValue(TLObject object, TLStructuredTypePart attribute, Object value) {
		checkSetValues(object, attribute, (Collection<?>) value);
	}

	protected abstract void checkSetValues(TLObject object, TLStructuredTypePart attribute,
			Collection collectionSetUpdate)
			throws TopLogicException;

	protected abstract void checkAddValue(TLObject object, TLStructuredTypePart attribute, Object collectionAddUpdate) throws TopLogicException;

	protected abstract void checkRemoveValue(TLObject object, TLStructuredTypePart attribute, Object collectionRemoveUpdate) throws TopLogicException;

	@Override
	public void update(AttributeUpdate update) throws AttributeException {
		try {
			if (update == null || update.isDisabled() || !update.isChanged()) {
				return;
			}

			TLObject object = update.getObject();
			TLStructuredTypePart attribute = update.getAttribute();
			if (update.isTouched()) {
				AttributeOperations.touch(object, attribute);
			}

			switch (update.getUpdateType()) {
				case TYPE_SET_COLLECTION:
					setAttributeValue(object, attribute, update.getCollectionSetUpdate());
					break;
				default: // other types are not allowed for collections
					break;
			}
		} catch (AttributeException e) {
			throw e;
		} catch (Exception e) {
			throw new AttributeException(e);
		}
	}

	/**
	 * Checks that the last value is not removed from a mandatory attribute.
	 */
	protected final void checkMandatoryRemove(TLStructuredTypePart attribute, Collection<?> values) {
		checkMandatoryRemove(attribute, values.size());
	}

	/**
	 * Checks that the last value is not removed from a mandatory attribute.
	 */
	protected final void checkMandatoryRemove(TLStructuredTypePart attribute, int currentValueSize) {
		if (attribute.isMandatory() && currentValueSize == 1) {
			throw new TopLogicException(

				I18NConstants.ERROR_LAST_VALUE_MUST_NOT_BE_REMOVED_FROM_MANDATORY_ATTRIBUTE__ATTRIBUTE.fill(
					attribute.getName()));
		}
	}

}
