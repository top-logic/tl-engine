/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.sql.Connection;

import com.top_logic.basic.TLID;

/**
 * Adaption of {@link Connection} to the {@link CommitContext} interface.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CommitContextWrapper implements CommitContext {

	private final PooledConnection connection;

	public CommitContextWrapper(PooledConnection connection) {
		this.connection = connection;
	}

	@Override
	public boolean transactionStarted() {
		return true;
	}

	@Override
	public long getCommitNumber() {
		throw new UnsupportedOperationException("Not within a commit.");
	}

	@Override
	public PooledConnection getConnection() {
		return connection;
	}

	@Override
	public TLID createID() {
		throw new UnsupportedOperationException();
	}
}