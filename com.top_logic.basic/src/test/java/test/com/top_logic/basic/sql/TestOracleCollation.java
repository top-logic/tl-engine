/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.sql;

import junit.framework.TestCase;

import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.Oracle10Helper;

/** Tests the case-insensitive collation emitted for non-binary string columns on Oracle. */
public class TestOracleCollation extends TestCase {

	private String columnDDL(DBType type, int size, boolean binary) {
		DBHelper oracle = DBHelper.createDefaultInstance(Oracle10Helper.class);
		StringBuilder ddl = new StringBuilder();
		oracle.appendDBType(ddl, type, "col", size, 0, false, binary);
		return ddl.toString();
	}

	/** A non-binary string column is NVARCHAR2 with the case-insensitive BINARY_CI collation. */
	public void testNonBinaryStringIsBinaryCi() {
		String ddl = columnDDL(DBType.STRING, 100, false);
		assertTrue(ddl, ddl.contains("NVARCHAR2") && ddl.contains("COLLATE BINARY_CI"));
	}

	/** A binary string column has no case-insensitive collation. */
	public void testBinaryStringHasNoCiCollation() {
		assertFalse(columnDDL(DBType.STRING, 100, true).contains("BINARY_CI"));
	}

	/** A non-binary char column is NCHAR(1) with the case-insensitive BINARY_CI collation. */
	public void testNonBinaryCharIsBinaryCi() {
		String ddl = columnDDL(DBType.CHAR, 1, false);
		assertTrue(ddl, ddl.contains("NCHAR(1)") && ddl.contains("COLLATE BINARY_CI"));
	}

	/** A binary char column has no case-insensitive collation. */
	public void testBinaryCharHasNoCiCollation() {
		assertFalse(columnDDL(DBType.CHAR, 1, true).contains("BINARY_CI"));
	}

	/** A non-binary CLOB (NCLOB) column has no collation, since Oracle does not allow it on LOBs. */
	public void testNonBinaryClobHasNoCiCollation() {
		String ddl = columnDDL(DBType.CLOB, 0, false);
		assertTrue(ddl, ddl.contains("NCLOB"));
		assertFalse(ddl, ddl.contains("BINARY_CI"));
	}

}
