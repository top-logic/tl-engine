/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** 
 * A helper class to avoid complex exception handling with jdbc.
 * 
 * This class can be dropped in favour of the functions found in {@link SQLH}.
 *<p>
 *  Whenever an Exception occurs all objects used so far are closed,
 *  So Server resources are freed (This even avoids some hard crashes
 *  with jdbc:odbc and MSSQL)
 *</p>
 * @author  Klaus Halfmann
 */
public class SQLQuery
{
    protected Connection  con;
    protected Statement   stm;
    protected ResultSet   res;

        /** Helper function to just execute a simple (update) query */
    static public boolean execute(Connection c, String sql)
        throws SQLException {
        Statement statm = null;
        boolean   result = false;
        try {
            statm = c.createStatement();
            result = statm.execute(sql);
        } finally {
            if (statm != null) {
                statm.close();
            }
        }

        return result;
    }

        /** Helper function to just execute a simple (update) query */
    static public int executeUpdate(Connection c, String sql)
        throws SQLException {
        Statement statm  = null;
        int       result = -1;
        try {
            statm = c.createStatement();
            result = statm.executeUpdate(sql);
        } finally {
            if (statm != null) {
                statm.close();
            }
        }

        return result;
    }

    /** empty Ctor for our subclasses */
    protected SQLQuery() { 
    }

    /** Creates a Query and execute the given query to produce a ResultSet. */
    public SQLQuery(Connection connection, String sql)
        throws SQLException
    {
        try
        {
            con = connection;
            stm = con.createStatement();
            res = stm.executeQuery(sql);
        } catch (SQLException sqle) {
            close();
            throw sqle;
        }
    }

    /** Creates new Query */
    /*
    public SQLQuery(DataSource ds, String sql)
    throws SQLException
    {
        this(ds.getConnection(), sql, CLOSE_CONNECTION);
    }
     */

    /** close everything that can be closed */
    public void close()
    throws SQLException
    {
        if (res != null) {
            res.close();
            res = null;
        }

        if (stm != null) {
            stm.close();
            stm = null;
        }
        con = null;
    }

    /** acessor for resultset, do not close it, but close the query. */
    public ResultSet getResultSet() {
        return res;
    }

}
