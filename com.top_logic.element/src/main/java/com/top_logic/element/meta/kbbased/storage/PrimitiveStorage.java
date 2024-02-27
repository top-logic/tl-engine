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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.shared.io.StringR;
import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.kbbased.AttributeUtil;
import com.top_logic.knowledge.service.db2.FlexAttributeFetch;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.access.StorageMapping;
import com.top_logic.model.annotate.util.TLAnnotations;
import com.top_logic.model.config.DatatypeConfig;
import com.top_logic.model.export.EmptyPreloadContribution;
import com.top_logic.model.export.PreloadContribution;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link AbstractStorage} for primitive values directly stored to persistency-layer attributes.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PrimitiveStorage<C extends PrimitiveStorage.Config<?>> extends AbstractStorage<C> {

	/**
	 * Configuration options for {@link PrimitiveStorage}.
	 */
	@TagName("primitive-storage")
	public interface Config<I extends PrimitiveStorage<?>> extends AbstractStorage.Config<I> {
		/** Property name of {@link #getStorageAttribute()}. */
		String STORAGE_ATTRIBUTE = "storage-attribute";

		/**
		 * Property name of {@link #getStorageMapping()}.
		 */
		String STORAGE_MAPPING = "storage-mapping";

		/**
		 * The name of the database column which is used to store the value.
		 * 
		 * <p>
		 * If not set, it defaults to the name of the attribute.
		 * </p>
		 */
		@Nullable
		@Name(STORAGE_ATTRIBUTE)
		String getStorageAttribute();

		/**
		 * @see #getStorageAttribute()
		 */
		void setStorageAttribute(String value);

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

		String tableName = TLAnnotations.getTable(attribute.getOwner());
		MOStructure tableType =
			(MOStructure) attribute.tHandle().getKnowledgeBase().getMORepository().getMetaObject(tableName);
		boolean isRowAttribute = tableType.hasAttribute(_storageAttribute);
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
							result.add(storageMapping.getBusinessObject(json.nextDouble()));
							break;
						case BOOLEAN:
							result.add(storageMapping.getBusinessObject(json.nextBoolean()));
							break;
						case STRING:
							result.add(storageMapping.getBusinessObject(json.nextString()));
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
			String stringValue = toJson(storageMapping, values);
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

	private String toJson(StorageMapping<?> storageMapping, Collection<?> values) {
		StringBuilder buffer = new StringBuilder();
		try (JsonWriter json = new JsonWriter(buffer)) {
			json.beginArray();
			for (Object element : values) {
				Object persistentValue = storageMapping.getStorageObject(element);

				if (persistentValue instanceof Number) {
					json.value((Number) persistentValue);
				} else if (persistentValue instanceof Boolean) {
					json.value((Boolean) persistentValue);
				} else {
					json.value((String) persistentValue);
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
