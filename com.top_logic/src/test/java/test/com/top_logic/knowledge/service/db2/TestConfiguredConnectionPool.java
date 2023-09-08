/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.DatabaseTestSetup;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.db.model.DBSchema;
import com.top_logic.basic.db.model.util.DBSchemaUtils;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.sql.CommonConfiguredConnectionPool;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.ex.DuplicateAttributeException;
import com.top_logic.dob.meta.MOClassImpl;

/**
 * Test case for {@link CommonConfiguredConnectionPool}.
 * 
 * <p>
 * Test case is in <i>TopLogic</i>, because of required utilities.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestConfiguredConnectionPool extends BasicTestCase {

	private static final MOAttributeImpl VALUE_ATTR = 
		new MOAttributeImpl("value", "value", MOPrimitive.INTEGER, true, false, DBType.INT, 20, 0);
	
	private static final MOAttributeImpl NAME_ATTR = 
		new MOAttributeImpl("name", "name", MOPrimitive.STRING, true, false, DBType.STRING, 150, 0);
	
	private static final MOClassImpl TEST_TABLE;
	static {
		MOClassImpl type = new MOClassImpl("testConnectionPool");
		
		try {
			type.addAttribute(NAME_ATTR);
			type.addAttribute(VALUE_ATTR);
		} catch (DuplicateAttributeException ex) {
			throw new UnreachableAssertion(ex);
		}
		type.setPrimaryKey(NAME_ATTR, VALUE_ATTR);
		
		TEST_TABLE = type;
	}
	
	ConnectionPool connectionPool;

	private DBHelper sqlDialect;

	private DBSchema _dbSchema;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		connectionPool = ConnectionPoolRegistry.getDefaultConnectionPool();
		sqlDialect = DBHelper.getDBHelper(connectionPool);
		
		_dbSchema = DBSchemaUtils.newDBSchema();
		_dbSchema.getTables().add(SchemaSetup.createTable(TEST_TABLE));
		DBSchemaUtils.recreateTables(connectionPool, _dbSchema);
	}
	
	@Override
	protected void tearDown() throws Exception {
		DBSchemaUtils.dropTables(connectionPool, _dbSchema);
		connectionPool = null;
		
		super.tearDown();
	}
	
	public void testConnectionRepair1() throws SQLException {
		PooledConnection readConnection = connectionPool.borrowReadConnection();
		try {
			assertTrue(readConnection.getAutoCommit());
			
			readConnection.createStatement().getConnection().setAutoCommit(false);
		} finally {
			connectionPool.releaseReadConnection(readConnection);
		}
		
		assertTrue(readConnection.getAutoCommit());
	}
	
	public void testConnectionRepair2() throws SQLException {
		PooledConnection readConnection = connectionPool.borrowReadConnection();
		try {
			assertTrue(readConnection.isReadOnly());
			
			readConnection.createStatement().getConnection().setReadOnly(false);
		} finally {
			connectionPool.releaseReadConnection(readConnection);
		}
		
		assertTrue(readConnection.isReadOnly());
	}
	
	public void testConnectionRepair3() throws SQLException {
		PooledConnection readConnection = connectionPool.borrowWriteConnection();
		try {
			assertFalse(readConnection.getAutoCommit());
			
			readConnection.createStatement().getConnection().setAutoCommit(true);
		} finally {
			connectionPool.releaseWriteConnection(readConnection);
		}
		
		assertFalse(readConnection.getAutoCommit());
	}
	
	public void testConnectionRepair4() throws SQLException {
		PooledConnection readConnection = connectionPool.borrowWriteConnection();
		try {
			assertFalse(readConnection.isReadOnly());
			
			readConnection.createStatement().getConnection().setReadOnly(true);
		} finally {
			connectionPool.releaseWriteConnection(readConnection);
		}
		
		assertFalse(readConnection.isReadOnly());
	}
	
	public void testReadOnly() {
		PooledConnection readConnection = connectionPool.borrowReadConnection();
		try {
			insert(readConnection, "test", 42);
			readConnection.commit();
			fail("Must not allow to update with a read connection.");
		} catch (SQLException ex) {
			// Expected.
		} finally {
			connectionPool.releaseReadConnection(readConnection);
		}
	}
	
	public void testTransactionLessRead() throws SQLException {
		{
			PooledConnection readConnection = connectionPool.borrowReadConnection();
			try {
				// Table initially empty.
				assertEquals(0, getValues(readConnection, "test").size());
			} finally {
				connectionPool.releaseReadConnection(readConnection);
			}
		}
			
		// Database write operation.
		{
			PooledConnection writeConnection = connectionPool.borrowWriteConnection();
			try {
				insert(writeConnection, "test", 42);
				writeConnection.commit();
			} finally {
				connectionPool.releaseWriteConnection(writeConnection);
			}
		}
		
		{
			PooledConnection readConnection = connectionPool.borrowReadConnection();
			try {
				// Next read sees new value.
				assertEquals(1, getValues(readConnection, "test").size());
			} finally {
				connectionPool.releaseReadConnection(readConnection);
			}
		}
		
		// Database write operation.
		{
			PooledConnection writeConnection = connectionPool.borrowWriteConnection();
			try {
				insert(writeConnection, "test", 43);
				writeConnection.commit();
			} finally {
				connectionPool.releaseWriteConnection(writeConnection);
			}
		}
		
		{
			PooledConnection readConnection = connectionPool.borrowReadConnection();
			try {
				// Next read sees new value.
				assertEquals(2, getValues(readConnection, "test").size());
			} finally {
				connectionPool.releaseReadConnection(readConnection);
			}
		}
	}

	public void testCleanup() throws SQLException {
		for (int n = 0; n < 30; n++) {
			PooledConnection writeConnection = connectionPool.borrowWriteConnection();
			try {
				// Database access.
				insert(writeConnection, "test", 42);
				
				// No commit.
			} finally {
				connectionPool.releaseWriteConnection(writeConnection);
			}
		}
		
		{
			PooledConnection writeConnection = connectionPool.borrowWriteConnection();
			try {
				// Database access.
				insert(writeConnection, "test", 42);
				
				writeConnection.commit();
			} finally {
				connectionPool.releaseWriteConnection(writeConnection);
			}
		}
	}

	
	public void testParallel() throws InterruptedException {
		final int threadCnt = 100;
		final int operationCnt = 100;
		
		TestFuture[] results = new TestFuture[threadCnt];
		for (int t = 0; t < threadCnt; t++) {
			final int threadId = t;
			
			switch (threadId % 5) {
			case 0: {
				results[threadId] = inParallel(new Execution() {
					@Override
					public void run() throws Exception {
						// Database write operation.
						String name = "thread" + threadId;
						for (int n = 0; n < operationCnt; n++) {
							PooledConnection writeConnection = connectionPool.borrowWriteConnection();
							try {
								// Database access.
								insert(writeConnection, name, n);
								writeConnection.commit();
							} finally {
								connectionPool.releaseWriteConnection(writeConnection);
							}
						}
					}
				});
				break;
			}
			default: {
				results[threadId] = inParallel(new Execution() {
					@Override
					public void run() throws Exception {
						PooledConnection readConnection = connectionPool.borrowReadConnection();
						try {
							getValues(readConnection, "thread" + (threadId - (threadId % 5)));
						} finally {
							connectionPool.releaseReadConnection(readConnection);
						}
					}
				});
				break;
			}
			}
		}
		
		checkAll(results);
	}
	
	public void testRandomAllocation() {
		
		PooledConnection[] connection = new PooledConnection[3];
		connection[0] = connectionPool.borrowWriteConnection();
		connection[1] = connectionPool.borrowReadConnection();
		connection[2] = connectionPool.borrowWriteConnection();
		connectionPool.releaseReadConnection(connection[1]);
		connectionPool.releaseWriteConnection(connection[0]);
		connectionPool.releaseWriteConnection(connection[2]);
		
	}
	
	List getValues(PooledConnection readConnection, String name) throws SQLException {
		try (PreparedStatement statement =
			readConnection.prepareStatement(("SELECT " + sqlDialect.columnRef(VALUE_ATTR.getDBName()) + " FROM "
				+ sqlDialect.tableRef(TEST_TABLE.getDBName()) + " WHERE " + sqlDialect.columnRef(NAME_ATTR.getDBName())
				+ "=? ORDER BY " + sqlDialect.columnRef(VALUE_ATTR.getDBName())))) {
			statement.setString(1, name);

			ArrayList result;
			try (ResultSet resultSet = statement.executeQuery()) {
				result = new ArrayList();
				while (resultSet.next()) {
					result.add(Integer.valueOf(resultSet.getInt(1)));
				}
			}
			return result;
		}
	}
	
	void insert(PooledConnection writeConnection, String name, int value) throws SQLException {
		try (PreparedStatement statement = writeConnection
			.prepareStatement(("INSERT INTO " + sqlDialect.tableRef(TEST_TABLE.getDBName()) + " VALUES (?, ?)"))) {
			statement.setString(1, name);
			statement.setInt(2, value);
			statement.executeUpdate();
		}
	}
	
    public static Test suite() {
		return TLTestSetup.createTLTestSetup(DatabaseTestSetup.getDBTest(TestConfiguredConnectionPool.class));
    }
	
}
