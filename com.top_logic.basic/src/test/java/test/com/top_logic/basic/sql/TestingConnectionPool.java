/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.sql;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.sql.AbstractConnectionPool;
import com.top_logic.basic.sql.CommonConfiguredConnectionPool;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;

/**
 * {@link ConnectionPool} wrapper that can simulate broken read and write
 * connections.
 * 
 * @see #simulateReadConnectionBreakdown()
 * @see #simulateWriteConnectionBreakdown()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestingConnectionPool extends AbstractConnectionPool {

	private final ConnectionPool impl;
	private boolean simulateBrokenReadConnection;
	private boolean simulateBrokenWriteConnection;

	/**
	 * Creates a {@link TestingConnectionPool}.
	 */
	public TestingConnectionPool(ConnectionPool impl) {
		super(null, null);
		this.impl = impl;
	}

	/**
	 * Creates a {@link TestingConnectionPool} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TestingConnectionPool(InstantiationContext context, CommonConfiguredConnectionPool.Config config) throws ConfigurationException {
		super(context, config);

		// Note: Must instantiate directly (not through context) to avoid switching implementation
		// class.
		this.impl = new CommonConfiguredConnectionPool(context, config);
	}
	
	@Override
	protected void initDBHelper() throws SQLException {
		// Trigger lazy initialization.
		impl.getSQLDialect();
	}

	@Override
	public DataSource getDataSource() {
		return impl.getDataSource();
	}
	
	@Override
	public PooledConnection borrowWriteConnection() {
		PooledConnection result = impl.borrowWriteConnection();
		if (simulateBrokenWriteConnection) {
			simulateBrokenWriteConnection = false;
			breakConnection(result);
		}
		return result;
	}

	@Override
	public PooledConnection borrowReadConnection() {
		PooledConnection result = impl.borrowReadConnection();
		if (simulateBrokenReadConnection) {
			simulateBrokenReadConnection = false;
			breakConnection(result);
		}
		return result;
	}

	@Override
	public void invalidateWriteConnection(PooledConnection connection) {
		impl.invalidateWriteConnection(connection);
	}

	@Override
	public void invalidateReadConnection(PooledConnection connection) {
		impl.invalidateReadConnection(connection);
	}
	
	@Override
	public void releaseWriteConnection(PooledConnection connection) {
		impl.releaseWriteConnection(connection);
	}
	
	@Override
	public void releaseReadConnection(PooledConnection connection) {
		impl.releaseReadConnection(connection);	
	}

	@Override
	public void clear() {
		impl.clear();
	}

	@Override
	public void close() {
		impl.close();
	}
	
	@Override
	public DBHelper getSQLDialect() throws SQLException{
	    return impl.getSQLDialect();
	}

	/**
	 * Breaks the read connection in the next request to {@link #borrowReadConnection()}.
	 */
	public void simulateReadConnectionBreakdown() {
		this.simulateBrokenReadConnection = true;
	}
	
	/**
	 * Breaks the read connection in the next request to {@link #borrowWriteConnection()}.
	 */
	public void simulateWriteConnectionBreakdown() {
		this.simulateBrokenWriteConnection = true;
	}
	
	static void breakConnection(PooledConnection result) {
		try {
			Connection connection = (Connection) BasicTestCase.getValueByReflection(result, "con");
			if (connection != null) {
				connection.close();
			}

		} catch (SQLException ex) {
			throw new AssertionError(ex);
		}
	}
	
}