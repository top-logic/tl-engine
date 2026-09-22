/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage;

import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.shared.io.StringR;
import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.kbbased.AttributeUtil;
import com.top_logic.knowledge.service.db2.FlexAttributeFetch;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.access.StorageMapping;
import com.top_logic.model.access.WithStorageAttribute;
import com.top_logic.model.annotate.util.TLAnnotations;
import com.top_logic.model.config.DatatypeConfig;
import com.top_logic.model.export.EmptyPreloadContribution;
import com.top_logic.model.export.PreloadContribution;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link AbstractStorage} for primitive values directly stored to persistency-layer attributes.
 * 
 * <p>
 * A single-valued attribute is stored as the storage object delivered by its
 * {@link StorageMapping}. A multi-valued attribute is stored as a JSON array in a single string
 * valued persistency-layer attribute.
 * </p>
 * 
 * @implNote The elements of the JSON array are encoded by
 *           {@link #toJson(TLStructuredTypePart, StorageMapping, Collection)} and decoded by
 *           {@link #fromJson(TLStructuredTypePart, StorageMapping, String)}: a {@link Number} is
 *           written as JSON number, a {@link Boolean} as JSON boolean, a {@link String} as JSON
 *           string, a {@link Date} as JSON number holding its {@link Date#getTime() epoch
 *           milliseconds}, and <code>null</code> as JSON <code>null</code>. Any other storage
 *           object type is rejected with an {@link IllegalArgumentException}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PrimitiveStorage<C extends PrimitiveStorage.Config<?>> extends AbstractStorage<C>
		implements ColumnStorage {

	/**
	 * Configuration options for {@link PrimitiveStorage}.
	 */
	@TagName("primitive-storage")
	public interface Config<I extends PrimitiveStorage<?>> extends AbstractStorage.Config<I>, WithStorageAttribute {
		/**
		 * Property name of {@link #getStorageMapping()}.
		 */
		String STORAGE_MAPPING = "storage-mapping";

		/**
		 * The mapping to apply when loading and storing values.
		 * 
		 * <p>
		 * If not set, the mapping from the target type of the attribute is used.
		 * </p>
		 * 
		 * @see DatatypeConfig#getStorageMapping()
		 */
		@Name(STORAGE_MAPPING)
		PolymorphicConfiguration<StorageMapping<?>> getStorageMapping();

		/** @see #getStorageMapping() */
		void setStorageMapping(PolymorphicConfiguration<StorageMapping<?>> value);
	}

	private String _storageAttribute;

	private StorageMapping<?> _storageMapping;

	private PreloadContribution _preload;

	/**
	 * Creates a {@link PrimitiveStorage} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public PrimitiveStorage(InstantiationContext context, C config) {
		super(context, config);
		_storageMapping = context.getInstance(config.getStorageMapping());
	}

	@Override
	public void init(TLStructuredTypePart attribute) {
		super.init(attribute);

		_storageAttribute = getConfig().getStorageAttribute();
		if (_storageAttribute == null) {
			_storageAttribute = attribute.getName();
		}
		if (_storageMapping == null) {
			_storageMapping = ((TLPrimitive) attribute.getType()).getStorageMapping();
		}

		TLStructuredType ownerType = attribute.getOwner();
		String tableName = TLAnnotations.getTable(ownerType);

		boolean isRowAttribute;
		try {
			MOStructure tableType =
				(MOStructure) attribute.tHandle().getKnowledgeBase().getMORepository().getMetaObject(tableName);
			isRowAttribute = tableType.hasAttribute(_storageAttribute);
		} catch (UnknownTypeException ex) {
			Logger.warn("Storage table '" + tableName + "' for type '" + ownerType + "' does not exist.",
				PrimitiveStorage.class);
			isRowAttribute = false;
		}
		_preload = isRowAttribute ? EmptyPreloadContribution.INSTANCE : FlexAttributeFetch.INSTANCE;
	}

	/**
	 * The {@link StorageMapping} to use when loading and storing values from and to the persistency
	 * layer.
	 */
	private StorageMapping<?> getStorageMapping() {
		return _storageMapping;
	}

	@Override
	public PreloadContribution getPreload() {
		return _preload;
	}

	@Override
	public String getStorageAttribute() {
		return _storageAttribute;
	}

	@Override
	public Object getAttributeValue(TLObject object, TLStructuredTypePart attribute)
			throws AttributeException {
		Object storageValue = object.tGetData(_storageAttribute);
		StorageMapping<?> storageMapping = getStorageMapping();
		if (isMultiple(attribute)) {
			return fromJson(attribute, storageMapping, (String) storageValue);
		} else {
			return storageMapping.getBusinessObject(storageValue);
		}
	}

	private boolean isMultiple(TLStructuredTypePart attribute) {
		return attribute.isMultiple();
	}

	/**
	 * Decodes the JSON array written by
	 * {@link #toJson(TLStructuredTypePart, StorageMapping, Collection)}.
	 * 
	 * <p>
	 * A JSON number is decoded to a {@link Long}, if its literal is integral and fits into a
	 * {@link Long}, and to a {@link Double} otherwise. If the application type of the given
	 * {@link StorageMapping} is a {@link Date}, a JSON number is interpreted as {@link Date#getTime()
	 * epoch milliseconds} of a {@link Date}. A JSON <code>null</code> is decoded to a
	 * <code>null</code> element.
	 * </p>
	 * 
	 * @param attribute
	 *        The attribute being read, decides whether the result is ordered.
	 * @param storageMapping
	 *        The mapping transforming storage objects to business objects.
	 * @param storageValue
	 *        The stored JSON array, may be <code>null</code> or empty.
	 * @return The decoded collection of business objects.
	 */
	private Collection<Object> fromJson(TLStructuredTypePart attribute, StorageMapping<?> storageMapping,
			String storageValue) {
		Collection<Object> result = attribute.isOrdered() ? new ArrayList<>() : new HashSet<>();
		if (!StringServices.isEmpty(storageValue)) {
			JsonReader json = new JsonReader(new StringR(storageValue));
			try {
				json.beginArray();
				while (json.hasNext()) {
					switch (json.peek()) {
						case NUMBER:
							// Note: Reading the literal keeps the precision of a long value that
							// cannot be represented exactly as double.
							Object numberValue = toNumberValue(storageMapping, json.nextString());
							result.add(storageMapping.getBusinessObject(numberValue));
							break;
						case BOOLEAN:
							result.add(storageMapping.getBusinessObject(json.nextBoolean()));
							break;
						case STRING:
							result.add(storageMapping.getBusinessObject(json.nextString()));
							break;
						case NULL:
							json.nextNull();
							result.add(storageMapping.getBusinessObject(null));
							break;
						default:
							json.skipValue();
					}
				}
				json.endArray();
			} catch (IOException ex) {
				// Should not happen.
				throw new IOError(ex);
			}
		}
		return result;
	}

	/**
	 * Converts the literal of a JSON number to the storage object expected by the given
	 * {@link StorageMapping}.
	 * 
	 * @param storageMapping
	 *        The mapping that finally transforms the result to a business object.
	 * @param literal
	 *        The literal of the JSON number.
	 * @return A {@link Date}, if the application type of the given mapping is a {@link Date}, the
	 *         numeric value of the literal otherwise.
	 */
	private Object toNumberValue(StorageMapping<?> storageMapping, String literal) {
		Number number = parseNumber(literal);
		if (Date.class.isAssignableFrom(storageMapping.getApplicationType())) {
			return new Date(number.longValue());
		}
		return number;
	}

	/**
	 * Parses the literal of a JSON number to the most precise matching value type.
	 * 
	 * @param literal
	 *        The literal of the JSON number.
	 * @return A {@link Long}, if the literal denotes an integral value that fits into a
	 *         {@link Long}, a {@link Double} otherwise.
	 */
	private static Number parseNumber(String literal) {
		try {
			return Long.valueOf(literal);
		} catch (NumberFormatException ex) {
			return Double.valueOf(literal);
		}
	}

	@Override
	protected void checkSetValue(TLObject aMetaAttributed, TLStructuredTypePart attribute, Object value)
			throws TopLogicException {
		// Remark: type should be checked be sub classes
		if (aMetaAttributed != null) {
			// Check attribute definition
			AttributeUtil.checkHasAttribute(aMetaAttributed, attribute);

			StorageMapping<?> mapping = getStorageMapping();
			if (isMultiple(attribute) && value instanceof Collection<?>) {
				for (Object element : (Collection<?>) value) {
					checkSingleValue(attribute, mapping, element);
				}
			} else {
				checkSingleValue(attribute, mapping, value);
			}
		}
	}

	private void checkSingleValue(TLStructuredTypePart attribute, StorageMapping<?> mapping, Object aValue) {
		if (!mapping.isCompatible(aValue)) {
			throw new IllegalArgumentException("Value '" + aValue + "'"
					+ (aValue != null ? " (" + aValue.getClass().getName() + ")" : "") + " can not be converted by '"
					+ mapping.getClass().getName() + "' to value of type '" + mapping.getApplicationType().getName()
					+ "' for attribute " + attribute);
		}
	}

	@Override
	public void internalSetAttributeValue(TLObject aMetaAttributed, TLStructuredTypePart attribute, Object value)
			throws NoSuchAttributeException, IllegalArgumentException, AttributeException {

		StorageMapping<?> storageMapping = getStorageMapping();

		if (isMultiple(attribute)) {
			Collection<?> values = toCollection(value);
			String stringValue = toJson(attribute, storageMapping, values);
			aMetaAttributed.tSetData(_storageAttribute, stringValue);
		} else {
			Object persistentValue = storageMapping.getStorageObject(value);
			aMetaAttributed.tSetData(_storageAttribute, persistentValue);
		}
		AttributeOperations.touch(aMetaAttributed, attribute);
	}

	private Collection<?> toCollection(Object value) {
		return value instanceof Collection<?> ? (Collection<?>) value : Collections.singletonList(value);
	}

	/**
	 * Encodes the given values as JSON array of their storage objects.
	 * 
	 * <p>
	 * A {@link Number} is written as JSON number, a {@link Boolean} as JSON boolean, a
	 * {@link String} as JSON string, a {@link Date} as JSON number holding its
	 * {@link Date#getTime() epoch milliseconds}, and <code>null</code> as JSON <code>null</code>.
	 * </p>
	 * 
	 * @param attribute
	 *        The attribute being written, used for error reporting.
	 * @param storageMapping
	 *        The mapping transforming business objects to storage objects.
	 * @param values
	 *        The business objects to encode.
	 * @return The JSON array to store.
	 * @throws IllegalArgumentException
	 *         If a storage object has a type that cannot be encoded.
	 */
	private String toJson(TLStructuredTypePart attribute, StorageMapping<?> storageMapping, Collection<?> values) {
		StringBuilder buffer = new StringBuilder();
		try (JsonWriter json = new JsonWriter(buffer)) {
			json.beginArray();
			for (Object element : values) {
				Object persistentValue = storageMapping.getStorageObject(element);

				if (persistentValue == null) {
					json.nullValue();
				} else if (persistentValue instanceof Number number) {
					json.value(number);
				} else if (persistentValue instanceof Boolean booleanValue) {
					json.value(booleanValue.booleanValue());
				} else if (persistentValue instanceof String stringValue) {
					json.value(stringValue);
				} else if (persistentValue instanceof Date date) {
					json.value(date.getTime());
				} else {
					throw new IllegalArgumentException("Value '" + persistentValue + "' ("
						+ persistentValue.getClass().getName() + ") delivered by '"
						+ storageMapping.getClass().getName() + "' cannot be stored in attribute " + attribute
						+ ", only numbers, booleans, strings and dates are supported.");
				}
			}
			json.endArray();
		} catch (IOException ex) {
			// Should not happen.
			throw new IOError(ex);
		}

		return buffer.toString();
	}

	@Override
	public void addAttributeValue(TLObject object, TLStructuredTypePart attribute, Object aValue)
			throws NoSuchAttributeException, IllegalArgumentException, AttributeException {
		checkIncrementalChange(object, attribute, aValue);

		Collection<?> oldValue = (Collection<?>) getAttributeValue(object, attribute);
		Collection<Object> newValue;
		if (oldValue instanceof List) {
			newValue = new ArrayList<>(oldValue);
		} else {
			newValue = new LinkedHashSet<>(oldValue);
		}
		boolean changed = newValue.add(aValue);
		if (changed) {
			internalSetAttributeValue(object, attribute, newValue);
		}
	}

	@Override
	public void removeAttributeValue(TLObject object, TLStructuredTypePart attribute, Object aValue)
			throws NoSuchAttributeException, AttributeException {
		checkIncrementalChange(object, attribute, aValue);

		Collection<?> oldValue = (Collection<?>) getAttributeValue(object, attribute);
		Collection<Object> newValue;
		if (oldValue instanceof List) {
			newValue = new ArrayList<>(oldValue);
		} else {
			newValue = new LinkedHashSet<>(oldValue);
		}
		boolean changed = newValue.remove(aValue);
		if (changed) {
			internalSetAttributeValue(object, attribute, newValue);
		}
	}

	private void checkIncrementalChange(TLObject object, TLStructuredTypePart attribute, Object aValue) {
		AttributeUtil.checkHasAttribute(object, attribute);
		checkMultiple(attribute);
		checkSingleValue(attribute, getStorageMapping(), aValue);
	}
	private void checkMultiple(TLStructuredTypePart attribute) {
		if (!isMultiple(attribute)) {
			throw new IllegalArgumentException("Attribute '" + attribute + "' is not multiple.");
		}
	}

}
