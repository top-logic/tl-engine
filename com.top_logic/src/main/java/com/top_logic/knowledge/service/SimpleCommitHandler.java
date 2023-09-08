/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.basic.TLID;
import com.top_logic.basic.sql.CommitContext;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.PooledConnection;

/**
 * <p>
 * The {@link SimpleCommitHandler} is a basic implementation of
 * {@link CommitHandler} and {@link CommitContext}.
 * </p>
 * <p>
 * There is no support of deleting {@link Committable} nor support for
 * generation commit numbers.
 * </p>
 * <p>
 * A SimpleCommitHandler should not be reused. After the end of all
 * operations, {@link SimpleCommitHandler#close()} must be called to release
 * the requested connection.
 * </p>
 *
 * @author <a href=mailto:fsc@top-logic.com>fsc</a>
 */
public class SimpleCommitHandler implements CommitHandler, CommitContext {

    private final ConnectionPool pool;
    private PooledConnection connection;

	private final List<Committable> commitables = new ArrayList<>();

	private final List<Committable> commitablesForDelete = new ArrayList<>();

    /**
     * Creates a new instance of this class.
     */
    public SimpleCommitHandler(ConnectionPool aPool) {
        this.pool = aPool;
    }

    @Override
	public boolean addCommittable(Committable aCommittable) {
        return this.commitables.add(aCommittable);
    }

    @Override
	public boolean addCommittableDelete(Committable aCommitable) {
		return this.commitablesForDelete.add(aCommitable);
    }

    @Override
	public CommitContext createCommitContext() {
        return this;
    }

	/**
	 * Never ever called from anywhere.
	 * 
	 * @deprecated as in Superclass
	 */
    @Override
	@Deprecated
    public CommitContext getCommitContext(boolean create) {
        throw new UnsupportedOperationException();
    }

    @Override
	public CommitContext getCurrentContext() {
        return this;
    }

    /**
     * Unsupported
     */
    @Override
	public boolean removeCommittable(Committable aCommittable) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported
     */
    @Override
	public long getCommitNumber() {
        throw new UnsupportedOperationException();
    }

	@Override
	public boolean transactionStarted() {
		return true;
	}

    /**
     * Return a requested {@link Connection}. Return <code>null</code>, if
     * the method is called without {@link #begin()} or after
     * {@link #close()}
     */
    @Override
	public PooledConnection getConnection() {
        return this.connection;
    }

    /**
     * Begin a transaction and request a connection. After all work is done,
     * {@link #close()} must be called to cleanup the resources.
     */
    public boolean begin() {
        this.connection = pool.borrowWriteConnection();
        return this.connection != null;
    }

	/**
	 * Call all {@link Committable#commit(CommitContext)} then {@link Connection#commit()}.
	 * 
	 * Call {@link #rollback()} in case anything fails (Avoid #4346)
	 * 
	 * @return true if commit was successful.
	 */
    public boolean commit() throws SQLException {
		boolean sucess = false;
		if (this.connection == null) {
			Logger.error("call bagin before Commit", SimpleCommitHandler.class);
			return false;
		}
		try {
			boolean commitOk = true;
			// Handle delete first assuming it may free memory in some way
			commitOk = prepareCommitablesDel()
					&& prepareCommitables()
					&& commitCommitablesDel()
					&& commitCommitables();
			if (commitOk) {
				this.connection.commit();
				sucess = true; // Database may still fail us.
			}
		} catch (Throwable any) {
			Logger.warn("Failed to commit ", any, SimpleCommitHandler.class);
		} finally {
			if (!sucess) {
				rollback(); // implies cleanup
			} else {
				cleanup();
			}
		}

		return sucess;
    }

	private boolean commitCommitablesDel() {
		for (int i = 0, cnt = this.commitablesForDelete.size(); i < cnt; i++) {
			Committable theCommit = this.commitablesForDelete.get(i);
			if (!theCommit.commit(this)) {
				Logger.warn("Failed to commit " + theCommit, SimpleCommitHandler.class);
				return false;
			}
		}
		return true;
	}

	private boolean commitCommitables() {
		for (int i = 0, cnt = this.commitables.size(); i < cnt; i++) {
			Committable theCommit = this.commitables.get(i);
			if (!theCommit.commit(this)) {
				Logger.warn("Failed to commit " + theCommit, SimpleCommitHandler.class);
				return false;
			}
		}
		return true;
	}

	private boolean prepareCommitables() {
		for (int i = 0, cnt = this.commitables.size(); i < cnt; i++) {
			Committable theCommit = this.commitables.get(i);
			if (!theCommit.prepare(this)) {
				Logger.warn("Failed to prepare " + theCommit, SimpleCommitHandler.class);
				return false;
			}
		}
		return true;
	}

	private boolean prepareCommitablesDel() {
		for (int i = 0, cnt = this.commitablesForDelete.size(); i < cnt; i++) {
			Committable theCommit = this.commitablesForDelete.get(i);
			if (!theCommit.prepareDelete(this)) {
				Logger.warn("Failed to prepareDelete " + theCommit, SimpleCommitHandler.class);
				return false;
			}
		}
		return true;
	}

	/**
	 * Call all {@link Committable#rollback(CommitContext)} then {@link Connection#rollback()}.
	 * 
	 * @return true if rollback was successful.
	 */
    public boolean rollback() throws SQLException {
		boolean sucess = true;
		try {
			try {
				for (int i = 0, cnt = this.commitables.size(); i < cnt; i++) {
					Committable theCommit = this.commitables.get(i);
					try {
						if (!theCommit.rollback(this)) {
							sucess = false;
							Logger.warn("Failed to rollback " + theCommit, SimpleCommitHandler.class);
						}
					} catch (Throwable ex) {
						Logger.error("Error during rollback.", ex, SimpleCommitHandler.class);
					}
				}

				for (int i = 0, cnt = this.commitablesForDelete.size(); i < cnt; i++) {
					Committable theCommit = this.commitablesForDelete.get(i);
					try {
						if (!theCommit.rollback(this)) {
							sucess = false;
							Logger.warn("Failed to rollback " + theCommit, SimpleCommitHandler.class);
						}
					} catch (Throwable ex) {
						Logger.error("Error during rollback.", ex, SimpleCommitHandler.class);
					}
				}
			} finally {
				this.connection.rollback();
			}
		} finally {
			cleanup();
		}

		return sucess;
    }

    /**
     * Release the connection and make the handler ready for a new
     * {@link #begin()}
     */
    public void close() {
		if (connection != null) {
			try {
				rollback();
			} catch (SQLException ex) {
				Logger.warn("Failed to rollback connection.", ex, SimpleCommitHandler.class);
			}
		}
    }

	/**
	 * Completes a {@link #commit()}, or {@link #rollback()} by notifying {@link Committable}s and
	 * releasing the connection.
	 */
	private void cleanup() {
		try {
			for (int i = 0, cnt = this.commitables.size(); i < cnt; i++) {
				Committable theCommit = this.commitables.get(i);
				theCommit.complete(this);
			}
			this.commitables.clear();
			for (int i = 0, cnt = this.commitablesForDelete.size(); i < cnt; i++) {
				Committable theCommit = this.commitablesForDelete.get(i);
				theCommit.complete(this);
			}
			this.commitablesForDelete.clear();
		} finally {
			PooledConnection theConnection = this.connection;
			this.connection = null;

			this.pool.releaseWriteConnection(theConnection);
		}
	}

	@Override
	public TLID createID() {
		throw new UnsupportedOperationException();
	}
}
