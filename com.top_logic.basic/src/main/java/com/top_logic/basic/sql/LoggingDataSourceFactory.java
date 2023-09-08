/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.sql.SQLException;

import javax.sql.DataSource;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.sql.LoggingDataSourceProxy.Config;

/**
 * {@link DefaultDataSourceFactory} that wraps the native driver with a
 * {@link LoggingDataSourceProxy}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LoggingDataSourceFactory extends DefaultDataSourceFactory {

	/**
	 * Creates a {@link LoggingDataSourceFactory} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public LoggingDataSourceFactory(InstantiationContext context, LoggingDataSourceProxy.Config config) {
		super(context, config);
	}

	@Override
	public DataSource createDataSource(InstantiationContext context) throws ConfigurationException {
		try {
			return new LoggingDataSourceProxy(context, config());
		} catch (SQLException ex) {
			throw new ConfigurationException("Setup of data source failed.", ex);
		}
	}

	private Config config() {
		return (Config) getConfig();
	}

}
