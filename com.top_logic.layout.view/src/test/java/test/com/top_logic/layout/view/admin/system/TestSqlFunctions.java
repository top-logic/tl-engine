/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.admin.system;

import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.layout.view.admin.system.I18NConstants;
import com.top_logic.layout.view.admin.system.SqlFunctions;
import com.top_logic.layout.view.admin.system.SqlHistoryEntry;
import com.top_logic.layout.view.admin.system.SqlResult;
import com.top_logic.layout.view.admin.system.SqlResult.Column;
import com.top_logic.layout.view.admin.system.SqlResult.Row;
import com.top_logic.layout.view.admin.system.SqlStatementKind;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * Test of {@link SqlFunctions}.
 */
@SuppressWarnings("javadoc")
public class TestSqlFunctions extends BasicTestCase {

	private static final String TABLE = "TEST_SQL_CONSOLE";

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		SqlFunctions.update("CREATE TABLE " + TABLE + " (ID INTEGER, NAME VARCHAR(20), AMOUNT DECIMAL(10, 2))");
		for (int n = 1; n <= 5; n++) {
			SqlFunctions.update("INSERT INTO " + TABLE + " VALUES (" + n + ", 'name" + n + "', " + n + ".5)");
		}
	}

	@Override
	protected void tearDown() throws Exception {
		SqlFunctions.update("DROP TABLE " + TABLE);
		super.tearDown();
	}

	public void testQuery() {
		SqlResult result = SqlFunctions.query("SELECT ID, NAME, AMOUNT, NAME FROM " + TABLE + " ORDER BY ID", 100);

		assertTrue(SqlFunctions.isQuery(result));
		assertFalse(result.truncated());

		List<Column> columns = SqlFunctions.columns(result);
		assertEquals(4, columns.size());
		assertEquals("ID", SqlFunctions.columnLabel(columns.get(0)));
		assertEquals("NAME", SqlFunctions.columnLabel(columns.get(1)));
		// Two columns of the same label still have distinct names.
		assertEquals(SqlFunctions.columnLabel(columns.get(1)), SqlFunctions.columnLabel(columns.get(3)));
		assertFalse(SqlFunctions.columnName(columns.get(1)).equals(SqlFunctions.columnName(columns.get(3))));

		assertEquals(TypeSpec.LONG_TYPE, columns.get(0).typeSpec());
		assertEquals(TypeSpec.STRING_TYPE, columns.get(1).typeSpec());
		// A decimal number with fraction digits is read exactly.
		assertEquals(TypeSpec.STRING_TYPE, columns.get(2).typeSpec());

		List<Row> rows = SqlFunctions.rows(result);
		assertEquals(5, rows.size());
		assertEquals(Long.valueOf(1), SqlFunctions.value(rows.get(0), columns.get(0)));
		assertEquals("name1", SqlFunctions.value(rows.get(0), columns.get(1)));
		assertEquals("1.50", SqlFunctions.value(rows.get(0), columns.get(2)));
		assertEquals(Long.valueOf(5), SqlFunctions.value(rows.get(4), columns.get(0)));

		assertNotNull(SqlFunctions.message(result));
	}

	public void testTruncation() {
		SqlResult result = SqlFunctions.query("SELECT * FROM " + TABLE, 3);
		assertEquals(3, SqlFunctions.rows(result).size());
		assertTrue(result.truncated());

		SqlResult exact = SqlFunctions.query("SELECT * FROM " + TABLE, 5);
		assertEquals(5, SqlFunctions.rows(exact).size());
		assertFalse(exact.truncated());
	}

	public void testUpdate() {
		SqlResult result = SqlFunctions.update("UPDATE " + TABLE + " SET NAME = 'changed' WHERE ID <= 2");
		assertFalse(SqlFunctions.isQuery(result));
		assertEquals(SqlStatementKind.UPDATE, result.kind());
		assertEquals(2, result.updateCount());
		assertNotNull(SqlFunctions.message(result));

		assertEquals(2, countChanged());
	}

	public void testSimulateRollsBack() {
		SqlResult result = SqlFunctions.simulate("UPDATE " + TABLE + " SET NAME = 'changed'");
		assertEquals(SqlStatementKind.SIMULATION, result.kind());
		assertEquals(5, result.updateCount());

		assertEquals(0, countChanged());
	}

	public void testSyntaxError() {
		try {
			SqlFunctions.query("SELEC * FROM " + TABLE, 10);
			fail("Syntax error not reported.");
		} catch (TopLogicException ex) {
			assertEquals(I18NConstants.ERROR_SQL_REJECTED__MESSAGE.fill("x").plain(), ex.getErrorKey().plain());
			assertTrue(ex.getMessage(), ex.getMessage().contains("SELEC"));
		}

		// The connection of the failed statement does not disturb the next one.
		assertEquals(5, SqlFunctions.rows(SqlFunctions.query("SELECT * FROM " + TABLE, 10)).size());
	}

	public void testConstraintViolation() {
		try {
			SqlFunctions.update("INSERT INTO " + TABLE + " VALUES (6, 'a name much longer than twenty characters', 1)");
			fail("Constraint violation not reported.");
		} catch (TopLogicException ex) {
			assertEquals(I18NConstants.ERROR_SQL_REJECTED__MESSAGE.fill("x").plain(), ex.getErrorKey().plain());
		}
		assertEquals(5, SqlFunctions.rows(SqlFunctions.query("SELECT * FROM " + TABLE, 10)).size());
	}

	public void testNoStatement() {
		try {
			SqlFunctions.query("  ", 10);
			fail("Empty statement accepted.");
		} catch (TopLogicException ex) {
			assertEquals(I18NConstants.ERROR_SQL_NO_STATEMENT, ex.getErrorKey());
		}
	}

	public void testHistory() {
		List<SqlHistoryEntry> history = SqlFunctions.remember(null, "SELECT 1", SqlStatementKind.QUERY);
		history = SqlFunctions.remember(history, " UPDATE X SET A = 1 ", SqlStatementKind.SIMULATION);
		history = SqlFunctions.remember(history, "", SqlStatementKind.QUERY);

		assertEquals(2, history.size());
		assertEquals("UPDATE X SET A = 1", SqlFunctions.historyStatement(history.get(0)));
		assertEquals(SqlStatementKind.SIMULATION, SqlFunctions.historyKind(history.get(0)));
		assertEquals(Integer.valueOf(2), SqlFunctions.historyNumber(history.get(0)));
		assertEquals("SELECT 1", SqlFunctions.historyStatement(history.get(1)));

		for (int n = 0; n < SqlFunctions.MAX_HISTORY; n++) {
			history = SqlFunctions.remember(history, "SELECT " + n, SqlStatementKind.QUERY);
		}
		assertEquals(SqlFunctions.MAX_HISTORY, history.size());
		assertEquals("SELECT " + (SqlFunctions.MAX_HISTORY - 1), SqlFunctions.historyStatement(history.get(0)));
	}

	private static int countChanged() {
		SqlResult changed = SqlFunctions.query("SELECT ID FROM " + TABLE + " WHERE NAME = 'changed'", 10);
		return SqlFunctions.rows(changed).size();
	}

	/**
	 * The suite of tests.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestSqlFunctions.class,
			ServiceTestSetup.createStarterFactoryForModules(TypeIndex.Module.INSTANCE, ModelService.Module.INSTANCE));
	}
}
