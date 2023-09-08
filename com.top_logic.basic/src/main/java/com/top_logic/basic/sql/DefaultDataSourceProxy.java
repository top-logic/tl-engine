/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.sql.SQLException;
import java.util.Map.Entry;
import java.util.Properties;

import javax.sql.DataSource;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;

/**
 * {@link DataSourceProxy} in which the implementation instance can be configured.
 * 
 * <p>
 * The class is abstract, because it must be sub-classed to provide any usful functionality.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class DefaultDataSourceProxy extends DataSourceProxy {

	/**
	 * Property to lookup the {@link DataSource} implementation class.
	 */
	public static final String IMPL_CLASS_PROPERTY = "implClass";

	/**
	 * Property to lookup JNDI name of the {@link DataSource}.
	 */
	public static final String IMPL_JNDI_NAME_PROPERTY = "implJndiName";

	/**
	 * Prefix for configurations of the {@link DataSource} implementation.
	 */
	public static final String INNER_CONFIGURATION_PREFIX = "inner.";
	
	/**
	 * @see #impl()
	 */
	private final DataSource impl;

	/**
	 * Creates a {@link DefaultDataSourceProxy}.
	 * 
	 * @param defaultClassName
	 *        The {@link DataSource} class to instantiate, if the given configuration does neither
	 *        contain a {@link #IMPL_CLASS_PROPERTY} nor a {@link #IMPL_JNDI_NAME_PROPERTY}.
	 * @param config
	 *        The datasource configuration.
	 */
	protected DefaultDataSourceProxy(String defaultClassName, Properties config) throws SQLException, ConfigurationException {
		this.impl = createDataSource(defaultClassName, config);
	}

	private DataSource createDataSource(String defaultClassName, Properties config)
			throws ConfigurationException, SQLException {
		String jndiName = config.getProperty(IMPL_JNDI_NAME_PROPERTY);
		if (!StringServices.isEmpty(jndiName)) {
			DataSource result = SQLH.fetchJNDIDataSource(jndiName);
			if (result != null) {
				return result;
			}
		}
		String implClassName = 
			defaultClassName == null ?
				ConfigUtil.getStringMandatory(config, IMPL_CLASS_PROPERTY) :
				ConfigUtil.getString(config, IMPL_CLASS_PROPERTY, defaultClassName);
		return SQLH.createDataSource(implClassName, implConfig(config));
	}
	
	private Properties implConfig(Properties config) {
		Properties result = new Properties();
		result.putAll(config);
		result.remove(IMPL_CLASS_PROPERTY);
		result.remove(IMPL_JNDI_NAME_PROPERTY);
		for (Entry<Object, Object> entry : config.entrySet()) {
			String key = (String) entry.getKey();
			if (key.startsWith(INNER_CONFIGURATION_PREFIX)) {
				result.remove(key);
				result.put(key.substring(INNER_CONFIGURATION_PREFIX.length()), entry.getValue());
			}
		}
		return result;
	}

	@Override
	protected final DataSource impl() {
		return impl;
	}

}
