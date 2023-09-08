/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.sql.AbstractConfiguredConnectionPoolBase.DataSourceConfig;

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
		try {
			String jndiName = config.getJndiName();
			String className = config.getDriverClassName();
			Properties options = toProperties(config.getDriverOptions());
			DataSource result = SQLH.createDataSource(jndiName, className, options);
			if (result == null) {
				throw new ConfigurationException("No database configured: " + config);
			}
			return result;
		} catch (SQLException ex) {
			throw new ConfigurationException("Database access failed for configuration: " + config, ex);
		}
	}

	/**
	 * {@link DataSourceConfig#getDriverOptions()} as {@link Properties}.
	 */
	public static Properties genericDriverOptions(DataSourceConfig config) {
		Properties result = toProperties(config.getDriverOptions());
		result.setProperty(DefaultDataSourceProxy.IMPL_CLASS_PROPERTY, config.getDriverClassName());
		result.setProperty(DefaultDataSourceProxy.IMPL_JNDI_NAME_PROPERTY, config.getJndiName());
		return result;
	}

	private static Properties toProperties(Map<String, DataSourceConfig.DriverOption> driverOptions) {
		Map<String, String> names = new HashMap<>();
		Properties result = new Properties();
		for (DataSourceConfig.DriverOption option : driverOptions.values()) {
			String clash = names.put(option.getName().toLowerCase(), option.getName());
			if (clash != null) {
				throw new IllegalArgumentException("Inconsistent DB options: " + option.getName() + "->" + option.getValue() + " vs. " + clash
					+ "->" + result.getProperty(clash));
			}
			result.setProperty(option.getName(), option.getValue());
		}
		return result;
	}

}
