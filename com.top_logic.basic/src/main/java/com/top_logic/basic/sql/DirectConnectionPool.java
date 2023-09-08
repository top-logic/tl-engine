/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import javax.sql.DataSource;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;

/**
 * {@link ConnectionPool} that does not pool connections but forwards all
 * requests to the underlying {@link DataSource}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DirectConnectionPool extends AbstractConfiguredConnectionPoolBase {

	/**
	 * Creates a {@link DirectConnectionPool} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DirectConnectionPool(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public PooledConnection borrowWriteConnection() {
		return createConnection();
	}

	@Override
	public PooledConnection borrowReadConnection() {
		return createConnection();
	}

	@Override
	public void releaseReadConnection(PooledConnection connection) {
		releaseConnection(connection);
	}

	@Override
	public void releaseWriteConnection(PooledConnection connection) {
		releaseConnection(connection);
	}

	@Override
	public void invalidateWriteConnection(PooledConnection connection) {
		releaseConnection(connection);
	}

	@Override
	public void invalidateReadConnection(PooledConnection connection) {
		releaseConnection(connection);
	}


	private PooledConnectionImpl createConnection() {
		PooledConnectionImpl connection = new PooledConnectionImpl(this);
		connection.activate();
		return connection;
	}

	private void releaseConnection(PooledConnection connection) {
		try {
			connection.closeConnection(null);
		} finally {
			// deactivate connection, also if exception is thrown.
			((PooledConnectionImpl) connection).deactivate();
		}
	}

	@Override
	public void clear() {
		// Ignore, there are no idle cached connections.
	}

}
