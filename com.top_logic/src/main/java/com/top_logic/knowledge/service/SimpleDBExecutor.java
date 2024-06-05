/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import java.sql.Connection;
import java.sql.SQLException;

import com.top_logic.basic.sql.CommitContext;
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
 * Before executing the first update statement, the {@link #beginTransaction()} or
 * {@link #beginTransaction(CommitHandler)} method must be called. Commit and rollback must
 * be done manually by the {@link #commitTransaction()} or {@link #rollbackTransaction()}
 * method if used {@link #beginTransaction()} or by the CommitHandler if used
 * {@link #beginTransaction(CommitHandler)}. After all operations have finished,
 * {@link #closeTransaction()} or {@link #releaseCommitContext()} must be called, usually in
 * a finally block. You may use the {@link #beginWithDefaultKB()} as convenience method.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class SimpleDBExecutor {

    /** The connection pool to get database connections from. */
    private ConnectionPool connectionPool;

    /** Current transaction connection. */
    private PooledConnection connection;

    /** Current commit handler. */
    private CommitContext commitContext;



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
        this.commitContext = null;
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
            throw new IllegalArgumentException("beginTransaction() was already called.");
        }
        if (commitContext != null) {
            throw new IllegalArgumentException("beginTransaction(CommitHandler) was already called.");
        }
        connection = connectionPool.borrowWriteConnection();
    }

    /**
     * Commits the transaction started before with {@link #beginTransaction()}.
     */
    public void commitTransaction() throws SQLException {
        if (connection == null) {
            throw new IllegalArgumentException("beginTransaction() was not callen yet.");
        }
        connection.commit();
    }

    /**
     * Rolls all uncommitted changes in the transaction started before with {@link #beginTransaction()}
     * back.
     */
    public void rollbackTransaction() throws SQLException {
        if (connection == null) {
            throw new IllegalArgumentException("beginTransaction() was not callen yet.");
        }
        connection.rollback();
    }

    /**
     * Ends the transaction started before with {@link #beginTransaction()} and releases the database
     * connection. Rolls all uncommitted changes back.
     */
    public void closeTransaction() {
        if (connection != null) {
			try {
				connectionPool.releaseWriteConnection(connection);
			} finally {
				// make sure to reset the connection no matter
				// if releasing was successful or not.
				connection = null;
			}
        }
    }



    /**
     * Uses the given CommitHandler to begin a database transaction. Each database query and
     * update uses the connection of the CommitHandler, until the
     * {@link #releaseCommitContext()} method is called.
     *
     * Note: After the operations are finished, {@link #releaseCommitContext()} must be
     * called to release the connection to the CommitHandler. So this method should be used
     * with the following pattern:
     *
     * <pre>
     * dbExecutor.beginTransaction(aHandler);
     * try {
     *     // perform DB queries and updates
     *     // perform a commit of the handler
     * }
     * finally {
     *     dbExecutor.releaseCommitContext();
     * }
     * </pre>
     *
     * Note: A commit or rollback of the CommitHandler will trigger a releaseCommitContext()
     * also.
     */
    public void beginTransaction(CommitHandler aHandler) {
        if (connection != null) {
            throw new IllegalArgumentException("beginTransaction() was already called.");
        }
        if (commitContext != null) {
            if (commitContext == aHandler.getCurrentContext()) {
                return;
            }
            throw new IllegalArgumentException("beginTransaction(CommitHandler) was already called with a different CommitHandler.");
        }
        commitContext = aHandler.createCommitContext();
        aHandler.addCommittable(new CommittableAdapter() {
            @Override
            public boolean commit(CommitContext context) {
                if (commitContext == context) releaseCommitContext();
                return true;
            }
            @Override
            public boolean rollback(CommitContext context) {
                if (commitContext == context) releaseCommitContext();
                return true;
            }
        });
    }

    /**
     * Releases the CommitHandler which was set before with the
     * {@link #beginTransaction(CommitHandler)} method. Not committed changes remain pending until the
     * commit handler commits them or rolls them back.
     */
    public void releaseCommitContext() {
        commitContext = null;
    }

    /**
     * Convenience method to call begin with the default knowledge base as commit handler.
     */
    public void beginWithDefaultKB() {
        beginTransaction((CommitHandler)PersistencyLayer.getKnowledgeBase());
    }



    /**
     * Gets the connection of the current transaction started with
     * {@link #beginTransaction()} or {@link #beginTransaction(CommitHandler)}. May be
     * <code>null</code>, if not transaction has started yet or was already finished.
     */
    public Connection getTransactionConnection() {
        if (connection != null) return connection;
        if (commitContext != null) return commitContext.getConnection();
        return null;
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
            throw new IllegalArgumentException("Either begin() or begin(CommitHandler) must be called before any write operations.");
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
