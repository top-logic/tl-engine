/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.sql.Connection;
import java.sql.SQLException;

import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;

/**
 * Factory for unique consecutive numbers.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface SequenceManager {
	
	/**
	 * Marker for a sequence that has not yet been created.
	 */
	long NO_NUMBER_CREATED = -1L;

	/**
	 * Convenience shortcut for {@link #generateId(ConnectionPool, String)} which uses the default
	 * {@link ConnectionPool}.
	 * 
	 * @param sequenceId
	 *        Is not allowed to be null.
	 */
	long generateId(String sequenceId);

	/**
	 * Generates a new id for the given sequence, commits the new id and returns it.
	 * 
	 * @param connectionPool
	 *        Is not allowed to be null.
	 * @param sequenceId
	 *        Is not allowed to be null.
	 * @return The new id.
	 */
	long generateId(ConnectionPool connectionPool, String sequenceId);

	/**
	 * Creates the next consecutive number in the given sequence.
	 * 
	 * @param sqlDialect
	 *        the DBMS specific dialect.
	 * @param aContext
	 *        The DB context to use.
	 * @param retryCount
	 *        The number of retries in case of non-final DB failures.
	 * @param sequence
	 *        the sequence ID for which the next number is requested.
	 * 
	 * @return The next consecutive number in the given sequence.
	 */
	default long nextSequenceNumber(DBHelper sqlDialect, Connection aContext, int retryCount, String sequence)
			throws SQLException {
		return nextSequenceNumber(sqlDialect, aContext, retryCount, sequence, 1);
	}

	/**
	 * Creates the next number by incrementing the given sequence with the given increment.
	 * 
	 * @param sqlDialect
	 *        the DBMS specific dialect.
	 * @param aContext
	 *        The DB context to use.
	 * @param retryCount
	 *        The number of retries in case of non-final DB failures.
	 * @param sequence
	 *        the sequence ID for which the next number is requested.
	 * @param inc
	 *        The increment to add to the sequence.
	 * 
	 * @return The next consecutive number in the given sequence.
	 */
	long nextSequenceNumber(DBHelper sqlDialect, Connection aContext, int retryCount, String sequence, int inc)
			throws SQLException;

	/**
	 * Check whether any number has been created for this sequence before.
	 * 
	 * @param aContext
	 *        The DB context to use.
	 * @param retryCount
	 *        The number of retries in case of non-final DB failures.
	 * @param sequence
	 *        the sequence ID for which the next number is requested.
	 * 
	 * @return <code>true</code> a number had been created for the sequence
	 */
	boolean isUsed(PooledConnection aContext, int retryCount, String sequence) throws SQLException;

	/**
	 * Get the last consecutive number in the given sequence
	 * 
	 * @param aContext
	 *        The DB context to use.
	 * @param retryCount
	 *        The number of retries in case of non-final DB failures.
	 * @param sequence
	 *        the sequence ID for which the next number is requested.
	 * 
	 * @return The last consecutive number in the given sequence or {@link #NO_NUMBER_CREATED}, if
	 *         the requested sequence has not yet been created.
	 */
	long getLastSequenceNumber(PooledConnection aContext, int retryCount, String sequence) throws SQLException;
}
