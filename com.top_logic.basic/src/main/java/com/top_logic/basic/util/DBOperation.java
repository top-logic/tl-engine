/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.util;

import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.PooledConnection;

/**
 * {@link Process} that performs a DB update.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class DBOperation extends Process {

	@Override
	protected void process() throws Exception {
		ConnectionPool pool = ConnectionPoolRegistry.getDefaultConnectionPool();
		PooledConnection connection = pool.borrowWriteConnection();
		try {
			update(connection);

			connection.commit();
		} finally {
			pool.releaseWriteConnection(connection);
		}
	}

	/**
	 * The DB update operation.
	 * 
	 * @param connection
	 *        The transaction.
	 */
	protected abstract void update(PooledConnection connection) throws Exception;

}