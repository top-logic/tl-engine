/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.credential.SecretSource;
import com.top_logic.basic.sql.AbstractConfiguredConnectionPoolBase.DataSourceConfig;
import com.top_logic.basic.sql.AbstractConfiguredConnectionPoolBase.DataSourceConfig.AbstractDriverOption;
import com.top_logic.basic.sql.AbstractConfiguredConnectionPoolBase.DataSourceConfig.DriverOption;
import com.top_logic.basic.sql.AbstractConfiguredConnectionPoolBase.DataSourceConfig.EncryptedDriverOption;

/**
 * {@link DataSourceFactory} that locates the driver class by reflection.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultDataSourceFactory extends DataSourceFactory {

	/**
	 * Creates a {@link DefaultDataSourceFactory} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DefaultDataSourceFactory(InstantiationContext context, DataSourceConfig config) {
		super(context, config);
	}

	@Override
	public DataSource createDataSource(InstantiationContext context) throws ConfigurationException {
		DataSourceConfig config = getConfig();
		return createDataSource(context, config, options -> {
			DataSource result = SQLH.createDataSource(config.getJndiName(), config.getDriverClassName(), options);
			if (result == null) {
				throw new ConfigurationException("No database configured: " + config);
			}
			return result;
		});
	}

	/**
	 * Creates a {@link DataSource} with the {@link DataSourceConfig#getDriverOptions() driver
	 * options} of the given configuration.
	 *
	 * <p>
	 * If the options contain {@link EncryptedDriverOption secrets}, the result is a
	 * {@link SecretOptionsDataSource} that reads them each time a connection is opened and creates
	 * a new {@link DataSource} with the given factory, when one of them has changed. Otherwise the
	 * {@link DataSource} of the factory is returned directly.
	 * </p>
	 *
	 * @param context
	 *        The context for instantiating the {@link SecretSource}s.
	 * @param config
	 *        The configuration of the data source.
	 * @param factory
	 *        Creates the {@link DataSource} from the driver options.
	 * @return The {@link DataSource} to open connections from.
	 */
	public static DataSource createDataSource(InstantiationContext context, DataSourceConfig config,
			SecretOptionsDataSource.Factory factory) throws ConfigurationException {
		Properties options = new Properties();
		Map<String, SecretSource> secrets = new LinkedHashMap<>();
		Map<String, String> names = new HashMap<>();
		for (AbstractDriverOption option : config.getDriverOptions().values()) {
			String name = option.getName();
			String clash = names.put(name.toLowerCase(), name);
			if (clash != null) {
				throw new IllegalArgumentException("Inconsistent DB options: " + name + " vs. " + clash);
			}
			if (option instanceof EncryptedDriverOption) {
				SecretSource secret = context.getInstance(((EncryptedDriverOption) option).getValue());
				if (secret == null) {
					options.setProperty(name, "");
				} else {
					secrets.put(name, secret);
				}
			} else {
				options.setProperty(name, ((DriverOption) option).getValue());
			}
		}
		try {
			if (secrets.isEmpty()) {
				return factory.createDataSource(options);
			}
			return new SecretOptionsDataSource(options, secrets, factory);
		} catch (SQLException ex) {
			throw new ConfigurationException("Database access failed for configuration: " + config, ex);
		}
	}

	/**
	 * The given driver options completed by the {@link DataSourceConfig#getDriverClassName()
	 * driver class} and the {@link DataSourceConfig#getJndiName() JNDI name} as options for a
	 * {@link DefaultDataSourceProxy}.
	 */
	public static Properties genericDriverOptions(DataSourceConfig config, Properties options) {
		Properties result = new Properties();
		result.putAll(options);
		result.setProperty(DefaultDataSourceProxy.IMPL_CLASS_PROPERTY, config.getDriverClassName());
		result.setProperty(DefaultDataSourceProxy.IMPL_JNDI_NAME_PROPERTY, config.getJndiName());
		return result;
	}

}
