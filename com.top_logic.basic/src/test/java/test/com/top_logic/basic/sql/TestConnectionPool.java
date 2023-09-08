/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.sql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.sql.AbstractConfiguredConnectionPool;
import com.top_logic.basic.sql.AbstractConfiguredConnectionPool.PoolSettings;
import com.top_logic.basic.sql.CommonConfiguredConnectionPool;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionProxy;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DataSourceProxy;
import com.top_logic.basic.sql.DatabaseMetaDataProxy;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.sql.StatementProxy;

/**
 * Test case for {@link AbstractConfiguredConnectionPool}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestConnectionPool extends BasicTestCase {

	private ConnectionPool _pool;

	private static TestingDataSource _ds;

	/**
	 * Stub {@link DataSource} for testing.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class TestingDataSource extends DataSourceProxy {
		int _openConnections = 0;

		int _openStatements = 0;

		public TestingDataSource() {
			_ds = this;
		}

		public int getOpenConnections() {
			return _openConnections;
		}

		@Override
		protected DataSource impl() {
			throw unsupported();
		}

		protected UnsupportedOperationException unsupported() {
			return new UnsupportedOperationException("Dummy testing driver.");
		}

		@Override
		public Connection getConnection() throws SQLException {
			_openConnections++;
			return new ConnectionProxy() {

				private boolean _autoCommit;

				private boolean _readOnly;

				private int _transactionIsolation;

				private boolean _closed = false;

				@Override
				protected Connection impl() throws SQLException {
					throw unsupported();
				}

				@Override
				public Statement createStatement() throws SQLException {
					_openStatements++;
					return new StatementProxy() {

						@Override
						protected Statement impl() {
							throw unsupported();
						}

						@Override
						public void close() throws SQLException {
							_openStatements--;
						}
					};
				}

				@Override
				public void setAutoCommit(boolean value) throws SQLException {
					_autoCommit = value;
				}

				@Override
				public boolean getAutoCommit() throws SQLException {
					return _autoCommit;
				}

				@Override
				public boolean isReadOnly() throws SQLException {
					return _readOnly;
				}

				@Override
				public void setReadOnly(boolean readOnly) {
					_readOnly = readOnly;
				}

				@Override
				public void setTransactionIsolation(int value) throws SQLException {
					_transactionIsolation = value;
				}

				@Override
				public int getTransactionIsolation() {
					return _transactionIsolation;
				}

				@Override
				public DatabaseMetaData getMetaData() throws SQLException {
					return new DatabaseMetaDataProxy() {
						@Override
						protected DatabaseMetaData impl() {
							throw unsupported();
						}

						@Override
						public String getDriverName() throws SQLException {
							return "Dummy";
						}

						@Override
						public int getDatabaseMajorVersion() throws SQLException {
							return 1;
						}

						@Override
						public int getDatabaseMinorVersion() throws SQLException {
							return 0;
						}

						@Override
						public String getDatabaseProductVersion() throws SQLException {
							return "Dummy";
						}
					};
				}

				@Override
				public void close() throws SQLException {
					if (!isClosed()) {
						_openConnections--;
						_closed = true;
					}
				}

				@Override
				public boolean isClosed() throws SQLException {
					return _closed;
				}
			};
		}
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		final CommonConfiguredConnectionPool.Config config =
			TypedConfiguration.newConfigItem(CommonConfiguredConnectionPool.Config.class);

		config.getDataSource().setDriverClassName(TestingDataSource.class.getName());
		config.setInitDuringStartup(true);

		PoolSettings pool = config.getReadPool();
		pool.setLifo(true);
		pool.setMinIdle(0);
		pool.setTimeBetweenEvictionRunsMillis(0L);
		pool.setTestOnBorrow(false);
		pool.setTestOnReturn(false);

		_pool = CommonConfiguredConnectionPool.createConfiguredPool(config);

		assertEquals(_pool.getName(), config.getName());
	}

	@Override
	protected void tearDown() throws Exception {
		_ds = null;
		_pool = null;

		super.tearDown();
	}

	public void testBorrowWrite() throws SQLException {
		assertEquals("Read connection has been allocated upon setup.", 1, _ds.getOpenConnections());

		PooledConnection connection = _pool.borrowWriteConnection();
		assertEquals("Write connection is only allocated upon use.", 1, _ds.getOpenConnections());

		Statement createStatement = connection.createStatement();
		assertEquals("Connection must be opened upon use.", 2, _ds.getOpenConnections());

		createStatement.close();

		_pool.releaseWriteConnection(connection);
		assertEquals("Connection is not closed upon release.", 2, _ds.getOpenConnections());
	}

	public void testInvalidateWrite() throws SQLException {
		assertEquals("Read connection has been allocated upon setup.", 1, _ds.getOpenConnections());

		PooledConnection connection = _pool.borrowWriteConnection();
		assertEquals("Write connection is only allocated upon use.", 1, _ds.getOpenConnections());

		Statement createStatement = connection.createStatement();
		assertEquals("Connection must be opened upon use.", 2, _ds.getOpenConnections());

		createStatement.close();

		_pool.invalidateWriteConnection(connection);
		assertEquals("Connection is closed upon invalidate.", 1, _ds.getOpenConnections());
	}

	public void testInvalidateRead() throws SQLException {
		assertEquals("Read connection has been allocated upon setup.", 1, _ds.getOpenConnections());
		
		PooledConnection connection = _pool.borrowReadConnection();
		
		Statement createStatement = connection.createStatement();
		assertEquals("Read connection is reused.", 1, _ds.getOpenConnections());
		
		createStatement.close();
		
		_pool.invalidateReadConnection(connection);
		assertEquals("Connection is closed upon invalidate.", 0, _ds.getOpenConnections());
	}
	
	public void testBorrowNestedRead() throws SQLException {
		PooledConnection outer = _pool.borrowReadConnection();

		PooledConnection inner = _pool.borrowReadConnection();
		assertSame("Nested read connections are shared.", outer, inner);

		Statement createStatement = inner.createStatement();
		assertEquals("Read connection has been allocated.", 1, _ds.getOpenConnections());

		_pool.releaseReadConnection(inner);

		_pool.releaseReadConnection(outer);
	}

	public void testPreventReleaseReleasedConnection() throws SQLException {
		PooledConnection connection = _pool.borrowReadConnection();
		_pool.releaseReadConnection(connection);

		try {
			_pool.releaseReadConnection(connection);
			fail("Must not allow releasing unowned connections.");
		} catch (RuntimeException ex) {
			// Expected.
		}
	}

	public void testPreventReadUsageAfterRelease() {
		PooledConnection connection = _pool.borrowReadConnection();
		_pool.releaseReadConnection(connection);

		try {
			connection.createStatement();
			fail("Must not allow usage after return.");
		} catch (SQLException ex) {
			// Expected.
		}
	}

	public void testPreventWriteUsageAfterRelease() {
		PooledConnection connection = _pool.borrowWriteConnection();
		_pool.releaseWriteConnection(connection);

		try {
			connection.createStatement();
			fail("Must not allow usage after return.");
		} catch (SQLException ex) {
			// Expected.
		}
	}

	public void testPreventReturningReadAsWriteConnection() {
		PooledConnection connection = _pool.borrowReadConnection();
		try {
			_pool.releaseWriteConnection(connection);
			fail("Must not allow returning a read as write connection.");
		} catch (RuntimeException ex) {
			// expected.
		}
	}

	public void testPreventReturningWriteAsReadConnection() {
		PooledConnection connection = _pool.borrowWriteConnection();
		try {
			_pool.releaseReadConnection(connection);
			fail("Must not allow returning a read as write connection.");
		} catch (RuntimeException ex) {
			// expected.
		}
	}

	public void testSQLDialectConsistency() throws SQLException {
		PooledConnection readConnection = _pool.borrowReadConnection();
		try {
			DBHelper readConnectionSQLDialect = readConnection.getSQLDialect();
			assertEquals(_pool.getSQLDialect(), readConnectionSQLDialect);
		} finally {
			_pool.releaseReadConnection(readConnection);
		}

		PooledConnection writeConnection = _pool.borrowReadConnection();
		try {
			DBHelper writeConnectionSQLDialect = writeConnection.getSQLDialect();
			assertEquals(_pool.getSQLDialect(), writeConnectionSQLDialect);
		} finally {
			_pool.releaseReadConnection(writeConnection);
		}
	}

	public static Test suite() {
		return ModuleTestSetup.setupModule(TestConnectionPool.class);
	}
}
