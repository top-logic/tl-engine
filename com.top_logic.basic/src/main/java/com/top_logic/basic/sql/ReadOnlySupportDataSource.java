/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import com.top_logic.basic.config.ConfigurationException;

/**
 * A {@link DataSourceProxy} for {@link DataSource} which does not handle
 * {@link Connection#isReadOnly()} correct.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ReadOnlySupportDataSource extends DefaultDataSourceProxy {

	/**
	 * Creates a new {@link ReadOnlySupportDataSource}.
	 */
	protected ReadOnlySupportDataSource(String defaultClassName, Properties config) throws SQLException,
			ConfigurationException {
		super(defaultClassName, config);
	}

	@Override
	public Connection getConnection() throws SQLException {
		return wrapConnection(internalGetConnection());
	}

	/**
	 * Returns the connection to wrap.
	 * 
	 * @see #getConnection()
	 */
	protected Connection internalGetConnection() throws SQLException {
		return super.getConnection();
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		return wrapConnection(internalGetConnection(username, password));
	}

	/**
	 * Returns the connection to wrap.
	 * 
	 * @see #getConnection(String, String)
	 */
	protected Connection internalGetConnection(String username, String password) throws SQLException {
		return super.getConnection(username, password);
	}

	/**
	 * Wraps the given connection.
	 */
	protected Connection wrapConnection(Connection connection) {
		return new ReadOnlySupportConnection(connection);
	}

}

