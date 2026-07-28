/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.sql;

import junit.framework.TestCase;

import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PostgreSQLHelper;

/** Tests the case-insensitive collation emitted for non-binary string columns on PostgreSQL. */
public class TestPostgreSQLCollation extends TestCase {

	private String columnDDL(DBType type, int size, boolean binary) {
		DBHelper pg = DBHelper.createDefaultInstance(PostgreSQLHelper.class);
		StringBuilder ddl = new StringBuilder();
		pg.appendDBType(ddl, type, "col", size, 0, false, binary);
		return ddl.toString();
	}

	/**
	 * A non-binary {@code VARCHAR} column is emitted with the case-insensitive collation.
	 */
	public void testNonBinaryVarcharIsCiCollated() {
		String ddl = columnDDL(DBType.STRING, 100, false);
		assertTrue(ddl, ddl.contains("COLLATE \"tl_ci\""));
	}

	/**
	 * A binary {@code VARCHAR} column keeps the case-sensitive {@code COLLATE "C"}.
	 */
	public void testBinaryVarcharIsCCollated() {
		String ddl = columnDDL(DBType.STRING, 100, true);
		assertTrue(ddl, ddl.contains("COLLATE \"C\""));
		assertFalse(ddl, ddl.contains("tl_ci"));
	}

	/**
	 * A non-binary CLOB ({@code TEXT}) column is emitted with the case-insensitive collation.
	 */
	public void testNonBinaryClobIsCiCollated() {
		assertTrue(columnDDL(DBType.CLOB, 0, false).contains("COLLATE \"tl_ci\""));
	}

}
