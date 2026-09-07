/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.sql;

import junit.framework.TestCase;

import com.top_logic.basic.sql.CollationHint;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PostgreSQLHelper;

/** Tests the collation applied to the NATURAL (non-binary) ordering hint on PostgreSQL. */
public class TestPostgreSQLNaturalCollation extends TestCase {

	/** Natural (non-binary) ordering on PostgreSQL uses the case-insensitive collation. */
	public void testNaturalOrderingUsesCiCollation() {
		DBHelper pg = DBHelper.createDefaultInstance(PostgreSQLHelper.class);
		StringBuilder sql = new StringBuilder();
		pg.appendCollatedExpression(sql, "COL", CollationHint.NATURAL);
		assertTrue(sql.toString(), sql.toString().contains("COLLATE \"tl_ci\""));
	}

	/** Binary ordering on PostgreSQL keeps the case-sensitive {@code COLLATE "C"}. */
	public void testBinaryOrderingUsesCCollation() {
		DBHelper pg = DBHelper.createDefaultInstance(PostgreSQLHelper.class);
		StringBuilder sql = new StringBuilder();
		pg.appendCollatedExpression(sql, "COL", CollationHint.BINARY);
		assertTrue(sql.toString(), sql.toString().contains("COLLATE \"C\""));
	}
}
