/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import junit.framework.Test;

import com.top_logic.basic.sql.SQLQuery;

/**
 * Tescase for the {@link SQLQuery}.
 * 
 * @author    <a href=mailto:kha@top-logic.com>kha</a>
 */
public class TestSQLQuery extends AbstractConnectionTest {

	private static final String TABLE_NAME = ConnectionSetup.TABLE_NAME;

	/** Test Normal usage of Query */
    public void testNormal() throws SQLException {
        Connection con   = getConnection();
		SQLQuery query =
			new SQLQuery(con, "SELECT " + columnRef("i2") + " FROM " + tableRef() + " WHERE " + columnRef("i1") + "=2");
        
        try {
            ResultSet res = query.getResultSet();
            assertTrue(res.next());
            assertEquals(3242, res.getInt(1));
            assertFalse(res.next());
        } finally {
            query.close();
        }
        assertFalse(con.isClosed());
    }

	private String columnRef(String columnName) {
		return getSQLDialect().columnRef(columnName);
	}

    /** Test execute() and executeUpdate(). */
    public void testExecute() throws SQLException {
        Connection con   = getConnection();

		SQLQuery.execute(con,
			"UPDATE " + tableRef() + " SET " + columnRef("i2") + "=999  WHERE " + columnRef("i1") + "=2");
		SQLQuery.execute(con,
			"UPDATE " + tableRef() + " SET " + columnRef("i2") + "=3242 WHERE " + columnRef("i2") + "=999");

		assertEquals(1, SQLQuery.executeUpdate(con,
			"UPDATE " + tableRef() + " SET " + columnRef("i2") + "=999  WHERE " + columnRef("i1") + "=2"));
		assertEquals(0, SQLQuery.executeUpdate(con,
			"UPDATE " + tableRef() + " SET " + columnRef("i2") + "=3242 WHERE " + columnRef("i1") + "=333"));
		assertEquals(1, SQLQuery.executeUpdate(con,
			"UPDATE " + tableRef() + " SET " + columnRef("i2") + "=3242 WHERE " + columnRef("i2") + "=999"));
    }

	private String tableRef() {
		return getSQLDialect().tableRef(TABLE_NAME);
	}
    
    /** Test using a broken SQL Statemenet */
    public void testBrokenSQL() throws SQLException {
        Connection    con    = getConnection();
        try {
            new SQLQuery(con,"WASISTDENDAS MIT DER \"KNETE z='q");
            fail ("Expected SQLException");
        } catch (SQLException expected) { /* expected */  }
        assertFalse(con.isClosed());
    }

    /**
     * the suite of tests to execute.
     */
    public static Test suite () {
        return suite(TestSQLQuery.class);
    }
}
