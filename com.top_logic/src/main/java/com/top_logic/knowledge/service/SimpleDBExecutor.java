/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import java.sql.Connection;
import java.sql.SQLException;

import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.util.db.DBUtil;

/**
 * This is a simple class for executing DB update queries in transactions. This class helps
 * with the transaction handling. For executing updates direct without transactions, use the
 * executeUpdateAndCommit() methods in DBUtil. For readOperations, use the executeQuery...
 * methods in DBUtil. The method {@link #getTransactionConnection()} gets the connection of
 * the transaction, so that read operations can see the changes already done
 *
 * Before executing the first update statement, the {@link #beginTransaction()} method must be
 * called. Commit and rollback must be done manually by the {@link #commitTransaction()} or
 * {@link #rollbackTransaction()} method. After all operations have finished,
 * {@link #closeTransaction()} must be called, usually in a finally block.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class SimpleDBExecutor {

    /** The connection pool to get database connections from. */
    private ConnectionPool connectionPool;

    /** Current transaction connection. */
    private PooledConnection connection;

	/**
	 * Whether the transaction started with {@link #beginTransaction()} was completed by
	 * {@link #commitTransaction()} or {@link #rollbackTransaction()}.
	 */
	private boolean _completed;


    /**
     * Creates a new instance of this class using the default connection pool.
     */
    public SimpleDBExecutor() {
        this(ConnectionPoolRegistry.getDefaultConnectionPool());
    }

    /**
     * Creates a new instance of this class with the given connection pool.
     */
    public SimpleDBExecutor(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
        this.connection = null;
    }

    /**
     * Begins a database transaction. This transaction is independent from the
     * knowledgebase.
     *
     * Note: After the operations are finished, {@link #closeTransaction()} must be called
     * to release the database connection and end the transaction. So this method should be
     * used with the following pattern:
     *
     * <pre>
     * dbExecutor.beginTransaction();
     * try {
     *     // perform DB queries and updates
     *     dbExecutor.commitTransaction();
     * }
     * finally {
     *     dbExecutor.closeTransaction();
     * }
     * </pre>
     */
    public void beginTransaction() {
        if (connection != null) {
            throw new IllegalArgumentException("beginTransaction() has already been called.");
        }
        connection = connectionPool.borrowWriteConnection();
		_completed = false;
    }

    /**
     * Commits the transaction started before with {@link #beginTransaction()}.
     */
    public void commitTransaction() throws SQLException {
        if (connection == null) {
            throw new IllegalArgumentException("beginTransaction() has not been called yet.");
        }
        connection.commit();
		_completed = true;
    }

    /**
     * Rolls all uncommitted changes in the transaction started before with {@link #beginTransaction()}
     * back.
     */
    public void rollbackTransaction() throws SQLException {
        if (connection == null) {
            throw new IllegalArgumentException("beginTransaction() has not been called yet.");
        }
        connection.rollback();
		_completed = true;
    }

    /**
     * Ends the transaction started before with {@link #beginTransaction()} and releases the database
     * connection. Rolls all uncommitted changes back.
     * 
     * <p>
     * If the transaction was neither completed by {@link #commitTransaction()} nor by
     * {@link #rollbackTransaction()}, it was aborted by a failure. Then the connection is dropped
     * from the pool instead of being reused: The failure may be caused by a state of the physical
     * connection that the pool cannot detect (e.g. a session that the database server only allows
     * to read), and would otherwise make every following transaction fail as well.
     * </p>
     */
    public void closeTransaction() {
        if (connection != null) {
			try {
				if (_completed) {
					connectionPool.releaseWriteConnection(connection);
				} else {
					connectionPool.invalidateWriteConnection(connection);
				}
			} finally {
				// make sure to reset the connection no matter
				// if releasing was successful or not.
				connection = null;
			}
        }
    }



    /**
     * Gets the connection of the current transaction started with {@link #beginTransaction()}. May
     * be <code>null</code>, if not transaction has started yet or was already finished.
     */
    public Connection getTransactionConnection() {
        return connection;
    }



    /**
     * @see DBUtil#executeUpdate(Connection, String, Object[])
     */
    public int executeUpdate(String aStatement) throws SQLException {
        return executeUpdate(aStatement, null);
    }

    /**
     * @see DBUtil#executeUpdate(Connection, String, Object[])
     */
    public int executeUpdate(String aStatement, Object[] params) throws SQLException {
        Connection con = getTransactionConnection();
        if (con == null) {
            throw new IllegalArgumentException("beginTransaction() must be called before any write operations.");
        }
        return DBUtil.executeUpdate(con, aStatement, params);
    }



    /**
     * Convenience method to create a parameter array for prepared statements containing the
     * elements given in the argument list.
     *
     * @param params
     *        the elements of the new array
     * @return an array containing the elements given in the argument list
     */
    public Object[] params(Object... params) {
        return params;
    }

}
