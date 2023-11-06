/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.form.overlay.TLFormObject;
import com.top_logic.element.meta.kbbased.storage.GenericMandatoryCheck;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.export.EmptyPreloadContribution;
import com.top_logic.model.export.PreloadContribution;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.check.InstanceCheck;

/**
 * Base class for all {@link StorageImplementation}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractStorageBase<C extends AbstractStorageBase.Config<?>> extends AbstractConfiguredInstance<C>
		implements StorageImplementation {

	/**
	 * Base configuration interface for {@link AbstractStorageBase}.
	 */
	public interface Config<I extends StorageImplementation> extends PolymorphicConfiguration<I> {
		// No properties by default.
	}

	/**
	 * Creates a {@link AbstractStorageBase} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AbstractStorageBase(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Override
	public void init(TLStructuredTypePart attribute) {
		// No initialization by default.
	}

	@Override
	public final void setAttributeValue(TLObject object, TLStructuredTypePart attribute, Object value)
			throws NoSuchAttributeException, IllegalArgumentException, AttributeException {

		Object persistentValue = toPersistentValue(value);
		checkSetValue(object, attribute, persistentValue);
		internalSetAttributeValue(object, attribute, persistentValue);
	}

	/**
	 * Check if the given value may be used to update this attribute
	 * 
	 * @param object
	 *        The object to access.
	 * @param attribute
	 *        The attribute to access.
	 * @param value
	 *        The value to set.
	 * @throws TopLogicException
	 *         If the value is not compatible with the given attribute.
	 */
	protected void checkSetValue(TLObject object, TLStructuredTypePart attribute, Object value) {
		// No check by default.
	}

	private static Object toPersistentValue(Object values) {
		if (values instanceof Collection<?>) {
			ArrayList<Object> result = new ArrayList<>();
			for (Object value : (Collection<?>) values) {
				result.add(toPersistentObject(value));
			}
			return result;
		} else {
			return toPersistentObject(values);
		}
	}

	private static Object toPersistentObject(Object value) {
		if (value instanceof TLFormObject) {
			TLObject result = ((TLFormObject) value).getEditedObject();
			if (result == null) {
				throw new IllegalStateException("Object creation not performed before updating persistent values.");
			}
			return result;
		} else {
			return value;
		}
	}

	/**
	 * Implementation of {@link #setAttributeValue(TLObject, TLStructuredTypePart, Object)}
	 */
	protected abstract void internalSetAttributeValue(TLObject object, TLStructuredTypePart attribute, Object aValues)
			throws NoSuchAttributeException, IllegalArgumentException, AttributeException;

	@Override
	public PreloadContribution getPreload() {
		return EmptyPreloadContribution.INSTANCE;
	}

	@Override
	public PreloadContribution getReversePreload() {
		return EmptyPreloadContribution.INSTANCE;
	}

	@Override
	public Unimplementable unimplementable() {
		return null;
	}

	/**
	 * Whether the given value is considered empty.
	 */
	public boolean isEmpty(Object value) {
		if (value == null) {
			return true;
		}
		if (value instanceof CharSequence && ((CharSequence) value).length() == 0) {
			return true;
		}
		if (value instanceof Collection<?> && ((Collection<?>) value).isEmpty()) {
			return true;
		}
		if (value instanceof Map<?, ?> && ((Map<?, ?>) value).isEmpty()) {
			return true;
		}
		return false;
	}

	@Override
	public void addConstraints(TLStructuredTypePart attribute, List<InstanceCheck> checks) {
		if (attribute.isMandatory()) {
			checks.add(new GenericMandatoryCheck(attribute, this));
		}
	}

	@Override
	public final boolean supportsLiveCollections() {
		return supportsLiveCollectionsInternal() && !isReadOnly();
	}

	/**
	 * Must be overridden when implementing
	 * {@link #getLiveCollection(TLObject, TLStructuredTypePart)}.
	 */
	protected boolean supportsLiveCollectionsInternal() {
		return false;
	}

}
