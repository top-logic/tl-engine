/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.util.Utils;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link StorageImplementation} that creates an overlay of storage attribute with a fallback
 * attribute providing a value, if no explicit value has been set to the storage attribute.
 */
@InApp
public class AttributeWithFallbackStorage extends AbstractStorageBase<AttributeWithFallbackStorage.Config<?>> {

	/**
	 * Configuration options for {@link AttributeWithFallbackStorage}.
	 */
	@TagName("fallback-storage")
	public interface Config<I extends AttributeWithFallbackStorage> extends AbstractStorageBase.Config<I> {

		/**
		 * The name of the attribute to store explicitly set values.
		 * 
		 * <p>
		 * The attribute must be defined in the same type as the attribute with fallback.
		 * </p>
		 */
		String getStorageAttribute();

		/**
		 * The attribute to take fallback values from, if no value was explicitly set.
		 * 
		 * <p>
		 * The attribute must be defined in the same type as the attribute with fallback.
		 * </p>
		 */
		String getFallbackAttribute();

	}

	private TLStructuredTypePart _storageAttr;

	private TLStructuredTypePart _fallbackAttr;

	/**
	 * Creates a {@link AttributeWithFallbackStorage} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AttributeWithFallbackStorage(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public void init(TLStructuredTypePart attribute) {
		super.init(attribute);

		_storageAttr = attribute.getOwner().getPart(getConfig().getStorageAttribute()).getDefinition();
		_fallbackAttr = attribute.getOwner().getPart(getConfig().getFallbackAttribute()).getDefinition();

		assert _storageAttr != attribute.getDefinition() : "A fallback attribute '"
			+ TLModelUtil.qualifiedName(attribute) + "' cannot be defined with itself as storage.";
	}

	/**
	 * The attribute storing explicitly set values.
	 */
	public TLStructuredTypePart getStorageAttr() {
		return _storageAttr;
	}

	/**
	 * The attribute that provides fallback values if no value has been explicitly set.
	 */
	public TLStructuredTypePart getFallbackAttr() {
		return _fallbackAttr;
	}

	@Override
	public void checkUpdate(AttributeUpdate update) throws TopLogicException {
		storage().checkUpdate(update);
	}

	@Override
	public void update(AttributeUpdate update) throws AttributeException {
		storage().update(update);
	}

	@Override
	public Object getAttributeValue(TLObject object, TLStructuredTypePart attribute) {
		Object explicitValue =
			storage().getAttributeValue(object, attribute);
		if (!Utils.isEmpty(explicitValue)) {
			return explicitValue;
		}

		return fallback().getAttributeValue(object, attribute);
	}

	@Override
	public void addAttributeValue(TLObject object, TLStructuredTypePart attribute, Object newEntry)
			throws NoSuchAttributeException, IllegalArgumentException {
		storage().addAttributeValue(object, attribute, newEntry);
	}

	@Override
	public void removeAttributeValue(TLObject object, TLStructuredTypePart attribute, Object oldEntry)
			throws NoSuchAttributeException {
		storage().removeAttributeValue(object, attribute, oldEntry);
	}

	@Override
	protected void internalSetAttributeValue(TLObject object, TLStructuredTypePart attribute, Object newValue)
			throws NoSuchAttributeException, IllegalArgumentException, AttributeException {
		storage().setAttributeValue(object, attribute, newValue);
	}

	private StorageImplementation storage() {
		return AttributeOperations.getStorageImplementation(_storageAttr);
	}

	private StorageImplementation fallback() {
		return AttributeOperations.getStorageImplementation(_fallbackAttr);
	}

}
