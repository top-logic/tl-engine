/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import java.sql.Connection;
import java.sql.SQLException;

import com.top_logic.basic.sql.CommitContext;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolAdapter;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;

/**
 * Abstract super class for {@link Committable}s using a ThreadLocal to store a
 * {@link ConnectionPool} for write access.
 * 
 * This holds another {@link PooledConnection} for read methods.
 * 
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
public abstract class ThreadLocalCommitable extends ConnectionPoolAdapter implements Committable {

	private final ThreadLocal<CommitContext> threadContext = new ThreadLocal<>();

    /** Saves the DBHelper for DB independent operations. */
    protected DBHelper dbHelper;


    /**
     * Creates a new ThreadLocalCommitable.
     *
     * @param connectionPool
     *            the prepared statement cache to use for read operations
     */
    protected ThreadLocalCommitable(ConnectionPool connectionPool) throws SQLException {
		super(connectionPool);
    	this.dbHelper = connectionPool.getSQLDialect();
    }

    // --------------------------------------------

    @Override
	public boolean commit(CommitContext aContext) {
        assert aContext == threadContext.get() : "Context changed during commit.";
        internalResetContext();
		return true;
    }

    @Override
	public void complete(CommitContext aContext) {
        // nothing do do here
    }

    @Override
	public boolean prepare(CommitContext aContext) {
        return true;
    }

    @Override
	public boolean prepareDelete(CommitContext aContext) {
        return true;
    }

    @Override
	public boolean rollback(CommitContext aContext) {
		/* The current thread context may already be removed, e.g. when the rollback is called in a
		 * finally block and a commit previously occurs. In that case rollback must not cause
		 * errors. */
		assert threadContext.get() == aContext || threadContext.get() == null : "Context changed during commit.";
        internalResetContext();
        return true;
    }

	private void internalResetContext() {
		threadContext.set(null);
	}

    /**
     * This method must be called before calling any write methods.
     *
     * @param aHandler
     *            the commit handler to get committed by
     * @throws IllegalArgumentException
     *             when CommitHandler does not use a {@link PooledConnection} or called
     *             twice with different handler before commit()/rollback()
     */
    public void begin(CommitHandler aHandler) {
        CommitContext comContext = aHandler.createCommitContext();
		CommitContext currContext = threadContext.get();
        if (currContext == comContext) {
            return; // already set, well
        }
        if (currContext != null) {
            throw new IllegalArgumentException("Begin called for different Context '" + currContext + "'");
        }
        threadContext.set(comContext);
        aHandler.addCommittable(this);
    }

    /**
     * Gets the connection used for write operations.
     */
    protected Connection getWriteConnection() {
        return getWriteCache().getConnection();
    }

    /**
     * Gets a {@link CommitContext} for write access.
     *
     * @return the {@link CommitContext} to use for write operations
     * @throws IllegalStateException
     *             if {@link #begin(CommitHandler)} wasn't called before.
     */
    protected CommitContext getWriteCache() throws IllegalStateException {
        CommitContext writeContext = getWriteCacheIfExists();
        if (writeContext == null) {
            throw new IllegalStateException("Call begin(CommitHandler) before writing.");
        }
    	return writeContext;
    }

	/**
	 * the Current CommitContext or null when there is no such CommitContext
	 */
	protected final CommitContext getWriteCacheIfExists() {
		return threadContext.get();
	}

	@Override
	public PooledConnection borrowReadConnection() {
		CommitContext currentContext = getWriteCacheIfExists();
		if (currentContext == null) {
			return super.borrowReadConnection();
		} else {
			return currentContext.getConnection();
		}
	}

	@Override
	public void releaseReadConnection(PooledConnection connection) {
		CommitContext currentContext = getWriteCacheIfExists();
		if (currentContext == null) {
			super.releaseReadConnection(connection);
		} else {
			if (connection != currentContext.getConnection()) {
				throw new IllegalArgumentException("Read connection was not borrowed before.");
			}
		}
	}

	@Override
	public void invalidateReadConnection(PooledConnection connection) {
		CommitContext currentContext = getWriteCacheIfExists();
		if (currentContext == null) {
			super.invalidateReadConnection(connection);
		} else {
			// Ignore, since following commit must fail.
		}
	}

}
