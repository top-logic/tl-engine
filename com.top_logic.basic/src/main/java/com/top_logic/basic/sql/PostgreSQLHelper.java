/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

import org.postgresql.jdbc.AutoSave;
import org.postgresql.jdbc.PgConnection;
import org.postgresql.util.PGTime;
import org.postgresql.util.PGTimestamp;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.binary.AbstractBinaryData;
import com.top_logic.basic.io.binary.BinaryData;

/**
 * {@link DBHelper} for the PostgreSQL database engine.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class PostgreSQLHelper extends DBHelper {

	private static Character CHAR_NULL_PLACEHOLDER = '#';

	private static final Format BOOLEAN_FORMAT = new Format() {

		@Override
		public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
			if (obj == null) {
				toAppendTo.append("NULL");
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
	 * Creates a new {@link PostgreSQLHelper}.
	 */
	public PostgreSQLHelper(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public String nullSpec() {
		return "";
	}

	@Override
	protected String internalGetDBType(int sqlType, boolean binary) {
		switch (sqlType) {
			case Types.TINYINT:
				return "SMALLINT";

			case Types.BLOB:
				return "BYTEA";
		}

		return super.internalGetDBType(sqlType, binary);
	}
	
	@Override
	protected Format getBooleanFormat() {
		return BOOLEAN_FORMAT;
	}

	@Override
	protected void appendFloatType(Appendable result, boolean mandatory) throws IOException {
		result.append("REAL");
	}

	@Override
	protected void appendDoubleType(Appendable result, boolean mandatory) throws IOException {
		result.append("DOUBLE PRECISION");
	}

	@Override
	protected void appendClobType(Appendable result, String columnName, long size, boolean mandatory, boolean binary,
			boolean castContext) throws IOException {
		// According to https://github.com/pgjdbc/pgjdbc/issues/458, PostgreSQL does not support
		// CLOB types.
		result.append("TEXT");
	}

	@Override
	public String getClobValue(ResultSet result, int index) throws SQLException {
		// According to https://github.com/pgjdbc/pgjdbc/issues/458, PostgreSQL does not support
		// CLOB types.
		return result.getString(index);
	}

	@Override
	protected void internalCheck(Statement statement) throws SQLException {
		AutoSave autosave = getAutosaveMode(statement.getConnection());

		if (autosave == AutoSave.NEVER) {
			Logger.error("PostgreSQL connection does not use autosave mode.", PostgreSQLHelper.class);
		}
	}

	private AutoSave getAutosaveMode(Connection connection) throws SQLException {
		PgConnection pgConnection = connection.unwrap(PgConnection.class);

		return pgConnection.getAutosave();
	}

	@Override
	protected void setClob(PreparedStatement pstm, int col, int length, Reader reader) throws SQLException {
		pstm.setCharacterStream(col, reader, length);
	}

	@Override
	protected void setClob(PreparedStatement pstm, int col, Reader reader) throws SQLException {
		pstm.setCharacterStream(col, reader);
	}

	@Override
	protected void setClob(PreparedStatement pstm, int col, Clob clob) throws SQLException {
		pstm.setClob(col, clob);
	}

	@Override
	protected void internalSetNull(PreparedStatement pstm, int col, DBType dbtype) throws SQLException {
		switch (dbtype) {
			case BLOB:
				pstm.setNull(col, Types.VARBINARY);
				break;
			case CLOB:
				pstm.setNull(col, Types.VARCHAR);
				break;
			default:
				super.internalSetNull(pstm, col, dbtype);
		}
	}

	@Override
	protected void setBlob(PreparedStatement pstm, int col, InputStream stream, long size) throws SQLException {
		pstm.setBinaryStream(col, stream, size);
	}

	@Override
	protected void setBlob(PreparedStatement pstm, int col, InputStream stream) throws SQLException {
		pstm.setBinaryStream(col, stream);
	}

	@Override
	protected void appendStringType(Appendable result, String columnName, long size, boolean mandatory, boolean binary,
			boolean castContext) throws IOException {
		super.appendStringType(result, columnName, size, mandatory, binary, castContext);

		if (!castContext) {
			appendCollationDefinition(result, binary);
		}
	}

	@Override
	protected void internalAppendCollatedExpression(Appendable buffer, String sqlExpression,
			CollationHint collationHint) throws IOException {
		switch (collationHint) {
			case NONE:
				super.internalAppendCollatedExpression(buffer, sqlExpression, collationHint);
				break;
			case BINARY:
				super.internalAppendCollatedExpression(buffer, sqlExpression, collationHint);
				buffer.append(" COLLATE \"C\" ");
				break;
			case NATURAL:
				super.internalAppendCollatedExpression(buffer, sqlExpression, collationHint);
				buffer.append(" COLLATE \"default\" ");
				break;
		}
	}

	@Override
	protected void internalAppendCastExpression(Appendable buffer, String sqlExpression, DBType dbType, long size,
			int precision, boolean binary) throws IOException {
		super.internalAppendCastExpression(buffer, sqlExpression, dbType, size, precision, binary);

		if (dbType == DBType.STRING) {
			appendCollationDefinition(buffer, binary);
		}
	}

	private void appendCollationDefinition(Appendable result, boolean binary) throws IOException {
		if (binary) {
			result.append(" COLLATE \"C\"");
		}
	}

	@Override
	public String forUpdate2() {
		return " FOR UPDATE";
	}

	@Override
	public String getCurrentSchema(Connection connection) throws SQLException {
		return connection.getSchema();
	}


	@Override
	public String columnRef(String columnName) {
		StringBuilder b = new StringBuilder(columnName.length() + 2);

		b.append('"');
		b.append(columnName);
		b.append('"');

		return b.toString();
	}

	@Override
	public String tableRef(String tableName) {
		StringBuilder b = new StringBuilder(tableName.length() + 2);

		b.append('"');
		b.append(tableName);
		b.append('"');

		return b.toString();
	}

	@Override
	protected void setTimeFromJava(PreparedStatement pstm, Object val, int col) throws SQLException {
		Time time;

		if (val instanceof PGTime) {
			time = (Time) val;
		} else if (val instanceof Time) {
			time = new PGTime(((Time) val).getTime());
		} else if (val instanceof java.util.Date) {
			long timeStamp = ((java.util.Date) val).getTime();
			time = new PGTime(timeStamp);
		} else {
			throw new SQLException("Dont know how to convert a " + val.getClass() + " to a "
				+ Time.class.getName());
		}

		setTime(pstm, col, time);
	}

	@Override
	protected void setTimestampFromJava(PreparedStatement pstm, Object val, int col) throws SQLException {
		Timestamp timeStamp;
		if (val instanceof PGTimestamp) {
			timeStamp = (Timestamp) val;
		} else if (val instanceof Timestamp) {
			/**
			 * Facing error with type Timestamp: column type is timestamp without time zone but
			 * expression is of type time without time zone.
			 * 
			 * Using a PGTimestamp sets the local variable oid which apparently results in the
			 * correct behavior when setTimestamp is invoked on the PgPreparedStatement.
			 */
			timeStamp = new PGTimestamp(((Timestamp) val).getTime());
		} else if (val instanceof java.util.Date) {
			/**
			 * Facing error with type Timestamp: column type is timestamp without time zone but
			 * expression is of type time without time zone.
			 * 
			 * Using a PGTimestamp sets the local variable oid which apparently results in the
			 * correct behavior when setTimestamp is invoked on the PgPreparedStatement.
			 */
			timeStamp = new PGTimestamp(((java.util.Date) val).getTime());
		} else {
			throw new SQLException("Dont know how to convert a " + val.getClass() + " to a "
				+ Timestamp.class.getName());
		}

		setTimestamp(pstm, col, timeStamp);
	}

	@Override
	public Object mapToJava(ResultSet res, int col, DBType dbtype) throws SQLException {
		Object result;

		switch (dbtype) {
			case BOOLEAN:
				result = res.getBoolean(col) ? Boolean.TRUE : Boolean.FALSE;
				if (res.wasNull())
					result = null;
				return result;
			default:
				result = super.mapToJava(res, col, dbtype);
		}

		return result;
	}

	/**
	 * {@link Format} to create a {@link DBType#BLOB} literal from a <code>byte[]</code>.
	 */
	@Override
	protected Format getBlobFormat() {
		return PostgreSQLBlobFormat.INSTANCE;
	}

	@Override
	public BinaryData getBlobValue(ResultSet result, int index) throws SQLException {
		byte[] bytes = result.getBytes(index);
		if (bytes == null) {
			return null;
		}
		
		return new AbstractBinaryData() {
			@Override
			public String getName() {
				return "BLOB";
			}

			@Override
			public InputStream getStream() throws IOException {
				return new ByteArrayInputStream(bytes);
			}

			@Override
			public long getSize() {
				return bytes.length;
			}

			@Override
			public String getContentType() {
				return CONTENT_TYPE_OCTET_STREAM;
			}
		};
	}

	@Override
	public Object getNullPlaceholder(DBType type) {
		switch (type) {
			case CHAR:
				return CHAR_NULL_PLACEHOLDER;
			default:
				return super.getNullPlaceholder(type);
		}

	}

	@Override
	public void appendLikeCaseInsensitive(Appendable sql) throws IOException {
		sql.append("ILIKE");
	}

	@Override
	public void appendDropIndex(Appendable sql, String idxName, String tableName) throws IOException {
		sql.append("DROP INDEX ");
		sql.append(columnRef(idxName));
	}

	/**
	 * @see <a href="https://www.postgresql.org/docs/9.4/errcodes-appendix.html">Postgresql error
	 *      codes</a>
	 */
	@Override
	public boolean canRetry(SQLException sqlX) {
		String sqlState = sqlX.getSQLState();

		if (sqlState != null
			&& ("08003".equals(sqlState) // connection_does_not_exist
				|| "40P01".equals(sqlState))) // deadlock detected
			return true;

		return super.canRetry(sqlX);
	}

	@Override
	public void appendChangeColumnName(Appendable result, String tableName, DBType sqlType, String columnName, String newName,
			long size, int precision, boolean mandatory, boolean binary, Object defaultValue) throws IOException {
		result.append(alterTable(tableName));
		result.append("RENAME COLUMN ");
		result.append(columnRef(columnName));
		result.append(" TO ");
		result.append(columnRef(newName));
	}

	@Override
	public void appendChangeColumnType(Appendable result, String tableName, DBType sqlType, String columnName, String newName,
			long size, int precision, boolean mandatory, boolean binary, Object defaultValue) throws IOException {
		result.append(alterTable(tableName));
		result.append("ALTER COLUMN ");
		result.append(columnRef(columnName));
		result.append(" TYPE ");
		internalAppendDBType(result, sqlType, size, precision, mandatory, binary, binary, newName);
		appendDefaultValue(result, sqlType, defaultValue);
	}

	@Override
	public void appendChangeMandatory(Appendable result, String tableName, DBType sqlType, String columnName, String newName,
			long size, int precision, boolean mandatory, boolean binary, Object defaultValue) throws IOException {
		result.append(alterTable(tableName));
		result.append("ALTER COLUMN ");
		result.append(columnRef(columnName));
		result.append(" SET");
		appendMandatory(result, mandatory);
	}

}
