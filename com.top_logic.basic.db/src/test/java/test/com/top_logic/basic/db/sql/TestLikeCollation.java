/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.db.sql;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import junit.framework.TestCase;

import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PostgreSQLHelper;

/** Tests that LIKE operands get a deterministic collation on PostgreSQL. */
public class TestLikeCollation extends TestCase {

	/** On PostgreSQL a LIKE operand is collated with COLLATE "C" so pattern matching works. */
	public void testPostgresLikeGetsDeterministicCollation() {
		DBHelper pg = DBHelper.createDefaultInstance(PostgreSQLHelper.class);
		CompiledStatement statement = query(
			select(columns(columnDef("X")), table("T"), like(column("X"), "pat"))).toSql(pg);
		String sql = statement.toString();
		assertTrue(sql, sql.contains("COLLATE \"C\" LIKE"));
	}

}
