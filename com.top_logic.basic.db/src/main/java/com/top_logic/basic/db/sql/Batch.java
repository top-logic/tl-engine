/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * A {@link Batch} encapsulates the batch commands of {@link Statement}. It is created by an
 * {@link CompiledStatement} and must be closed after usage: The general usage is
 * 
 * <pre>
 * 	Connection con = ...;
 * 	CompiledStatement stmt = ...;
 * 	Batch batch = stmt.createBatch(con);
 * 	try {
 * 		batch.addBatch(args);
 * 		batch.executeBatch();
 * 	} finally {
 * 		batch.close();
 * 	}
 * </pre>
 * 
 * @see CompiledStatement#createBatch(java.sql.Connection) creation of batch objects
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface Batch extends AutoCloseable {

	/**
	 * adds a new execution of the represented {@link CompiledStatement} to the list of batches to
	 * execute
	 * 
	 * @param arguments
	 *        the arguments for the represented SQL command
	 * 
	 * @throws SQLException
	 *         if a database access error occurs, or the driver does not support batch updates
	 * 
	 * @see java.sql.Statement#addBatch(String)
	 */
	void addBatch(Object... arguments) throws SQLException;

	/**
	 * Removes all batches.
	 * 
	 * @throws SQLException
	 *         if a database access error occurs, or the driver does not support batch updates
	 * 
	 * @see java.sql.Statement#clearBatch()
	 */
	void clearBatch() throws SQLException;

	/**
	 * Executes the list of added SQL commands.
	 * 
	 * @return an array of update counts containing one element for each command in the batch
	 * 
	 * @throws SQLException
	 *         if a database access error occurs or the driver does not support batch statements.
	 *         Throws {@link BatchUpdateException} (a subclass of <code>SQLException</code>) if one
	 *         of the commands sent to the database fails to execute properly or attempts to return
	 *         a result set.
	 * 
	 * @see java.sql.Statement#executeBatch()
	 */
	int[] executeBatch() throws SQLException;

	/**
	 * Closes this batch.
	 * 
	 * @throws SQLException
	 *         if a database access error occurs
	 * 
	 * @see Statement#close()
	 */
	@Override
	void close() throws SQLException;

}
