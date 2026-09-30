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
import com.top_logic.basic.config.DefaultConfigConstructorScheme;
import com.top_logic.basic.config.DefaultConfigConstructorScheme.Factory;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.equal.ConfigEquality;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.ex.IncompatibleTypeException;

/**
 * {@link DBAttributeStorageImpl} that expects that the application value (and the cache value) are
 * configured objects.
 * 
 * <p>
 * Actually the configuration of the application value is stored within the database.
 * </p>
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ConfiguredInstanceStorage extends DBAttributeStorageImpl
		implements ConfiguredInstance<ConfiguredInstanceStorage.Config> {

	/**
	 * Configuration of an {@link ConfiguredInstanceStorage}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static interface Config extends PolymorphicConfiguration<ConfiguredInstanceStorage> {

		/**
		 * The {@link Object#getClass() class} the application value must have.
		 */
		@Mandatory
		Class<?> getInstanceClass();

		/**
		 * Setter for {@link #getInstanceClass()}
		 */
		void setInstanceClass(Class<?> instanceClass);

	}

	private static final String CONFIG_TAG = "config";

	private static final Map<String, ConfigurationDescriptor> INSTANCE_TYPE = Collections.singletonMap(CONFIG_TAG,
		TypedConfiguration.getConfigurationDescriptor(PolymorphicConfiguration.class));

	/**
	 * Configuration of this {@link ConfiguredInstanceStorage}.
	 */
	private final Config _config;

	/**
	 * Creates a new {@link ConfiguredInstanceStorage}.
	 * 
	 * @param context
	 *        Context to instantiate sub configurations.
	 * @param config
	 *        The configuration for this {@link ConfiguredInstanceStorage}.
	 * 
	 * @throws ConfigurationException
	 *         iff the configuration is invalid.
	 */
	public ConfiguredInstanceStorage(InstantiationContext context, Config config) throws ConfigurationException {
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
		PolymorphicConfiguration<?> config;
		try {
			config = toConfig(cacheValue);
		} catch (ConfigurationException ex) {
			throw new IllegalArgumentException("Value configuration cannot be extracted.", ex);
		}

		try {
			StringWriter buffer = new StringWriter();
			try (ConfigurationWriter w = new ConfigurationWriter(buffer)) {
				w.write(CONFIG_TAG, PolymorphicConfiguration.class, config);
			}
			return buffer.toString();
		} catch (XMLStreamException ex) {
			throw new IllegalArgumentException("Value cannot be serialized.", ex);
		}
	}

	private PolymorphicConfiguration<?> toConfig(Object cacheValue) throws ConfigurationException {
		PolymorphicConfiguration<?> config;
		if (cacheValue instanceof ConfiguredInstance<?>) {
			config = ((ConfiguredInstance<?>) cacheValue).getConfig();
		} else {
			Class<? extends Object> implementationClass = cacheValue.getClass();
			Factory factory = DefaultConfigConstructorScheme.getFactory(implementationClass);
			@SuppressWarnings("unchecked")
			Class<? extends PolymorphicConfiguration<?>> configurationInterface =
				(Class<? extends PolymorphicConfiguration<?>>) factory.getConfigurationInterface();
			@SuppressWarnings("unchecked")
			PolymorphicConfiguration<Object> newConfig =
				(PolymorphicConfiguration<Object>) TypedConfiguration.newConfigItem(configurationInterface);
			newConfig.setImplementationClass(implementationClass);
			config = newConfig;
		}
		return config;
	}

	@Override
	protected Object fromDBToCacheValue(MOAttribute attribute, Object dbValue) {
		if (dbValue == null) {
			return null;
		}
		String configSrc = (String) dbValue;
		try {
			InstantiationContext context = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY;
			PolymorphicConfiguration<?> config =
				(PolymorphicConfiguration<?>) new ConfigurationReader(context, INSTANCE_TYPE)
					.setSource(CharacterContents.newContent(configSrc, attribute.getName())).read();

			Factory factory = DefaultConfigConstructorScheme.getFactory(config.getImplementationClass());
			Object instance = factory.createInstance(context, config);
			if (!_config.getInstanceClass().isInstance(instance)) {
				return InvalidCacheValue.error("Datatabase value '" + dbValue
					+ "' of column '" + attribute + "' instantiates to a configured instance '" + instance
					+ "' of unexpected type. Expected was '" + _config.getInstanceClass().getName()
					+ "', actual type is '" + instance.getClass().getName() + "'.");
			}
			return instance;
		} catch (ConfigurationError | ConfigurationException ex) {
			return InvalidCacheValue.error("Datatabase value '" + dbValue
				+ "' in column  '" + attribute + "' can not be parsed as configuration.", ex);
		}
	}

	@Override
	public void checkAttributeValue(MOAttribute attribute, DataObject data, Object newValue) throws DataObjectException {
		checkImmutable(attribute, data, newValue);

		if (newValue != null && !_config.getInstanceClass().isInstance(newValue)) {
			throw new IncompatibleTypeException("New value '" + newValue + "' for attribute '" + attribute
				+ "' in '" + data + "' is of unexpected type: Expected: '" + _config.getInstanceClass().getName()
				+ "', Actual: '" + newValue.getClass().getName() + "'");
		}
	}

	@Override
	public boolean sameValue(MOAttribute attribute, Object val1, Object val2) {
		if (val1 == val2) {
			return true;
		}
		if (val1 == null || val2 == null) {
			return false;
		}
		assert val2 != null && val1 != null;

		if (val1 instanceof ConfiguredInstance<?>) {
			ConfigurationItem config1 = ((ConfiguredInstance<?>) val1).getConfig();
			if (val2 instanceof ConfiguredInstance<?>) {
				ConfigurationItem otherConf = ((ConfiguredInstance<?>) val2).getConfig();
				return ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(config1, otherConf);
			} else {
				return hasImplClass(config1, val2.getClass());
			}
		} else if (val2 instanceof ConfiguredInstance<?>) {
			return hasImplClass(((ConfiguredInstance<?>) val2).getConfig(), val1.getClass());
		} else {
			return val1.getClass() == val2.getClass();
		}
	}

	private boolean hasImplClass(ConfigurationItem config, Class<?> implClass) {
		if (config.getConfigurationInterface() != PolymorphicConfiguration.class) {
			return false;
		} else {
			return ((PolymorphicConfiguration<?>) config).getImplementationClass() == implClass;
		}
	}

}
