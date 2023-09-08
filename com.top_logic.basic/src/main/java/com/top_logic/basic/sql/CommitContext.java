/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.sql.Connection;

/**
 * Context that provides information about a currently running commit operation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface CommitContext extends ObjectNameSource {

	/**
	 * Whether the underling transaction with the database has already been started.
	 * 
	 * <p>
	 * Calling {@link #getCommitNumber()}, or {@link #getConnection()} implicitly starts the
	 * transaction.
	 * </p>
	 */
	boolean transactionStarted();

	/**
	 * The commit number identifying the current commit.
	 */
	long getCommitNumber();
	
	/** 
	 * returns the {@link Connection} to the database used in this {@link CommitContext}
	 */
	PooledConnection getConnection();
	
}
