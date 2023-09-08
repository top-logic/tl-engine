/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.access.StorageMapping;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * Storage for {@link TLStructuredTypePart}s storing {@link TLObject}s.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class TLItemStorage<C extends TLItemStorage.Config<?>> extends AbstractStorage<C> {

	/**
	 * Configuration of a {@link TLItemStorage}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config<I extends TLItemStorage<?>> extends AbstractStorage.Config<I> {

		/**
		 * Optional configuration of a {@link StorageMapping} which maps the business object value
		 * to a {@link TLObject} database value.
		 */
		@Hidden
		PolymorphicConfiguration<? extends StorageMapping<?>> getStorageMapping();

	}

	private StorageMapping<?> _storageMapping;

	/**
	 * Creates a new {@link TLItemStorage}.
	 */
	@CalledByReflection
	public TLItemStorage(InstantiationContext context, C config) {
		super(context, config);
		_storageMapping = context.getInstance(config.getStorageMapping());
	}

	@Override
	public void init(TLStructuredTypePart attribute) {
		super.init(attribute);
		initStorageMapping(attribute);
	}

	/**
	 * Updates {@link #storageMapping()} based on the given attribute
	 * 
	 * @see #init(TLStructuredTypePart)
	 */
	protected void initStorageMapping(TLStructuredTypePart attribute) {
		TLType type = attribute.getType();
		if (type instanceof TLPrimitive) {
			// Do not override explicitly configured storage mapping.
			if (_storageMapping == null) {
				_storageMapping = ((TLPrimitive) type).getStorageMapping();
			}
		}
	}

	@Override
	public Object getAttributeValue(TLObject object, TLStructuredTypePart attribute) throws AttributeException {
		Object storedReference = getReferencedTLObject(object, attribute);
		storedReference = toBusinessObject(storedReference);
		return storedReference;
	}

	/**
	 * Returns the object which is referenced in the the given attribute of the given object,
	 * without transforming the value to a business object.
	 * 
	 * @param self
	 *        The holder of the referenced object.
	 * @param attribute
	 *        The attribute to get value for.
	 * @return The referenced object or <code>null</code> when currently no object is referenced.
	 * 
	 * @see #storageMapping()
	 */
	protected abstract Object getReferencedTLObject(TLObject self, TLStructuredTypePart attribute);

	@Override
	protected void internalSetAttributeValue(TLObject object, TLStructuredTypePart attribute, Object value)
			throws NoSuchAttributeException, IllegalArgumentException, AttributeException {
		value = toStorageObject(value);
		if (value == getReferencedTLObject(object, attribute)) {
			// no change
			return;
		}

		storeReferencedTLObject(object, attribute, value);
	}

	/**
	 * Actual implementation of
	 * {@link #internalSetAttributeValue(TLObject, TLStructuredTypePart, Object)}.
	 * 
	 * <p>
	 * This method stores the given value as reference for the given attribute in the given
	 * <code>self</code>-item.
	 * </p>
	 * 
	 * @param object
	 *        The holder for the reference.
	 * @param attribute
	 *        The attribute describing the reference.
	 * @param value
	 *        The referenced value. May be <code>null</code>. The value is already transformed to a
	 *        storage object.
	 * 
	 * @see #storageMapping()
	 */
	protected abstract void storeReferencedTLObject(TLObject object, TLStructuredTypePart attribute, Object value)
			throws NoSuchAttributeException, IllegalArgumentException, AttributeException;

	@Override
	protected void checkSetValue(TLObject object, TLStructuredTypePart attribute, Object simpleValue) throws TopLogicException {
		checkType(attribute, simpleValue);
	}

	/**
	 * Checks that the "type" of the value is suitable for the given {@link TLStructuredTypePart}.
	 * 
	 * @param attribute
	 *        The {@link TLStructuredTypePart} to store value for.
	 * @param simpleValue
	 *        The value to store.
	 */
	protected void checkType(TLStructuredTypePart attribute, Object simpleValue) {
		checkCorrectType(attribute, simpleValue, storageMapping());
	}

	/**
	 * Checks that the given value is either <code>null</code> or a {@link TLObject} (or a
	 * {@link KnowledgeObject}) whose {@link TLObject#tType() type} is compatible to the type of the
	 * given {@link TLStructuredTypePart}.
	 * 
	 * @param attribute
	 *        The {@link TLStructuredTypePart} to check valid type for.
	 * @param simpleValue
	 *        The value to store for the given {@link TLStructuredTypePart}. May be
	 *        <code>null</code>.
	 * @param mapping
	 *        A {@link StorageMapping} that is used to map the given <code>simpleValue</code> to the
	 *        actual storage value. May be <code>null</code>.
	 * @throws IllegalArgumentException
	 *         iff the condition is not fulfilled.
	 */
	public static void checkCorrectType(TLStructuredTypePart attribute, Object simpleValue, StorageMapping<?> mapping) {
		checkCorrectType(attribute, simpleValue, false, mapping);
	}

	/**
	 * Variant of {@link #checkCorrectType(TLStructuredTypePart, Object, StorageMapping)} that
	 * allows to ignore it if {@link TLObject#tType()} is null.
	 * 
	 * @param mapping
	 *        A {@link StorageMapping} that is used to map the given <code>simpleValue</code> to the
	 *        actual storage value. May be <code>null</code>.
	 */
	public static void checkCorrectType(TLStructuredTypePart attribute, Object simpleValue,
			boolean ignoreMissingValueType, StorageMapping<?> mapping) {
		if (mapping != null) {
			if (!mapping.isCompatible(simpleValue)) {
				throw failIncompatibleApplicationType(attribute, simpleValue, mapping);
			}
			// Do not get storage object to avoid fetching or creating TLObject on each check.
			return;
		}
		if (simpleValue == null) {
			return;
		}
		TLType expectedType = attribute.getType();
		if (simpleValue instanceof KnowledgeItem) {
			// ensure that also the KB objects can be stored.
			simpleValue = ((KnowledgeItem) simpleValue).getWrapper();
		}
		if (!(simpleValue instanceof TLObject)) {
			throw failNoTLObject(attribute, simpleValue);
		}
		if (!TLModelUtil.isCompatibleInstance(expectedType, simpleValue, ignoreMissingValueType)) {
			throw failIncompatibleValueType(attribute, (TLObject) simpleValue);
		}
	}

	private static IllegalArgumentException failIncompatibleApplicationType(TLStructuredTypePart attribute,
			Object simpleValue, StorageMapping<?> mapping) {
		assert mapping != null;
		StringBuilder incompatibleType = new StringBuilder();
		incompatibleType.append("Storage mapping ");
		incompatibleType.append(mapping);
		incompatibleType.append(" for application type ");
		incompatibleType.append(mapping.getApplicationType());
		incompatibleType.append(" does not support value ");
		incompatibleType.append(simpleValue);
		incompatibleType.append(" as value for attribute ");
		incompatibleType.append(attribute);
		incompatibleType.append(".");
		throw new IllegalArgumentException(incompatibleType.toString());
	}

	private static IllegalArgumentException failNoTLObject(TLStructuredTypePart attribute, Object simpleValue) {
		StringBuilder noTLObject = new StringBuilder();
		noTLObject.append("Attribute '");
		noTLObject.append(attribute);
		noTLObject.append("' only supports values of type '");
		noTLObject.append(attribute.getType());
		noTLObject.append("': Value: '");
		noTLObject.append(simpleValue);
		noTLObject.append("'");
		throw new IllegalArgumentException(noTLObject.toString());
	}

	private static IllegalArgumentException failIncompatibleValueType(TLStructuredTypePart attribute, TLObject simpleValue) {
		StringBuilder wrongType = new StringBuilder();
		wrongType.append("Attribute '");
		wrongType.append(attribute);
		wrongType.append("' only supports values of type '");
		wrongType.append(attribute.getType());
		wrongType.append("': Value: '");
		wrongType.append(simpleValue);
		wrongType.append("' has type '");
		wrongType.append(simpleValue.tType());
		wrongType.append("'");
		throw new IllegalArgumentException(wrongType.toString());
	}

	/**
	 * The instantiated {@link Config#getStorageMapping()}. May be <code>null</code>.
	 */
	protected final StorageMapping<?> storageMapping() {
		return _storageMapping;
	}

	/**
	 * Maps the given business object to a storage value.
	 */
	protected Object toStorageObject(Object businessObject) {
		if (storageMapping() != null) {
			businessObject = storageMapping().getStorageObject(businessObject);
		}
		return businessObject;
	}

	/**
	 * Maps the given storage value to a business object.
	 */
	protected Object toBusinessObject(Object storageValue) {
		if (storageMapping() != null) {
			storageValue = storageMapping().getBusinessObject(storageValue);
		}
		return storageValue;
	}

}

