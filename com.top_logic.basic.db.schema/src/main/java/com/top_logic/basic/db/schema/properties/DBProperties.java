/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.schema.properties;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.H2Helper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.util.Utils;

/**
 * Provides properties stored in the database.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class DBProperties implements DBPropertiesSchema {

    /** Represents a property value valid for all cluster nodes. */
    public static final String GLOBAL_PROPERTY = "__global__";
    
	private ConnectionPool _pool;

	/**
	 * Creates a {@link DBProperties} service for the given {@link ConnectionPool}.
	 */
    public DBProperties(ConnectionPool pool) {
    	_pool = pool;
    }

	/**
	 * Return the property value for the given key.
	 * 
	 * This method will look up a global property (identified by {@link #GLOBAL_PROPERTY}).
	 * 
	 * @param aKey
	 *        The key to get the property for, must not be <code>null</code>.
	 * @return The requested value, may be <code>null</code>.
	 * @throws IllegalArgumentException
	 *         If key is <code>null</code> or empty.
	 * 
	 * @see #getProperty(String, String)
	 */
    public String getProperty(String aKey) {
        return this.getProperty(DBProperties.GLOBAL_PROPERTY, aKey);
    }

    /**
     * Requests a property in its own transaction.
     * 
     * @see #getProperty(PooledConnection, String, String)
     */
    public String getProperty(String nodeName, String property) throws IllegalArgumentException {
        try {
    		DBHelper sqlDialect = connectionPool().getSQLDialect();
			int theCounter = sqlDialect.retryCount();
            while (true) {
            	final PooledConnection connection = connectionPool().borrowReadConnection();
            	try {
					return getProperty(connection, nodeName, property);
        		} catch (SQLException ex) {
					if (sqlDialect.canRetry(ex)) {
                        connection.closeConnection(ex);
						if (theCounter-- <= 0) {
                            throw ex;
                        }
                    } else {
                        throw ex;
                    }
                } finally {
                    connectionPool().releaseReadConnection(connection);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to get property '" + property + "' (node is: '" + nodeName + "')!", ex);
        }
    }

	/**
	 * Return the property value for the given cluster node and property name.
	 * 
	 * @param connection
	 *        The transaction to request the property value in.
	 * @param nodeName
	 *        The cluster node to get the property value for (may be {@link #GLOBAL_PROPERTY}).
	 * @param property
	 *        The key to get the property value for, must not be <code>null</code>.
	 * @return The requested value, <code>null</code> if the property is not set.
	 * @throws IllegalArgumentException
	 *         If node or key is <code>null</code> or empty.
	 */
	public static String getProperty(PooledConnection connection, String nodeName, String property)
			throws IllegalArgumentException, SQLException {
		DBHelper sqlDialect = connection.getSQLDialect();
        if (StringServices.isEmpty(nodeName)) {
            throw new IllegalArgumentException("Given node is null or empty!");
        }
        else if (StringServices.isEmpty(property)) {
            throw new IllegalArgumentException("Given key is null or empty!");
        }

		try (PreparedStatement theStatement = connection.prepareStatement(
			"SELECT " + valueColumn(sqlDialect)
				+ " FROM " + tableRef(sqlDialect)
				+ " WHERE " + nodeColumn(sqlDialect) + "=? " + "AND " + keyColumn(sqlDialect) + "=?")) {
        	theStatement.setString(1, nodeName);
        	theStatement.setString(2, property);
        	
			try (ResultSet theResultSet = theStatement.executeQuery()) {
        		if (theResultSet.next()) {
        			return theResultSet.getString(1);
        		} else {
        			return null;
        		}
        	}
        }
    }

	/**
	 * Sets a property for the {@link #GLOBAL_PROPERTY global node}.
	 */
	public boolean setProperty(String property, String newValue) throws IllegalArgumentException {
		return setProperty(DBProperties.GLOBAL_PROPERTY, property, newValue);
	}

    /**
     * Sets a property in a separate transaction.
     * 
     * @see #setProperty(PooledConnection, String, String, String)
     */
    public boolean setProperty(String nodeName, String property, String newValue) throws IllegalArgumentException {
        try {
        	PooledConnection connection = this.connectionPool().borrowWriteConnection();
            try {
                boolean result = setProperty(connection, nodeName, property, newValue);
				if (result) {
					connection.commit();
				}
                return result;
            } finally {
                this.connectionPool().releaseWriteConnection(connection);
            }
        }
        catch (SQLException ex) {
        	throw new RuntimeException("Failed to set property '" + property + "' (node is: '" + nodeName + "')!", ex);
        }
    }

	/**
	 * Stores a property to the database.
	 * 
	 * <p>
	 * 
	 * If the given value is <code>null</code> or empty, the property is removed from data base.
	 * </p>
	 * 
	 * @param connection
	 *        The transaction to set the property in.
	 * @param nodeName
	 *        The cluster node name of the node-local property ({@link #GLOBAL_PROPERTY} for
	 *        cluster-global properties), not <code>null</code> or empty.
	 * @param property
	 *        The name of the property, must not be <code>null</code> or empty.
	 * @param newValue
	 *        The value of the property to store, <code>null</code> to remove the property.
	 * @return Whether the persistent state has changed.
	 * @throws IllegalArgumentException
	 *         If node or key is <code>null</code> or empty.
	 */
	public static boolean setProperty(PooledConnection connection, String nodeName, String property, String newValue)
			throws IllegalArgumentException, SQLException {
		logDebugUpdateStart(connection, nodeName, property, newValue);
		DBHelper sqlDialect = connection.getSQLDialect();
        if (StringServices.isEmpty(nodeName)) {
            throw new IllegalArgumentException("Given node is null or empty!");
        }
        if (StringServices.isEmpty(property)) {
            throw new IllegalArgumentException("Given key is null or empty!");
        }
        
        
        if (StringServices.isEmpty(newValue)) {
			deleteProperty(connection, nodeName, property);
        } else {
			String currentValue = getProperty(connection, nodeName, property);
            if (StringServices.equals(currentValue, newValue)) {
            	// No change.
				logDebugUpdateNotNecessary();
            	return false;
            }

            boolean hasValue = currentValue != null;
			// IGNORE FindBugs(SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING): Dynamic
			// SQL construction is necessary for DBMS abstraction. No user-input is passed to the
			// statement source.
			try (PreparedStatement theStatement = connection.prepareStatement(
            	(hasValue ? 
					"UPDATE " + tableRef(sqlDialect)
						+ " SET " + valueColumn(sqlDialect) + "=?"
						+ " WHERE " + nodeColumn(sqlDialect) + "=? AND " + keyColumn(sqlDialect) + "=?"
					:
            			
					"INSERT INTO " + tableRef(sqlDialect)
						+ " (" + valueColumn(sqlDialect) + "," + nodeColumn(sqlDialect) + ","
						+ keyColumn(sqlDialect) + ")"
						+ " VALUES (?,?,?)"))) {
            

            	theStatement.setString(1, newValue);
            	theStatement.setString(2, nodeName);
            	theStatement.setString(3, property);
            	
            	theStatement.executeUpdate();
            }
        }
		logDebugUpdateDone();
        return true;
    }

	private static void logDebugUpdateStart(PooledConnection connection, String nodeName, String property,
			String newValue) {
		if (isLoggingDebug()) {
			String connectionName = connection.getPool().getName();
			logDebug("Setting property '" + property + "' to '" + newValue
				+ "' on node '" + nodeName + "' at connection '" + connectionName + "'.");
		}
	}

	private static void logDebugUpdateNotNecessary() {
		if (isLoggingDebug()) {
			logDebug("The property has already the new value.");
		}
	}

	private static void logDebugUpdateDone() {
		if (isLoggingDebug()) {
			logDebug("The property was updated.");
		}
	}

	/**
	 * Checks whether represented table exists.
	 * 
	 * @see #tableExists(PooledConnection)
	 */
	public boolean tableExists() throws SQLException {
		ConnectionPool pool = connectionPool();
		PooledConnection connection = pool.borrowReadConnection();
		try {
			return tableExists(connection);
		} finally {
			pool.releaseReadConnection(connection);
		}
	}

	/**
	 * Checks whether the represented table exists.
	 * 
	 * @param connection
	 *        The connection to database to use.
	 */
	public static boolean tableExists(PooledConnection connection) throws SQLException {
		try (Statement stmt = connection.createStatement()) {
			try {
				stmt.executeQuery("SELECT * FROM " + tableRef(connection.getSQLDialect()) + " WHERE 1=0").close();
				return true;
			} catch (SQLException ex) {
				return false;
			}
		}
	}

	/**
	 * Fetches the values for all the properties on the given node.
	 * 
	 * @param connection
	 *        The transaction to set the property in.
	 * @param nodeName
	 *        The cluster node name of the node-local property ({@link #GLOBAL_PROPERTY} for
	 *        cluster-global properties), not <code>null</code> or empty.
	 * 
	 * @return {@link Map} containing for each property in the table the value.
	 */
	public static Map<String, String> getPropertiesForNode(PooledConnection connection, String nodeName)
			throws SQLException {
		DBHelper sqlDialect = connection.getSQLDialect();
		StringBuilder stmt = new StringBuilder();
		stmt.append("SELECT ");
		stmt.append(keyColumn(sqlDialect));
		stmt.append(',');
		stmt.append(valueColumn(sqlDialect));
		stmt.append(" FROM ");
		stmt.append(tableRef(sqlDialect));
		stmt.append(" WHERE ");
		stmt.append(nodeColumn(sqlDialect));
		stmt.append("=?");
		try (PreparedStatement statement = connection.prepareStatement(stmt.toString())) {
			statement.setString(1, nodeName);
			try (ResultSet result = statement.executeQuery()) {
				Map<String, String> propertiesMap = new HashMap<>();
				while (result.next()) {
					propertiesMap.put(result.getString(1), result.getString(2));
				}
				return propertiesMap;
			}
		}
	}

	/**
	 * Fetches the values for all the given properties on the given node.
	 * 
	 * <p>
	 * In contrast to call {@link #getProperty(PooledConnection, String, String)} for each property,
	 * only one database hit occurs and no changes can happen between the fetch of the different
	 * properties.
	 * </p>
	 * 
	 * @param connection
	 *        The transaction to set the property in.
	 * @param nodeName
	 *        The cluster node name of the node-local property ({@link #GLOBAL_PROPERTY} for
	 *        cluster-global properties), not <code>null</code> or empty.
	 * @param properties
	 *        The properties to get values for.
	 * 
	 * @return {@link Map} containing for each property the value. A value may be <code>null</code>,
	 *         which means that there is currently no entry for the corresponding database.
	 */
	public static Map<String, String> getProperties(PooledConnection connection, String nodeName, String... properties)
			throws SQLException {
		switch (properties.length) {
			case 0:{
				return Collections.emptyMap();
			}
			case 1:{
				String property = properties[0];
				return Collections.singletonMap(property, getProperty(connection, nodeName, property));
			}
			default:{
				DBHelper sqlDialect = connection.getSQLDialect();
				StringBuilder stmt = new StringBuilder();
				stmt.append("SELECT ");
				stmt.append(keyColumn(sqlDialect));
				stmt.append(',');
				stmt.append(valueColumn(sqlDialect));
				stmt.append(" FROM ");
				stmt.append(tableRef(sqlDialect));
				stmt.append(" WHERE ");
				stmt.append(nodeColumn(sqlDialect));
				stmt.append("=? AND ");
				stmt.append(keyColumn(sqlDialect));
				stmt.append(" IN  (");
				sqlDialect.literal(stmt, DBType.STRING, properties[0]);
				for (int i = 1; i < properties.length; i++) {
					stmt.append(", ");
					sqlDialect.literal(stmt, DBType.STRING, properties[i]);
				}
				stmt.append(")");
				try (PreparedStatement statement = connection.prepareStatement(stmt.toString())) {
					statement.setString(1, nodeName);
					try (ResultSet result = statement.executeQuery()) {
						Map<String, String> propertiesMap = MapUtil.newMap(properties.length);
						while (result.next()) {
							propertiesMap.put(result.getString(1), result.getString(2));
						}
						if (properties.length > propertiesMap.size()) {
							// Not all properties have a value.
							for (String property : properties) {
								if (!propertiesMap.containsKey(property)) {
									propertiesMap.put(property, null);
								}
							}
						}
						return propertiesMap;
					}
				}
			}
		}

	}

	/**
	 * Updates the given property on the given node to <code>update</code> iff the current value is
	 * <code>expect</code>.
	 * 
	 * <p>
	 * This is the database corresponding form of
	 * {@link AtomicReference#compareAndSet(Object, Object)}.
	 * </p>
	 * 
	 * @param connection
	 *        The transaction to set the property in.
	 * @param nodeName
	 *        The cluster node name of the node-local property ({@link #GLOBAL_PROPERTY} for
	 *        cluster-global properties), not <code>null</code> or empty.
	 * @param property
	 *        The name of the property, must not be <code>null</code> or empty.
	 * @param expect
	 *        The value that the property must currently have to update the database.
	 *        <code>null</code> means it is expected that there is currently no entry for the given
	 *        property.
	 * @param update
	 *        The value to set property to when the current value is <code>expect</code>.
	 *        <code>null</code> means delete the entry.
	 * 
	 * @return <code>true</code> if successful. <code>false</code> return indicates that the actual
	 *         value was not equal to the expected value.
	 */
	public static boolean compareAndSet(PooledConnection connection, String nodeName, String property, String expect,
			String update) throws SQLException {
		if (Utils.equals(update, expect)) {
			/* No op change. Just check whether current value is the expected. */
			return Utils.equals(expect, getProperty(connection, nodeName, property));
		}
		DBHelper sqlDialect = connection.getSQLDialect();
		int retryCount = sqlDialect.retryCount();

		StringBuilder selectStmtString = newSelectStmt(sqlDialect);
		while (true) {
			// IGNORE FindBugs(SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING):
			// Dynamic SQL construction is necessary for DBMS abstraction. No user-input is
			// passed to the statement source.
			try (PreparedStatement updateRowStmt = connection.prepareStatement(
				selectStmtString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE)) {
				updateRowStmt.setString(1, nodeName);
				updateRowStmt.setString(2, property);

				try (ResultSet result = updateRowStmt.executeQuery()) {
					if (result.next()) {
						boolean updated = update(result, expect, update);
						assert !result.next() : "More than one row with the same key values.";
						return updated;
					} else {
						insert(result, nodeName, property, update);
						return true;
					}

				}
			} catch (SQLException ex) {
				// Assume "Deadlock detected" failure.

				// In certain RDBMS (including MySQL), the lookup might deadlock with a pending
				// sequence setup below, see Ticket #11782.

				if (retryCount-- > 0) {
					logRetryUpdate(nodeName, property, retryCount, ex);
					continue;
				}
				// Break the commit.
				throw ex;
			}

		}

	}

	private static StringBuilder newSelectStmt(DBHelper sqlDialect) {
		/* Note: Here workarounds for two bugs are included: 1) Oracle: The column "propValue" was
		 * created with this notation. The column in the database is "propValue". Accessing the
		 * value with "propValue" is possible. BUT: Update of a ResultSet will set the value to
		 * column "PROPVALUE" which does not exist. When aliases are used this is no problem. 2) H2:
		 * When aliases are used, the ResultSet is not updatable. Therefore aliases for H2 can not
		 * be used. */
		boolean isH2 = sqlDialect instanceof H2Helper;
		StringBuilder lockStmtString = new StringBuilder();
		lockStmtString.append("SELECT ");
		lockStmtString.append(nodeColumn(sqlDialect));
		if (!isH2) {
			lockStmtString.append(" AS ");
			lockStmtString.append(NODE_COLUMN_NAME);
		}
		lockStmtString.append(',');
		lockStmtString.append(keyColumn(sqlDialect));
		if (!isH2) {
			lockStmtString.append(" AS ");
			lockStmtString.append(PROP_KEY_COLUMN_NAME);
		}
		lockStmtString.append(',');
		lockStmtString.append(valueColumn(sqlDialect));
		if (!isH2) {
			lockStmtString.append(" AS ");
			lockStmtString.append(PROP_VALUE_COLUMN_NAME);
		}
		lockStmtString.append(" FROM ");
		lockStmtString.append(tableRef(sqlDialect));
		lockStmtString.append(sqlDialect.forUpdate1());
		lockStmtString.append(" WHERE ");
		lockStmtString.append(nodeColumn(sqlDialect));
		lockStmtString.append("=? ");
		lockStmtString.append("AND ");
		lockStmtString.append(keyColumn(sqlDialect));
		lockStmtString.append("=?");
		lockStmtString.append(sqlDialect.forUpdate2());
		return lockStmtString;
	}

	private static void logRetryUpdate(String nodeName, String property, int retryCount, SQLException ex) {
		Logger.info("Retry " + retryCount + " times to get value for node '" + nodeName + "' and property '"
			+ property + "' '" + ex.getMessage() + "'", DBProperties.class);
	}

	private static void insert(ResultSet result, String node, String property, String value) throws SQLException {
		result.moveToInsertRow();
		result.updateString(NODE_COLUMN_NAME, node);
		result.updateString(PROP_KEY_COLUMN_NAME, property);
		result.updateString(PROP_VALUE_COLUMN_NAME, value);
		result.insertRow();

	}

	private static boolean update(ResultSet resultSet, String expect, String update) throws SQLException {
		String currentValue = resultSet.getString(PROP_VALUE_COLUMN_NAME);
		if (!StringServices.equals(currentValue, expect)) {
			return false;
		}
		if (StringServices.isEmpty(update)) {
			resultSet.deleteRow();
		} else {
			/* Note: To actually keep the row lock after the current result set is closed, the
			 * retrieved row must be actually updated. Even if MySQL and Oracle seem to keep the row
			 * locks if the statement mentions the "FOR UPDATE" clause, e.g. Apache Derby would
			 * release the lock, if the retrieved row is not directly updated. */
			resultSet.updateString(PROP_VALUE_COLUMN_NAME, update);
			resultSet.updateRow();
		}

		return true;
	}

    /** 
     * Delete a property value from the data base.
     * 
     * @param    aNode     The cluster node to remove the property value for (may be {@link #GLOBAL_PROPERTY}), must not be <code>null</code> or empty.
     * @param    aKey      The key to remove the property value for, must not be <code>null</code> or empty.
     */
	private static void deleteProperty(PooledConnection connection, String aNode, String aKey) throws SQLException {
		DBHelper sqlDialect = connection.getSQLDialect();

		try (PreparedStatement theStatement = connection.prepareStatement(
			"DELETE FROM " + tableRef(sqlDialect) + " WHERE " + nodeColumn(sqlDialect) + "=? AND "
				+ keyColumn(sqlDialect) + "=?")) {
    		theStatement.setString(1, aNode);
    		theStatement.setString(2, aKey);
    		
    		theStatement.executeUpdate();
    	}
    }

	private static String tableRef(DBHelper sqlDialect) {
		return sqlDialect.tableRef(TABLE_NAME);
	}

	private static String keyColumn(DBHelper sqlDialect) {
		return sqlDialect.columnRef(PROP_KEY_COLUMN_NAME);
	}

	private static String nodeColumn(DBHelper sqlDialect) {
		return sqlDialect.columnRef(NODE_COLUMN_NAME);
	}

	private static String valueColumn(DBHelper sqlDialect) {
		return sqlDialect.columnRef(PROP_VALUE_COLUMN_NAME);
	}

	/**
	 * The base {@link ConnectionPool} used by this {@link DBProperties}.
	 */
	public ConnectionPool connectionPool() {
		return _pool;
	}

	private static boolean isLoggingDebug() {
		return Logger.isDebugEnabled(DBProperties.class);
	}

	private static void logDebug(String message) {
		Logger.debug(message, DBProperties.class);
	}

}

