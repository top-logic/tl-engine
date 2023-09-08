/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.db.model.util;

import static com.top_logic.basic.db.model.DBSchemaFactory.*;

import java.sql.SQLException;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.DatabaseTestSetup;
import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.db.model.DBColumn;
import com.top_logic.basic.db.model.DBSchema;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.db.model.util.DBSchemaUtils;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;

/**
 * Test case for {@link DBSchemaUtils}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestDBSchemaUtils extends BasicTestCase {

	public void testExistence() throws SQLException {
		DBSchema schema = createDBSchema();
		DBTable table = createTable("TestExistence");
		DBColumn foo = createColumn("foo");
		foo.setSize(128L);
		foo.setType(DBType.STRING);
		table.getColumns().add(foo);

		DBColumn bar = createColumn("bar");
		bar.setType(DBType.INT);
		table.getColumns().add(bar);

		schema.getTables().add(table);

		ConnectionPool pool = ConnectionPoolRegistry.getDefaultConnectionPool();

		PooledConnection read = pool.borrowReadConnection();
		try {
			assertFalse(DBSchemaUtils.exists(read, table));
		} finally {
			pool.releaseReadConnection(read);
		}

		PooledConnection write = pool.borrowWriteConnection();
		try {
			DBSchemaUtils.create(write, table);
		} finally {
			pool.releaseWriteConnection(write);
		}

		PooledConnection read2 = pool.borrowReadConnection();
		try {
			assertTrue(DBSchemaUtils.exists(read2, table));
		} finally {
			pool.releaseReadConnection(read2);
		}

		PooledConnection write2 = pool.borrowWriteConnection();
		try {
			DBSchemaUtils.resetTables(write2, schema, true);
		} finally {
			pool.releaseWriteConnection(write2);
		}

		PooledConnection read3 = pool.borrowReadConnection();
		try {
			assertTrue(DBSchemaUtils.exists(read3, table));
		} finally {
			pool.releaseReadConnection(read3);
		}

		PooledConnection write3 = pool.borrowWriteConnection();
		try {
			DBSchemaUtils.resetTables(write3, schema, false);
		} finally {
			pool.releaseWriteConnection(write3);
		}

		PooledConnection read4 = pool.borrowReadConnection();
		try {
			assertFalse(DBSchemaUtils.exists(read4, table));
		} finally {
			pool.releaseReadConnection(read4);
		}
	}

	/**
	 * Suite of tests.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(DatabaseTestSetup.getDBTest(TestDBSchemaUtils.class));
	}

}
