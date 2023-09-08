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

import com.top_logic.basic.sql.PreparedQuery;

/**
 * Testcase for the class {PreparedQuery}.
 * 
 * @author    <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public class TestPreparedQuery extends AbstractConnectionTest {

	/** Test Normal usage of PreparedQuery */
    public void testNormal() throws SQLException {
        Connection    con    = getConnection();
        PreparedQuery pQuery = new PreparedQuery(con,
			"SELECT " + columnRef("i2") + " FROM " + tableRef() + " WHERE " + columnRef("i1") + "=?");
        try {
            pQuery.getPreparedStatement().setInt(1, 2);
            ResultSet res = pQuery.executeQuery();
            assertTrue(res.next());
            assertEquals(3242, res.getInt(1));
            assertFalse(res.next());
        } finally {
            pQuery.close();
        }
        assertFalse(con.isClosed());
    }

	private String tableRef() {
		return getSQLDialect().tableRef(ConnectionSetup.TABLE_NAME);
	}

	private String columnRef(String columnName) {
		return getSQLDialect().columnRef(columnName);
	}

    /**
     * Test executing an UPDATE Statement
     */
    public void testExecute() throws SQLException {
        Connection    con    = getConnection();
        PreparedQuery pQuery = new PreparedQuery(con,
			"UPDATE " + tableRef() + " SET " + columnRef("i2") + "=? WHERE " + columnRef("i1") + "=?");
        try {
            pQuery.getPreparedStatement().setInt(1, 999);
            pQuery.getPreparedStatement().setInt(2, 9);
            pQuery.execute();
            pQuery.getPreparedStatement().setInt(1, 3242);
            pQuery.getPreparedStatement().setInt(2, 9);
            pQuery.execute();
        } finally {
            pQuery.close();
        }
        assertFalse(con.isClosed());
    }

    /** Test using a broken SQL for a Query */
    public void testBrokenQuery() throws SQLException {
        Connection    con    = getConnection();
        try {
            PreparedQuery pQuery = new PreparedQuery(con,
				"SELECT " + columnRef("2i") + " FROM pervers WHERE ?=" + columnRef("i1") + "");
            /* ResultSet res = */ pQuery.executeQuery();
            fail ("Expected SQLException");
        } catch (SQLException expected) { /* expected */  }
        assertFalse(con.isClosed());
    }

    /** Test using a broken SQL for Execution */
    public void testBrokenExecute() throws SQLException {
        Connection    con    = getConnection();
        try {
            PreparedQuery pQuery = new PreparedQuery(con,
				"UPDATE " + tableRef() + " SET " + columnRef("i2") + "=? WHERE " + columnRef("i1") + "=?");
            pQuery.getPreparedStatement().setInt(1, 999);
            pQuery.getPreparedStatement().setInt(2, 9);
            pQuery.getPreparedStatement().clearParameters();
            pQuery.execute();
            fail ("Expected SQLException");
        } catch (SQLException expected) { /* expected */  }
        assertFalse(con.isClosed());
    }

    /**
     * the suite of tests to execute.
     */
    public static Test suite () {
        return suite(TestPreparedQuery.class);
    }

}
