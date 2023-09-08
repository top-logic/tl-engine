/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;

import javax.sql.DataSource;

import com.top_logic.basic.Configuration;
import com.top_logic.basic.Logger;

/**
 * A quick hack to implement the Datasource interface for any Driver.
 * 
 * DO NOT USE expecept there is no alternative,
 * (as most Drivers these day contain a Datasource implementation).
 * 
 * For ODBC use the <code>sun.jdbc.odbc.ee.DataSource</code>.
 * See %JAVA_HOME%docs/guide/jdbc/getstart/bridge.html
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class JDBCDriverDataSource implements DataSource, Serializable {

    private String url;

    private String driver;

    private String user;

    private String password;

    /** 
     * Create a new data source out of the configuration.
     */
    public JDBCDriverDataSource(Properties someProps) {
        this.url      = someProps.getProperty("url");
        this.driver   = someProps.getProperty("driver");
        this.user     = someProps.getProperty("user");
        this.password = someProps.getProperty("password");
    }
    
    /** 
     * Create a new data source from Properties.
     */
    public JDBCDriverDataSource() {
        this (Configuration.getConfiguration(
                JDBCDriverDataSource.class).getProperties());
    }

    /** 
     * @see javax.sql.DataSource#getConnection()
     */
    @Override
	public Connection getConnection() throws SQLException {
        return (this.getConnection(this.user, this.password));
    }

    /** 
     * @see javax.sql.DataSource#getConnection(java.lang.String, java.lang.String)
     */
    @Override
	public Connection getConnection(String aUserName, String aPassword) throws SQLException {
        try {
            Class.forName(this.driver);
        }
        catch (Exception ex) {
            Logger.error("Driver class not found: " + this.driver, ex, this);
        }

        return (DriverManager.getConnection(this.url, aUserName, aPassword));
    }

    /** 
     * @see javax.sql.DataSource#getLoginTimeout()
     */
    @Override
	public int getLoginTimeout() throws SQLException {
        return 1000;
    }

    /** 
     * @see javax.sql.DataSource#getLogWriter()
     */
    @Override
	public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    /** 
     * @see javax.sql.DataSource#setLoginTimeout(int)
     */
    @Override
	public void setLoginTimeout(int i) throws SQLException {
        // not implemented here.
    }

    /** 
     * @see javax.sql.DataSource#setLogWriter(java.io.PrintWriter)
     */
    @Override
	public void setLogWriter(PrintWriter printwriter) throws SQLException {
        // not implemented here.
    }

    /** 
     * Return calssname + {@link #toStringValues()}. 
     */
    @Override
	public String toString() {
        return this.getClass().getName() + " [" + this.toStringValues() + ']';
    }

    /** 
     * Concat all values (except passwd) to some readeable string.
     */
    protected String toStringValues() {
        return "driver: '" + this.driver + "', URL: '" + this.url + "', user: '" + this.user + '\'';
    }
    
    // Special for JDK 1.6

    /**
     * This class does not Wrap any other class.
     * 
     * @return always false.
     */
    @Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    /**
     * This class does not Wrap any other class.
     * 
     * @return always null.
     */
    @Override
	public <T> T unwrap(Class<T> aIface) throws SQLException {   
        return null;
	}

	@Override
	public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
		throw new SQLFeatureNotSupportedException();
	}
}
