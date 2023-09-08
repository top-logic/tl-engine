/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** A PreparedQuery Can be used to specify positional parmeters.
 *<p>
 *  You must call executeQuery() before you can use the ResultSet.
 *</p>
 *
 * @author  Klaus Halfmann
 */
public class PreparedQuery extends SQLQuery
{
    PreparedStatement  pstm;

      /** Create a new PreparedQuery, with given sql statemet.
       *<p>
       *  You usually shoud proceed as follows:<pre>
       *   PreparedQuery q = new PreparedQuery(" .... WHERE X=? AND Y=? ...");
       *   PreparedStatement pstm = q.getPreparedStatement();
       *   pstm.setXXX(1, ...);
       *   ...
       *   ResultSet res = pstm.executeQuery();
       *   ...
       *   q.close();
       * </pre>
       *</p>
       */
    public PreparedQuery(Connection connection, String sql) 
        throws SQLException
    {
    	try
    	{
    		con = connection;
    		stm = pstm = con.prepareStatement(sql);
    	} 
    	catch (SQLException sqle) {
    		close();
    		throw sqle;
    	}  
    }
  
    /** Creates new PreparedQuery from datasource */
    /*
    public PreparedQuery(DataSource ds, String sql)
        throws SQLException
    {
        this(ds.getConnection(), sql, CLOSE_CONNECTION);
    }
    */
    
      /** Call this function after you filled all the parameters.  
       */
    public ResultSet executeQuery()
        throws SQLException
    {
        try
        {
            res = pstm.executeQuery();
        } catch (SQLException sqle) {
            close();
            throw sqle;
        }  
        return res;
  }

    /** Call this function after you filled all the parameters.  
     */
    public boolean execute()
        throws SQLException
    {
        try
        {
            return pstm.execute();
        } catch (SQLException sqle) {
            close();
            throw sqle;
        }  
    }

    /** Call this function after you filled all the parameters.  
     */
    public int executeUpdate()
        throws SQLException
    {
        int result = -1;
        try
        {
            result = pstm.executeUpdate();
        } catch (SQLException sqle) {
            close();
            throw sqle;
        }  
        return result;
    }

    /** overriden to clear out pstm */
    @Override
	public void close()
        throws SQLException
    {
        // give gc a change to git rid of pstm quickly
        pstm = null;
        super.close();
    }
  
    /** acessor fot the PreparedStatement */
    public PreparedStatement getPreparedStatement()
    {
        return pstm;
    }
}
