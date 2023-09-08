/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * The class {@link PooledConnection} is a {@link Connection} which will be
 * delivered by the {@link ConnectionPool} in
 * {@link ConnectionPool#borrowReadConnection()} and
 * {@link ConnectionPool#borrowWriteConnection()}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface PooledConnection extends Connection {

	/**
	 * Close the {@link Connection} due to the given reason, e.g. if executing a
	 * &quot;read&quot; statement fails. After closing the connection it is
	 * possible to retry executing the action which failed.
	 * 
	 * <p>
	 * <b> This method must not be called in commit context </b>
	 * </p>
	 * 
	 * @param aReason
	 *        optional cause why {@link Connection}s should be closed. may be
	 *        <code>null</code>
	 */
	void closeConnection(Throwable aReason);

	/**
	 * Returns the dialect the statements must have to be created by this {@link PooledConnection}.
	 */
	DBHelper getSQLDialect() throws SQLException;

	/**
	 * {@link ConnectionPool} this {@link PooledConnection} belongs to.
	 */
	ConnectionPool getPool();

}
