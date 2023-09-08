/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Pattern;

import com.top_logic.basic.Log;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.StringServices;

/**
 * Tool for executing a sequence of SQL statements given as text.
 * 
 * @see #executeSQL(Reader)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SQLLoader {

	/** Pattern that matches comment lines in SQL scripts, i.e. lines that start with "--". */
	private static Pattern SQL_COMMENT_LINE_PATTERN = Pattern.compile("^\\s*--.*$");

	/** the separator of the SQL statements */
	private static String SQL_STATEMENT_SEPARATOR = ";";

	private Log _log = new LogProtocol(SQLLoader.class);

	private final Connection _connection;

	private boolean _autoCommit;

	private boolean _ignoreProblems;

	/**
	 * Creates a {@link SQLLoader}.
	 */
	public SQLLoader(Connection connection) {
		_connection = connection;
	}

	/**
	 * The error log of the load process.
	 */
	public Log getLog() {
		return _log;
	}

	/**
	 * @see #getLog()
	 */
	public SQLLoader setLog(Log log) {
		_log = log;
		return this;
	}

	/**
	 * Whether to commit each statement.
	 * 
	 * <p>
	 * Note: This is independent of a potential auto-commit setting on the underlying
	 * {@link Connection}. By setting the auto-commit option on this {@link SQLLoader}, an explicit
	 * commit is done after each executed statement.
	 * </p>
	 */
	public boolean isAutoCommit() {
		return _autoCommit;
	}

	/**
	 * @see #isAutoCommit()
	 */
	public SQLLoader setAutoCommit(boolean value) {
		_autoCommit = value;
		return this;
	}

	/**
	 * Whether to execute all statements even if some fail with an error.
	 * 
	 * <p>
	 * In any case, a call to {@link #executeSQL(Reader)} throws the first error reported.
	 * </p>
	 */
	public boolean getContinueOnError() {
		return _ignoreProblems;
	}

	/**
	 * @see #getContinueOnError()
	 */
	public SQLLoader setContinueOnError(boolean value) {
		_ignoreProblems = value;
		return this;
	}

	/**
	 * Fires SQL statements read from the given {@link Reader}.
	 *
	 * @param sql
	 *        The SQL statements to execute.
	 */
	public void executeSQL(String sql) throws IOException, SQLException {
		executeSQL(new StringReader(sql));
	}

	/**
	 * Fires SQL statements read from the given {@link Reader}.
	 *
	 * @param sqlReader
	 *        The SQL statements to execute.
	 */
	public void executeSQL(Reader sqlReader) throws IOException, SQLException {
		try (Statement statement = _connection.createStatement()) {
			fireSQL(statement, sqlReader);
		}
	}

	private void fireSQL(Statement statement, Reader sqlReader) throws IOException, SQLException {
		SQLException firstProblem = null;
		
		int statementNo = 1;
		int lineNo = 1;
		int statementLine = lineNo;

	    String sql = "";    // the SQL statement for the executeUpdate method
	    StringBuffer lineBuffer = new StringBuffer(4096);
		boolean inString = false;
		try (BufferedReader in = new BufferedReader(sqlReader)) {
			String line = null; // read first line
			while (true) {
				line = in.readLine();
				if (line == null) {
					break;
				}
	
				if (!inString) {
					if (SQL_COMMENT_LINE_PATTERN.matcher(line).matches()) {
						continue;
					}
				}
	
				inString = consume(inString, line);
	
				if (!inString) {
					line = StringServices.cutTrailing(' ', line);
					lineBuffer.append(line);
					if (line.endsWith(SQL_STATEMENT_SEPARATOR)) {
						lineBuffer.setLength(lineBuffer.length() - 1); // drop ';'
						sql = lineBuffer.toString();
						firstProblem = fireSQL(statement, sql, statementLine, statementNo++, firstProblem);
						lineBuffer.setLength(0); // clear the line buffer
						statementLine = lineNo;
					} else {
						// Replace end of line by space.
						lineBuffer.append(' ');
					}
				} else {
					lineBuffer.append(line);
					lineBuffer.append('\n');
				}

				lineNo++;
			}
	        sql = lineBuffer.toString().trim();
	        if (sql.length() > 0) {
				firstProblem = fireSQL(statement, sql, statementLine, statementNo, firstProblem);
	        }
	    } 
	    
		if (_ignoreProblems) {
			if (firstProblem != null) {
				throw firstProblem;
			}
		}
	}

	private boolean consume(boolean inString, String line) {
		for (int n = 0, len = line.length(); n < len; n++) {
			if (line.charAt(n) == '\'') {
				inString = !inString;
			}
		}
		return inString;
	}

	private SQLException fireSQL(Statement statement, String sql, int statementLine, int statementNo,
			SQLException firstProblem) throws SQLException {
		try {
			_log.info(
				"Processing statement " + statementNo + " at line '" + statementLine + "': " + ellipsis(150, sql));

			// IGNORE FindBugs(SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE): Statement is loaded from
			// trusted source.
			statement.executeUpdate(sql); // fire SQL statement to database

			if (_autoCommit) {
				_connection.commit();
			}
		} catch (SQLException ex) {
			firstProblem = handleProblem(sql, firstProblem, ex);
		}
		return firstProblem;
	}

	private static String ellipsis(int limit, String sql) {
		int max = sql.indexOf('\n');
		int length = sql.length();
		if (max < 0) {
			max = Math.min(limit, length);
		} else {
			max = Math.min(limit, max);
		}
		if (max == length) {
			return sql;
		} else {
			return sql.substring(0, Math.min(limit - 3, max)) + "...";
		}
	}

	private SQLException handleProblem(String sql, SQLException firstProblem, SQLException ex) throws SQLException {
		if (_ignoreProblems) {
			if (firstProblem == null) {
				firstProblem = ex;
			}
			_log.error("SQL statement failed: " + sql + ".", ex);
		} else {
			// Terminate processing.
			throw new SQLException("Statement failed: " + sql, ex.getSQLState(), ex.getErrorCode(), ex);
		}
		return firstProblem;
	}

}