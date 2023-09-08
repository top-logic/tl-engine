/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.db.model.util;

import static com.top_logic.basic.db.model.DBSchemaFactory.*;
import static com.top_logic.basic.util.StopWatch.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.DatabaseTestSetup;
import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.Logger;
import com.top_logic.basic.db.model.DBColumn;
import com.top_logic.basic.db.model.DBPrimary;
import com.top_logic.basic.db.model.DBSchema;
import com.top_logic.basic.db.model.DBSchemaFactory;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.db.model.util.DBSchemaUtils;
import com.top_logic.basic.sql.BatchController;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.util.StopWatch;

/**
 * Test case for {@link ChunkedRead}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BenchChunkedRead extends BasicTestCase {

	private static final int ROW_COUNT = 100000;

	private static final int CHUNK_SIZE = 567;

	public void testReadAll() throws SQLException {
		String tableName = "TestChunkedRead";

		DBSchema schema = createSchema(tableName);

		createDB(schema);

		fillTable(tableName);

		DBTable table = schema.getTable(tableName);

		doReadCursor(table);

		doReadChunked(table);
	}

	private void doReadChunked(DBTable table) throws SQLException {
		StopWatch watch = StopWatch.createStartedWatch();

		ConnectionPool pool = ConnectionPoolRegistry.getDefaultConnectionPool();
		DBHelper sqlDialect = pool.getSQLDialect();
		PooledConnection connection = pool.borrowReadConnection();
		try {
			ChunkedRead chunkedRead = new ChunkedRead(sqlDialect, connection, table, CHUNK_SIZE);
			ResultSet resultSet = chunkedRead.executeQuery();
			try {
				int rowsRead = 0;
				while (resultSet.next()) {
					rowsRead++;
				}
				assertEquals(ROW_COUNT, rowsRead);
			} finally {
				resultSet.close();
			}
		} finally {
			pool.releaseReadConnection(connection);
		}

		Logger.info("Chunked read in " + toStringNanos(watch.getElapsed()), BenchChunkedRead.class);
	}

	private void doReadCursor(DBTable table) throws SQLException {
		StopWatch watch = StopWatch.createStartedWatch();

		ConnectionPool pool = ConnectionPoolRegistry.getDefaultConnectionPool();
		DBHelper sqlDialect = pool.getSQLDialect();
		PooledConnection connection = pool.borrowReadConnection();
		try {
			PreparedStatement statement =
				connection.prepareStatement("SELECT * FROM " + sqlDialect.tableRef(table.getDBName()),
					ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			try {
				sqlDialect.setFetchSize(statement, CHUNK_SIZE);

				ResultSet resultSet = statement.executeQuery();
				try {
					int rowsRead = 0;
					while (resultSet.next()) {
						resultSet.getObject(1);
						resultSet.getObject(2);
						resultSet.getObject(3);

						rowsRead++;
					}
					assertEquals(ROW_COUNT, rowsRead);
				} finally {
					resultSet.close();
				}
			} finally {
				statement.close();
			}
		} finally {
			pool.releaseReadConnection(connection);
		}

		Logger.info("Cursor read in " + toStringNanos(watch.getElapsed()), BenchChunkedRead.class);
	}

	private void fillTable(String tableName) throws SQLException {
		StopWatch watch = StopWatch.createStartedWatch();

		ConnectionPool pool = ConnectionPoolRegistry.getDefaultConnectionPool();
		DBHelper sqlDialect = pool.getSQLDialect();
		PooledConnection connection = pool.borrowWriteConnection();
		try {
			PreparedStatement statement =
				connection.prepareStatement(
					"INSERT INTO " + sqlDialect.tableRef(tableName) + " VALUES (?,?,?)");
			try {
				int value2 = 0;

				BatchController batch = new BatchController(statement, CHUNK_SIZE);

				Random rnd = new Random(42);
				for (int n = 0; n < ROW_COUNT; n++) {
					String value1 = randomString(rnd, 100, true, false, false, false);
					double value3 = rnd.nextDouble();

					int parameterIndex = 1;
					statement.setString(parameterIndex++, value1);
					statement.setInt(parameterIndex++, value2);
					statement.setDouble(parameterIndex++, value3);

					batch.addBatch();
					value2++;
				}
				batch.finishBatch();

				connection.commit();
			} finally {
				statement.close();
			}
		} finally {
			pool.releaseWriteConnection(connection);
		}

		Logger.info("Fill table in " + toStringNanos(watch.getElapsed()), BenchChunkedRead.class);
	}

	private DBSchema createSchema(String tableName) {
		DBSchema schema = DBSchemaFactory.createDBSchema();
		DBTable table = DBSchemaFactory.createTable(tableName);

		DBColumn c1 = DBSchemaFactory.createColumn("c1");
		c1.setType(DBType.STRING);
		c1.setSize(100);
		c1.setMandatory(true);
		table.getColumns().add(c1);

		DBColumn c2 = DBSchemaFactory.createColumn("c2");
		c2.setType(DBType.INT);
		c2.setMandatory(true);
		table.getColumns().add(c2);

		DBColumn c3 = DBSchemaFactory.createColumn("c3");
		c3.setType(DBType.DOUBLE);
		c3.setMandatory(false);
		table.getColumns().add(c3);

		DBPrimary primary = DBSchemaFactory.createPrimary();
		primary.getColumnRefs().add(ref(c1));
		primary.getColumnRefs().add(ref(c2));
		table.setPrimaryKey(primary);

		schema.getTables().add(table);
		return schema;
	}

	private void createDB(DBSchema schema) throws SQLException {
		ConnectionPool pool = ConnectionPoolRegistry.getDefaultConnectionPool();
		DBSchemaUtils.recreateTables(pool, schema);
	}

	/**
	 * Suite of tests.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(DatabaseTestSetup.getDBTest(BenchChunkedRead.class,
			test.com.top_logic.basic.DatabaseTestSetup.DEFAULT_DB));
	}

}
