/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.system;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.element.meta.TypeSpec;
import com.top_logic.util.Resources;

/**
 * The outcome of an SQL statement run by the {@link SqlFunctions}: the rows a query found, or the
 * number of rows an update affected.
 *
 * <p>
 * A query result holds at most the number of rows it was read with and tells whether the database
 * would have delivered more, see {@link #truncated()}. Each value is converted into the kind of
 * value of a {@link Column#typeSpec() model type} fitting the SQL type of its column, so that the
 * values of a column sort and display as numbers, dates or texts.
 * </p>
 */
public final class SqlResult {

	/**
	 * The greatest number of characters a text value is shown with. A longer text is cut there.
	 */
	public static final int MAX_TEXT_LENGTH = 4096;

	/**
	 * The greatest number of digits a whole number of type {@link Types#NUMERIC} or
	 * {@link Types#DECIMAL} may have to be read as {@link Long}.
	 */
	private static final int MAX_LONG_DIGITS = 18;

	private final String _statement;

	private final SqlStatementKind _kind;

	private final List<Column> _columns;

	private final List<Row> _rows;

	private final boolean _truncated;

	private final int _updateCount;

	private final long _duration;

	private SqlResult(String statement, SqlStatementKind kind, List<Column> columns, List<Row> rows,
			boolean truncated, int updateCount, long duration) {
		_statement = statement;
		_kind = kind;
		_columns = columns;
		_rows = rows;
		_truncated = truncated;
		_updateCount = updateCount;
		_duration = duration;
	}

	/**
	 * Reads the result of a query.
	 *
	 * @param statement
	 *        The query that was run.
	 * @param result
	 *        The rows the database delivers.
	 * @param maxRows
	 *        The greatest number of rows to read. If the database delivers more, the result is
	 *        {@link #truncated()}.
	 * @param start
	 *        The time in milliseconds when the query was started.
	 */
	public static SqlResult read(String statement, ResultSet result, int maxRows, long start) throws SQLException {
		ResultSetMetaData meta = result.getMetaData();
		int columnCount = meta.getColumnCount();
		List<Column> columns = new ArrayList<>(columnCount);
		List<ValueKind> kinds = new ArrayList<>(columnCount);
		for (int n = 1; n <= columnCount; n++) {
			ValueKind kind = valueKind(meta, n);
			kinds.add(kind);
			columns.add(new Column(n, meta.getColumnLabel(n), kind.typeSpec()));
		}

		List<Row> rows = new ArrayList<>();
		boolean truncated = false;
		while (result.next()) {
			if (rows.size() == maxRows) {
				truncated = true;
				break;
			}
			List<Object> values = new ArrayList<>(columnCount);
			for (int n = 1; n <= columnCount; n++) {
				values.add(kinds.get(n - 1).read(result, n));
			}
			rows.add(new Row(Collections.unmodifiableList(values)));
		}
		return new SqlResult(statement, SqlStatementKind.QUERY, Collections.unmodifiableList(columns),
			Collections.unmodifiableList(rows), truncated, 0, System.currentTimeMillis() - start);
	}

	/**
	 * The result of an update.
	 *
	 * @param statement
	 *        The update that was run.
	 * @param kind
	 *        Whether the update was {@link SqlStatementKind#UPDATE committed} or
	 *        {@link SqlStatementKind#SIMULATION rolled back}.
	 * @param updateCount
	 *        The number of rows affected.
	 * @param start
	 *        The time in milliseconds when the update was started.
	 */
	public static SqlResult update(String statement, SqlStatementKind kind, int updateCount, long start) {
		return new SqlResult(statement, kind, Collections.emptyList(), Collections.emptyList(), false, updateCount,
			System.currentTimeMillis() - start);
	}

	/**
	 * The statement that was run.
	 */
	public String statement() {
		return _statement;
	}

	/**
	 * How the {@link #statement()} was run.
	 */
	public SqlStatementKind kind() {
		return _kind;
	}

	/**
	 * The columns of a query result, in the order of the query. Empty for an update.
	 */
	public List<Column> columns() {
		return _columns;
	}

	/**
	 * The rows read of a query result. Empty for an update.
	 */
	public List<Row> rows() {
		return _rows;
	}

	/**
	 * Whether the database would have delivered more {@link #rows()} than were read.
	 */
	public boolean truncated() {
		return _truncated;
	}

	/**
	 * The number of rows an update affected, <code>0</code> for a query.
	 */
	public int updateCount() {
		return _updateCount;
	}

	/**
	 * The time in milliseconds the statement took, including reading the rows.
	 */
	public long duration() {
		return _duration;
	}

	private static ValueKind valueKind(ResultSetMetaData meta, int column) throws SQLException {
		switch (meta.getColumnType(column)) {
			case Types.BIT:
			case Types.BOOLEAN:
				return ValueKind.BOOLEAN;
			case Types.TINYINT:
			case Types.SMALLINT:
			case Types.INTEGER:
			case Types.BIGINT:
				return ValueKind.LONG;
			case Types.NUMERIC:
			case Types.DECIMAL: {
				int precision = meta.getPrecision(column);
				if (meta.getScale(column) == 0 && precision > 0 && precision <= MAX_LONG_DIGITS) {
					return ValueKind.LONG;
				}
				// Read exactly: a double cannot hold every number of this type.
				return ValueKind.DECIMAL;
			}
			case Types.REAL:
			case Types.FLOAT:
			case Types.DOUBLE:
				return ValueKind.DOUBLE;
			case Types.DATE:
			case Types.TIMESTAMP:
			case Types.TIMESTAMP_WITH_TIMEZONE:
				return ValueKind.DATE_TIME;
			case Types.BINARY:
			case Types.VARBINARY:
			case Types.LONGVARBINARY:
				return ValueKind.BYTES;
			case Types.BLOB:
				return ValueKind.BLOB;
			case Types.CLOB:
			case Types.NCLOB:
				return ValueKind.CLOB;
			default:
				return ValueKind.TEXT;
		}
	}

	private static String cut(String text) {
		if (text == null || text.length() <= MAX_TEXT_LENGTH) {
			return text;
		}
		return text.substring(0, MAX_TEXT_LENGTH) + "...";
	}

	private static String binary(long length) {
		return Resources.getInstance().getString(I18NConstants.SQL_BINARY_VALUE__SIZE.fill(Long.valueOf(length)));
	}

	/**
	 * How the values of a column are read and which model type they are values of.
	 */
	private enum ValueKind {

		/** A truth value. */
		BOOLEAN(TypeSpec.BOOLEAN_TYPE) {
			@Override
			Object read(ResultSet result, int column) throws SQLException {
				boolean value = result.getBoolean(column);
				return result.wasNull() ? null : Boolean.valueOf(value);
			}
		},

		/** A whole number. */
		LONG(TypeSpec.LONG_TYPE) {
			@Override
			Object read(ResultSet result, int column) throws SQLException {
				long value = result.getLong(column);
				return result.wasNull() ? null : Long.valueOf(value);
			}
		},

		/** A floating point number. */
		DOUBLE(TypeSpec.DOUBLE_TYPE) {
			@Override
			Object read(ResultSet result, int column) throws SQLException {
				double value = result.getDouble(column);
				return result.wasNull() ? null : Double.valueOf(value);
			}
		},

		/** A decimal number, shown with all of its digits. */
		DECIMAL(TypeSpec.STRING_TYPE) {
			@Override
			Object read(ResultSet result, int column) throws SQLException {
				BigDecimal value = result.getBigDecimal(column);
				return value == null ? null : value.toPlainString();
			}
		},

		/** A point in time. */
		DATE_TIME(TypeSpec.DATE_TIME_TYPE) {
			@Override
			Object read(ResultSet result, int column) throws SQLException {
				return result.getTimestamp(column);
			}
		},

		/** Binary data, shown by its size. */
		BYTES(TypeSpec.STRING_TYPE) {
			@Override
			Object read(ResultSet result, int column) throws SQLException {
				byte[] value = result.getBytes(column);
				return value == null ? null : binary(value.length);
			}
		},

		/** A binary large object, shown by its size. */
		BLOB(TypeSpec.STRING_TYPE) {
			@Override
			Object read(ResultSet result, int column) throws SQLException {
				Blob value = result.getBlob(column);
				return value == null ? null : binary(value.length());
			}
		},

		/** A character large object. */
		CLOB(TypeSpec.STRING_TYPE) {
			@Override
			Object read(ResultSet result, int column) throws SQLException {
				Clob value = result.getClob(column);
				if (value == null) {
					return null;
				}
				long length = value.length();
				return cut(value.getSubString(1, (int) Math.min(length, MAX_TEXT_LENGTH + 1)));
			}
		},

		/** Any other value, shown as the text the database gives for it. */
		TEXT(TypeSpec.STRING_TYPE) {
			@Override
			Object read(ResultSet result, int column) throws SQLException {
				return cut(result.getString(column));
			}
		};

		private final String _typeSpec;

		ValueKind(String typeSpec) {
			_typeSpec = typeSpec;
		}

		/**
		 * The qualified name of the model type the values are values of.
		 */
		String typeSpec() {
			return _typeSpec;
		}

		/**
		 * Reads the value of the given column of the current row.
		 */
		abstract Object read(ResultSet result, int column) throws SQLException;
	}

	/**
	 * A column of a query result.
	 *
	 * @param index
	 *        The position of the column in the query, starting with <code>1</code>. Unique among
	 *        the columns of a result, other than the {@link #label()}.
	 * @param label
	 *        The name the database gives the column.
	 * @param typeSpec
	 *        The qualified name of the model type the values of this column are values of.
	 */
	public record Column(int index, String label, String typeSpec) {
		// Accessors are generated.
	}

	/**
	 * A row of a query result.
	 *
	 * <p>
	 * Rows are compared by identity, so that two rows with equal values stay apart, e.g. in a
	 * table selection.
	 * </p>
	 */
	public static final class Row {

		private final List<Object> _values;

		Row(List<Object> values) {
			_values = values;
		}

		/**
		 * The value in the given column.
		 */
		public Object value(Column column) {
			return _values.get(column.index() - 1);
		}

		/**
		 * The values of this row, in the order of the {@link SqlResult#columns()}.
		 */
		public List<Object> values() {
			return _values;
		}
	}
}
