/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.access;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.dob.AttributeStorage;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.attr.storage.DBAttributeStorageImpl;
import com.top_logic.dob.ex.IncompatibleTypeException;
import com.top_logic.model.TLPrimitive;

/**
 * {@link AttributeStorage} that can use a {@link StorageMapping} of a {@link TLPrimitive} to
 * directly fill the database cache with the application value.
 */
public class PrimitiveColumnStorage extends DBAttributeStorageImpl
		implements ConfiguredInstance<PrimitiveColumnStorage.Config<?>> {

	/**
	 * Configuration options for {@link PrimitiveColumnStorage}.
	 */
	@TagName("primitive-column-storage")
	public interface Config<I extends PrimitiveColumnStorage> extends PolymorphicConfiguration<I> {

		/**
		 * The primitive type mapping used for filling the cache value.
		 * 
		 * @see TLPrimitive#getStorageMapping()
		 */
		PolymorphicConfiguration<? extends StorageMapping<?>> getStorageMapping();

	}

	private final Config<?> _config;

	private final StorageMapping<Object> _mapping;

	/**
	 * Creates a {@link PrimitiveColumnStorage} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public PrimitiveColumnStorage(InstantiationContext context, Config<?> config) {
		_config = config;
		_mapping = createMapping(context, config);
	}

	private StorageMapping<Object> createMapping(InstantiationContext context, Config<?> config) {
		@SuppressWarnings("unchecked")
		StorageMapping<Object> result = (StorageMapping<Object>) context.getInstance(config.getStorageMapping());
		return result;
	}

	@Override
	public Config<?> getConfig() {
		return _config;
	}

	@Override
	protected Object fromCacheToDBValue(MOAttribute attribute, Object cacheValue) {
		return _mapping.getStorageObject(cacheValue);
	}

	@Override
	protected Object fromDBToCacheValue(MOAttribute attribute, Object dbValue) {
		return _mapping.getBusinessObject(dbValue);
	}

	@Override
	public void checkAttributeValue(MOAttribute attribute, DataObject data, Object newValue)
			throws DataObjectException {
		if (!_mapping.isCompatible(newValue)) {
			throw new IncompatibleTypeException("Value '"
				+ newValue + "' " + (newValue == null ? ""
					: "of class '" + newValue.getClass().getName()
						+ "' ")
				+ "is not compatible with storage mapping '" + _mapping.getClass().getName() + "' for column '"
				+ attribute.getName() + "' of table '" + attribute.getOwner().getName() + "'.");
		}
	}

}
