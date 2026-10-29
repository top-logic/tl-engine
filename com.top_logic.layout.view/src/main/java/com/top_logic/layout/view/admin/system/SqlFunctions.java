/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.system;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.SimpleDBExecutor;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.view.admin.system.SqlResult.Column;
import com.top_logic.layout.view.admin.system.SqlResult.Row;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.config.operations.AdminOnly;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.SideEffectFree;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;

/**
 * TL-Script functions running SQL statements entered by the administrator against the database
 * of the application: the SQL console of the administration.
 *
 * <p>
 * {@link #query(String, int)} runs a query and reads at most a given number of its rows,
 * {@link #update(String)} runs an update and commits it, {@link #simulate(String)} runs an update
 * and rolls it back. Each of them answers with an {@link SqlResult}, which the other functions
 * describe for a table whose columns are the columns of the query: {@link #columns(SqlResult)},
 * {@link #rows(SqlResult)}, {@link #value(Row, Column)}. A statement the database rejects fails
 * with the message of the database.
 * </p>
 *
 * <p>
 * Every statement is written to the application log together with the account running it. The
 * rows read are not.
 * </p>
 *
 * <p>
 * The statements run beside the knowledge base: an update of its tables is not seen by the objects
 * it holds in its caches, until the persistency layer is restarted.
 * </p>
 *
 * @implNote Every public static method here is a TL-Script function named by the
 *           {@link ScriptPrefix} of this class followed by the capitalized method name, so
 *           {@link #query(String, int)} is called <code>sqlQuery</code>. The functions read and
 *           write the database bypassing the access control, which only an administrator may do
 *           from a script entered interactively.
 */
@ScriptPrefix(SqlFunctions.PREFIX)
@AdminOnly
public class SqlFunctions extends TLScriptFunctions {

	/** Prefix all function names of this class start with. */
	public static final String PREFIX = "sql";

	/**
	 * Number of rows {@link #query(String, int)} reads at most, unless told otherwise.
	 */
	public static final int DEFAULT_MAX_ROWS = 1000;

	/**
	 * Number of statements {@link #remember(List, String, SqlStatementKind)} keeps in a history.
	 */
	public static final int MAX_HISTORY = 256;

	/**
	 * Prefix of the {@link #columnName(Column) name of a column}, followed by its position.
	 */
	private static final String COLUMN_NAME_PREFIX = "c";

	/**
	 * Runs a query against the database of the application.
	 *
	 * @param statement
	 *        The SQL query.
	 * @param maxRows
	 *        The greatest number of rows to read. If the query delivers more, the result is
	 *        marked as {@link SqlResult#truncated() truncated}.
	 * @return The columns and the rows read.
	 *
	 * @throws TopLogicException
	 *         If the database rejects the statement, with the message of the database.
	 */
	@Label("Run an SQL query")
	public static SqlResult query(String statement, @IntDefault(DEFAULT_MAX_ROWS) int maxRows) {
		String sql = checkStatement(statement);
		if (maxRows < 1) {
			throw new TopLogicException(I18NConstants.ERROR_SQL_MAX_ROWS);
		}
		ConnectionPool pool = pool();
		log(SqlStatementKind.QUERY, sql);

		long start = System.currentTimeMillis();
		PooledConnection connection = pool.borrowReadConnection();
		boolean success = false;
		try {
			SqlResult result;
			try (Statement query = connection.createStatement()) {
				// One row more than shown tells whether there are more.
				query.setMaxRows(maxRows < Integer.MAX_VALUE ? maxRows + 1 : 0);
				try (ResultSet rows = query.executeQuery(sql)) {
					result = SqlResult.read(sql, rows, maxRows, start);
				}
			}
			success = true;
			return result;
		} catch (SQLException ex) {
			throw rejected(ex);
		} finally {
			if (success) {
				pool.releaseReadConnection(connection);
			} else {
				// The failure may have left the connection in a state that fails further
				// statements, e.g. in an aborted transaction.
				pool.invalidateReadConnection(connection);
			}
		}
	}

	/**
	 * Runs an update against the database of the application and commits it.
	 *
	 * <p>
	 * The knowledge base does not see a change of its tables in the objects it holds in its
	 * caches.
	 * </p>
	 *
	 * @param statement
	 *        The SQL statement changing data or the schema.
	 * @return The number of rows affected.
	 *
	 * @throws TopLogicException
	 *         If the database rejects the statement, with the message of the database. Nothing is
	 *         committed then.
	 */
	@Label("Run an SQL update")
	public static SqlResult update(String statement) {
		return runUpdate(statement, SqlStatementKind.UPDATE);
	}

	/**
	 * Runs an update against the database of the application and rolls it back.
	 *
	 * <p>
	 * A statement the database commits implicitly, e.g. one changing the schema in many
	 * databases, cannot be rolled back.
	 * </p>
	 *
	 * @param statement
	 *        The SQL statement changing data.
	 * @return The number of rows the update would affect.
	 *
	 * @throws TopLogicException
	 *         If the database rejects the statement, with the message of the database.
	 */
	@Label("Simulate an SQL update")
	public static SqlResult simulate(String statement) {
		return runUpdate(statement, SqlStatementKind.SIMULATION);
	}

	private static SqlResult runUpdate(String statement, SqlStatementKind kind) {
		String sql = checkStatement(statement);
		ConnectionPool pool = pool();
		log(kind, sql);

		long start = System.currentTimeMillis();
		SimpleDBExecutor db = new SimpleDBExecutor(pool);
		db.beginTransaction();
		try {
			int count = db.executeUpdate(sql);
			if (kind == SqlStatementKind.SIMULATION) {
				db.rollbackTransaction();
			} else {
				db.commitTransaction();
			}
			return SqlResult.update(sql, kind, count, start);
		} catch (SQLException ex) {
			throw rejected(ex);
		} finally {
			// Rolls back what was not completed and drops the connection after a failure.
			db.closeTransaction();
		}
	}

	/**
	 * Whether the given result is the one of a query.
	 *
	 * @param result
	 *        The result of a statement.
	 * @return Whether the result holds rows, <code>false</code> without a result.
	 */
	@SideEffectFree
	@Label("Whether a result is the one of a query")
	public static boolean isQuery(SqlResult result) {
		return result != null && result.kind() == SqlStatementKind.QUERY;
	}

	/**
	 * The columns of the given query result.
	 *
	 * @param result
	 *        The result of a statement.
	 * @return The columns in the order of the query, empty for the result of an update and
	 *         without a result.
	 */
	@SideEffectFree
	@Label("Columns of a query result")
	public static List<Column> columns(SqlResult result) {
		return result == null ? List.of() : result.columns();
	}

	/**
	 * The rows read of the given query result.
	 *
	 * @param result
	 *        The result of a statement.
	 * @return The rows in the order the database delivered them, empty for the result of an
	 *         update and without a result.
	 */
	@SideEffectFree
	@Label("Rows of a query result")
	public static List<Row> rows(SqlResult result) {
		return result == null ? List.of() : result.rows();
	}

	/**
	 * A name of the given column unique among the columns of its result.
	 *
	 * @param column
	 *        A column of a query result.
	 * @return The name, built from the position of the column, since the query may name two
	 *         columns alike.
	 */
	@SideEffectFree
	@Label("Name of a result column")
	public static String columnName(Column column) {
		return column == null ? null : COLUMN_NAME_PREFIX + column.index();
	}

	/**
	 * The name the database gives the given column.
	 *
	 * @param column
	 *        A column of a query result.
	 * @return The label of the column in the query.
	 */
	@SideEffectFree
	@Label("Label of a result column")
	public static String columnLabel(Column column) {
		return column == null ? null : column.label();
	}

	/**
	 * The type of the values in the given column.
	 *
	 * @param column
	 *        A column of a query result.
	 * @return The primitive model type fitting the SQL type of the column, the type of texts for
	 *         a value that has no other.
	 */
	@SideEffectFree
	@Label("Type of a result column")
	public static TLType columnType(Column column) {
		return column == null ? null : TLModelUtil.findType(column.typeSpec());
	}

	/**
	 * The value of a row of a query result in a column.
	 *
	 * @param row
	 *        A row of a query result.
	 * @param column
	 *        A column of the same result.
	 * @return The value, <code>null</code> for an SQL <code>NULL</code>.
	 */
	@SideEffectFree
	@Label("Value of a result row")
	public static Object value(Row row, Column column) {
		return row == null || column == null ? null : row.value(column);
	}

	/**
	 * What the given result says about the statement: the number of rows read or affected, and
	 * the time the statement took.
	 *
	 * @param result
	 *        The result of a statement.
	 * @return The message, <code>null</code> without a result.
	 */
	@SideEffectFree
	@Label("Message of a statement result")
	public static String message(SqlResult result) {
		if (result == null) {
			return null;
		}
		Long duration = Long.valueOf(result.duration());
		ResKey message;
		switch (result.kind()) {
			case QUERY:
				Integer rows = Integer.valueOf(result.rows().size());
				message = result.truncated()
					? I18NConstants.SQL_QUERY_TRUNCATED__ROWS_DURATION.fill(rows, duration)
					: I18NConstants.SQL_QUERY__ROWS_DURATION.fill(rows, duration);
				break;
			case UPDATE:
				message = I18NConstants.SQL_UPDATE__ROWS_DURATION.fill(Integer.valueOf(result.updateCount()), duration);
				break;
			case SIMULATION:
				message =
					I18NConstants.SQL_SIMULATION__ROWS_DURATION.fill(Integer.valueOf(result.updateCount()), duration);
				break;
			default:
				throw new IllegalArgumentException("No such kind of statement: " + result.kind());
		}
		return Resources.getInstance().getString(message);
	}

	/**
	 * Adds a statement to a history of statements.
	 *
	 * @param history
	 *        The statements run so far, the newest first. Elements that are no
	 *        {@link SqlHistoryEntry} are dropped.
	 * @param statement
	 *        The statement to add.
	 * @param kind
	 *        How the statement is run.
	 * @return The history with the statement in front, holding at most the {@link #MAX_HISTORY}
	 *         newest statements. The given history unchanged, if no statement is given.
	 */
	@SideEffectFree
	@Label("Add a statement to a history")
	public static List<SqlHistoryEntry> remember(List<?> history, String statement, SqlStatementKind kind) {
		List<SqlHistoryEntry> entries = new ArrayList<>();
		if (history != null) {
			for (Object entry : history) {
				if (entry instanceof SqlHistoryEntry sqlEntry) {
					entries.add(sqlEntry);
				}
			}
		}
		if (statement == null || statement.isBlank() || kind == null) {
			return entries;
		}
		int number = entries.isEmpty() ? 1 : entries.get(0).number() + 1;
		entries.add(0, new SqlHistoryEntry(number, kind, statement.strip()));
		while (entries.size() > MAX_HISTORY) {
			entries.remove(entries.size() - 1);
		}
		return entries;
	}

	/**
	 * The running number of the given statement in its history.
	 *
	 * @param entry
	 *        A statement of a history.
	 * @return The number, <code>null</code> without an entry.
	 */
	@SideEffectFree
	@Label("Number of a statement in the history")
	public static Integer historyNumber(SqlHistoryEntry entry) {
		return entry == null ? null : Integer.valueOf(entry.number());
	}

	/**
	 * How the given statement of a history was run.
	 *
	 * @param entry
	 *        A statement of a history.
	 * @return The kind, <code>null</code> without an entry.
	 */
	@SideEffectFree
	@Label("Kind of a statement in the history")
	public static SqlStatementKind historyKind(SqlHistoryEntry entry) {
		return entry == null ? null : entry.kind();
	}

	/**
	 * The text of the given statement of a history.
	 *
	 * @param entry
	 *        A statement of a history.
	 * @return The statement, <code>null</code> without an entry.
	 */
	@SideEffectFree
	@Label("Text of a statement in the history")
	public static String historyStatement(SqlHistoryEntry entry) {
		return entry == null ? null : entry.statement();
	}

	private static String checkStatement(String statement) {
		String sql = statement == null ? "" : statement.strip();
		if (sql.isEmpty()) {
			throw new TopLogicException(I18NConstants.ERROR_SQL_NO_STATEMENT);
		}
		return sql;
	}

	private static ConnectionPool pool() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		if (!(kb instanceof DBKnowledgeBase db)) {
			throw new TopLogicException(I18NConstants.ERROR_SQL_NO_DATABASE);
		}
		return db.getConnectionPool();
	}

	private static TopLogicException rejected(SQLException ex) {
		String message = ex.getMessage();
		return new TopLogicException(
			I18NConstants.ERROR_SQL_REJECTED__MESSAGE.fill(message == null ? ex.toString() : message), ex);
	}

	private static void log(SqlStatementKind kind, String sql) {
		Person user = TLContext.currentUser();
		Logger.info("SQL console, " + kind + " by " + (user == null ? "<unknown>" : user.getName()) + ": " + sql,
			SqlFunctions.class);
	}

}
