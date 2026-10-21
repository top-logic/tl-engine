/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.db.sql;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import junit.framework.TestCase;

import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLUpdate;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.H2Helper;
import com.top_logic.basic.sql.MySQLHelper;

/**
 * Test that an {@link SQLUpdate} reading a column assigned earlier in the same statement is
 * rejected in a dialect with {@link DBHelper#updateAssignmentsSeeNewValues()}.
 */
@SuppressWarnings("javadoc")
public class TestUpdateAssignmentOrder extends TestCase {

	private static DBHelper mysql() {
		return DBHelper.createDefaultInstance(MySQLHelper.class);
	}

	private static DBHelper h2() {
		return DBHelper.createDefaultInstance(H2Helper.class);
	}

	private static SQLUpdate updateColumns(String[] columns, String[] valueColumns) {
		SQLExpression[] values = new SQLExpression[valueColumns.length];
		for (int n = 0; n < valueColumns.length; n++) {
			values[n] = column(valueColumns[n]);
		}
		return update(table("T"), literalTrueLogical(), columnNames(columns), expressions(values));
	}

	public void testDialectCapability() {
		assertTrue(mysql().updateAssignmentsSeeNewValues());
		assertFalse(h2().updateAssignmentsSeeNewValues());
	}

	public void testSwapRejectedOnMySQL() {
		SQLUpdate swap = updateColumns(new String[] { "a", "b" }, new String[] { "b", "a" });
		try {
			query(swap).toSql(mysql());
			fail("Swapping columns in a single UPDATE must be rejected on MySQL.");
		} catch (IllegalArgumentException ex) {
			String message = ex.getMessage();
			assertTrue(message, message.contains("'a'"));
			assertTrue(message, message.contains("'b'"));
			assertTrue(message, message.contains(MySQLHelper.class.getName()));
		}
	}

	public void testSwapRenderedOnH2() {
		SQLUpdate swap = updateColumns(new String[] { "a", "b" }, new String[] { "b", "a" });
		String sql = query(swap).toSql(h2()).toString();
		assertTrue(sql, sql.contains("SET \"a\"=\"b\",\"b\"=\"a\""));
	}

	public void testSingleAssignmentOnMySQL() {
		SQLUpdate update = updateColumns(new String[] { "a" }, new String[] { "b" });
		String sql = query(update).toSql(mysql()).toString();
		assertTrue(sql, sql.contains("SET `a`=`b`"));
	}

	public void testReadBeforeAssignmentOnMySQL() {
		SQLUpdate update = updateColumns(new String[] { "a", "b" }, new String[] { "b", "c" });
		String sql = query(update).toSql(mysql()).toString();
		assertTrue(sql, sql.contains("SET `a`=`b`,`b`=`c`"));
	}

	public void testReadAfterAssignmentRejectedOnMySQL() {
		SQLUpdate update = updateColumns(new String[] { "a", "c" }, new String[] { "b", "a" });
		try {
			query(update).toSql(mysql());
			fail("Reading a column after it is assigned in the same UPDATE must be rejected on MySQL.");
		} catch (IllegalArgumentException ex) {
			String message = ex.getMessage();
			assertTrue(message, message.contains("'c'"));
			assertTrue(message, message.contains("'a'"));
		}
	}

	public void testReadAfterAssignmentInNestedExpressionRejectedOnMySQL() {
		SQLUpdate update = update(table("T"), literalTrueLogical(),
			columnNames("a", "c"),
			expressions(column("b"), sqlCase(isNull(column("b")), literalInteger(0), column("a"))));
		try {
			query(update).toSql(mysql());
			fail("Reading a column after it is assigned in the same UPDATE must be rejected on MySQL.");
		} catch (IllegalArgumentException ex) {
			// expected
		}
	}

}
