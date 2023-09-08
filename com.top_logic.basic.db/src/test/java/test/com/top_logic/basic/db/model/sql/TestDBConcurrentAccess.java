/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.db.model.sql;

import static com.top_logic.basic.db.model.DBSchemaFactory.*;
import static com.top_logic.basic.db.sql.SQLFactory.*;
import static java.util.Arrays.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import junit.framework.AssertionFailedError;
import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.DatabaseTestSetup;
import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.db.model.DBColumn;
import com.top_logic.basic.db.model.DBPrimary;
import com.top_logic.basic.db.model.DBSchema;
import com.top_logic.basic.db.model.DBSchemaFactory;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.db.model.util.DBSchemaUtils;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLInsert;
import com.top_logic.basic.db.sql.SQLQuery;
import com.top_logic.basic.db.sql.SQLSelect;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.MSSQLHelper;
import com.top_logic.basic.sql.PooledConnection;

/**
 * Test case that ensures that supported databases allow to read rows while
 * there is an open transaction that did modify the same table but only touches
 * rows that are not selected.
 * 
 * <p>
 * The test case simulates the access pattern of the security storage. If this
 * test fails, the security storage is likely to malfunction with the
 * corresponding database.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestDBConcurrentAccess extends BasicTestCase {

	protected DBSchema schema;
	protected CompiledStatement insertStatement;
	protected CompiledStatement selectWhereAStatement;
	protected ConnectionPool pool;
	protected CompiledStatement selectWhereBStatement;

	/**
	 * Creates a {@link TestDBConcurrentAccess}.
	 */
	public TestDBConcurrentAccess(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		schema = DBSchemaFactory.createDBSchema();
		DBTable table = DBSchemaFactory.createTable(TestDBConcurrentAccess.class.getSimpleName());
		schema.getTables().add(table);
		
		DBColumn a = DBSchemaFactory.createColumn("a");
		a.setBinary(true);
		a.setType(DBType.STRING);
		a.setSize(100);
		a.setMandatory(true);
		table.getColumns().add(a);
		
		DBColumn b = DBSchemaFactory.createColumn("b");
		b.setBinary(true);
		b.setType(DBType.STRING);
		b.setSize(100);
		b.setMandatory(true);
		table.getColumns().add(b);
		
		DBPrimary primaryKey = DBSchemaFactory.createPrimary();
		primaryKey.getColumnRefs().add(ref(a));
		primaryKey.getColumnRefs().add(ref(b));
		table.setPrimaryKey(primaryKey);
		
//		DBIndex index = schema.createIndex("idxBA");
//		index.setKind(Kind.UNIQUE);
//		index.getColumns().add(b);
//		index.getColumns().add(a);
//		table.getIndices().add(index);
		
		pool = ConnectionPoolRegistry.getDefaultConnectionPool();
		
		DBSchemaUtils.recreateTables(pool, schema);
		
		SQLQuery<SQLInsert> insert = query(
			asList(
				parameterDef(a.getType(), "a"),
				parameterDef(a.getType(), "b")), 
			insert(
				table(table.getDBName(), "t"), 
				asList(a.getDBName(), b.getDBName()),
				asList(
					parameter(a.getType(), "a"),
					parameter(a.getType(), "b"))));

		insertStatement = insert.toSql(pool.getSQLDialect());
		
		// Insert test data.
		PooledConnection connection = pool.borrowWriteConnection();
		try {
			insertValues(connection, "a1", "b1");
			insertValues(connection, "a1", "b2");
			insertValues(connection, "a2", "b1");
			insertValues(connection, "a2", "b2");
			
			connection.commit();
		} finally {
			pool.releaseWriteConnection(connection);
		}
		
		SQLQuery<SQLSelect> selectWhereA = query(
			asList(
				parameterDef(a.getType(), "a")), 
			select(true, 
				asList(columnDef(column("t", b.getDBName(), b.isMandatory()), "b")),
				table(table.getDBName(), "t"), 
				eq(
					column("t", a.getDBName(), a.isMandatory()),
					parameter(a.getType(), "a")),
				asList(order(false, column("t", "b")))));

		selectWhereAStatement = selectWhereA.toSql(pool.getSQLDialect());
		
		SQLQuery<SQLSelect> selectWhereB = query(
			asList(
				parameterDef(a.getType(), "b")), 
				select(true, 
				asList(columnDef(column("t", a.getDBName(), a.isMandatory()), "a")),
					table(table.getDBName(), "t"), 
					eq(
					column("t", b.getDBName(), b.isMandatory()),
					parameter(b.getType(), "b")),
						asList(order(false, column("t", "a")))));
		
		selectWhereBStatement = selectWhereB.toSql(pool.getSQLDialect());
	}
	
	@Override
	protected void tearDown() throws Exception {
		DBSchemaUtils.resetTables(pool, schema, false);

		pool = null;
		schema = null;
		insertStatement = null;
		selectWhereAStatement = null;
		selectWhereBStatement = null;
		
		super.tearDown();
	}
	
	/**
	 * Test that values inserted by the setup can be retrieved correctly.
	 */
	public void testSetup() throws SQLException {
		PooledConnection connection = pool.borrowReadConnection();
		try {
			assertEquals(asList("b1", "b2"), selectWhereA(connection, "a1"));
			assertEquals(asList("a1", "a2"), selectWhereB(connection, "b1"));
		} finally {
			pool.releaseReadConnection(connection);
		}
	}

	/**
	 * Test that modifications do not block selects, if the query does not
	 * access new rows in the same transaction.
	 */
	public void testSequentialAccess() throws SQLException, InterruptedException {
		doTestAccess(false);
	}

	/**
	 * Test that modifications do not block selects, if the query does not
	 * access new rows in a foreign transaction.
	 */
	public void testConcurrentAccess() throws SQLException, InterruptedException {
		try {
			doTestAccess(true);
			if (isMSSQL()) {
				fail(
					"Test should fail due to the known bug in ticket #3780: Concurrent access failed: Test execution did not terminate in time.");
			}
		} catch (AssertionFailedError exception) {
			if (isMSSQL()) {
				/* Expected due to the known bug in ticket #3780: Concurrent access failed: Test
				 * execution did not terminate in time. */
				return;
			}

			throw exception;
		}
	}
	
	private boolean isMSSQL() throws SQLException {
		return pool.getSQLDialect() instanceof MSSQLHelper;
	}

	private void doTestAccess(final boolean concurrent) throws SQLException, InterruptedException {
		final PooledConnection writeConnection = pool.borrowWriteConnection();
		try {
			insertValues(writeConnection, "a00", "b3");
			insertValues(writeConnection, "a0", "b3");
			insertValues(writeConnection, "a11", "b3");
			insertValues(writeConnection, "a21", "b3");
			insertValues(writeConnection, "a3", "b3");

			TestFuture result = inParallel(new Execution() {
				@Override
				public void run() throws Exception {
					PooledConnection readConnection = pool.borrowReadConnection();
					try {
						PooledConnection c = concurrent ? readConnection : writeConnection;
						
						assertEquals(asList("b1", "b2"), selectWhereA(c, "a1"));
						assertEquals(asList("b1", "b2"), selectWhereA(c, "a2"));
						assertEquals(asList("a1", "a2"), selectWhereB(c, "b1"));
						assertEquals(asList("a1", "a2"), selectWhereB(c, "b2"));
					} finally {
						pool.releaseReadConnection(readConnection);
					}
				}
			});
			
			result.check("Ticket #3780: Concurrent access failed.", 10000);
			
			writeConnection.commit();
		} finally {
			pool.releaseWriteConnection(writeConnection);
		}
	}

	private void insertValues(PooledConnection writeConnection, String a, String b) throws SQLException {
		insertStatement.executeUpdate(writeConnection, a, b);
	}

	protected ArrayList<String> selectWhereA(PooledConnection connection, String a) throws SQLException {
		ArrayList<String> bs = new ArrayList<>();
		ResultSet resultSet = selectWhereAStatement.executeQuery(connection, a);
		try {
			while (resultSet.next()) {
				bs.add(resultSet.getString(1));
			}
		} finally {
			resultSet.close();
		}
		return bs;
	}

	protected ArrayList<String> selectWhereB(PooledConnection connection, String b) throws SQLException {
		ArrayList<String> bs = new ArrayList<>();
		ResultSet resultSet = selectWhereBStatement.executeQuery(connection, b);
		try {
			while (resultSet.next()) {
				bs.add(resultSet.getString(1));
			}
		} finally {
			resultSet.close();
		}
		return bs;
	}
	
	/**
	 * Suite of tests.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(DatabaseTestSetup.getDBTest(TestDBConcurrentAccess.class));
	}

}
