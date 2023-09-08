/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.attr.storage;

import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.equal.ConfigEquality;
import com.top_logic.basic.io.character.CharacterContent;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.ex.IncompatibleTypeException;

/**
 * {@link DBAttributeStorageImpl} that stores a {@link ConfigurationItem}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ConfigurationAttributeStorage extends DBAttributeStorageImpl
		implements ConfiguredInstance<ConfigurationAttributeStorage.Config> {

	/**
	 * Configuration of an {@link ConfigurationAttributeStorage}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static interface Config extends PolymorphicConfiguration<ConfigurationAttributeStorage> {

		/**
		 * The {@link Object#getClass() class} the application value must have.
		 */
		@Mandatory
		Class<? extends ConfigurationItem> getConfigInterface();

	}

	private static final String CONFIG_TAG = "config";

	private static final Map<String, ConfigurationDescriptor> INSTANCE_TYPE = Collections.singletonMap(CONFIG_TAG,
		TypedConfiguration.getConfigurationDescriptor(ConfigurationItem.class));

	/**
	 * Configuration of this {@link ConfigurationAttributeStorage}.
	 */
	private final Config _config;

	/**
	 * Creates a new {@link ConfigurationAttributeStorage}.
	 * 
	 * @param context
	 *        The {@link InstantiationContext} to create sub configurations
	 * @param config
	 *        The configuration of this {@link ConfigurationAttributeStorage}.
	 * 
	 * @throws ConfigurationException
	 *         iff configuration is invalid.
	 */
	public ConfigurationAttributeStorage(InstantiationContext context, Config config) throws ConfigurationException {
		_config = config;
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
		try {
			StringWriter buffer = new StringWriter();
			new ConfigurationWriter(buffer).write(CONFIG_TAG, ConfigurationItem.class, (ConfigurationItem) cacheValue);
			return buffer.toString();
		} catch (XMLStreamException ex) {
			throw new IllegalArgumentException("Value cannot be serialized.", ex);
		}
	}

	@Override
	protected Object fromDBToCacheValue(MOAttribute attribute, Object dbValue) {
		if (dbValue == null) {
			return null;
		}
		String configSrc = (String) dbValue;
		try {
			CharacterContent content = CharacterContents.newContent(configSrc, attribute.getName());
			ConfigurationItem result =
				new ConfigurationReader(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, INSTANCE_TYPE)
					.setSource(content).read();
			if (!_config.getConfigInterface().isInstance(result)) {
				return InvalidCacheValue.error("Configuration item '" + result + "' in column '" + attribute
					+ "' is of unexpected type, expected was " + _config.getConfigInterface().getName()
					+ ", the actual type is '" + result.getConfigurationInterface().getName() + "'.", null);
			}
			return result;
		} catch (ConfigurationError | ConfigurationException ex) {
			return InvalidCacheValue.error("Datatabase value '" + dbValue
				+ "' of column '" + attribute + "' cannot be parsed as configuration.", ex);
		}
	}

	@Override
	public void checkAttributeValue(MOAttribute attribute, DataObject data, Object newValue) throws DataObjectException {
		checkImmutable(attribute, data, newValue);

		if (newValue != null && !_config.getConfigInterface().isInstance(newValue)) {
			throw new IncompatibleTypeException("New value '" + newValue + "' for attribute '" + attribute
				+ "' in '" + data + "' is of unexpected type. Expected was '" + _config.getConfigInterface().getName()
				+ "' the actual type is '" + newValue.getClass().getName() + "'.");
		}
	}

	@Override
	public boolean sameValue(MOAttribute attribute, Object val1, Object val2) {
		if (val1 == val2) {
			return true;
		}
		if (val1 == null) {
			assert val2 != null : "== is checked before";
			return false;
		}
		return ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals((ConfigurationItem) val1, (ConfigurationItem) val2);
	}

}

