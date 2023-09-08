/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;

import oracle.jdbc.OracleConnection;

/**
 * Proxy to an Oracle {@link DataSource} that applies configurations per connection.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class OracleDataSourceProxy extends ReadOnlySupportDataSource {
	
	private static final Set<String> OPTIMIZER_FEATURES = new HashSet<>(Arrays.asList("8.0.0", "8.0.3", "8.0.4",
		"8.0.5", "8.0.6", "8.0.7", "8.1.0", "8.1.3", "8.1.4", "8.1.5", "8.1.6", "8.1.7", "9.0.0", "9.0.1", "9.2.0",
		"9.2.0.8", "10.1.0", "10.1.0.3", "10.1.0.4", "10.1.0.5", "10.2.0.1", "10.2.0.2", "10.2.0.3", "10.2.0.4",
		"10.2.0.5", "11.1.0.6", "11.1.0.7", "11.2.0.1", "12.1.0.1", "19.1.0"));

	/**
	 * Default {@link DataSource} implementation.
	 */
	public static final String ORACLE_DATA_SOURCE_DEFAULT = "oracle.jdbc.pool.OracleDataSource";

	/**
	 * Default value for {@link #STATEMENT_CACHE_SIZE_PROPERTY}.
	 */
    private static final int STATEMENT_CACHE_SIZE_DEFAULT = 100;

    /**
     * Configuration property to set {@link #statementCacheSize}.
     */
	private static final String STATEMENT_CACHE_SIZE_PROPERTY = "statementCacheSize";

    /**
     * Configuration property to set {@link #implicitCachingEnabled}.
     */
	private static final String IMPLICIT_CACHING_ENABLED_PROPERTY = "implicitCachingEnabled";
	
	/**
	 * Configuration property to set {@link #_optimizerFeatures}.
	 */
	private static final String OPTIMIZER_FEATURES_PROPERTY = "optimizerFeatures";

	private final boolean implicitCachingEnabled;
	
	private final int statementCacheSize;

	private final String _optimizerFeatures;

	/**
	 * Creates a {@link OracleDataSourceProxy}.
	 * 
	 * @param config
	 *        The {@link DataSource} and {@link Connection} configuration.
	 */
	public OracleDataSourceProxy(Properties config) throws SQLException, ConfigurationException {
		super(ORACLE_DATA_SOURCE_DEFAULT, config);
		
		this.implicitCachingEnabled = 
			ConfigUtil.getBooleanValue(config, IMPLICIT_CACHING_ENABLED_PROPERTY, true);
		this.statementCacheSize = 
			ConfigUtil.getIntValue(config, STATEMENT_CACHE_SIZE_PROPERTY, STATEMENT_CACHE_SIZE_DEFAULT);
		_optimizerFeatures = ConfigUtil.getString(config, OPTIMIZER_FEATURES_PROPERTY, null);
		if (!StringServices.isEmpty(_optimizerFeatures)) {
			if (!OPTIMIZER_FEATURES.contains(_optimizerFeatures)) {
				throw new ConfigurationException("Optimizer feature must be contained in " + OPTIMIZER_FEATURES,
					OPTIMIZER_FEATURES_PROPERTY, _optimizerFeatures);
			}
		}
	}
	
	@Override
	public Connection internalGetConnection() throws SQLException {
		return applyConfiguration(super.internalGetConnection());
	}
	
	@Override
	public Connection internalGetConnection(String username, String password) throws SQLException {
		return applyConfiguration(super.internalGetConnection(username, password));
	}

	private Connection applyConfiguration(Connection connection) throws SQLException {
		OracleConnection  oracleConnection = (OracleConnection) connection;
        oracleConnection.setImplicitCachingEnabled(implicitCachingEnabled);
        if (implicitCachingEnabled) {
        	oracleConnection.setStatementCacheSize(statementCacheSize);
        }
		if (!StringServices.isEmpty(_optimizerFeatures)) {
			Statement stmt = oracleConnection.createStatement();
			try {
				DBHelper sqlDialect = DBHelper.getDBHelper(connection);
				StringBuilder out = new StringBuilder();
				out.append("alter session set optimizer_features_enable=");
				sqlDialect.escape(out, _optimizerFeatures);
				String sql = out.toString();

				// IGNORE FindBugs(SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE): At least Oracle 12
				// does not support setting session variables using prepared statements with
				// parameter passing.
				stmt.executeUpdate(sql);
			} finally {
				stmt.close();
			}
		}
        return oracleConnection;
	}

}
