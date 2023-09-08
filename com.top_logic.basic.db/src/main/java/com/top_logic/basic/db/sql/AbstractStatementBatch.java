/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

import java.sql.SQLException;
import java.sql.Statement;

import com.top_logic.basic.Logger;

/**
 * {@link Batch} that bases on a given {@link Statement}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractStatementBatch<T extends Statement> implements Batch {
	
	/**
	 * @see AbstractStatementBatch#AbstractStatementBatch(Statement)
	 */
	protected final T _statement;

	/** whether this Batch was closed */
	private boolean _closed;

	/**
	 * Creates a {@link AbstractStatementBatch} which delivers operations to an
	 * {@link Statement}.
	 * 
	 * @param statement
	 *        the actual {@link Statement} implementation
	 */
	public AbstractStatementBatch(T statement) {
		_statement = statement;
	}

	@Override
	public void clearBatch() throws SQLException {
		_statement.clearBatch();
	}

	@Override
	public int[] executeBatch() throws SQLException {
		return _statement.executeBatch();
	}

	@Override
	public void close() throws SQLException {
		_closed = true;
		doClose();
	}

	private void doClose() throws SQLException {
		_statement.close();
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			if (!_closed) {
				doClose();

				Logger.warn("Resource leak detected: Batch was not closed.", AbstractStatementBatch.class);
			}
		} finally {
			super.finalize();
		}
	}

}

