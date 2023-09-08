/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.db.model.sql;

import static com.top_logic.basic.db.sql.SQLFactory.*;
import static java.util.Arrays.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.AssertionFailedError;
import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.DatabaseTestSetup;
import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.db.model.DBColumn;
import com.top_logic.basic.db.model.DBSchema;
import com.top_logic.basic.db.model.DBSchemaFactory;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.db.model.util.DBSchemaUtils;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLBoolean;
import com.top_logic.basic.db.sql.SQLInsert;
import com.top_logic.basic.db.sql.SQLQuery;
import com.top_logic.basic.db.sql.SQLSelect;
import com.top_logic.basic.sql.CollationHint;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.H2Helper;
import com.top_logic.basic.sql.PooledConnection;

/**
 * Test case that checks binary and non-binary ordering of {@link DBType#STRING}
 * columns.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestStringOrdering extends BasicTestCase {

	public void testBinaryOrderingOfBinaryColumn() throws SQLException {
		doTestOrdering(true, true);
	}

	public void testNaturalOrderingOfNaturalColumn() throws SQLException {
		try {
			doTestOrdering(false, false);
			if (ConnectionPoolRegistry.getDefaultConnectionPool().getSQLDialect() instanceof H2Helper) {
				fail("Has known bug been fixed? Expected error in natural ordering of natural columns.");
			}
		} catch (AssertionFailedError ex) {
			if (ConnectionPoolRegistry.getDefaultConnectionPool().getSQLDialect() instanceof H2Helper) {
				BasicTestCase.assertContains("Lists differ at index 2. Element expected 'äc', element actual 'ba'",
					ex.getMessage());
			} else {
				throw ex;
			}
		}
	}

	public void testBinaryOrderingOfNaturalColumn() throws SQLException {
		doTestOrdering(false, true);
	}

	public void testNaturalOrderingOfBinaryColumn() throws SQLException {
		try {
			doTestOrdering(true, false);
			if (ConnectionPoolRegistry.getDefaultConnectionPool().getSQLDialect() instanceof H2Helper) {
				fail("Known bug has been fixed? Expected Natural ordering of binary columns to fail with h2.");
			}
		} catch (AssertionFailedError ex) {
			if (ConnectionPoolRegistry.getDefaultConnectionPool().getSQLDialect() instanceof H2Helper) {
				// Known bug.
				BasicTestCase.assertContains("Lists differ at index 2. Element expected 'äc', element actual 'ba'",
					ex.getMessage());
			} else {
				throw ex;
			}
		}
	}

	private List<String> getBinaryValues() {
		List<String> values = new ArrayList<>();
		for (char ch = ' '; ch <= 127; ch++) {
			String value = String.valueOf(ch);
			values.add(value);
		}
		return values;
	}

	private List<String> getNaturalValues() {
		return asList("aa", "Ab", "äc", "Äd", "áe", "Áf", "ba", "Bb");
	}

	private void doTestOrdering(boolean binaryColumn, boolean binary) throws SQLException {
		List<String> values;
		if (binary) {
			values = getBinaryValues();
		} else {
			values = getNaturalValues();
		}

		DBSchema schema = DBSchemaFactory.createDBSchema();
		DBTable table = DBSchemaFactory.createTable("TestOrdering");
		schema.getTables().add(table);

		DBColumn col = DBSchemaFactory.createColumn("x");
		col.setType(DBType.STRING);
		col.setBinary(binaryColumn);
		col.setMandatory(true);
		col.setSize(254);
		table.getColumns().add(col);

		ConnectionPool pool = ConnectionPoolRegistry.getDefaultConnectionPool();
		DBSchemaUtils.recreateTables(pool, schema);

		insertValues(pool, table, col, values);

		List<String> result = loadValues(pool, table, col, binary ? CollationHint.BINARY : CollationHint.NATURAL);
		Collections.reverse(result);
		assertEquals(values, result);

		DBSchemaUtils.dropTables(pool, schema);
	}

	private void insertValues(ConnectionPool pool, DBTable table, DBColumn col, List<String> values) throws SQLException {
		SQLQuery<SQLInsert> insert = query(
			asList(parameterDef(DBType.STRING, "x")), 
			insert(
				table(table.getDBName(), "t"), 
				asList(col.getDBName()), 
				asList(parameter(DBType.STRING, "x"))));
		CompiledStatement statement = insert.toSql(pool.getSQLDialect());

		PooledConnection connection = pool.borrowWriteConnection();
		try {
			for (int n = 0, cnt = values.size(); n < cnt; n++) {
				String value = values.get(n);
				int touchedRows = statement.executeUpdate(connection, value);

				assertEquals(1, touchedRows);
			}
			connection.commit();
		} finally {
			pool.releaseWriteConnection(connection);
		}
	}

	private List<String> loadValues(ConnectionPool pool, DBTable table, DBColumn col, CollationHint collation) throws SQLException {
		List<String> result = new ArrayList<>();
		{
			SQLQuery<SQLSelect> select = query(
				select(false,
					asList(columnDef(column("t", col.getDBName(), col.isMandatory()), "c")),
					table(table.getDBName(), "t"), 
					SQLBoolean.TRUE,
					asList(order(true, collation, column("t", col.getDBName(), col.isMandatory())))));
			CompiledStatement statement = select.toSql(pool.getSQLDialect());

			PooledConnection connection = pool.borrowReadConnection();
			try {
				ResultSet resultSet = statement.executeQuery(connection);
				try {
					while (resultSet.next()) {
						result.add(resultSet.getString(1));
					}
				} finally {
					resultSet.close();
				}
			} finally {
				pool.releaseReadConnection(connection);
			}
		}
		return result;
	}

	/**
	 * Suite of tests.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(DatabaseTestSetup.getDBTest(TestStringOrdering.class));
	}

}
