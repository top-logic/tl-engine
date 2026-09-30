/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.LongDefault;

/**
 * {@link DBHelper} for the H2 database engine.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class H2Helper extends DBHelper {

	/**
	 * Configuration of the {@link H2Helper}
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends DBHelper.Config {

		/** Maximum allowed length of varchar. */
		long VARCHAR_LENGTH_LIMIT = Integer.MAX_VALUE;

		@LongDefault(VARCHAR_LENGTH_LIMIT)
		@Override
		long getVarcharLimit();

		@LongDefault(VARCHAR_LENGTH_LIMIT)
		@Override
		long getNVarcharLimit();
	}

	private static final Format BOOLEAN_FORMAT = new Format() {

		@Override
		public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
			if (obj == null) {
				toAppendTo.append("UNKNOWN");
				return toAppendTo;
			}

			if (obj instanceof Boolean) {
				toAppendTo.append(((Boolean) obj).booleanValue() ? "TRUE" : "FALSE");
				return toAppendTo;
			}

			if (obj instanceof Number) {
				toAppendTo.append(((Number) obj).intValue() != 0 ? "TRUE" : "FALSE");
				return toAppendTo;
			}

			String value = obj.toString();
			toAppendTo.append(value.equalsIgnoreCase("true") ? "TRUE" : "FALSE");
			return toAppendTo;
		}

		@Override
		public Object parseObject(String source, ParsePosition pos) {
			throw new UnsupportedOperationException();
		}

	};

	/**
	 * Creates a new {@link H2Helper}.
	 */
	public H2Helper(InstantiationContext context, Config config) {
		super(context, config);
		if (config.getVarcharLimit() > Config.VARCHAR_LENGTH_LIMIT) {
			context.error("Value of " + Config.VARCHAR_LIMIT + " must be less or equla to "
				+ Config.VARCHAR_LENGTH_LIMIT + ". Configuration: " + config.getVarcharLimit());
		}
		if (config.getNVarcharLimit() > Config.VARCHAR_LENGTH_LIMIT) {
			context.error("Value of " + Config.NVARCHAR_LIMIT + " must be less or equla to "
				+ Config.VARCHAR_LENGTH_LIMIT + ". Configuration: " + config.getNVarcharLimit());
		}
	}

	@Override
	public String nullSpec() {
		return "DEFAULT NULL";
	}

	@Override
	protected Format getBooleanFormat() {
		return BOOLEAN_FORMAT;
	}

	@Override
	protected String internalGetDBType(int sqlType, boolean binary) {
		switch (sqlType) {
			case Types.VARCHAR:
			case Types.NVARCHAR:
			case Types.LONGVARCHAR:
			case Types.LONGNVARCHAR:
				if (binary) {
					return "VARCHAR";
				} else {
					return "VARCHAR_IGNORECASE";
				}
			case Types.FLOAT:
				return "REAL";
			default:
				return super.internalGetDBType(sqlType, binary);
		}
	}

	@Override
	public String tableRef(String tableName) {
		StringBuilder tableRef = new StringBuilder(tableName.length() + 2);
		tableRef.append('"');
		tableRef.append(tableName);
		tableRef.append('"');
		return tableRef.toString();
	}

	@Override
	public String columnRef(String columnName) {
		StringBuilder columnRef = new StringBuilder(columnName.length() + 2);
		columnRef.append('"');
		columnRef.append(columnName);
		columnRef.append('"');
		return columnRef.toString();
	}

	@Override
	protected void appendFloatType(Appendable result, boolean mandatory) throws IOException {
		result.append("FLOAT4");
	}

	@Override
	protected void appendStringType(Appendable result, String columnName, long size, boolean mandatory, boolean binary, boolean castContext)
			throws IOException {
		result.append(internalGetDBType(Types.VARCHAR, binary));
		size(result, size);
	}

	@Override
	public boolean canRetry(SQLException sqlX) {
		switch (sqlX.getErrorCode()) {
			case 90039: // LOB_CLOSED_ON_TIMEOUT_1
			case 90007: // OBJECT_CLOSED
			case 90067: // CONNECTION_BROKEN_1
			case 90098: // DATABASE_IS_CLOSED
			case 50200: // LOCK_TIMEOUT_1
			case 40001: // DEADLOCK_1
			case 90131: // CONCURRENT_UPDATE_1
				return true;
		}
		return super.canRetry(sqlX);
	}

	@Override
	public String fnNow() {
		return "CURRENT_TIMESTAMP";
	}

	@Override
	public String fromNoTable() {
		return " FROM DUAL";
	}

	@Override
	protected void internalAppendCollatedExpression(Appendable buffer, String sqlExpression, CollationHint collationHint)
			throws IOException {
		switch (collationHint) {
			case NONE:
				super.internalAppendCollatedExpression(buffer, sqlExpression, collationHint);
				break;
			case BINARY:
				buffer.append("CAST(");
				super.internalAppendCollatedExpression(buffer, sqlExpression, collationHint);
				buffer.append(" AS VARCHAR)");
				break;
			case NATURAL:
				buffer.append("CAST(");
				super.internalAppendCollatedExpression(buffer, sqlExpression, collationHint);
				buffer.append(" AS VARCHAR_IGNORECASE)");
				break;
		}
	}

	@Override
	public boolean supportsLimitStart() {
		return true;
	}

	@Override
	public boolean supportsLimitStop() {
		return true;
	}

	@Override
	public int getMaxNameLength() {
		return 256;
	}

	@Override
	public StringBuilder limitLast(StringBuilder sql, int startRow, int stopRow) {
		if (stopRow >= 0) {
			sql.append(" LIMIT ");
			sql.append(stopRow - startRow);
		}
		if (startRow > 0) {
			if (stopRow < 0) {
				sql.append(" LIMIT " + Integer.MAX_VALUE);
			}
			sql.append(" OFFSET ");
			sql.append(startRow);
		}
		return sql;
	}

	@Override
	public String getCurrentSchema(Connection connection) throws SQLException {
		return "PUBLIC";
	}

	@Override
	public String forUpdate2() {
		return " FOR UPDATE";
	}

	@Override
	protected Format getDateFormat() {
		return dateFormat("'DATE' ''yyyy-MM-dd''");
	}

	@Override
	protected Format getTimeFormat() {
		return dateFormat("'TIME' ''HH:mm:ss.SSS''");
	}

	@Override
	protected Format getDateTimeFormat() {
		return dateFormat("'TIMESTAMP' ''yyyy-MM-dd HH:mm:ss.SSS''");
	}

	@Override
	public boolean supportsUnicodeSupplementaryCharacters() {
		return false;
	}

	/**
	 * Create a valid SQL statement to drop an index.
	 * 
	 * H2 does not need (or use) the tableName
	 */
	@Override
	public void appendDropIndex(Appendable sql, String idxName, String tableName) throws IOException {
		sql.append("DROP INDEX ");
		sql.append(columnRef(idxName));
	}

	@Override
	public void appendChangeColumnName(Appendable result, String tableName, DBType sqlType, String columnName, String newName,
			long size, int precision, boolean mandatory, boolean binary, Object defaultValue) throws IOException {
		result.append(alterTable(tableName));
		appendModifyColumnKeyword(result);
		result.append(columnRef(columnName));
		result.append(" ");
		result.append("RENAME TO ");
		result.append(columnRef(newName));
	}

	@Override
	protected void appendModifyColumnKeyword(Appendable result) throws IOException {
		result.append("ALTER COLUMN ");
	}

	@Override
	public boolean allowParameterColumn() {
		return false;
	}
}
