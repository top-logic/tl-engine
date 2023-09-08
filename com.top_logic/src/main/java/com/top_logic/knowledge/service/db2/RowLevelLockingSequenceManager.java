/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import static com.top_logic.knowledge.service.db2.SequenceTypeProvider.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;

import com.top_logic.basic.Logger;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.service.KnowledgeBase;

/**
 * {@link SequenceManager} that assumes row-level locking for updates on the
 * underlying DB.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class RowLevelLockingSequenceManager implements SequenceManager {

	/**
	 * Creates a {@link RowLevelLockingSequenceManager}.
	 */
	public RowLevelLockingSequenceManager() {
		super();
	}

	@Override
	public long generateId(String sequenceId) {
		ConnectionPool connectionPool = ConnectionPoolRegistry.getDefaultConnectionPool();
		return generateId(connectionPool, sequenceId);
	}

	@Override
	public long generateId(ConnectionPool connectionPool, String sequenceId) {
		PooledConnection connection = connectionPool.borrowWriteConnection();
		try {
			DBHelper dbHelper = connectionPool.getSQLDialect();
			int retryCount = dbHelper.retryCount();
			long nextNumber = nextSequenceNumber(dbHelper, connection, retryCount, sequenceId);
			connection.commit();
			return nextNumber;
		} catch (ThreadDeath ex) {
			// Make sure the following catch block does not interfere with ThreadDeath.
			throw ex;
		} catch (SQLException | RuntimeException | Error ex) {
			throw new RuntimeException("Failed to create an id for sequence '" + sequenceId + "'."
				+ " Cause: " + ex.getMessage(), ex);
		} finally {
			rollbackInFinally(connection);
			connectionPool.releaseWriteConnection(connection);
		}
	}

	private void rollbackInFinally(PooledConnection connection) {
		try {
			connection.rollback();
		} catch (ThreadDeath ex) {
			// Make sure the following catch block does not interfere with ThreadDeath.
			throw ex;
		} catch (SQLException | RuntimeException | Error ex) {
			// Don't throw exceptions in the surrounding "finally" block.
			Logger.error("Failed to rollback transaction. Cause: " + ex.getMessage(), ex);
		}
	}

	@Override
	public long nextSequenceNumber(DBHelper sqlDialect, Connection aContext, int retryCount, String sequence) throws SQLException {
		final String LOCK_SEQUENCE_VALUE_STATEMENT =
			"SELECT "
				// Note: Must select the primary key to make the statement updatable.
				+ sqlDialect.columnRef(SEQUENCE_ID_ATTR.getDBName()) + ", "
				+ sqlDialect.columnRef(CURRENT_VALUE_ATTR.getDBName()) +
				" FROM " + sqlDialect.tableRef(SEQUENCE_TYPE.getDBName()) + sqlDialect.forUpdate1() +
				" WHERE " + sqlDialect.columnRef(SEQUENCE_ID_ATTR.getDBName()) + "=?" + sqlDialect.forUpdate2();
		
		Savepoint sp = sqlDialect.setSavepoint(aContext);
		try {
		while (true) {
			{
				// IGNORE FindBugs(SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING):
				// Dynamic SQL construction is necessary for DBMS abstraction. No user-input is
				// passed to the statement source.
					try (PreparedStatement lockStmt = aContext.prepareStatement(
						LOCK_SEQUENCE_VALUE_STATEMENT, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE)) {
					lockStmt.setString(1, sequence);
					
						try (ResultSet result = lockStmt.executeQuery()) {
						if (result.next()) {
							return update(result);
						}
					}
				} catch (SQLException ex) {
						sqlDialect.rollback(aContext, sp);
					// Assume "Deadlock detected" failure.

					// In certain RDBMS (including MySQL), the lookup might deadlock with a pending
					// sequence setup below, see Ticket #11782.

					if (retryCount-- > 0) {
						Logger.info("Retry " + retryCount + " times to get sequence number for '" + sequence + "' '"
							+ ex.getMessage() + "'", RowLevelLockingSequenceManager.class);
						// Try again to reserve a number.
						continue;
					}
					// Still normal behaviour but better log the stacktrace here.
					Logger.info("Lookup sequence number for '" + sequence + "' failed.", ex,
						RowLevelLockingSequenceManager.class);

					// Break the commit.
					throw ex;
				}
				
				{
					// The sequence entry may not yet have been created. Try to create the sequence.
					try {
							final String newSequenceSQLStatement =
								"INSERT INTO " + sqlDialect.tableRef(SEQUENCE_TYPE.getDBName()) + " VALUES (?, ?)";
						// IGNORE FindBugs(SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING):
						// Dynamic SQL construction is necessary for DBMS abstraction. No user-input is
						// passed to the statement source.
						try (PreparedStatement newSequence = aContext.prepareStatement(newSequenceSQLStatement)){
							newSequence.setString(1, sequence);
							newSequence.setLong(2, 0L);
							newSequence.execute();
						}
			
						// The sequence has been created, reserve the next number.
						continue;
					} catch (SQLException ex) {
							sqlDialect.rollback(aContext, sp);
						// Assume "Duplicate key" failure.
						
						// The problem might still be a concurrent cluster update,
						// where two nodes tried to created the sequence
						// concurrently.
			
						if (retryCount-- > 0) {
							Logger.info("Retry " + retryCount + " times to setup sequence '" + sequence + "' '"
								+ ex.getMessage() + "'", RowLevelLockingSequenceManager.class);
							// Try again to reserve a number.
							continue;
						}
						// Still normal behaviour but better log the stacktrace here.
						Logger.info("Sequence setup for '" + sequence + "' failed.", ex,
							RowLevelLockingSequenceManager.class);
						
						// Break the commit.
						throw ex;
					}
				}
			}
			}
		} finally {
			sqlDialect.releaseSavepoint(aContext, sp);
		}
	}
	
	private long update(ResultSet resultSet) throws SQLException {
		long currentValue = resultSet.getLong(2);

		// Has sequence lock.
		long result = currentValue + 1;

		// Reserve updated number.
		//
		// Note: To actually keep the row lock after the current result set is closed, the retrieved
		// row must be actually updated. Even if MySQL and Oracle seem to keep the row locks if the
		// statement mentions the "FOR UPDATE" clause, e.g. Apache Derby would release the lock, if
		// the retrieved row is not directly updated.
		resultSet.updateLong(2, result);
		resultSet.updateRow();

		return result;
	}

	@Override
	public long getLastSequenceNumber(PooledConnection connection, int retryCount, String sequence)
			throws SQLException {

		while (true) {
			try {
				long currentValue;
				try (PreparedStatement getSequence =
					connection.prepareStatement(getSelectSequenceSQLStatement(connection))) {
					getSequence.setString(1, sequence);
					try (ResultSet result = getSequence.executeQuery()) {
						if (result.next()) {
							currentValue = result.getLong(1);
						} else {
							currentValue = NO_NUMBER_CREATED;
						}
					}
				}

				return currentValue;
			} catch (SQLException ex) {

				// The problem might still be a concurrent cluster update,
				// where two nodes tried to created the sequence
				// concurrently.

				if (retryCount-- > 0) {
					Logger.info("Retry " + retryCount + " times to get last sequence number for '" + sequence + "' '"
						+ ex.getMessage() + "'", RowLevelLockingSequenceManager.class);
					// Try again get the number again
					continue;
				}
				// Still normal behaviour but better log the stacktrace here.
				Logger.info("Looking up last sequence number for '" + sequence + "' failed.", ex,
					RowLevelLockingSequenceManager.class);

				// Break the commit.
				throw ex;
			}
		}
	}

	private String getSelectSequenceSQLStatement(PooledConnection connection) throws SQLException {
		DBHelper sqlDialect = connection.getSQLDialect();
		return "SELECT " + sqlDialect.columnRef(CURRENT_VALUE_ATTR.getDBName())
				+ " FROM " + sqlDialect.tableRef(SEQUENCE_TYPE.getDBName())
			+ " WHERE " + sqlDialect.columnRef(SEQUENCE_ID_ATTR.getDBName()) + "=?";
	}

	/**
	 * Whether the given sequence was already allocated.
	 * 
	 * @return true iff a number was already created for the given sequence
	 * @throws SQLException
	 *         In case of an internal database failure.
	 */
	@Override
	public boolean isUsed(PooledConnection connection, int retryCount, String sequence) throws SQLException {

		while (true) {
			try {
				boolean used = false;
				try (PreparedStatement lockStmt =
					connection.prepareStatement(getSelectSequenceSQLStatement(connection))) {
					lockStmt.setString(1, sequence);

					try (ResultSet result = lockStmt.executeQuery()) {
						if (result.next()) {
							used = true;
						}
					}
				}
				return used;
			} catch (SQLException ex) {

				// The problem might still be a concurrent cluster update,
				// where two nodes tried to created the sequence
				// concurrently.

				if (retryCount-- > 0) {
					Logger.info("Retry " + retryCount + " times to to look up if '" + sequence + "' is in use.'"
						+ ex.getMessage() + "'", RowLevelLockingSequenceManager.class);
					// Try again to look up the table
					continue;
				}
				// Still normal behaviour but better log the stacktrace here.
				Logger.info("Looking up usage of sequence '" + sequence + "' failed.", ex,
					RowLevelLockingSequenceManager.class);

				// Break the commit.
				throw ex;
			}

		}

	}

	/**
	 * Check if the sequence table exists. If not, the {@link KnowledgeBase} has not been set up
	 * yet.
	 * 
	 * @param connection
	 *        Connection to be used for checking.
	 * @return <code>true</code> when sequence table exists.
	 * @throws SQLException
	 *         In case of an internal database failure.
	 */
	public static boolean checkTable(PooledConnection connection) throws SQLException {
		Statement stmt = connection.createStatement();
		try {
			final String checkTableSQLStatement =
				"SELECT * FROM " + connection.getSQLDialect().tableRef(SEQUENCE_TYPE.getDBName()) + " WHERE 1=0";

			ResultSet result = stmt.executeQuery(checkTableSQLStatement);
			try {
				return true;
			} finally {
				result.close();
			}
		} catch (SQLException ex) {
			return false;
		} finally {
			stmt.close();
		}
	}

	/**
	 * Update the given sequence to the given value.
	 * 
	 * @param connection
	 *        Connection to the database containing the sequence table.
	 * @param sequence
	 *        The name of the sequence.
	 * @param newValue
	 *        The new value of the sequence.
	 * @return Whether the update was successful.
	 * 
	 * @throws SQLException
	 *         In case of an internal database failure.
	 */
	public static boolean resetSequence(PooledConnection connection, String sequence, long newValue)
			throws SQLException {
		DBHelper sqlDialect = connection.getSQLDialect();
		final String resetSequenceSQLStatement =
			"UPDATE " + sqlDialect.tableRef(SEQUENCE_TYPE.getDBName()) + " "
				+ "SET " + sqlDialect.columnRef(CURRENT_VALUE_ATTR.getDBName()) + "=? "
				+ "WHERE "
				+ sqlDialect.columnRef(SEQUENCE_ID_ATTR.getDBName()) + "=?";

		PreparedStatement setStmt = connection.prepareStatement(resetSequenceSQLStatement);
		try {
			setStmt.setLong(1, newValue);
			setStmt.setString(2, sequence);
			
			int affectedRows = setStmt.executeUpdate();
			return affectedRows == 1;
		} finally {
			setStmt.close();
		}
	}

	/**
	 * Removes the given sequence from the table.
	 * 
	 * @param connection
	 *        Connection to the database containing the sequence table.
	 * @param sequence
	 *        The name of the sequence.
	 * @return Whether the row was actually removed.
	 * 
	 * @throws SQLException
	 *         In case of an internal database failure.
	 */
	public static boolean dropSequence(PooledConnection connection, String sequence) throws SQLException {
		DBHelper sqlDialect = connection.getSQLDialect();
		final String dropSequenceSQLStatement =
			"DELETE FROM " + sqlDialect.tableRef(SEQUENCE_TYPE.getDBName()) + " "
				+ "WHERE "
				+ sqlDialect.columnRef(SEQUENCE_ID_ATTR.getDBName()) + "=?";

		PreparedStatement setStmt = connection.prepareStatement(dropSequenceSQLStatement);
		try {
			setStmt.setString(1, sequence);

			int affectedRows = setStmt.executeUpdate();
			return affectedRows == 1;
		} finally {
			setStmt.close();
		}
	}

}
