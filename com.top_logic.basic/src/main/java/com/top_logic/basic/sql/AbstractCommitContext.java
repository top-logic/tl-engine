/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;



/**
 * Base class for {@link CommitContext} implementations based on a {@link PooledConnection}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractCommitContext implements CommitContext {
	
	private long currentCommitNumber;

	@Override
	public long getCommitNumber() {
		if (currentCommitNumber == 0) {
			startCommit();
			assert currentCommitNumber != 0;
		}
		return currentCommitNumber;
	}
	
	/**
	 * Starts the commit if it has not yet been started.
	 * 
	 * <p>
	 * After this method returns, {@link #beginCommit(long)} must have been called.
	 * </p>
	 */
	protected abstract void startCommit();

	/**
	 * Marks the commit to be in process.
	 * 
	 * @param commitNumber The commit number of the current commit.
	 */
	protected void beginCommit(long commitNumber) {
		assert commitNumber > 0;
		this.currentCommitNumber = commitNumber;
	}
	
	protected void cleanup() {
		this.currentCommitNumber = 0;
	}
	
	@Override
	public final PooledConnection getConnection() {
		return internalGetConnection();
	}
	
	protected abstract PooledConnection internalGetConnection();
}
