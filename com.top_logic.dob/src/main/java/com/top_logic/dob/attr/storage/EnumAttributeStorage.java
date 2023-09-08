/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.attr.storage;

import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.ex.IncompatibleTypeException;

/**
 * {@link DBAttributeStorageImpl} that stores enum's.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class EnumAttributeStorage extends DBAttributeStorageImpl
		implements ConfiguredInstance<EnumAttributeStorage.Config> {

	/**
	 * Configuration of an {@link EnumAttributeStorage}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static interface Config extends PolymorphicConfiguration<EnumAttributeStorage> {

		/** Property name of the {@link Config#getEnum() enum} attribute. */
		String ENUM_ATTRIBUTE = "enum";

		/**
		 * The expected enumeration type.
		 * 
		 * <p>
		 * Must be an {@link Class#isEnum() enum} type and {@link ExternallyNamed}.
		 * </p>
		 */
		@Name(ENUM_ATTRIBUTE)
		@Mandatory
		Class<? extends Enum<?>> getEnum();

	}

	/**
	 * Configuration of this {@link EnumAttributeStorage}.
	 */
	private final Config _config;

	/**
	 * Creates a new {@link EnumAttributeStorage}.
	 * 
	 * @param context
	 *        The context to instantiate sub configurations.
	 * @param config
	 *        The configuration for this {@link EnumAttributeStorage}.
	 * 
	 * @throws ConfigurationException
	 *         iff configuration is invalid.
	 */
	public EnumAttributeStorage(InstantiationContext context, Config config) throws ConfigurationException {
		_config = config;
		if (!_config.getEnum().isEnum()) {
			throw new ConfigurationException("Configuration for property '" + Config.ENUM_ATTRIBUTE
				+ "' must be an enum class: was '" + _config.getEnum().getName() + "'.");
		}
		if (!ExternallyNamed.class.isAssignableFrom(_config.getEnum())) {
			throw new ConfigurationException("Configuration for property '" + Config.ENUM_ATTRIBUTE
				+ "' must be an '" + ExternallyNamed.class.getName() + "': was '" + _config.getEnum().getName() + "'.");
		}
	}

	@Override
	public final Config getConfig() {
		return _config;
	}

	@Override
	protected Object fromCacheToDBValue(MOAttribute attribute, Object cacheValue) {
		if (cacheValue == null) {
			return null;
		}
		return ((ExternallyNamed) cacheValue).getExternalName();
	}

	@Override
	protected Object fromDBToCacheValue(MOAttribute attribute, Object dbValue) {
		if (dbValue == null) {
			return null;
		}
		try {
			return ConfigUtil.getEnum(_config.getEnum(), (String) dbValue);
		} catch (ConfigurationException ex) {
			return InvalidCacheValue.error("Unable to create enum of type '" + _config.getEnum().getName() +
				"' from external name '" + dbValue + "' in column '" + attribute + "'.", ex);
		}
	}

	@Override
	public void checkAttributeValue(MOAttribute attribute, DataObject data, Object newValue) throws DataObjectException {
		checkImmutable(attribute, data, newValue);
		
		if (newValue != null && !_config.getEnum().isInstance(newValue)) {
			throw new IncompatibleTypeException("New value '" + newValue + "' for attribute '" + attribute
				+ "' in data '" + data + "' is of unexpected type: Expected: '" + _config.getEnum().getName()
				+ "', Acutally: '" + newValue.getClass().getName() + "'.");
		}
		
	}
}
