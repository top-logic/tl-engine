/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;

/**
 * {@link ConnectionPool} implementation that can be configured.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class CommonConfiguredConnectionPool extends AbstractConfiguredConnectionPool {

	/**
	 * Creates a {@link CommonConfiguredConnectionPool} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public CommonConfiguredConnectionPool(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	CommonConfiguredConnectionPool(DataSource aDs, DBHelper sqlDialect, Config config) {
		super(aDs, sqlDialect, config);
	}

	/**
	 * Creates a new {@link CommonConfiguredConnectionPool} using a default configuration.
	 * 
	 * @param config
	 *        The pool configuration.
	 * 
	 * @return The newly created connection pool.
	 */
	public static ConnectionPool createConfiguredPool(Config config) throws SQLException {
		try {
			CommonConfiguredConnectionPool result =
				new CommonConfiguredConnectionPool(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);
			if (config.getInitDuringStartup()) {
				result.initDBHelper();
			}
			return result;
		} catch (ConfigurationException ex) {
			throw new ConfigurationError("Invalid default configuration.", ex);
		}
	}
	
	/**
	 * Creates a new {@link CommonConfiguredConnectionPool} with a datasource instance.
	 * 
	 * @param maxCut
	 *        The number of statements cached per connection.
	 * @param ds
	 *        The {@link DataSource} that should be used by the pool to create new connections from.
	 * @return The newly created connection pool.
	 * 
	 * @deprecated Use {@link #createConfiguredPool(Config)}
	 */
	@Deprecated
	public static ConnectionPool createConfiguredPool(int maxCut, DataSource ds) throws SQLException {
		Connection connection = ds.getConnection();
		DBHelper sqlDialect;
		try {
			sqlDialect = DBHelper.getDBHelper(connection);
		} finally {
			connection.close();
		}
		return new CommonConfiguredConnectionPool(ds, sqlDialect, defaultConfig());
	}

	private static Config defaultConfig() {
		return TypedConfiguration.newConfigItem(Config.class);
	}
}
